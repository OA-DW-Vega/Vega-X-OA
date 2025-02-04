package com.olam.warehouse.vegax.processingghana.di

import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.vegax.processingghana.data.api.VegaGhanaProcessingApi
import com.olam.warehouse.vegax.processingghana.data.domain.usecase.VegaGhanaProcessingUseCase
import com.olam.warehouse.vegax.processingghana.data.repo.VegaGhanaProcessingRepoImpl
import com.olam.warehouse.vegax.processingghana.data.repo.VegaGhanaProcessingRepository
import com.olam.warehouse.vegax.processingghana.ui.fgrn.VegaGhanaFgrnViewModel
import com.olam.warehouse.vegax.processingghana.ui.rmin.VegaGhanaRminViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.loadKoinModules
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit

fun injectGhanaProcessingFeature() = loadFeature

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
    factory { VegaGhanaProcessingUseCase(get()) }
    viewModel { VegaGhanaRminViewModel(get(), get()) }
    viewModel { VegaGhanaFgrnViewModel(get(), get()) }
}

val networkModule: Module = module {
    factory { (get(named(Constants.BASE)) as Retrofit).create(VegaGhanaProcessingApi::class.java) }
}

val repositoryModule: Module = module {
    factory<VegaGhanaProcessingRepository> { VegaGhanaProcessingRepoImpl(get(), get(), get(), get()) }
}
