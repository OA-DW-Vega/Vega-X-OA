package com.olam.warehouse.vegax.qualityapprovenigeria.di

import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.vegax.qualityapprovenigeria.data.api.VegaQualityApproveNigeriaApi
import com.olam.warehouse.vegax.qualityapprovenigeria.data.domain.usecase.VegaQualityApproveNigeriaUseCase
import com.olam.warehouse.vegax.qualityapprovenigeria.data.repo.VegaApproveRepositoryImpl
import com.olam.warehouse.vegax.qualityapprovenigeria.data.repo.VegaQualityApproveNigeriaRepository
import com.olam.warehouse.vegax.qualityapprovenigeria.ui.VegaQualityApproveNigeriaViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.loadKoinModules
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit

fun injectQualityApproveNigeriaFeature() = loadFeature

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
    factory { VegaQualityApproveNigeriaUseCase(get()) }
    viewModel { VegaQualityApproveNigeriaViewModel(get(), get()) }
}

val networkModule: Module = module {
    factory { (get(named(Constants.BASE)) as Retrofit).create(VegaQualityApproveNigeriaApi::class.java) }
}

val repositoryModule: Module = module {
    factory<VegaQualityApproveNigeriaRepository> { VegaApproveRepositoryImpl(get(), get()) }
}



