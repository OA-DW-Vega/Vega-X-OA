package com.olam.warehouse.vegax.qualityapproveindiacoffee.di

import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.vegax.qualityapproveindiacoffee.data.api.VegaQualityApproveIndiaCoffeeApi
import com.olam.warehouse.vegax.qualityapproveindiacoffee.data.domain.usecase.VegaQualityApproveIndiaCoffeeUseCase
import com.olam.warehouse.vegax.qualityapproveindiacoffee.data.repo.VegaApproveRepositoryImpl
import com.olam.warehouse.vegax.qualityapproveindiacoffee.data.repo.VegaQualityApproveIndiaCoffeeRepository
import com.olam.warehouse.vegax.qualityapproveindiacoffee.ui.VegaQualityApproveIndiaCoffeeViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.loadKoinModules
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit

fun injectQualityApproveIndiaCoffeeFeature() = loadFeature

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
    factory { VegaQualityApproveIndiaCoffeeUseCase(get()) }
    viewModel { VegaQualityApproveIndiaCoffeeViewModel(get(), get()) }
}

val networkModule: Module = module {
    factory { (get(named(Constants.BASE)) as Retrofit).create(VegaQualityApproveIndiaCoffeeApi::class.java) }
}

val repositoryModule: Module = module {
    factory<VegaQualityApproveIndiaCoffeeRepository> { VegaApproveRepositoryImpl(get(), get()) }
}



