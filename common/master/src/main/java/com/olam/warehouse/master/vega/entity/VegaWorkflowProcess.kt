package com.olam.warehouse.master.vega.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Created by Baskaran Kannan on 12/21/2022.
 */
@Entity
data class VegaWorkflowProcess(
    @PrimaryKey
    var processId: String = "",
    var processName: String? = "",
)

data class VegaWorkflowDetails(
    var plantId: String?= "",
    var storageLocCode: String?= "",
    var workflow: String?= "",
)

data class WorkFlowItems(
    var workflow:List<WorkflowFields> = emptyList()
)

data class WorkflowFields(
    var module: String? = "",
    var submodule1: String? = "",
    var submodule2: String? = "",
    var currentId: String? = "",
    var workflowId: String? = "",
    var workflowRole: String? = "",
    var workflowModule:String? = "",
    var NotificationFlag: String? = ""
)

