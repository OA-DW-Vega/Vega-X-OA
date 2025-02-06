package com.olam.warehouse.vegax.gateentrycameroon.di

import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.vegax.gateentrycameroon.data.api.VegaGateEntryCameroonApi
import com.olam.warehouse.vegax.gateentrycameroon.data.domain.usecase.VegaGateEntryCameroonUseCase
import com.olam.warehouse.vegax.gateentrycameroon.data.repo.VegaGateEntryCameroonRepository
import com.olam.warehouse.vegax.gateentrycameroon.data.repo.VegaGateEntryCameroonRepositoryImpl
import com.olam.warehouse.vegax.gateentrycameroon.ui.VegaGateEntryCameroonViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.loadKoinModules
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit

/**
 * Created by Baskaran Kannan on 3/4/2020.
 */
fun injectGateEntryCameroonFeature() = loadFeature

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
    factory { VegaGateEntryCameroonUseCase(get()) }
    viewModel { VegaGateEntryCameroonViewModel(get(), get()) }
}

val networkModule: Module = module {
    factory { (get(named(Constants.BASE)) as Retrofit).create(VegaGateEntryCameroonApi::class.java) }
}

val repositoryModule: Module = module {
    factory<VegaGateEntryCameroonRepository> { VegaGateEntryCameroonRepositoryImpl(get(), get(), get()) }
}
