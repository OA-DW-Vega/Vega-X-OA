package com.olam.warehouse.vegax.approve.di

import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.vegax.approve.data.api.VegaApproveApi
import com.olam.warehouse.vegax.approve.data.domain.usecase.VegaApproveUseCase
import com.olam.warehouse.vegax.approve.data.repo.VegaApproveRepository
import com.olam.warehouse.vegax.approve.data.repo.VegaApproveRepositoryImpl
import com.olam.warehouse.vegax.approve.ui.VegaApproveViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.loadKoinModules
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit

fun injectApprovalFeature() = loadFeature

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
    factory { VegaApproveUseCase(get()) }
    viewModel { VegaApproveViewModel(get(), get()) }
}

val networkModule: Module = module {
    factory { (get(named(Constants.BASE)) as Retrofit).create(VegaApproveApi::class.java) }
}

val repositoryModule: Module = module {
    factory<VegaApproveRepository> { VegaApproveRepositoryImpl(get()) }
}



