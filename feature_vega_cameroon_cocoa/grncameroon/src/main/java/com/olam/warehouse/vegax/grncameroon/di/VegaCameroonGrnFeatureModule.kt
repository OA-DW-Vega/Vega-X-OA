package com.olam.warehouse.vegax.grncameroon.di

import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.vegax.grncameroon.data.api.VegaCameroonGrnApi
import com.olam.warehouse.vegax.grncameroon.data.domain.usecase.VegaCameroonGrnUseCase
import com.olam.warehouse.vegax.grncameroon.data.repo.VegaCameroonGrnRepository
import com.olam.warehouse.vegax.grncameroon.data.repo.VegaCameroonGrnRepositoryImpl
import com.olam.warehouse.vegax.grncameroon.ui.VegaCameroonGrnViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.loadKoinModules
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit

/**
 * Created by Keerthi Santhanam on 6/29/2020.
 */
fun injectCameroonGrnFeature() = loadFeature

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
    factory { VegaCameroonGrnUseCase(get()) }
    viewModel { VegaCameroonGrnViewModel(get(), get()) }
}

val networkModule: Module = module {
    factory { (get(named(Constants.BASE)) as Retrofit).create(VegaCameroonGrnApi::class.java) }
}

val repositoryModule: Module = module {
    factory<VegaCameroonGrnRepository> { VegaCameroonGrnRepositoryImpl(get(), get()) }
}



