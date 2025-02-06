package com.olam.warehouse.master.vega.model

/**
 * Created by Baskaran Kannan on 12/21/2022.
 */
data class VegaWorkflowModel(
    var workflow: List<WorkFlow>?= emptyList(),
    var plantId: String?= "",
    var storageLocationCode: String?= "",
)

data class WorkFlow(
    var module:String? ="",
    var currentId:String? ="",
    var submodule1:String? ="",
    var submodule2:String? ="",
    var workflowId:String? ="",
    var workflowRole:String? ="",
    var NotificationFlag:String? =""
)
