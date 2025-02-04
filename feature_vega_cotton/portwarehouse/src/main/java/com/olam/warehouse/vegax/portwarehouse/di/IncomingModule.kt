package com.olam.warehouse.vegax.portwarehouse.di

import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.vegax.portwarehouse.data.api.IncomingApi
import com.olam.warehouse.vegax.portwarehouse.data.repository.PortIncomingRepository
import com.olam.warehouse.vegax.portwarehouse.data.repository.PortIncomingRepositoryImpl
import com.olam.warehouse.vegax.portwarehouse.ui.incoming.PortIncomingClassficationViewModel
import com.olam.warehouse.vegax.portwarehouse.ui.incoming.PortIncomingMtnViewModel
import com.olam.warehouse.vegax.portwarehouse.ui.incoming.PortIncomingReviewViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.loadKoinModules
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit

/**
 * Created by Baskaran Kannan on 3/30/2021.
 */

fun injectPortIncomingFeaturee() = loadFeature

private val loadFeature by lazy {
    loadKoinModules(
        listOf(
            viewModelIncomingModule,
            networkIncomingModule,
            repositoryIncomingModule
        )
    )
}

val viewModelIncomingModule: Module = module {
    viewModel { PortIncomingMtnViewModel(get(), get()) }
    viewModel { PortIncomingClassficationViewModel(get(), get()) }
    viewModel { PortIncomingReviewViewModel(get(), get()) }
}

val networkIncomingModule: Module = module {
    factory { (get(named(Constants.BASE)) as Retrofit).create(IncomingApi::class.java) }
}

val repositoryIncomingModule: Module = module {
    factory<PortIncomingRepository> { PortIncomingRepositoryImpl(get(), get()) }
}
