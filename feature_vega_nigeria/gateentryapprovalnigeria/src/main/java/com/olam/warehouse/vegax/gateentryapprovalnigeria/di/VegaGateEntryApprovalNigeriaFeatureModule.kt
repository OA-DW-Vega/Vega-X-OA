package com.olam.warehouse.vegax.gateentryapprovalnigeria.di

import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.vegax.gateentryapprovalnigeria.data.api.VegaGateEntryApprovalNigeriaApi
import com.olam.warehouse.vegax.gateentryapprovalnigeria.data.domain.usecase.VegaGateEntryApprovalNigeriaUseCase
import com.olam.warehouse.vegax.gateentryapprovalnigeria.data.repo.VegaGateEntryApprovalNigeriaRepository
import com.olam.warehouse.vegax.gateentryapprovalnigeria.data.repo.VegaGateEntryApprovalNigeriaRepositoryImpl
import com.olam.warehouse.vegax.gateentryapprovalnigeria.ui.VegaGateEntryApprovalNigeriaViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.loadKoinModules
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit

/**
 * Created by Baskaran Kannan on 3/4/2020.
 */
fun injectGateEntryApprovalNigeriaFeature() = loadFeature

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
    factory { VegaGateEntryApprovalNigeriaUseCase(get()) }
    viewModel { VegaGateEntryApprovalNigeriaViewModel(get(), get()) }
}

val networkModule: Module = module {
    factory { (get(named(Constants.BASE)) as Retrofit).create(VegaGateEntryApprovalNigeriaApi::class.java) }
}

val repositoryModule: Module = module {
    factory<VegaGateEntryApprovalNigeriaRepository> { VegaGateEntryApprovalNigeriaRepositoryImpl(get(), get(), get()) }
}
