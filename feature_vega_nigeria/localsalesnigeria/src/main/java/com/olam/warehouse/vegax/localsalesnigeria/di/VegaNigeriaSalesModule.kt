package com.olam.warehouse.vegax.localsalesnigeria.di

import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.vegax.localsalesnigeria.data.api.VegaNigeriaSalesApi
import com.olam.warehouse.vegax.localsalesnigeria.data.domain.usecase.VegaNigeriaSalesDispatchUseCase
import com.olam.warehouse.vegax.localsalesnigeria.data.repo.VegaCoffeeSalesRepository
import com.olam.warehouse.vegax.localsalesnigeria.data.repo.VegaCoffeeSalesRepositoryImpl
import com.olam.warehouse.vegax.localsalesnigeria.ui.VegaNigeriaSalesViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.loadKoinModules
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit

/**
 * Created by Baskaran Kannan on 8/19/2020.
 */

fun injectNigeriaSalesFeature() = loadFeature

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
    factory { VegaNigeriaSalesDispatchUseCase(get()) }
    viewModel { VegaNigeriaSalesViewModel(get(), get()) }
}

val networkModule: Module = module {
    factory { (get(named(Constants.BASE)) as Retrofit).create(VegaNigeriaSalesApi::class.java) }
}

val repositoryModule: Module = module {
    factory<VegaCoffeeSalesRepository> { VegaCoffeeSalesRepositoryImpl(get(), get()) }
}
