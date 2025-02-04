package com.olam.warehouse.vegax.bcapprovecameroon.di

import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.vegax.bcapprovecameroon.data.api.VegaBCApproveCameroonApi
import com.olam.warehouse.vegax.bcapprovecameroon.data.domain.usecase.VegaBcApproveCameroonUseCase
import com.olam.warehouse.vegax.bcapprovecameroon.data.repo.VegaApproveRepositoryImpl
import com.olam.warehouse.vegax.bcapprovecameroon.data.repo.VegaQualityApproveCameroonRepository
import com.olam.warehouse.vegax.bcapprovecameroon.ui.VegaBcApproveCameroonViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.loadKoinModules
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit

fun injectQualityApproveCameroonFeature() = loadFeature

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
    factory { VegaBcApproveCameroonUseCase(get()) }
    viewModel { VegaBcApproveCameroonViewModel(get(), get()) }
}

val networkModule: Module = module {
    factory { (get(named(Constants.BASE)) as Retrofit).create(VegaBCApproveCameroonApi::class.java) }
}

val repositoryModule: Module = module {
    factory<VegaQualityApproveCameroonRepository> { VegaApproveRepositoryImpl(get(), get()) }
}



