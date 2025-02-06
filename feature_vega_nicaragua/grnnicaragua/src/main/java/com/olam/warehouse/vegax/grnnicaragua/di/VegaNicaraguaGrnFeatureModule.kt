package com.olam.warehouse.vegax.grnnicaragua.di

import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.vegax.grnnicaragua.data.api.VegaNicaraguaGrnApi
import com.olam.warehouse.vegax.grnnicaragua.data.domain.usecase.VegaNicaraguaGrnUseCase
import com.olam.warehouse.vegax.grnnicaragua.data.repo.VegaNicaraguaGrnRepository
import com.olam.warehouse.vegax.grnnicaragua.data.repo.VegaNicaraguaGrnRepositoryImpl
import com.olam.warehouse.vegax.grnnicaragua.ui.VegaNicaraguaGrnViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.loadKoinModules
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit

/**
 * Created by Keerthi Santhanam on 9/01/2020.
 */
fun injectNicaraguaGrnFeature() = loadFeature

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
    factory { VegaNicaraguaGrnUseCase(get()) }
    viewModel { VegaNicaraguaGrnViewModel(get(), get()) }
}

val networkModule: Module = module {
    factory { (get(named(Constants.BASE)) as Retrofit).create(VegaNicaraguaGrnApi::class.java) }
}

val repositoryModule: Module = module {
    factory<VegaNicaraguaGrnRepository> { VegaNicaraguaGrnRepositoryImpl(get(), get()) }
}



