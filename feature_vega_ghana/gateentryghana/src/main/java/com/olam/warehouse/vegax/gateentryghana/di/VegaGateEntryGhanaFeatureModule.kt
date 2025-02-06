package com.olam.warehouse.vegax.gateentryghana.di

import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.vegax.gateentryghana.data.api.VegaGateEntryGhanaApi
import com.olam.warehouse.vegax.gateentryghana.data.domain.usecase.VegaGateEntryGhanaUseCase
import com.olam.warehouse.vegax.gateentryghana.data.repo.VegaGateEntryGhanaRepository
import com.olam.warehouse.vegax.gateentryghana.data.repo.VegaGateEntryGhanaRepositoryImpl
import com.olam.warehouse.vegax.gateentryghana.ui.VegaGateEntryGhanaViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.loadKoinModules
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit

/**
 * Created by Baskaran Kannan on 3/4/2020.
 */
fun injectGateEntryGhanaFeature() = loadFeature

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
    factory { VegaGateEntryGhanaUseCase(get()) }
    viewModel { VegaGateEntryGhanaViewModel(get(), get()) }
}

val networkModule: Module = module {
    factory { (get(named(Constants.BASE)) as Retrofit).create(VegaGateEntryGhanaApi::class.java) }
}

val repositoryModule: Module = module {
    factory<VegaGateEntryGhanaRepository> { VegaGateEntryGhanaRepositoryImpl(get(), get(), get()) }
}
