package com.olam.warehouse.vegax.qualitynigeria.di

import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.vegax.qualitynigeria.data.api.VegaNigeriaQualityApi
import com.olam.warehouse.vegax.qualitynigeria.data.domain.usecase.VegaNigeriaQualityUseCase
import com.olam.warehouse.vegax.qualitynigeria.data.repo.VegaNigeriaQualityRepository
import com.olam.warehouse.vegax.qualitynigeria.data.repo.VegaNigeriaQualityRepositoryImpl
import com.olam.warehouse.vegax.qualitynigeria.ui.VegaNigeriaQualityViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.loadKoinModules
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit

/**
 * Created by Keerthi Santhanam on 6/21/2020.
 */
fun injectNigeriaQualityFeature() = loadFeature

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
    factory { VegaNigeriaQualityUseCase(get()) }
    viewModel { VegaNigeriaQualityViewModel(get(), get()) }
}

val networkModule: Module = module {
    factory { (get(named(Constants.BASE)) as Retrofit).create(VegaNigeriaQualityApi::class.java) }
}

val repositoryModule: Module = module {
    factory<VegaNigeriaQualityRepository> { VegaNigeriaQualityRepositoryImpl(get(), get()) }
}



