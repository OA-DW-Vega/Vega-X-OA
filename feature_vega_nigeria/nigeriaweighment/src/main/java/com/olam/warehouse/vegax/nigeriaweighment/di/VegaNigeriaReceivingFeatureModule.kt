package com.olam.warehouse.vegax.nigeriaweighment.di

import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.vegax.nigeriaweighment.data.api.VegaNigeriaMtntApi
import com.olam.warehouse.vegax.nigeriaweighment.data.api.VegaNigeriaReceivingApi
import com.olam.warehouse.vegax.nigeriaweighment.data.domain.usecase.VegaNigeriaMtntUseCase
import com.olam.warehouse.vegax.nigeriaweighment.data.domain.usecase.VegaNigeriaReceivingUseCase
import com.olam.warehouse.vegax.nigeriaweighment.data.repo.VegaMtntRepository
import com.olam.warehouse.vegax.nigeriaweighment.data.repo.VegaMtntRepositoryImpl
import com.olam.warehouse.vegax.nigeriaweighment.data.repo.VegaReceivingRepository
import com.olam.warehouse.vegax.nigeriaweighment.data.repo.VegaReceivingRepositoryImpl
import com.olam.warehouse.vegax.nigeriaweighment.ui.VegaNigeriaMtntViewModel
import com.olam.warehouse.vegax.nigeriaweighment.ui.VegaNigeriaReceivingViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.loadKoinModules
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit

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
    factory { VegaNigeriaReceivingUseCase(get()) }
    factory { VegaNigeriaMtntUseCase(get()) }
    viewModel { VegaNigeriaReceivingViewModel(get(), get()) }
    viewModel { VegaNigeriaMtntViewModel(get(), get()) }
}

val repositoryModule = module {
    factory <VegaReceivingRepository> { VegaReceivingRepositoryImpl(get(), get(), get()) }
    factory <VegaMtntRepository> { VegaMtntRepositoryImpl(get(), get()) }
}

val networkModule: Module = module {
    factory { (get(named(Constants.BASE)) as Retrofit).create(VegaNigeriaReceivingApi::class.java) }
    factory { (get(named(Constants.BASE)) as Retrofit).create(VegaNigeriaMtntApi::class.java) }

}
