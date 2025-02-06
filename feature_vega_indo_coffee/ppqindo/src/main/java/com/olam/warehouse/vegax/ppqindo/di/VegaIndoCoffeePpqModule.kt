package com.olam.warehouse.vegax.ppqindo.di

import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.vegax.ppqindo.data.api.VegaIndoCoffeePpqApi
import com.olam.warehouse.vegax.ppqindo.data.domain.usecase.VegaIndoCoffeePpqUsecase
import com.olam.warehouse.vegax.ppqindo.data.repo.VegaIndoCoffeePpqRepository
import com.olam.warehouse.vegax.ppqindo.data.repo.VegaIndoCoffeePpqRepositoryImpl
import com.olam.warehouse.vegax.ppqindo.ui.VegaIndoCoffeePpqViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.loadKoinModules
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit

/**
 * Created by Baskaran Kannan on 4/9/2021.
 */

fun injectIndoCoffeePpqFeature1() = loadFeature

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
    factory { VegaIndoCoffeePpqUsecase(get()) }
    viewModel { VegaIndoCoffeePpqViewModel(get(), get()) }
}

val networkModule: Module = module {
    factory { (get(named(Constants.BASE)) as Retrofit).create(VegaIndoCoffeePpqApi::class.java) }
}

val repositoryModule: Module = module {
    factory<VegaIndoCoffeePpqRepository> { VegaIndoCoffeePpqRepositoryImpl(get()) }
}
