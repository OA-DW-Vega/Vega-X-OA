package com.olam.warehouse.vegax.lotqualitynigeria.data.domain.model

import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.data.remote.Resource.Status.SUCCESS

/**
 * Created by Muskan Jain on 20/09/2021.
 */
data class VegaCocoaLotQualityPostResponse(
    var status: Resource.Status = SUCCESS,
    var message: String? = ""
)
