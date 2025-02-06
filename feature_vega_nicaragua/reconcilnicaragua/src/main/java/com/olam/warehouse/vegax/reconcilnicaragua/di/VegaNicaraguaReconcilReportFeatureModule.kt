package com.olam.warehouse.vegax.reconcilnicaragua.di

import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.vegax.reconcilnicaragua.data.api.VegaNicaraguaReconcilReportApi
import com.olam.warehouse.vegax.reconcilnicaragua.data.domain.usecase.VegaNicaraguaReconcilReportUseCase
import com.olam.warehouse.vegax.reconcilnicaragua.data.repo.VegaNicaraguaReConcilReportRepository
import com.olam.warehouse.vegax.reconcilnicaragua.data.repo.VegaNicaraguaReConcilReportRepositoryImpl
import com.olam.warehouse.vegax.reconcilnicaragua.ui.VegaNicaraguaReconcilReportViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.loadKoinModules
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit

fun injectNicaraguaReconcilReportFeature() = loadFeature

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
    factory { VegaNicaraguaReconcilReportUseCase(get()) }
    viewModel { VegaNicaraguaReconcilReportViewModel(get(), get()) }
}

val networkModule: Module = module {
    factory { (get(named(Constants.BASE)) as Retrofit).create(VegaNicaraguaReconcilReportApi::class.java) }
}

val repositoryModule: Module = module {
    factory<VegaNicaraguaReConcilReportRepository> { VegaNicaraguaReConcilReportRepositoryImpl(get(), get()) }
}



