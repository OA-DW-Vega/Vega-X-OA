package com.olam.warehouse.vegax.historytransactionsghanacocoa.di

import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.vegax.historytransactionsghanacocoa.data.api.VegaGhanaCocoaHistoryTransactionsApi
import com.olam.warehouse.vegax.historytransactionsghanacocoa.data.domain.usecase.VegaHistoryTransactionsGhanaCocoaUseCase
import com.olam.warehouse.vegax.historytransactionsghanacocoa.data.repo.VegaHistoryTransactionsGhanaCocoaRepository
import com.olam.warehouse.vegax.historytransactionsghanacocoa.data.repo.VegaHistoryTransactionsGhanaCocoaRepositoryImpl
import com.olam.warehouse.vegax.historytransactionsghanacocoa.ui.VegaHistoryTransactionGhanaCocoaViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.loadKoinModules
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit

fun injectGhanaCocoaHistoryTransactionFeature() = loadFeature

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
    factory { VegaHistoryTransactionsGhanaCocoaUseCase(get()) }
    viewModel { VegaHistoryTransactionGhanaCocoaViewModel(get(), get()) }
}

val networkModule: Module = module {
    factory { (get(named(Constants.BASE)) as Retrofit).create(VegaGhanaCocoaHistoryTransactionsApi::class.java) }
}

val repositoryModule: Module = module {
    factory<VegaHistoryTransactionsGhanaCocoaRepository> { VegaHistoryTransactionsGhanaCocoaRepositoryImpl(get(), get()) }
}
