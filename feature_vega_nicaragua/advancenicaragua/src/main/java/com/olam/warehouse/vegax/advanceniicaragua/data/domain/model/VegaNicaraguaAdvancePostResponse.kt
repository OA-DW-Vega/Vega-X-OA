package com.olam.warehouse.vegax.advanceniicaragua.data.domain.model

data class VegaNicaraguaAdvancePostResponse
    (
    val cashJournalMessage : String?="",
    val cashJournalDocumentNumber : String="",
    val accountingDocMessage : String="",
    val accountingDocNumber : String="",
    val errorMessage : String=""
)
