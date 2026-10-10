package org.setu.laundrytracker.models

import java.util.concurrent.atomic.AtomicLong

/**
 * Keeps machines in memory. Everything is lost when the app closes.
 */
class MachineMemStore : MachineStore {

    private val machines = ArrayList<MachineModel>()
    private val lastId = AtomicLong(0L)

    override fun findAll(): List<MachineModel> {
        return machines
    }

    override fun create(machine: MachineModel) {
        machine.id = lastId.incrementAndGet()
        machines.add(machine)
    }

    override fun update(machine: MachineModel): Boolean {
        val foundIndex = machines.indexOfFirst { it.id == machine.id }
        return if (foundIndex != -1) {
            machines[foundIndex] = machines[foundIndex].copy(
                name = machine.name,
                location = machine.location,
                type = machine.type,
                status = machine.status,
                cycleMinutes = machine.cycleMinutes
            )
            true
        } else {
            false
        }
    }

    override fun delete(id: Long): Boolean {
        val foundMachine = findOne(id)
        return if (foundMachine != null) {
            machines.remove(foundMachine)
            true
        } else {
            false
        }
    }

    override fun findOne(id: Long): MachineModel? {
        return machines.find { m -> m.id == id }
    }
}
