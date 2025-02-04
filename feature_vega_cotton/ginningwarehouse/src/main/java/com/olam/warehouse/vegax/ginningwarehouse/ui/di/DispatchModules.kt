package com.olam.warehouse.ginning.di

import com.olam.warehouse.ginning.data.api.GinningDispatchApi
import com.olam.warehouse.ginning.data.repo.GinningDispatchRepository
import com.olam.warehouse.ginning.data.repo.VegaGinningDispatchRepositoryImpl
import com.olam.warehouse.ginning.ui.dispatch.GinningDispatchViewModel
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.vegax.ginningwarehouse.data.domain.VegaCottonDispatchUseCase
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.loadKoinModules
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit


fun injectGinningDispatchFeature() = loadFeature

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
    factory { VegaCottonDispatchUseCase(get()) }
    viewModel { GinningDispatchViewModel(get(), get()) }
}

val networkModule: Module = module {
    factory { (get(named(Constants.BASE)) as Retrofit).create(GinningDispatchApi::class.java) }
}

val repositoryModule: Module = module {
    factory<GinningDispatchRepository> { VegaGinningDispatchRepositoryImpl(get(), get()) }
}
