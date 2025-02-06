package com.olam.warehouse.vegax.mtntcameroon.di

import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.vegax.mtntcameroon.data.api.VegaCameroonMtntApi
import com.olam.warehouse.vegax.mtntcameroon.data.domain.usecase.VegaCameroonDispatchUseCase
import com.olam.warehouse.vegax.mtntcameroon.data.repo.VegaCameroonMtntRepository
import com.olam.warehouse.vegax.mtntcameroon.data.repo.VegaCameroonMtntRepositoryImpl
import com.olam.warehouse.vegax.mtntcameroon.ui.VegaCameroonMtntViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.loadKoinModules
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit


fun injectCameroonDispatchFeature() = loadFeature

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
    factory { VegaCameroonDispatchUseCase(get()) }
    viewModel { VegaCameroonMtntViewModel(get(), get()) }
}

val networkModule: Module = module {
    factory { (get(named(Constants.BASE)) as Retrofit).create(VegaCameroonMtntApi::class.java) }
}

val repositoryModule: Module = module {
    factory<VegaCameroonMtntRepository> { VegaCameroonMtntRepositoryImpl(get(), get(), get()) }
}
