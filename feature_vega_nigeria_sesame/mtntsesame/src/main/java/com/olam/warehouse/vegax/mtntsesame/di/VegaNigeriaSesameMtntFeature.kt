package com.olam.warehouse.vegax.mtntsesame.di

import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.vegax.mtntsesame.data.api.VegaNigeriaSesameMtntApi
import com.olam.warehouse.vegax.mtntsesame.data.domain.usecase.VegaNigeriaSesameDispatchUseCase
import com.olam.warehouse.vegax.mtntsesame.data.repo.VegaNigeriaSesameMtntRepository
import com.olam.warehouse.vegax.mtntsesame.data.repo.VegaNigeriaSesameMtntRepositoryImpl
import com.olam.warehouse.vegax.mtntsesame.ui.VegaNigeriaSesameMtntViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.loadKoinModules
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit


fun injectNigeriaSesameMtntDispatchFeature() = loadFeature

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
    factory { VegaNigeriaSesameDispatchUseCase(get()) }
    viewModel { VegaNigeriaSesameMtntViewModel(get(), get()) }
}

val networkModule: Module = module {
    factory { (get(named(Constants.BASE)) as Retrofit).create(VegaNigeriaSesameMtntApi::class.java) }
}

val repositoryModule: Module = module {
    factory<VegaNigeriaSesameMtntRepository> { VegaNigeriaSesameMtntRepositoryImpl(get(), get(), get()) }
}
