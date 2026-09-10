package com.example.class10resources.data.model

data class AppUpdateInfo(
    val latestVersionCode: Int = 2,
    val latestVersionName: String = "2.0",
    val updateTitle: String = "New Lumio Update Available",
    val releaseNotes: String = "Performance improvements, new notes, DPPs and board preparation resources.",
    val apkDownloadUrl: String = "",
    val isForceUpdate: Boolean = false,
    val releasedAt: String = "",
    val isTriggerActive: Boolean = true,
    val triggerTimestamp: Long = 0L
)
