package com.olam.warehouse.ginning.ui.incominglots.incomingmtn
import androidx.lifecycle.viewModelScope
import com.olam.warehouse.ginning.utils.enums.BaleStatus
import com.olam.warehouse.master.vegaCotton.vegaGinningWareHouse.entity.MtnBales
import com.olam.warehouse.master.vegaCotton.vegaGinningWareHouse.entity.VegaCottonGinningDispatchDelivery
import com.olam.warehouse.presentation.data.remote.AppDispatchers
import com.olam.warehouse.presentation.ui.BaseViewModel
import com.olam.warehouse.vegax.ginningwarehouse.ui.data.domain.VegaCottonIncomingLotsIncomingMtnUseCase
import com.olam.warehouse.vegax.ginningwarehouse.ui.data.domain.VegaCottonIncomingLotsSeedCottonUseCase
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

 class GinningIncomingBaleStatusViewModel(private val useCase: VegaCottonIncomingLotsIncomingMtnUseCase,
                                          private val dispatchers: AppDispatchers
    ) :  BaseViewModel() {

     fun updateMtnBale(mtnBale: MtnBales?, baleStatus: BaleStatus?)  = viewModelScope.launch(dispatchers.main) {
         withContext(dispatchers.io) {
             mtnBale?.let {
                 it.isVerified = 1
                 baleStatus?.let { baleStatus ->
                     it.toSloc = baleStatus.id.toString()
                     it.baleStatus = baleStatus.status
                     it.dateTime = Date()
                 }
                 useCase.updateMtnBale(mtnBale)
             }
         }
     }


}
