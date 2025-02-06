package com.olam.warehouse.vegax.qualitycameroon.di

import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.vegax.qualitycameroon.data.api.VegaCameroonQualityApi
import com.olam.warehouse.vegax.qualitycameroon.data.domain.usecase.VegaCameroonQualityUseCase
import com.olam.warehouse.vegax.qualitycameroon.data.repo.VegaCameroonQualityRepository
import com.olam.warehouse.vegax.qualitycameroon.data.repo.VegaCameroonQualityRepositoryImpl
import com.olam.warehouse.vegax.qualitycameroon.ui.VegaCameroonQualityViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.loadKoinModules
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit

/**
 * Created by Keerthi Santhanam on 6/21/2020.
 */
fun injectCameroonQualityFeature() = loadFeature

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
    factory { VegaCameroonQualityUseCase(get()) }
    viewModel { VegaCameroonQualityViewModel(get(), get()) }
}

val networkModule: Module = module {
    factory { (get(named(Constants.BASE)) as Retrofit).create(VegaCameroonQualityApi::class.java) }
}

val repositoryModule: Module = module {
    factory<VegaCameroonQualityRepository> { VegaCameroonQualityRepositoryImpl(get(), get()) }
}



