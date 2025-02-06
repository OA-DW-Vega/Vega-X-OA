package com.olam.warehouse.vegax.localsalescameroon.di

import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.vegax.localsalescameroon.data.api.VegaCameroonSalesApi
import com.olam.warehouse.vegax.localsalescameroon.data.domain.usecase.VegaCameroonSalesDispatchUseCase
import com.olam.warehouse.vegax.localsalescameroon.data.repo.VegaCoffeeSalesRepository
import com.olam.warehouse.vegax.localsalescameroon.data.repo.VegaCoffeeSalesRepositoryImpl
import com.olam.warehouse.vegax.localsalescameroon.ui.VegaCameroonSalesViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.loadKoinModules
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit

/**
 * Created by Baskaran Kannan on 8/19/2020.
 */

fun injectCameroonSalesFeature() = loadFeature

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
    factory { VegaCameroonSalesDispatchUseCase(get()) }
    viewModel { VegaCameroonSalesViewModel(get(), get()) }
}

val networkModule: Module = module {
    factory { (get(named(Constants.BASE)) as Retrofit).create(VegaCameroonSalesApi::class.java) }
}

val repositoryModule: Module = module {
    factory<VegaCoffeeSalesRepository> { VegaCoffeeSalesRepositoryImpl(get(), get(), get(), get()) }
}
