package com.olam.warehouse.vegax.dispatch.di

import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.vegax.dispatch.data.api.VegaDispatchApi
import com.olam.warehouse.vegax.dispatch.data.domain.usecase.VegaDispatchUseCase
import com.olam.warehouse.vegax.dispatch.data.repo.VegaDispatchRepository
import com.olam.warehouse.vegax.dispatch.data.repo.VegaDispatchRepositoryImpl
import com.olam.warehouse.vegax.dispatch.ui.VegaDispatchViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.loadKoinModules
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit

/**
 * Created by Baskaran Kannan on 2/10/2020.
 */

fun injectVegaDispatchFeature() = loadFeature

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
    factory { VegaDispatchUseCase(get()) }
    viewModel { VegaDispatchViewModel(get(), get()) }
}

val networkModule: Module = module {
    factory { (get(named(Constants.BASE)) as Retrofit).create(VegaDispatchApi::class.java) }
}

val repositoryModule: Module = module {
    factory<VegaDispatchRepository> { VegaDispatchRepositoryImpl(get(), get(), get()) }
}
