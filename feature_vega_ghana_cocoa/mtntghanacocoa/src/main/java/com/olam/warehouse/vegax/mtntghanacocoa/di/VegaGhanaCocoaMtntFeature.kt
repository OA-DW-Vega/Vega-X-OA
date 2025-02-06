package com.olam.warehouse.vegax.mtntghanacocoa.di

import com.olam.warehouse.presentation.data.api.TruckManageApi
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.vegax.mtntghanacocoa.data.api.VegaGhanaCocoaMtntApi
import com.olam.warehouse.vegax.mtntghanacocoa.data.domain.usecase.VegaGhanaCocoaDispatchUseCase
import com.olam.warehouse.vegax.mtntghanacocoa.data.repo.VegaGhanaCocoaMtntRepository
import com.olam.warehouse.vegax.mtntghanacocoa.data.repo.VegaGhanaMtntRepositoryImpl
import com.olam.warehouse.vegax.mtntghanacocoa.ui.VegaGhanaCocoaMtntViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.loadKoinModules
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit


fun injectGhanaMtntDispatchFeature() = loadFeature

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
    factory { VegaGhanaCocoaDispatchUseCase(get()) }
    viewModel { VegaGhanaCocoaMtntViewModel(get(), get()) }
}

val networkModule: Module = module {
    factory { (get(named(Constants.BASE)) as Retrofit).create(VegaGhanaCocoaMtntApi::class.java) }
    factory { (get(named(Constants.TRUCK_MANAGE)) as Retrofit).create(TruckManageApi::class.java) }
}

val repositoryModule: Module = module {
    factory<VegaGhanaCocoaMtntRepository> {
        VegaGhanaMtntRepositoryImpl(
            get(),
            get(),
            get(),
            get(),
            get()
        )
    }
}
