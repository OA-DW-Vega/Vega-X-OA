package com.olam.warehouse.vegax.containermanagementnigeria.di

import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.vegax.containermanagementnigeria.data.api.VegaNigeriaContainerManagementApi
import com.olam.warehouse.vegax.containermanagementnigeria.data.domain.usecase.VegaNigeriaContainerManagementUseCase
import com.olam.warehouse.vegax.containermanagementnigeria.data.repo.VegaNigeriaContainerManagementRepository
import com.olam.warehouse.vegax.containermanagementnigeria.data.repo.VegaNigeriaContainerManagementRepositoryImpl
import com.olam.warehouse.vegax.containermanagementnigeria.ui.VegaNigeriaContainerManagementViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.loadKoinModules
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit

/**
 * Created by Keerthi Santhanam on 6/29/2020.
 */
fun injectNigeriaContainerMangementFeature() = loadFeature

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
    factory { VegaNigeriaContainerManagementUseCase(get()) }
    viewModel { VegaNigeriaContainerManagementViewModel(get(), get()) }
}

val networkModule: Module = module {
    factory { (get(named(Constants.BASE)) as Retrofit).create(VegaNigeriaContainerManagementApi::class.java) }
}

val repositoryModule: Module = module {
    factory<VegaNigeriaContainerManagementRepository> { VegaNigeriaContainerManagementRepositoryImpl(get(), get()) }
}
