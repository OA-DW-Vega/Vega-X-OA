package com.olam.warehouse.vegax.grnsesame.di

import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.vegax.grnsesame.data.api.VegaNigeriaSesameGrnApi
import com.olam.warehouse.vegax.grnsesame.data.domain.usecase.VegaNigeriaSesameGrnUseCase
import com.olam.warehouse.vegax.grnsesame.data.repo.VegaNigeriaSesameGrnRepository
import com.olam.warehouse.vegax.grnsesame.data.repo.VegaNigeriaSesameGrnRepositoryImpl
import com.olam.warehouse.vegax.grnsesame.ui.VegaNigeriaSesameGrnViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.loadKoinModules
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit

/**
 * Created by Keerthi Santhanam on 6/29/2020.
 */
fun injectNigeriaSesameGrnFeature() = loadFeature

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
    factory { VegaNigeriaSesameGrnUseCase(get()) }
    viewModel { VegaNigeriaSesameGrnViewModel(get(), get()) }
}

val networkModule: Module = module {
    factory { (get(named(Constants.BASE)) as Retrofit).create(VegaNigeriaSesameGrnApi::class.java) }
}

val repositoryModule: Module = module {
    factory<VegaNigeriaSesameGrnRepository> { VegaNigeriaSesameGrnRepositoryImpl(get(), get()) }
}



