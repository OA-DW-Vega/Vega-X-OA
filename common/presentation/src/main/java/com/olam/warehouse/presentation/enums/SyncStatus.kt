package com.olam.warehouse.presentation.enums

/**
 * Created by Baskaran Kannan on 12/30/2019.
 */
enum class SyncStatus(val id: Int, val value: String) {
    NoProgress(1, "Sync No progress"),
    InProgress(2, "Sync In progress"),
    OnError(3, "Sync On Error"),
    Completed(4, "Sync Completed"),
    Pending(5, "Sync Pending");

    companion object {
        fun from(value: Int): SyncStatus? = values().find { it.id == value }
    }
}
