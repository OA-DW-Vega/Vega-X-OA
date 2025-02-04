package com.olam.warehouse.vegax.secretidnigeria.di

import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.vegax.secretidnigeria.data.api.VegaNigeriaSecretIdApi
import com.olam.warehouse.vegax.secretidnigeria.data.domain.usecase.VegaNigeriaSecretIdUsecase
import com.olam.warehouse.vegax.secretidnigeria.data.repo.VegaNigeriaSecretIdRepository
import com.olam.warehouse.vegax.secretidnigeria.data.repo.VegaNigeriaSecretIdRepositoryImpl
import com.olam.warehouse.vegax.secretidnigeria.ui.VegaNigeriaSecretIdViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.loadKoinModules
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit


/**
 * Created by Baskaran Kannan on 8/7/2020.
 */

fun injectNigeriaSecretIdFeature() = loadFeature

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
    factory { VegaNigeriaSecretIdUsecase(get()) }
    viewModel { VegaNigeriaSecretIdViewModel(get(), get()) }
}

val networkModule: Module = module {
    factory { (get(named(Constants.BASE)) as Retrofit).create(VegaNigeriaSecretIdApi::class.java) }
}

val repositoryModule: Module = module {
    factory<VegaNigeriaSecretIdRepository> { VegaNigeriaSecretIdRepositoryImpl(get(), get()) }
}
