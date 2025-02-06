package com.olam.warehouse.vegax.portwarehouse.di

import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.vegax.portwarehouse.data.api.PortPileApi
import com.olam.warehouse.vegax.portwarehouse.data.domain.VegaCottonPortWareHousePileUseCase
import com.olam.warehouse.vegax.portwarehouse.data.repository.PortPileRepository
import com.olam.warehouse.vegax.portwarehouse.data.repository.VegaPortWareHousePortPileRepositoryImpl
import com.olam.warehouse.vegax.portwarehouse.ui.pile.PortPileAddBaleViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.loadKoinModules
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit


fun injectPortPileFeature() = loadFeature

private val loadFeature by lazy {
    loadKoinModules(
        listOf(
            viewModelPileModule,
            networkPileModule,
            repositoryPileModule
        )
    )
}

val viewModelPileModule: Module = module {
    factory { VegaCottonPortWareHousePileUseCase(get()) }
    viewModel { PortPileAddBaleViewModel(get(), get()) }
}

val networkPileModule: Module = module {
    factory { (get(named(Constants.BASE)) as Retrofit).create(PortPileApi::class.java) }
}

val repositoryPileModule: Module = module {
    factory<PortPileRepository> { VegaPortWareHousePortPileRepositoryImpl(get(), get()) }
}
