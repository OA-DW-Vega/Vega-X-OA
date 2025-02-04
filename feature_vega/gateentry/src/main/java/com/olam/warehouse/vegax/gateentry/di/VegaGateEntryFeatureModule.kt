package com.olam.warehouse.vegax.gateentry.di

import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.vegax.gateentry.data.api.VegaGateEntryApi
import com.olam.warehouse.vegax.gateentry.data.domain.usecase.VegaGateEntryUseCase
import com.olam.warehouse.vegax.gateentry.data.repo.VegaGateEntryRepository
import com.olam.warehouse.vegax.gateentry.data.repo.VegaGateEntryRepositoryImpl
import com.olam.warehouse.vegax.gateentry.ui.VegaGateEntryViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.loadKoinModules
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit

/**
 * Created by Baskaran Kannan on 3/4/2020.
 */
fun injectGateEntryFeature() = loadFeature

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
    factory { VegaGateEntryUseCase(get()) }
    viewModel { VegaGateEntryViewModel(get(), get()) }
}

val networkModule: Module = module {
    factory { (get(named(Constants.BASE)) as Retrofit).create(VegaGateEntryApi::class.java) }
}

val repositoryModule: Module = module {
    factory<VegaGateEntryRepository> { VegaGateEntryRepositoryImpl(get(), get(), get()) }
}
