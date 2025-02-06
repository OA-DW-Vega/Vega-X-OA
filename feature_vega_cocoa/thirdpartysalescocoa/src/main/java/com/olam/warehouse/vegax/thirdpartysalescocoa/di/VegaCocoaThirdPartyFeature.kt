package com.olam.warehouse.vegax.thirdpartysalescoffee.di

import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.vegax.thirdpartysalescoffee.data.api.VegaCocoaThirdPartyApi
import com.olam.warehouse.vegax.thirdpartysalescoffee.data.domain.usecase.VegaCocoaThirdPartyUseCase
import com.olam.warehouse.vegax.thirdpartysalescoffee.data.repo.VegaCocoaThirdPartyRepository
import com.olam.warehouse.vegax.thirdpartysalescoffee.data.repo.VegaCocoaThirdPartyRepositoryImpl
import com.olam.warehouse.vegax.thirdpartysalescoffee.ui.VegaCocoaThirdPartyViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.loadKoinModules
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit

fun injectCocoaThirdPartyFeature() = loadFeature

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
    factory { VegaCocoaThirdPartyUseCase(get()) }
    viewModel { VegaCocoaThirdPartyViewModel(get(), get()) }
}

val networkModule: Module = module {
    factory { (get(named(Constants.BASE)) as Retrofit).create(VegaCocoaThirdPartyApi::class.java) }
}

val repositoryModule: Module = module {
    factory<VegaCocoaThirdPartyRepository> { VegaCocoaThirdPartyRepositoryImpl(get(), get(), get()) }
}
