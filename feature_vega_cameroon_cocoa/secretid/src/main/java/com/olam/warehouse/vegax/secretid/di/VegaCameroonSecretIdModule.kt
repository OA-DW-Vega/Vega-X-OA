package com.olam.warehouse.vegax.secretid.di

import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.vegax.secretid.data.api.VegaCameroonSecretIdApi
import com.olam.warehouse.vegax.secretid.data.domain.usecase.VegaCameroonSecretIdUsecase
import com.olam.warehouse.vegax.secretid.data.repo.VegaCameroonSecretIdRepository
import com.olam.warehouse.vegax.secretid.data.repo.VegaCameroonSecretIdRepositoryImpl
import com.olam.warehouse.vegax.secretid.ui.VegaCameroonSecretIdViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.loadKoinModules
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit


/**
 * Created by Baskaran Kannan on 8/7/2020.
 */

fun injectCameroonSecretIdFeature() = loadFeature

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
    factory { VegaCameroonSecretIdUsecase(get()) }
    viewModel { VegaCameroonSecretIdViewModel(get(), get()) }
}

val networkModule: Module = module {
    factory { (get(named(Constants.BASE)) as Retrofit).create(VegaCameroonSecretIdApi::class.java) }
}

val repositoryModule: Module = module {
    factory<VegaCameroonSecretIdRepository> { VegaCameroonSecretIdRepositoryImpl(get(), get()) }
}
