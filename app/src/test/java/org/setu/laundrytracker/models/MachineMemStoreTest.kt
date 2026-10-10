package org.setu.laundrytracker.models

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class MachineMemStoreTest {

    private lateinit var store: MachineMemStore

    @Before
    fun setUp() {
        store = MachineMemStore()
    }

    @Test
    fun createGivesEachMachineANewId() {
        store.create(MachineModel(name = "Washer 1"))
        store.create(MachineModel(name = "Dryer 1", type = MachineType.DRYER))

        val all = store.findAll()
        assertEquals(2, all.size)
        assertEquals(1L, all[0].id)
        assertEquals(2L, all[1].id)
    }

    @Test
    fun updateChangesAnExistingMachine() {
        store.create(MachineModel(name = "Washer 1"))
        val updated = store.update(
            MachineModel(id = 1L, name = "Washer 1", status = MachineStatus.IN_USE)
        )

        assertTrue(updated)
        assertEquals(MachineStatus.IN_USE, store.findOne(1L)?.status)
    }

    // checks the cycle length is saved on create and changed on update
    @Test
    fun cycleMinutesIsSavedAndUpdated() {
        store.create(MachineModel(name = "Washer 1"))
        assertEquals(60, store.findOne(1L)?.cycleMinutes)

        store.update(MachineModel(id = 1L, name = "Washer 1", cycleMinutes = 45))
        assertEquals(45, store.findOne(1L)?.cycleMinutes)
    }

    @Test
    fun updateReturnsFalseForUnknownId() {
        assertFalse(store.update(MachineModel(id = 99L)))
    }

    @Test
    fun deleteRemovesTheMachine() {
        store.create(MachineModel(name = "Washer 1"))

        assertTrue(store.delete(1L))
        assertNull(store.findOne(1L))
        assertFalse(store.delete(1L))
    }
}
