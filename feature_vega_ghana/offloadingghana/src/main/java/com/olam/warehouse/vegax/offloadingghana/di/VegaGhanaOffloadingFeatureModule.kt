package com.olam.warehouse.vegax.offloadingghana.di

import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.vegax.offloadingghana.data.api.VegaGhanaOffloadingApi
import com.olam.warehouse.vegax.offloadingghana.data.domain.usecase.VegaGhanaOffloadingUseCase
import com.olam.warehouse.vegax.offloadingghana.data.repo.VegaGhanaOffloadingRepository
import com.olam.warehouse.vegax.offloadingghana.data.repo.VegaGhanaOffloadingRepositoryImpl
import com.olam.warehouse.vegax.offloadingghana.ui.VegaGhanaOffloadingViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.loadKoinModules
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit

fun injectGhanaOffloadingFeature() = loadFeature

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
    factory { VegaGhanaOffloadingUseCase(get()) }
    viewModel { VegaGhanaOffloadingViewModel(get(), get()) }
}

val networkModule: Module = module {
    factory { (get(named(Constants.BASE)) as Retrofit).create(VegaGhanaOffloadingApi::class.java) }
}

val repositoryModule: Module = module {
    factory<VegaGhanaOffloadingRepository> { VegaGhanaOffloadingRepositoryImpl(get(), get(), get(), get()) }
}



