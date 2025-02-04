package com.olam.warehouse.vegax.inventoryghanacocoa.di

import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.vegax.inventoryghanacocoa.data.api.VegaGhanaCocoaInventoryApi
import com.olam.warehouse.vegax.inventoryghanacocoa.data.domain.usecase.VegaGhanaCocoaInventoryUseCase
import com.olam.warehouse.vegax.inventoryghanacocoa.data.repo.VegaGhanaCocoaInventoryRepository
import com.olam.warehouse.vegax.inventoryghanacocoa.data.repo.VegaGhanaCocoaInventoryRepositoryImpl
import com.olam.warehouse.vegax.inventoryghanacocoa.ui.VegaGhanaCocoaInventoryViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.loadKoinModules
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit

/**
 * Created by Keerthi Santhanam on 7/6/2020.
 */

fun injectVegaGhanaCocoaInventoryFeature() = loadFeature

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
    factory { VegaGhanaCocoaInventoryUseCase(get()) }
    viewModel { VegaGhanaCocoaInventoryViewModel(get(), get()) }
}

val networkModule: Module = module {
    factory { (get(named(Constants.BASE)) as Retrofit).create(VegaGhanaCocoaInventoryApi::class.java) }
}

val repositoryModule: Module = module {
    factory { VegaGhanaCocoaInventoryRepositoryImpl(get(), get()) as VegaGhanaCocoaInventoryRepository }
}
