package com.example.class10resources.data.model

data class VersionStats(
    val totalActiveDevices: Int = 0,
    val updatedDevicesCount: Int = 0,
    val pendingDevicesCount: Int = 0,
    val updatePercentage: Int = 0
)
