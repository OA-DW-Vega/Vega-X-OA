package com.olam.warehouse.vegax.gateentrynigeria.di

import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.vegax.gateentrynigeria.data.api.VegaGateEntryNigeriaApi
import com.olam.warehouse.vegax.gateentrynigeria.data.domain.usecase.VegaGateEntryNigeriaUseCase
import com.olam.warehouse.vegax.gateentrynigeria.data.repo.VegaGateEntryNigeriaRepository
import com.olam.warehouse.vegax.gateentrynigeria.data.repo.VegaGateEntryNigeriaRepositoryImpl
import com.olam.warehouse.vegax.gateentrynigeria.ui.VegaGateEntryNigeriaViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.loadKoinModules
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit

/**
 * Created by Baskaran Kannan on 3/4/2020.
 */
fun injectGateEntryNigeriaFeature() = loadFeature

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
    factory { VegaGateEntryNigeriaUseCase(get()) }
    viewModel { VegaGateEntryNigeriaViewModel(get(), get()) }
}

val networkModule: Module = module {
    factory { (get(named(Constants.BASE)) as Retrofit).create(VegaGateEntryNigeriaApi::class.java) }
}

val repositoryModule: Module = module {
    factory<VegaGateEntryNigeriaRepository> { VegaGateEntryNigeriaRepositoryImpl(get(), get(), get()) }
}
