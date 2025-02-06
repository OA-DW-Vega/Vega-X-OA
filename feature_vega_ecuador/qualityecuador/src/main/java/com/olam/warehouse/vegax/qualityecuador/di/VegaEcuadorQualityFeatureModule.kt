package com.olam.warehouse.vegax.qualityecuador.di

import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.vegax.qualityecuador.data.api.VegaEcuadorQualityApi
import com.olam.warehouse.vegax.qualityecuador.data.domain.usecase.VegaEcuadorQualityUseCase
import com.olam.warehouse.vegax.qualityecuador.data.repo.VegaEcuadorQualityRepository
import com.olam.warehouse.vegax.qualityecuador.data.repo.VegaEcuadorQualityRepositoryImpl
import com.olam.warehouse.vegax.qualityecuador.ui.VegaEcuadorQualityViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.loadKoinModules
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit

/**
 * Created by Keerthi Santhanam on 6/21/2020.
 */
fun injectEcuadorQualityFeature() = loadFeature

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
    factory { VegaEcuadorQualityUseCase(get()) }
    viewModel { VegaEcuadorQualityViewModel(get(), get()) }
}

val networkModule: Module = module {
    factory { (get(named(Constants.BASE)) as Retrofit).create(VegaEcuadorQualityApi::class.java) }
}

val repositoryModule: Module = module {
    factory<VegaEcuadorQualityRepository> { VegaEcuadorQualityRepositoryImpl(get(), get()) }
}



