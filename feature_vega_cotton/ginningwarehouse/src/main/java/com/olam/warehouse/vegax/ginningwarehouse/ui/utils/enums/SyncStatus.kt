package com.olam.warehouse.ginning.utils.enums

enum class SyncStatus(val id: Int, val value: String) {
    NoProgress(1, "Sync No progress"),
    InProgress(2, "Sync In progress"),
    OnError(3, "Sync On Error"),
    Completed(4, "Sync Completed");

    companion object {
        fun from(value: Int): SyncStatus? = values().find { it.id == value }
    }
}