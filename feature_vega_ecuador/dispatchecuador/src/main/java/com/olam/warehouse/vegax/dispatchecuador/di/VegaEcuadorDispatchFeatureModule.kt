package com.olam.warehouse.vegax.dispatchecuador.di

import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.vegax.dispatchecuador.data.api.VegaEcuadorDispatchApi
import com.olam.warehouse.vegax.dispatchecuador.data.domain.usecase.VegaEcuadorDispatchUseCase
import com.olam.warehouse.vegax.dispatchecuador.data.repo.VegaEcuadorDispatchRepository
import com.olam.warehouse.vegax.dispatchecuador.data.repo.VegaEcuadorDispatchRepositoryImpl
import com.olam.warehouse.vegax.dispatchecuador.ui.VegaEcuadorDispatchViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.loadKoinModules
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit

/**
 * Created by Keerthi Santhanam on 7/12/2020.
 */
fun injectEcuadorDispatchFeature() = loadFeature

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
    factory { VegaEcuadorDispatchUseCase(get()) }
    viewModel { VegaEcuadorDispatchViewModel(get(), get()) }
}

val networkModule: Module = module {
    factory { (get(named(Constants.BASE)) as Retrofit).create(VegaEcuadorDispatchApi::class.java) }
}

val repositoryModule: Module = module {
    factory<VegaEcuadorDispatchRepository> { VegaEcuadorDispatchRepositoryImpl(get(), get(), get()) }
}



