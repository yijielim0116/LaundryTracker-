package org.setu.laundrytracker.models

interface MachineStore {
    fun findAll(): List<MachineModel>
    fun create(machine: MachineModel)
    fun update(machine: MachineModel): Boolean
    fun delete(id: Long): Boolean
    fun findOne(id: Long): MachineModel?
}
