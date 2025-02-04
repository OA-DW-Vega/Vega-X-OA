package com.olam.warehouse.vegax.receiving.di

import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.vegax.receiving.data.api.VegaMtntApi
import com.olam.warehouse.vegax.receiving.data.api.VegaReceivingApi
import com.olam.warehouse.vegax.receiving.data.domain.usecase.VegaMtntUseCase
import com.olam.warehouse.vegax.receiving.data.domain.usecase.VegaReceivingUseCase
import com.olam.warehouse.vegax.receiving.data.repo.VegaMtntRepository
import com.olam.warehouse.vegax.receiving.data.repo.VegaMtntRepositoryImpl
import com.olam.warehouse.vegax.receiving.data.repo.VegaReceivingRepository
import com.olam.warehouse.vegax.receiving.data.repo.VegaReceivingRepositoryImpl
import com.olam.warehouse.vegax.receiving.ui.VegaMtntViewModel
import com.olam.warehouse.vegax.receiving.ui.VegaReceivingViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.loadKoinModules
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit

/**
 * Created by Baskaran Kannan on 1/28/2020.
 */

fun injectVegaReceivingFeature() = loadFeature

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
    factory { VegaReceivingUseCase(get()) }
    factory { VegaMtntUseCase(get()) }
    viewModel { VegaReceivingViewModel(get(), get()) }
    viewModel { VegaMtntViewModel(get(), get()) }
}

val repositoryModule = module {
    factory { VegaReceivingRepositoryImpl(get(), get(), get()) as VegaReceivingRepository }
    factory { VegaMtntRepositoryImpl(get(), get()) as VegaMtntRepository }
}

val networkModule: Module = module {
    factory { (get(named(Constants.BASE)) as Retrofit).create(VegaReceivingApi::class.java) }
    factory { (get(named(Constants.BASE)) as Retrofit).create(VegaMtntApi::class.java) }

}
