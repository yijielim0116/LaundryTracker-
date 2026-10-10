package org.setu.laundrytracker.activities

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.NumberPicker
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import org.setu.laundrytracker.R
import org.setu.laundrytracker.main.AppData
import org.setu.laundrytracker.models.MachineModel
import org.setu.laundrytracker.models.MachineStatus
import org.setu.laundrytracker.models.MachineType

class AddEditActivity : AppCompatActivity() {

    private lateinit var nameInput: EditText
    private lateinit var locationInput: EditText
    private lateinit var typeGroup: RadioGroup
    private lateinit var statusGroup: RadioGroup
    private lateinit var cyclePicker: NumberPicker
    private lateinit var cycleSection: LinearLayout

    // null = adding a new machine, otherwise it's the id of the machine I'm editing
    private var editingId: Long? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_add_edit)

        // my toolbar becomes the action bar, and the up arrow shows on the left
        val toolbar =
            findViewById<Toolbar>(R.id.toolbar)

        setSupportActionBar(toolbar)
        supportActionBar?.title = "Add Machine"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        nameInput =
            findViewById(R.id.nameInput)

        locationInput =
            findViewById(R.id.locationInput)

        typeGroup =
            findViewById(R.id.typeGroup)

        statusGroup =
            findViewById(R.id.statusGroup)

        cyclePicker =
            findViewById(R.id.cyclePicker)

        cycleSection =
            findViewById(R.id.cycleSection)

        setUpCyclePicker()

        // show or hide the cycle picker whenever a different status is ticked
        statusGroup.setOnCheckedChangeListener { _, _ ->
            updateCycleSection()
        }

        val saveButton =
            findViewById<Button>(R.id.saveButton)

        val cancelButton =
            findViewById<Button>(R.id.cancelButton)

        // MainActivity sends the id when Edit is pressed, -1 means no id was sent
        editingId =
            intent.getLongExtra("id", -1L)
                .takeIf { it != -1L }

        if (editingId != null) {

            supportActionBar?.title = "Edit Machine"

            loadExistingMachine(editingId!!)
        }

        saveButton.setOnClickListener {
            saveMachine()
        }

        cancelButton.setOnClickListener {
            finish()
        }
    }

    // up arrow on the toolbar, just closes the form and goes back to the list (nothing gets saved)
    // I use finish() so the list keeps its search and filter instead of opening a new main screen
    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    // fills the form with the machine's current details so I can change them
    private fun loadExistingMachine(id: Long) {

        val machine =
            AppData.machines.findOne(id)

        if (machine == null) {

            Toast.makeText(
                this,
                "Machine not found",
                Toast.LENGTH_SHORT
            ).show()

            finish()
            return
        }

        nameInput.setText(machine.name)
        locationInput.setText(machine.location)

        // tick the right radio buttons for the type and status
        typeGroup.check(
            when (machine.type) {
                MachineType.WASHER -> R.id.washerRadio
                MachineType.DRYER -> R.id.dryerRadio
            }
        )

        statusGroup.check(
            when (machine.status) {
                MachineStatus.AVAILABLE -> R.id.availableRadio
                MachineStatus.IN_USE -> R.id.inUseRadio
                MachineStatus.OUT_OF_ORDER -> R.id.outOfOrderRadio
            }
        )

        cyclePicker.value = minutesToIndex(machine.cycleMinutes)

        updateCycleSection()
    }

    // the cycle length only matters when someone is using the machine,
    // so the picker only shows for In use (Available / Out of order hide it)
    private fun updateCycleSection() {
        cycleSection.visibility =
            if (selectedStatus() == MachineStatus.IN_USE) View.VISIBLE else View.GONE
    }

    // the picker goes 10, 15, 20 ... 120 minutes
    // NumberPicker only counts 0, 1, 2 ... so I use displayedValues to show the minutes instead
    private fun setUpCyclePicker() {
        val minutes = (MIN_MINUTES..MAX_MINUTES step STEP_MINUTES).toList()

        cyclePicker.minValue = 0
        cyclePicker.maxValue = minutes.size - 1
        cyclePicker.displayedValues = minutes.map { "$it min" }.toTypedArray()
        // stops it jumping from 120 back round to 10
        cyclePicker.wrapSelectorWheel = false
        // start on 60 min for a new machine
        cyclePicker.value = minutesToIndex(60)
    }

    // e.g. 10 min -> 0, 15 min -> 1, 60 min -> 10
    private fun minutesToIndex(minutes: Int): Int {
        val index = (minutes - MIN_MINUTES) / STEP_MINUTES
        return index.coerceIn(0, cyclePicker.maxValue)
    }

    // the other way round, picker position -> minutes
    private fun selectedCycleMinutes(): Int {
        return MIN_MINUTES + cyclePicker.value * STEP_MINUTES
    }

    private fun selectedType(): MachineType {
        return if (typeGroup.checkedRadioButtonId == R.id.dryerRadio) {
            MachineType.DRYER
        } else {
            MachineType.WASHER
        }
    }

    private fun selectedStatus(): MachineStatus {
        return when (statusGroup.checkedRadioButtonId) {
            R.id.inUseRadio -> MachineStatus.IN_USE
            R.id.outOfOrderRadio -> MachineStatus.OUT_OF_ORDER
            else -> MachineStatus.AVAILABLE
        }
    }

    private fun saveMachine() {

        val name =
            nameInput.text.toString().trim()

        val location =
            locationInput.text.toString().trim()

        if (name.isEmpty()) {
            nameInput.error = "Name is required"
            return
        }

        // no id means it's a new machine, otherwise update the one I'm editing
        if (editingId == null) {

            val machine = MachineModel(
                name = name,
                location = location,
                type = selectedType(),
                status = selectedStatus(),
                cycleMinutes = selectedCycleMinutes()
            )

            AppData.machines.create(machine)

            Toast.makeText(
                this,
                "Machine created",
                Toast.LENGTH_SHORT
            ).show()

        } else {

            // keep the same id so the store knows which machine to replace
            val machine = MachineModel(
                id = editingId!!,
                name = name,
                location = location,
                type = selectedType(),
                status = selectedStatus(),
                cycleMinutes = selectedCycleMinutes()
            )

            AppData.machines.update(machine)

            Toast.makeText(
                this,
                "Machine updated",
                Toast.LENGTH_SHORT
            ).show()
        }

        finish()
    }

    // limits for the cycle length picker
    companion object {
        private const val MIN_MINUTES = 10
        private const val MAX_MINUTES = 120
        private const val STEP_MINUTES = 5
    }
}
