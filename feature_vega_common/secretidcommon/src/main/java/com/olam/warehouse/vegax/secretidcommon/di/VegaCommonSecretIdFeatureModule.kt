package com.olam.warehouse.vegax.secretid.di

import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.vegax.secretidcommon.data.api.VegaCommonSecretIdApi
import com.olam.warehouse.vegax.secretidcommon.data.domain.usecase.VegaCommonSecretIdUseCase
import com.olam.warehouse.vegax.secretidcommon.data.repo.VegaCommonSecretIdRepoImpl
import com.olam.warehouse.vegax.secretidcommon.data.repo.VegaCommonSecretIdRepository
import com.olam.warehouse.vegax.secretidcommon.ui.VegaCommonSecretIdViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.loadKoinModules
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit


fun injectVegaCommonSecretIdFeature() = loadFeature

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
    factory { VegaCommonSecretIdUseCase(get()) }
    viewModel { VegaCommonSecretIdViewModel(get(), get()) }
}

val networkModule: Module = module {
    factory { (get(named(Constants.BASE)) as Retrofit).create(VegaCommonSecretIdApi::class.java) }
}

val repositoryModule: Module = module {
    factory<VegaCommonSecretIdRepository> { VegaCommonSecretIdRepoImpl(get(), get(), get()) }
}
