package com.olam.warehouse.login.di

import com.olam.warehouse.master.CottonDataBase
import com.olam.warehouse.presentation.utils.Constants.COTTONDATABASE
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.loadKoinModules
import org.koin.core.qualifier.named
import org.koin.dsl.module

/**
 * Created by Pavani Buchupalli on 03/03/2021.
 */
fun injectCottonFeature() = loadFeature

private val loadFeature by lazy {
    loadKoinModules(listOf(localCottonModule))
}
val localCottonModule = module {
    single(named(COTTONDATABASE)) { CottonDataBase.buildDatabase(androidContext()) }
    factory { (get(named(COTTONDATABASE)) as CottonDataBase).vegaCottonGinningDispatchDao() }
    factory { (get(named(COTTONDATABASE)) as CottonDataBase).vegaGinningIncomingLotDao() }
    factory { (get(named(COTTONDATABASE)) as CottonDataBase).vegaGinningPileDao() }
    factory { (get(named(COTTONDATABASE)) as CottonDataBase).vegaGinningInprogressDao() }
    factory { (get(named(COTTONDATABASE)) as CottonDataBase).vegaGinningReceivingDao() }
    factory { (get(named(COTTONDATABASE)) as CottonDataBase).portPileDao() }
    factory { (get(named(COTTONDATABASE)) as CottonDataBase).portMtnDispatchDao() }
    factory { (get(named(COTTONDATABASE)) as CottonDataBase).portDispatchDao() }
    factory { (get(named(COTTONDATABASE)) as CottonDataBase).portReceivingDao() }
}
