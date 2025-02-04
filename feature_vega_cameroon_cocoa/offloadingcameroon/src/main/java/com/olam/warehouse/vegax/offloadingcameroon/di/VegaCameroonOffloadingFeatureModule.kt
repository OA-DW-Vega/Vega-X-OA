package com.olam.warehouse.vegax.offloadingcameroon.di

import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.vegax.offloadingcameroon.data.api.VegaCameroonOffloadingApi
import com.olam.warehouse.vegax.offloadingcameroon.data.domain.usecase.VegaCameroonOffloadingUseCase
import com.olam.warehouse.vegax.offloadingcameroon.data.repo.VegaCameroonOffloadingRepository
import com.olam.warehouse.vegax.offloadingcameroon.data.repo.VegaCameroonOffloadingRepositoryImpl
import com.olam.warehouse.vegax.offloadingcameroon.ui.VegaCameroonOffloadingViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.loadKoinModules
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit

fun injectCameroonOffloadingFeature() = loadFeature

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
    factory { VegaCameroonOffloadingUseCase(get()) }
    viewModel { VegaCameroonOffloadingViewModel(get(), get()) }
}

val networkModule: Module = module {
    factory { (get(named(Constants.BASE)) as Retrofit).create(VegaCameroonOffloadingApi::class.java) }
}

val repositoryModule: Module = module {
    factory<VegaCameroonOffloadingRepository> { VegaCameroonOffloadingRepositoryImpl(get(), get(), get(), get()) }
}



