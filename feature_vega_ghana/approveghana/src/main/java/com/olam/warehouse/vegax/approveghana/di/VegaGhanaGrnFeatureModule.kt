package com.olam.warehouse.vegax.approveghana.di

import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.vegax.approveghana.data.api.VegaGhanaGrnApi
import com.olam.warehouse.vegax.approveghana.data.domain.usecase.VegaGhanaGrnUseCase
import com.olam.warehouse.vegax.approveghana.data.repo.VegaGhanaGrnRepository
import com.olam.warehouse.vegax.approveghana.data.repo.VegaGhanaGrnRepositoryImpl
import com.olam.warehouse.vegax.approveghana.ui.VegaGhanaGrnViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.loadKoinModules
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit

/**
 * Created by Keerthi Santhanam on 6/29/2020.
 */
fun injectGhanaGrnFeature() = loadFeature

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
    factory { VegaGhanaGrnUseCase(get()) }
    viewModel { VegaGhanaGrnViewModel(get(), get()) }
}

val networkModule: Module = module {
    factory { (get(named(Constants.BASE)) as Retrofit).create(VegaGhanaGrnApi::class.java) }
}

val repositoryModule: Module = module {
    factory<VegaGhanaGrnRepository> { VegaGhanaGrnRepositoryImpl(get(), get()) }
}



