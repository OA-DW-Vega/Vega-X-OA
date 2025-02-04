package com.olam.warehouse.login.di

import com.olam.warehouse.master.VegaDatabase
import com.olam.warehouse.presentation.utils.Constants.VEGADATABASE
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.loadKoinModules
import org.koin.core.qualifier.named
import org.koin.dsl.module

/**
 * Created by Baskaran Kannan on 12/25/2019.
 */
fun injectVegaFeature() = loadFeature

private val loadFeature by lazy {
    loadKoinModules(listOf(localVegaModule))
}
val localVegaModule = module {
    single(named(VEGADATABASE)) { VegaDatabase.buildDatabase(androidContext()) }
    factory { (get(named(VEGADATABASE)) as VegaDatabase).vegaQualityDao() }
    factory { (get(named(VEGADATABASE)) as VegaDatabase).vegaOffloadingDao() }
    factory { (get(named(VEGADATABASE)) as VegaDatabase).vegaReceivingDao() }
    factory { (get(named(VEGADATABASE)) as VegaDatabase).vegaMtntDao() }
    factory { (get(named(VEGADATABASE)) as VegaDatabase).vegaDispatchDao() }
    factory { (get(named(VEGADATABASE)) as VegaDatabase).vegaProcessingDao() }
    factory { (get(named(VEGADATABASE)) as VegaDatabase).vegaGateEntryDao() }
    factory { (get(named(VEGADATABASE)) as VegaDatabase).vegaInventoryDao() }
    factory { (get(named(VEGADATABASE)) as VegaDatabase).vegaCocoaDispatchDao() }
    factory { (get(named(VEGADATABASE)) as VegaDatabase).vegaCoCoaOffloadDao() }
    factory { (get(named(VEGADATABASE)) as VegaDatabase).vegaCocoaSweepingDao() }
    factory { (get(named(VEGADATABASE)) as VegaDatabase).vegaCocoaRminDao() }
    factory { (get(named(VEGADATABASE)) as VegaDatabase).vegaCocoaSalesDao() }
    factory { (get(named(VEGADATABASE)) as VegaDatabase).vegaEcuadorOffloadingDao() }
    factory { (get(named(VEGADATABASE)) as VegaDatabase).vegaEcuadorGrnDao() }
    factory { (get(named(VEGADATABASE)) as VegaDatabase).vegaCoCoaQualityDoa() }
    factory { (get(named(VEGADATABASE)) as VegaDatabase).vegaEcuadorDispatchDao() }
    factory { (get(named(VEGADATABASE)) as VegaDatabase).vegaCoffeeInventoryDao() }
    factory { (get(named(VEGADATABASE)) as VegaDatabase).vegaCoffeeRminDao() }
    factory { (get(named(VEGADATABASE)) as VegaDatabase).vegaCoffeeDispatchDao() }
    factory { (get(named(VEGADATABASE)) as VegaDatabase).vegaCoffeeQualityDoa() }
    factory { (get(named(VEGADATABASE)) as VegaDatabase).vegaCoffeeSalesDao() }
    factory { (get(named(VEGADATABASE)) as VegaDatabase).vegaCoffeeOffloadDao() }
    factory { (get(named(VEGADATABASE)) as VegaDatabase).vegaCoffeeExportSalesDao() }
    factory { (get(named(VEGADATABASE)) as VegaDatabase).vegaNicaraguaGrnDao() }
    factory { (get(named(VEGADATABASE)) as VegaDatabase).vegaNicaraguaMtntDao() }
    factory { (get(named(VEGADATABASE)) as VegaDatabase).vegaNicaraguaInvoiceDao() }
    factory { (get(named(VEGADATABASE)) as VegaDatabase).vegaCoffeeGrnDao() }
    factory { (get(named(VEGADATABASE)) as VegaDatabase).VegaNicaraguaForwardPODao() }
    factory { (get(named(VEGADATABASE)) as VegaDatabase).vegaNicaraguaAdvanceDao() }
    factory { (get(named(VEGADATABASE)) as VegaDatabase).vegaNicInventoryDao() }
    factory { (get(named(VEGADATABASE)) as VegaDatabase).vegaGhanaProcessingDao() }
}
