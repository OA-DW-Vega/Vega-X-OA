package com.olam.warehouse.vegax.grnecuador.di

import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.vegax.grnecuador.data.api.VegaEcuadorGrnApi
import com.olam.warehouse.vegax.grnecuador.data.domain.usecase.VegaEcuadorGrnUseCase
import com.olam.warehouse.vegax.grnecuador.data.repo.VegaEcuadorGrnRepository
import com.olam.warehouse.vegax.grnecuador.data.repo.VegaEcuadorGrnRepositoryImpl
import com.olam.warehouse.vegax.grnecuador.ui.VegaEcuadorGrnViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.loadKoinModules
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit

/**
 * Created by Keerthi Santhanam on 6/29/2020.
 */
fun injectEcuadorGrnFeature() = loadFeature

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
    factory { VegaEcuadorGrnUseCase(get()) }
    viewModel { VegaEcuadorGrnViewModel(get(), get()) }
}

val networkModule: Module = module {
    factory { (get(named(Constants.BASE)) as Retrofit).create(VegaEcuadorGrnApi::class.java) }
}

val repositoryModule: Module = module {
    factory<VegaEcuadorGrnRepository> { VegaEcuadorGrnRepositoryImpl(get(), get()) }
}



