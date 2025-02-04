package com.olam.warehouse.vegax.gateentryghanacocoa.di

import com.olam.warehouse.presentation.data.api.TruckManageApi
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.vegax.gateentryghanacocoa.data.api.VegaGateEntryGhanaCocoaApi
import com.olam.warehouse.vegax.gateentryghanacocoa.data.domain.usecase.VegaGateEntryGhanaCocoaUseCase
import com.olam.warehouse.vegax.gateentryghanacocoa.data.repo.VegaGateEntryGhanaCocoaRepository
import com.olam.warehouse.vegax.gateentryghanacocoa.data.repo.VegaGateEntryGhanaRepositoryImpl
import com.olam.warehouse.vegax.gateentryghanacocoa.ui.VegaGateEntryGhanaCocoaViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.loadKoinModules
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit


fun injectGateEntryGhanaFeature() = loadFeature

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
    factory { VegaGateEntryGhanaCocoaUseCase(get()) }
    viewModel { VegaGateEntryGhanaCocoaViewModel(get(), get()) }
}

val networkModule: Module = module {
    factory { (get(named(Constants.BASE)) as Retrofit).create(VegaGateEntryGhanaCocoaApi::class.java) }
    factory { (get(named(Constants.TRUCK_MANAGE)) as Retrofit).create(TruckManageApi::class.java) }
}

val repositoryModule: Module = module {
    factory<VegaGateEntryGhanaCocoaRepository> { VegaGateEntryGhanaRepositoryImpl(get(), get(), get(),get()) }
}
