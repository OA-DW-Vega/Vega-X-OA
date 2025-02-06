package com.olam.warehouse.vegax.containermanagement.di

import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.vegax.containermanagement.data.api.VegaCameroonContainerManagementApi
import com.olam.warehouse.vegax.containermanagement.data.domain.usecase.VegaCameroonContainerManagementUseCase
import com.olam.warehouse.vegax.containermanagement.data.repo.VegaCameroonContainerManagementRepository
import com.olam.warehouse.vegax.containermanagement.data.repo.VegaCameroonContainerManagementRepositoryImpl
import com.olam.warehouse.vegax.containermanagement.ui.VegaCameroonContainerManagementViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.loadKoinModules
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit

/**
 * Created by Keerthi Santhanam on 6/29/2020.
 */
fun injectCameroonContainerMangementFeature() = loadFeature

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
    factory { VegaCameroonContainerManagementUseCase(get()) }
    viewModel { VegaCameroonContainerManagementViewModel(get(), get()) }
}

val networkModule: Module = module {
    factory { (get(named(Constants.BASE)) as Retrofit).create(VegaCameroonContainerManagementApi::class.java) }
}

val repositoryModule: Module = module {
    factory<VegaCameroonContainerManagementRepository> { VegaCameroonContainerManagementRepositoryImpl(get(), get()) }
}
