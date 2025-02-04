package com.olam.warehouse.vegax.mtntcocoa.di

import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.vegax.mtntcocoa.data.api.VegaCocoaMtntApi
import com.olam.warehouse.vegax.mtntcocoa.data.domain.usecase.VegaCocoaDispatchUseCase
import com.olam.warehouse.vegax.mtntcocoa.data.repo.VegaCocoaMtntRepository
import com.olam.warehouse.vegax.mtntcocoa.data.repo.VegaCocoaMtntRepositoryImpl
import com.olam.warehouse.vegax.mtntcocoa.ui.VegaCocoaMtntViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.loadKoinModules
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit

/**
 * Created by Baskaran Kannan on 5/18/2020.
 */

fun injectCocoaDispatchFeature() = loadFeature

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
    factory { VegaCocoaDispatchUseCase(get()) }
    viewModel { VegaCocoaMtntViewModel(get(), get()) }
}

val networkModule: Module = module {
    factory { (get(named(Constants.BASE)) as Retrofit).create(VegaCocoaMtntApi::class.java) }
}

val repositoryModule: Module = module {
    factory<VegaCocoaMtntRepository> { VegaCocoaMtntRepositoryImpl(get(), get(), get()) }
}
