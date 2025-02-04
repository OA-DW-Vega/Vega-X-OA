package com.olam.warehouse.vegax.qualityghana.di

import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.vegax.qualityghana.data.api.VegaGhanaQualityApi
import com.olam.warehouse.vegax.qualityghana.data.domain.usecase.VegaGhanaQualityUseCase
import com.olam.warehouse.vegax.qualityghana.data.repo.VegaGhanaQualityRepository
import com.olam.warehouse.vegax.qualityghana.data.repo.VegaGhanaQualityRepositoryImpl
import com.olam.warehouse.vegax.qualityghana.ui.VegaGhanaQualityViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.loadKoinModules
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit

/**
 * Created by Baskaran Kannan on 9/25/2020.
 */

fun injectGhanaQualityFeature() = loadFeature

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
    factory { VegaGhanaQualityUseCase(get()) }
    viewModel { VegaGhanaQualityViewModel(get(), get()) }
}

val networkModule: Module = module {
    factory { (get(named(Constants.BASE)) as Retrofit).create(VegaGhanaQualityApi::class.java) }
}

val repositoryModule: Module = module {
    factory<VegaGhanaQualityRepository> { VegaGhanaQualityRepositoryImpl(get(), get()) }
}
