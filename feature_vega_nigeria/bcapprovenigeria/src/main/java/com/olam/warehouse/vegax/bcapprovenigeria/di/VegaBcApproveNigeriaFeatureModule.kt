package com.olam.warehouse.vegax.bcapprovenigeria.di

import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.vegax.bcapprovenigeria.data.api.VegaBCApproveNigeriaApi
import com.olam.warehouse.vegax.bcapprovenigeria.data.domain.usecase.VegaBcApproveNigeriaUseCase
import com.olam.warehouse.vegax.bcapprovenigeria.data.repo.VegaApproveRepositoryImpl
import com.olam.warehouse.vegax.bcapprovenigeria.data.repo.VegaQualityApproveNigeriaRepository
import com.olam.warehouse.vegax.bcapprovenigeria.ui.VegaBcApproveNigeriaViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.loadKoinModules
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit

fun injectQualityApproveNigeriaFeature() = loadFeature

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
    factory { VegaBcApproveNigeriaUseCase(get()) }
    viewModel { VegaBcApproveNigeriaViewModel(get(), get()) }
}

val networkModule: Module = module {
    factory { (get(named(Constants.BASE)) as Retrofit).create(VegaBCApproveNigeriaApi::class.java) }
}

val repositoryModule: Module = module {
    factory<VegaQualityApproveNigeriaRepository> { VegaApproveRepositoryImpl(get(), get()) }
}



