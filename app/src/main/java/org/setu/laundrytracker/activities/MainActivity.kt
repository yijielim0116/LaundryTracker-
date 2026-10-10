package org.setu.laundrytracker.activities

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.doAfterTextChanged
import org.setu.laundrytracker.R
import org.setu.laundrytracker.main.AppData
import org.setu.laundrytracker.models.MachineModel
import org.setu.laundrytracker.models.MachineStatus
import org.setu.laundrytracker.models.MachineType

// Main screen - has the Add Machine button and shows all the machines below it
class MainActivity : AppCompatActivity() {

    // empty layout from activity_main.xml, I add the machines into this in code
    private lateinit var listLayout: LinearLayout
    private lateinit var searchInput: EditText
    private lateinit var statusFilter: Spinner

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // stops the content going under the status bar / nav bar
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // use my Toolbar as the action bar, the title comes from the app name in the manifest
        val toolbar =
            findViewById<Toolbar>(R.id.toolbar)

        setSupportActionBar(toolbar)

        listLayout =
            findViewById(R.id.listLayout)

        searchInput =
            findViewById(R.id.searchInput)

        // redraw the list every time the search text changes
        searchInput.doAfterTextChanged {
            displayMachines()
        }

        statusFilter =
            findViewById(R.id.statusFilter)

        // redraw the list when a different status is picked
        statusFilter.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                displayMachines()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        displayMachines()
    }

    // puts my menu (res/menu/menu_main.xml) on the toolbar
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    // what happens when a menu item is tapped
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_add -> {
                openAddMachine()
                true
            }

            R.id.action_clear_filters -> {
                clearFilters()
                true
            }

            R.id.action_about -> {
                showAbout()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    // opens the form to add a new machine (from the Add item on the toolbar)
    private fun openAddMachine() {
        startActivity(
            Intent(this, AddEditActivity::class.java)
        )
    }

    // empties the search box and sets the drop-down back to All,
    // both of these redraw the list by themselves
    private fun clearFilters() {
        searchInput.setText("")
        statusFilter.setSelection(0)
    }

    // small pop up with info about the app
    private fun showAbout() {
        AlertDialog.Builder(this)
            .setTitle("About Laundry Tracker")
            .setMessage("Check if the laundry machines are free before going down, and keep track of your wash and dry cycles.")
            .setPositiveButton("OK", null)
            .show()
    }

    // onResume runs when I come back from AddEditActivity,
    // so I reload the list here otherwise the new machine won't show
    override fun onResume() {
        super.onResume()

        if (::listLayout.isInitialized) {
            displayMachines()
        }
    }

    // builds the list from the store, each machine gets its own little block
    private fun displayMachines() {

        // clear it first or the machines show up twice
        listLayout.removeAllViews()

        val allMachines = AppData.machines.findAll()

        // only keep the machines where the name or location has the search text in it
        // ignoreCase so "washer" still finds "Washer 1"
        val search = searchInput.text.toString().trim()

        // null means "All" so I don't filter by status
        val status = selectedStatus()

        // a machine has to match the search AND the status to show up
        val machines = allMachines.filter { machine ->
            val matchesSearch =
                machine.name.contains(search, ignoreCase = true) ||
                    machine.location.contains(search, ignoreCase = true)

            val matchesStatus =
                status == null || machine.status == status

            matchesSearch && matchesStatus
        }

        // nothing to show, the message depends on if there's no machines at all or just no matches
        if (machines.isEmpty()) {

            val emptyText = TextView(this).apply {
                text = if (allMachines.isEmpty()) "No machines yet." else "No machines match your search or filter."
                textSize = 18f
                setPadding(0, 40, 0, 40)
            }

            listLayout.addView(emptyText)

            return
        }

        for (machine in machines) {

            // one block for each machine (name, location, status, edit and delete buttons)
            val machineLayout = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(0, 20, 0, 20)
            }

            // shows like "1: Washer 1"
            val machineTitle = TextView(this).apply {
                text = "${machine.id}: ${machine.name}"
                textSize = 20f
            }

            val machineLocation = TextView(this).apply {
                text = machine.location
                textSize = 16f
            }

            // shows like "Washer · Available", and "Washer · In use · 60 min" when it's running
            val machineStatus = TextView(this).apply {
                text = statusLine(machine)
                textSize = 14f
            }

            // edit button, opens the form and sends the machine's id so it knows which one to edit
            val editButton = Button(this).apply {
                text = "Edit"

                setOnClickListener {
                    val intent = Intent(this@MainActivity, AddEditActivity::class.java)
                    intent.putExtra("id", machine.id)
                    startActivity(intent)
                }
            }

            // delete button, removes it from the store then redraws the list
            val deleteButton = Button(this).apply {
                text = "Delete"

                setOnClickListener {
                    AppData.machines.delete(machine.id)
                    displayMachines()
                }
            }

            machineLayout.addView(machineTitle)
            machineLayout.addView(machineLocation)
            machineLayout.addView(machineStatus)
            machineLayout.addView(editButton)
            machineLayout.addView(deleteButton)

            listLayout.addView(machineLayout)
        }
    }

    // only add the cycle length for machines that are in use, it doesn't mean anything otherwise
    private fun statusLine(machine: MachineModel): String {
        val line = "${typeLabel(machine.type)} · ${statusLabel(machine.status)}"

        return if (machine.status == MachineStatus.IN_USE) {
            "$line · ${machine.cycleMinutes} min"
        } else {
            line
        }
    }

    // turns the drop-down position into a status, same order as status_filter_options in strings.xml
    private fun selectedStatus(): MachineStatus? {
        return when (statusFilter.selectedItemPosition) {
            1 -> MachineStatus.AVAILABLE
            2 -> MachineStatus.IN_USE
            3 -> MachineStatus.OUT_OF_ORDER
            else -> null
        }
    }

    // the enum names (WASHER, DRYER) look ugly on screen so I change them to normal words
    private fun typeLabel(type: MachineType): String {
        return when (type) {
            MachineType.WASHER -> "Washer"
            MachineType.DRYER -> "Dryer"
        }
    }

    // same thing for the status
    private fun statusLabel(status: MachineStatus): String {
        return when (status) {
            MachineStatus.AVAILABLE -> "Available"
            MachineStatus.IN_USE -> "In use"
            MachineStatus.OUT_OF_ORDER -> "Out of order"
        }
    }
}
