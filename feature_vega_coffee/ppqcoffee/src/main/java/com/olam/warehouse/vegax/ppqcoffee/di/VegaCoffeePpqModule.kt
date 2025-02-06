package com.olam.warehouse.vegax.ppqcoffee.di

import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.vegax.ppqcoffee.data.api.VegaCoffeePpqApi
import com.olam.warehouse.vegax.ppqcoffee.data.domain.usecase.VegaCoffeePpqUsecase
import com.olam.warehouse.vegax.ppqcoffee.data.repo.VegaCoffeePpqRepository
import com.olam.warehouse.vegax.ppqcoffee.data.repo.VegaCoffeePpqRepositoryImpl
import com.olam.warehouse.vegax.ppqcoffee.ui.VegaCoffeePpqViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.loadKoinModules
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit


/**
 * Created by Baskaran Kannan on 8/7/2020.
 */

fun injectCoffeePpqFeature() = loadFeature

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
    factory { VegaCoffeePpqUsecase(get()) }
    viewModel { VegaCoffeePpqViewModel(get(), get()) }
}

val networkModule: Module = module {
    factory { (get(named(Constants.BASE)) as Retrofit).create(VegaCoffeePpqApi::class.java) }
}

val repositoryModule: Module = module {
    factory<VegaCoffeePpqRepository> { VegaCoffeePpqRepositoryImpl(get()) }
}
