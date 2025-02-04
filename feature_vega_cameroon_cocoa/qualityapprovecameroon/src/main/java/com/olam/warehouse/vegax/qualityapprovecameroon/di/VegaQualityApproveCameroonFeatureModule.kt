package com.olam.warehouse.vegax.qualityapprovecameroon.di

import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.vegax.qualityapprovecameroon.data.api.VegaQualityApproveCameroonApi
import com.olam.warehouse.vegax.qualityapprovecameroon.data.domain.usecase.VegaQualityApproveCameroonUseCase
import com.olam.warehouse.vegax.qualityapprovecameroon.data.repo.VegaApproveRepositoryImpl
import com.olam.warehouse.vegax.qualityapprovecameroon.data.repo.VegaQualityApproveCameroonRepository
import com.olam.warehouse.vegax.qualityapprovecameroon.ui.VegaQualityApproveCameroonViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.loadKoinModules
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit

fun injectQualityApproveCameroonFeature() = loadFeature

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
    factory { VegaQualityApproveCameroonUseCase(get()) }
    viewModel { VegaQualityApproveCameroonViewModel(get(), get()) }
}

val networkModule: Module = module {
    factory { (get(named(Constants.BASE)) as Retrofit).create(VegaQualityApproveCameroonApi::class.java) }
}

val repositoryModule: Module = module {
    factory<VegaQualityApproveCameroonRepository> { VegaApproveRepositoryImpl(get(), get()) }
}



