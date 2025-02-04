package com.olam.warehouse.login.di

import com.olam.warehouse.master.DODatabase
import com.olam.warehouse.presentation.utils.Constants.DODATABASE
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.loadKoinModules
import org.koin.core.qualifier.named
import org.koin.dsl.module

/**
 * Created by Baskaran Kannan on 12/25/2019.
 */
fun injectDOFeature() = loadFeature

private val loadFeature by lazy {
    loadKoinModules(listOf(localDOModule))
}
val localDOModule = module {
    single(named(DODATABASE)) { DODatabase.buildDatabase(androidContext()) }
    factory { (get(named(DODATABASE)) as DODatabase).doQualityDao() }
    factory { (get(named(DODATABASE)) as DODatabase).doReceivingDao() }
    factory { (get(named(DODATABASE)) as DODatabase).doMasterDao() }
}
