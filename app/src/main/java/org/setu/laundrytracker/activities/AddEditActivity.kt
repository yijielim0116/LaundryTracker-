package org.setu.laundrytracker.activities

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
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

    // null = adding a new machine, otherwise it's the id of the machine I'm editing
    private var editingId: Long? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_add_edit)

        nameInput =
            findViewById(R.id.nameInput)

        locationInput =
            findViewById(R.id.locationInput)

        typeGroup =
            findViewById(R.id.typeGroup)

        statusGroup =
            findViewById(R.id.statusGroup)

        val saveButton =
            findViewById<Button>(R.id.saveButton)

        val cancelButton =
            findViewById<Button>(R.id.cancelButton)

        val backButton =
            findViewById<ImageButton>(R.id.backButton)

        // MainActivity sends the id when Edit is pressed, -1 means no id was sent
        editingId =
            intent.getLongExtra("id", -1L)
                .takeIf { it != -1L }

        if (editingId != null) {

            findViewById<TextView>(R.id.formTitle)
                .text = "Edit Machine"

            loadExistingMachine(editingId!!)
        }

        saveButton.setOnClickListener {
            saveMachine()
        }

        cancelButton.setOnClickListener {
            finish()
        }

        // back arrow at the top, just closes the form and goes back to the list (nothing gets saved)
        backButton.setOnClickListener {
            finish()
        }
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
                status = selectedStatus()
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
                status = selectedStatus()
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
}
