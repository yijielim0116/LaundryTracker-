package org.setu.laundrytracker.main

import org.setu.laundrytracker.models.MachineMemStore

/**
 * Holds the one machine store that every screen shares.
 */
object AppData {
    val machines = MachineMemStore()
}
