package com.olam.warehouse.vegax.receivingghana.di

import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.vegax.receivingghana.data.api.VegaGhanaReceivingApi
import com.olam.warehouse.vegax.receivingghana.data.domain.usecase.VegaGhanaReceivingUseCase
import com.olam.warehouse.vegax.receivingghana.data.repo.VegaGhanaReceivingRepository
import com.olam.warehouse.vegax.receivingghana.data.repo.VegaGhanaReceivingRepositoryImpl
import com.olam.warehouse.vegax.receivingghana.ui.VegaGhanaReceivingViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.loadKoinModules
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit

/**
 * Created by Baskaran Kannan on 9/24/2020.
 */

fun injectGhanaReceivingFeature() = loadFeature

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
    factory { VegaGhanaReceivingUseCase(get()) }
    viewModel { VegaGhanaReceivingViewModel(get(), get()) }
}

val networkModule: Module = module {
    factory { (get(named(Constants.BASE)) as Retrofit).create(VegaGhanaReceivingApi::class.java) }
}

val repositoryModule: Module = module {
    factory<VegaGhanaReceivingRepository> { VegaGhanaReceivingRepositoryImpl(get(), get(), get()) }
}
