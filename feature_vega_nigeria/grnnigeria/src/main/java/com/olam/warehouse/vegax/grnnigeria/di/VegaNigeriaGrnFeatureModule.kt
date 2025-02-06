package com.olam.warehouse.vegax.grnnigeria.di

import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.vegax.grnnigeria.ui.VegaNigeriaGrnViewModel
import com.olam.warehouse.vegax.grnnigeria.data.api.VegaNigeriaGrnApi
import com.olam.warehouse.vegax.grnnigeria.data.domain.usecase.VegaNigeriaGrnUseCase
import com.olam.warehouse.vegax.grnnigeria.data.repo.VegaNigeriaGrnRepository
import com.olam.warehouse.vegax.grnnigeria.data.repo.VegaNigeriaGrnRepositoryImpl
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.loadKoinModules
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit

/**
 * Created by Roshna Parambil on 9/9/2020.
 */
fun injectNigeriaGrnFeature() = loadFeature

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
    factory { VegaNigeriaGrnUseCase(get()) }
    viewModel { VegaNigeriaGrnViewModel(get(), get()) }
}

val networkModule: Module = module {
    factory { (get(named(Constants.BASE)) as Retrofit).create(VegaNigeriaGrnApi::class.java) }
}

val repositoryModule: Module = module {
    factory<VegaNigeriaGrnRepository> { VegaNigeriaGrnRepositoryImpl(get(), get()) }
}



