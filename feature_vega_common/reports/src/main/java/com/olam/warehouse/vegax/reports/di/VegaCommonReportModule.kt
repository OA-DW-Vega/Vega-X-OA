package com.olam.warehouse.vegax.reports.di

import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.vegax.reports.data.api.VegaCommonReportAdTokenApi
import com.olam.warehouse.vegax.reports.data.api.VegaCommonReportApi
import com.olam.warehouse.vegax.reports.data.repo.VegaCommonReportRepo
import com.olam.warehouse.vegax.reports.data.repo.VegaCommonReportRepoImpl
import com.olam.warehouse.vegax.reports.vm.VegaCommonReportViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.loadKoinModules
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit

fun injectCommonReportFeature() = loadFeature

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
    viewModel { VegaCommonReportViewModel(get(), get()) }
}

val repositoryModule = module {
    factory<VegaCommonReportRepo> { VegaCommonReportRepoImpl(get(), get()) }
}

val networkModule: Module = module {
    factory { (get(named(Constants.REPORT_POWER_BI_URL)) as Retrofit).create(VegaCommonReportApi::class.java) }
    factory { (get(named(Constants.AZURE_URL)) as Retrofit).create(VegaCommonReportAdTokenApi::class.java) }
}
