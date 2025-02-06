package com.olam.warehouse.vegax.qualitysesame.di

import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.vegax.qualitysesame.data.api.VegaNigeriaSesameQualityApi
import com.olam.warehouse.vegax.qualitysesame.data.domain.usecase.VegaNigeriaSesameQualityUseCase
import com.olam.warehouse.vegax.qualitysesame.data.repo.VegaNigeriaSesameQualityRepository
import com.olam.warehouse.vegax.qualitysesame.data.repo.VegaNigeriaSesameQualityRepositoryImpl
import com.olam.warehouse.vegax.qualitysesame.ui.VegaNigeriaSesameQualityViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.loadKoinModules
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit

/**
 * Created by Keerthi Santhanam on 6/21/2020.
 */
fun injectNigeriaSesameQualityFeature() = loadFeature

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
    factory { VegaNigeriaSesameQualityUseCase(get()) }
    viewModel { VegaNigeriaSesameQualityViewModel(get(), get()) }
}

val networkModule: Module = module {
    factory { (get(named(Constants.BASE)) as Retrofit).create(VegaNigeriaSesameQualityApi::class.java) }
}

val repositoryModule: Module = module {
    factory<VegaNigeriaSesameQualityRepository> { VegaNigeriaSesameQualityRepositoryImpl(get(), get()) }
}



