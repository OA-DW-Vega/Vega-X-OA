package com.olam.warehouse.vegax.qualityofanylot.di

import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.vegax.qualityofanylot.data.api.VegaAnyLotQualityApi
import com.olam.warehouse.vegax.qualityofanylot.data.domain.usecase.VegaAnyLotQualityUsecase
import com.olam.warehouse.vegax.qualityofanylot.data.repo.VegaAnyLotQualityRepository
import com.olam.warehouse.vegax.qualityofanylot.data.repo.VegaAnyLotQualityRepositoryImpl
import com.olam.warehouse.vegax.qualityofanylot.ui.VegaAnyLotQualityViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.loadKoinModules
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit

/**
 * Created by Ramesh Rm on 20/09/2021.
 */

fun injectAnyLotQualityFeature() = loadFeature

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
    factory { VegaAnyLotQualityUsecase(get()) }
    viewModel { VegaAnyLotQualityViewModel(get(), get()) }
}

val networkModule: Module = module {
    factory { (get(named(Constants.BASE)) as Retrofit).create(VegaAnyLotQualityApi::class.java) }
}

val repositoryModule: Module = module {
    factory<VegaAnyLotQualityRepository> {
        VegaAnyLotQualityRepositoryImpl(
            get(),
            get(),
            get()
        )
    }
}
