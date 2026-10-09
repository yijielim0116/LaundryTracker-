package org.setu.laundrytracker.activities

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.RadioGroup
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

        saveButton.setOnClickListener {
            saveMachine()
        }

        cancelButton.setOnClickListener {
            finish()
        }
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

        finish()
    }
}
