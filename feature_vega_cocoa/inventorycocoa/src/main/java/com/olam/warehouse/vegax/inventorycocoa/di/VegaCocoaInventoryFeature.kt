package com.olam.warehouse.vegax.inventorycocoa.di

import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.vegax.inventorycocoa.data.api.VegaCocoaInventoryApi
import com.olam.warehouse.vegax.inventorycocoa.data.domain.usecase.VegaCocoaInventoryUsecase
import com.olam.warehouse.vegax.inventorycocoa.data.repo.VegaCocoaInventoryRepository
import com.olam.warehouse.vegax.inventorycocoa.data.repo.VegaCocoaInventoryRepositoryImpl
import com.olam.warehouse.vegax.inventorycocoa.ui.VegaCocoaInventoryViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.loadKoinModules
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit

/**
 * Created by Baskaran Kannan on 5/18/2020.
 */

fun injectVegaCocoaInventoryFeature() = loadFeature

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
    factory { VegaCocoaInventoryUsecase(get()) }
    viewModel { VegaCocoaInventoryViewModel(get(), get()) }
}

val networkModule: Module = module {
    factory { (get(named(Constants.BASE)) as Retrofit).create(VegaCocoaInventoryApi::class.java) }
}

val repositoryModule: Module = module {
    factory<VegaCocoaInventoryRepository> { VegaCocoaInventoryRepositoryImpl(get(), get()) }
}
