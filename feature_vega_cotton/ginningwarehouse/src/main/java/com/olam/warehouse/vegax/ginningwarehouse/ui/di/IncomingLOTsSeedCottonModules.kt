package com.olam.warehouse.vegax.ginningwarehouse.ui.di

import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.vegax.ginningwarehouse.ui.data.api.IncomingLotApi
import com.olam.warehouse.vegax.ginningwarehouse.ui.data.domain.VegaCottonIncomingLotsSeedCottonUseCase
import com.olam.warehouse.vegax.ginningwarehouse.ui.data.repo.IncomingLotRepository
import com.olam.warehouse.vegax.ginningwarehouse.ui.data.repo.VegaIncomingLotRepositoryImpl
import com.olam.warehouse.vegax.ginningwarehouse.ui.ui.incominglots.seedcotton.GinningIncomingLotViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.loadKoinModules
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit

fun injectGinningIncomingLOTsFeature() = loadFeature

private val loadFeature by lazy {
    loadKoinModules(
        listOf(
            viewModelModule,
            networkModule,
            repositoryModule
        )
    )
}

val viewModelModule: Module = module {
    factory { VegaCottonIncomingLotsSeedCottonUseCase(get()) }
    viewModel { GinningIncomingLotViewModel(get(), get()) }
}

val networkModule: Module = module {
    factory { (get(named(Constants.BASE)) as Retrofit).create(IncomingLotApi::class.java) }
}

val repositoryModule: Module = module {
    factory<IncomingLotRepository> { VegaIncomingLotRepositoryImpl(get()) }
}
