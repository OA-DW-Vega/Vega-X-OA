package com.olam.warehouse.portwarehouse.utils.enums

/**
 * Created by SangiliPandian C on 12-02-2019.
 */
enum class ContainerStatus(val id: Int, val value: String) {
    InProgress(1, "Stuffing In progress"),
    Resume(2, "Stuffing On Resume"),
    OnHold(3, "Stuffing On Hold"),
    Completed(4, "Stuffing Completed");

    companion object {
        fun from(value: Int): ContainerStatus? = ContainerStatus.values().find { it.id == value }
    }
}