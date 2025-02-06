package com.olam.warehouse.vegax.ginningwarehouse.ui.di

import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.vegax.ginningwarehouse.data.domain.VegaCottonDispatchUseCase
import com.olam.warehouse.vegax.ginningwarehouse.ui.data.api.GinningDispatchApi
import com.olam.warehouse.vegax.ginningwarehouse.ui.data.repo.GinningDispatchRepository
import com.olam.warehouse.vegax.ginningwarehouse.ui.data.repo.VegaGinningDispatchRepositoryImpl
import com.olam.warehouse.vegax.ginningwarehouse.ui.ui.dispatch.GinningDispatchViewModel
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
            viewModelDispatchModule,
            networkDispatchModule,
            repositoryDispatchModule
        )
    )
}

val viewModelDispatchModule: Module = module {
    factory { VegaCottonDispatchUseCase(get()) }
    viewModel { GinningDispatchViewModel(get(), get()) }
}

val networkDispatchModule: Module = module {
    factory { (get(named(Constants.BASE)) as Retrofit).create(GinningDispatchApi::class.java) }
}

val repositoryDispatchModule: Module = module {
    factory<GinningDispatchRepository> { VegaGinningDispatchRepositoryImpl(get(), get()) }
}
