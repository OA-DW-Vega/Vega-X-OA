package com.olam.warehouse.vegax.thirdpartysalescoffee.di

import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.vegax.thirdpartysalescoffee.data.api.VegaCoffeeThirdPartyApi
import com.olam.warehouse.vegax.thirdpartysalescoffee.data.domain.usecase.VegaCoffeeThirdPartyUseCase
import com.olam.warehouse.vegax.thirdpartysalescoffee.data.repo.VegaCoffeeThirdPartyRepository
import com.olam.warehouse.vegax.thirdpartysalescoffee.data.repo.VegaCoffeeThirdPartyRepositoryImpl
import com.olam.warehouse.vegax.thirdpartysalescoffee.ui.VegaCoffeeThirdPartyViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.loadKoinModules
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit

fun injectCoffeeThirdPartyFeature() = loadFeature

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
    factory { VegaCoffeeThirdPartyUseCase(get()) }
    viewModel { VegaCoffeeThirdPartyViewModel(get(), get()) }
}

val networkModule: Module = module {
    factory { (get(named(Constants.BASE)) as Retrofit).create(VegaCoffeeThirdPartyApi::class.java) }
}

val repositoryModule: Module = module {
    factory<VegaCoffeeThirdPartyRepository> {
        VegaCoffeeThirdPartyRepositoryImpl(
            get(),
            get(),
            get(),
            get()
        )
    }
}
