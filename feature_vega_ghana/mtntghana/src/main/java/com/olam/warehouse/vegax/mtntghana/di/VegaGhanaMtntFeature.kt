package com.olam.warehouse.vegax.mtntghana.di

import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.vegax.mtntghana.data.api.VegaGhanaMtntApi
import com.olam.warehouse.vegax.mtntghana.data.domain.usecase.VegaGhanaDispatchUseCase
import com.olam.warehouse.vegax.mtntghana.data.repo.VegaGhanaMtntRepository
import com.olam.warehouse.vegax.mtntghana.data.repo.VegaGhanaMtntRepositoryImpl
import com.olam.warehouse.vegax.mtntghana.ui.VegaGhanaMtntViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.loadKoinModules
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit


fun injectGhanaMtntDispatchFeature() = loadFeature

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
    factory { VegaGhanaDispatchUseCase(get()) }
    viewModel { VegaGhanaMtntViewModel(get(), get()) }
}

val networkModule: Module = module {
    factory { (get(named(Constants.BASE)) as Retrofit).create(VegaGhanaMtntApi::class.java) }
}

val repositoryModule: Module = module {
    factory<VegaGhanaMtntRepository> { VegaGhanaMtntRepositoryImpl(get(), get(), get(), get()) }
}
