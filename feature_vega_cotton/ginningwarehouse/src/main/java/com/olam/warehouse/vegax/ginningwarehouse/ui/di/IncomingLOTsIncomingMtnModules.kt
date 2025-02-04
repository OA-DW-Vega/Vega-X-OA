package com.olam.warehouse.vegax.ginningwarehouse.ui.di

import com.olam.warehouse.ginning.ui.incominglots.incomingmtn.GinningIncomingBaleStatusViewModel
import com.olam.warehouse.ginning.ui.incominglots.incomingmtn.GinningIncomingClassficationViewModel
import com.olam.warehouse.ginning.ui.incominglots.incomingmtn.GinningIncomingMtnViewModel
import com.olam.warehouse.ginning.ui.incominglots.incomingmtn.GinningIncomingReviewViewModel
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.vegax.ginningwarehouse.ui.data.api.IncomingMtnApi
import com.olam.warehouse.vegax.ginningwarehouse.ui.data.domain.VegaCottonIncomingLotsIncomingMtnUseCase
import com.olam.warehouse.vegax.ginningwarehouse.ui.data.repo.IncomingMtnRepository
import com.olam.warehouse.vegax.ginningwarehouse.ui.data.repo.VegaGinningIncomingMtnRepositoryImpl
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.loadKoinModules
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit

fun injectGinningIncomingLOTsIncomingMtnFeature() = loadFeature

private val loadFeature by lazy {
    loadKoinModules(
        listOf(
            viewModelModulee,
            networkModulee,
            repositoryModulee
        )
    )
}

val viewModelModulee: Module = module {
    factory { VegaCottonIncomingLotsIncomingMtnUseCase(get()) }
    viewModel { GinningIncomingMtnViewModel(get(), get()) }
    viewModel { GinningIncomingReviewViewModel(get(), get()) }
    viewModel { GinningIncomingClassficationViewModel(get(), get()) }
    viewModel { GinningIncomingBaleStatusViewModel(get(), get()) }
}

val networkModulee: Module = module {
    factory { (get(named(Constants.BASE)) as Retrofit).create(IncomingMtnApi::class.java) }
}

val repositoryModulee: Module = module {
    factory<IncomingMtnRepository> { VegaGinningIncomingMtnRepositoryImpl(get(), get()) }
}
