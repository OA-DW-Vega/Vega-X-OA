package com.olam.warehouse.vegax.reports.data.domain

class VegaCommonReportDataSetModel (
    val webUrl: String? = "",
    val embedUrl: String? = "",
    val datasetId: String? = "",
    val datasetWorkspaceId: String? = "",
    val name: String? = ""
)

data class VegaCommonReportDataSetTokenModel(
    val token: String? = "",
    val tokenId: String? = ""
)

data class VegaCommonReportDataSetTokenRequest(
    var reports: List<ReportDataSetModel>? = emptyList(),
    var targetWorkspaces: List<ReportDataSetModel>? = emptyList(),
    var datasets: List<ReportDataSetModel>? = emptyList(),
    var identities: List<IdentityModel>? = emptyList()
)

data class ReportDataSetModel(
    var id: String?= ""
)

data class IdentityModel(
    var username: String?= "bill.flack@ofi.com",
    var roles: List<String>?= arrayListOf("RLS_SP"),
    var datasets: List<String>?= arrayListOf(),
)
