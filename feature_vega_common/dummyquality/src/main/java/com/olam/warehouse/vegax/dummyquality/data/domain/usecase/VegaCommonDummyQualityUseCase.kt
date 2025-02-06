package com.olam.warehouse.vegax.dummyquality.data.domain.usecase

import androidx.lifecycle.LiveData
import com.olam.warehouse.master.vega.model.VegaQualityParamsWithQualitative
import com.olam.warehouse.vegax.dummyquality.data.domain.model.VegaCommonDummySampleModel
import com.olam.warehouse.vegax.dummyquality.data.repo.VegaCommonDummyQualityRepository

class VegaCommonDummyQualityUseCase(private val repository: VegaCommonDummyQualityRepository) {
    suspend fun getProducts() = repository.getProducts()
    suspend fun getSupplierList() = repository.getSupplierList()
    suspend fun getQualityParams(materialId: String): LiveData<List<VegaQualityParamsWithQualitative>> =
        repository.getQualityParams(materialId)

    suspend fun postDummySample(vegaCommonDummySampleModel: VegaCommonDummySampleModel) =
        repository.postDummySample(vegaCommonDummySampleModel)

    suspend fun getDummySample(key: String, plantId: String, materialCode: String, vendorCode: String,dummySampleFlag: Boolean) =
        repository.getDummySample(key, plantId, materialCode, vendorCode,dummySampleFlag)

    suspend fun getDummySampleList(key: String, plantId: String, materialCode: String, vendorCode: String,dummySampleFlag: Boolean) =
        repository.getDummySampleList(key, plantId, materialCode, vendorCode,dummySampleFlag)

    suspend fun getDummySampleDetails(key: String, id: String) =
        repository.getDummySampleDetails(key, id)

    suspend fun deleteDummySample(id: String) =
        repository.deleteDummySample(id)

    suspend fun getVendorAndMaterial(plantId: String) =
        repository.getVendorAndMaterial(plantId)
}
