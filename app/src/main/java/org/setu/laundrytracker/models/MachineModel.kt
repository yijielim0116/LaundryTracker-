package org.setu.laundrytracker.models

/**
 * Whether a machine washes or dries clothes.
 */
enum class MachineType {
    WASHER,
    DRYER
}

/**
 * What a machine is doing right now.
 */
enum class MachineStatus {
    AVAILABLE,
    IN_USE,
    OUT_OF_ORDER
}

/**
 * Data class representing a single laundry machine.
 * Kotlin automatically generates toString(), equals(), hashCode(), and copy().
 */
data class MachineModel(
    var id: Long = 0L,
    val name: String = "",
    val location: String = "",
    val type: MachineType = MachineType.WASHER,
    val status: MachineStatus = MachineStatus.AVAILABLE
)
