package com.olam.warehouse.vegax.portwarehouse.di

import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.vegax.portwarehouse.data.api.DirectDispatchApi
import com.olam.warehouse.vegax.portwarehouse.data.api.MtnDispatchApi
import com.olam.warehouse.vegax.portwarehouse.data.repository.DirectDispatchRepository
import com.olam.warehouse.vegax.portwarehouse.data.repository.DirectDispatchRepositoryImpl
import com.olam.warehouse.vegax.portwarehouse.data.repository.MtnDispatchRepository
import com.olam.warehouse.vegax.portwarehouse.data.repository.MtnDispatchRepositoryImpl
import com.olam.warehouse.vegax.portwarehouse.ui.dispatch.MtnDispatchViewModel
import com.olam.warehouse.vegax.portwarehouse.ui.dispatch.PortDispatchSealViewModel
import com.olam.warehouse.vegax.portwarehouse.ui.dispatch.PortDispatchViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.loadKoinModules
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit

/**
 * Created by Baskaran Kannan on 3/30/2021.
 */

fun injectPortDispatchFeaturee() = loadFeature

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
    viewModel { MtnDispatchViewModel(get(), get()) }
    viewModel { PortDispatchViewModel(get(), get()) }
    viewModel { PortDispatchSealViewModel(get(), get()) }
}

val networkModule: Module = module {
    factory { (get(named(Constants.BASE)) as Retrofit).create(MtnDispatchApi::class.java) }
    factory { (get(named(Constants.BASE)) as Retrofit).create(DirectDispatchApi::class.java) }
}

val repositoryModule: Module = module {
    factory<MtnDispatchRepository> { MtnDispatchRepositoryImpl(get(), get()) }
    factory<DirectDispatchRepository> { DirectDispatchRepositoryImpl(get(), get()) }
}
