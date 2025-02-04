package com.olam.warehouse.vegax.inventory.di

import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.vegax.inventory.data.api.VegaInventoryApi
import com.olam.warehouse.vegax.inventory.data.domain.usecase.VegaInventoryUseCase
import com.olam.warehouse.vegax.inventory.data.repo.VegaInventoryRepository
import com.olam.warehouse.vegax.inventory.data.repo.VegaInventoryRepositoryImpl
import com.olam.warehouse.vegax.inventory.ui.VegaInventoryViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.loadKoinModules
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit

fun injectInventoryFeature() = loadFeature

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
    factory { VegaInventoryUseCase(get()) }
    viewModel { VegaInventoryViewModel(get(), get()) }
}

val networkModule: Module = module {
    factory { (get(named(Constants.BASE)) as Retrofit).create(VegaInventoryApi::class.java) }
}

val repositoryModule: Module = module {
    factory<VegaInventoryRepository> { VegaInventoryRepositoryImpl(get(), get()) }
}
