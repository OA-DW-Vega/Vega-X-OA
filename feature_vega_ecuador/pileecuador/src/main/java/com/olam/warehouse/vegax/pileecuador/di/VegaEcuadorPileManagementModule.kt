package com.olam.warehouse.vegax.pileecuador.ui.di

import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.vegax.pileecuador.ui.VegaEcuadorPileManagementViewModel
import com.olam.warehouse.vegax.pileecuador.ui.data.api.VegaEcuadorPileManagementApi
import com.olam.warehouse.vegax.pileecuador.ui.data.domain.VegaEcuadorPileManagementUseCase
import com.olam.warehouse.vegax.pileecuador.ui.data.repo.VegaEcuadorPileManagementRepository
import com.olam.warehouse.vegax.pileecuador.ui.data.repo.VegaEcuadorPileManagementRepositoryImpl
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.loadKoinModules
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit

fun injectIndiaCoffeeProcessingFeature() = loadFeature

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
    factory { VegaEcuadorPileManagementUseCase(get()) }
    viewModel { VegaEcuadorPileManagementViewModel(get(), get()) }
}

val networkModule: Module = module {
    factory { (get(named(Constants.BASE)) as Retrofit).create(VegaEcuadorPileManagementApi::class.java) }
}

val repositoryModule: Module = module {
    factory<VegaEcuadorPileManagementRepository> { VegaEcuadorPileManagementRepositoryImpl(get(), get()) }
}
