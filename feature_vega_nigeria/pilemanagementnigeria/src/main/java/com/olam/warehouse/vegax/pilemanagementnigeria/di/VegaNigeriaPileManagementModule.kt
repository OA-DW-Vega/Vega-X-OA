package com.olam.warehouse.vegax.pilemanagementnigeria.di

import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.vegax.pilemanagementnigeria.data.api.VegaNigeriaPileManagementApi
import com.olam.warehouse.vegax.pilemanagementnigeria.data.domain.VegNigeriaPileManagementUseCase
import com.olam.warehouse.vegax.pilemanagementnigeria.data.repo.VegaNigeriaPileManagementRepository
import com.olam.warehouse.vegax.pilemanagementnigeria.data.repo.VegaNigeriaPileManagementRepositoryImpl
import com.olam.warehouse.vegax.pilemanagementnigeria.ui.VegaNigeriaPileManagementViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.loadKoinModules
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit

fun injectNigeriaProcessingFeature() = loadFeature

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
    factory { VegNigeriaPileManagementUseCase(get()) }
    viewModel { VegaNigeriaPileManagementViewModel(get(), get()) }
}

val networkModule: Module = module {
    factory { (get(named(Constants.BASE)) as Retrofit).create(VegaNigeriaPileManagementApi::class.java) }
}

val repositoryModule: Module = module {
    factory<VegaNigeriaPileManagementRepository> {
        VegaNigeriaPileManagementRepositoryImpl(
            get(),
            get(),
            get(),
            get()
        )
    }
}
