package com.olam.warehouse.vegax.ginningwarehouse.ui.di

import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.vegax.ginningwarehouse.ui.data.api.DryingApi
import com.olam.warehouse.vegax.ginningwarehouse.ui.data.domain.VegaCottonGinningDryingUsecase
import com.olam.warehouse.vegax.ginningwarehouse.ui.data.repo.DryingRepository
import com.olam.warehouse.vegax.ginningwarehouse.ui.data.repo.VegaGinningDryingRepositoryImpl
import com.olam.warehouse.vegax.ginningwarehouse.ui.ui.drying.GinningDryingViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.loadKoinModules
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit


fun injectGinningDryingFeature() = loadFeature

private val loadFeature by lazy {
    loadKoinModules(
        listOf(
            viewModelDryingModule,
            networkDryingModule,
            repositoryDryingModule
        )
    )
}

val viewModelDryingModule: Module = module {
    factory { VegaCottonGinningDryingUsecase(get()) }
    viewModel { GinningDryingViewModel(get(), get()) }
}

val networkDryingModule: Module = module {
    factory { (get(named(Constants.BASE)) as Retrofit).create(DryingApi::class.java) }
}

val repositoryDryingModule: Module = module {
    factory<DryingRepository> { VegaGinningDryingRepositoryImpl(get()) }
}
