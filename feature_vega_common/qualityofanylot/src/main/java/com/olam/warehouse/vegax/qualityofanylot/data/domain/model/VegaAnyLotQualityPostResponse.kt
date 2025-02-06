package com.olam.warehouse.vegax.qualityofanylot.data.domain.model

import com.olam.warehouse.presentation.data.remote.Resource

/**
 * Created by Ramesh Rm on 07/11/2022.
 */

data class VegaAnyLotQualityPostResponse(
    var status: Resource.Status = Resource.Status.SUCCESS,
    var message: String? = ""
)

