package com.olam.warehouse.vegax.splitlot.di

import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.vegax.splitlot.data.api.VegaCommonSplitLotApi
import com.olam.warehouse.vegax.splitlot.data.domain.usecase.VegaCommonSplitLotUsecase
import com.olam.warehouse.vegax.splitlot.data.repo.VegaCommonSplitLotRepo
import com.olam.warehouse.vegax.splitlot.data.repo.VegaCommonSplitLotRepoImpl
import com.olam.warehouse.vegax.splitlot.ui.VegaCommonSplitLotViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.loadKoinModules
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit

/**
 * Created by Baskaran Kannan on 9/26/2022.
 */

fun injectCommonSplitLotFeature() = loadFeature

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
    factory { VegaCommonSplitLotUsecase(get()) }
    viewModel { VegaCommonSplitLotViewModel(get(), get()) }
}

val repositoryModule = module {
    factory<VegaCommonSplitLotRepo> { VegaCommonSplitLotRepoImpl(get(), get()) }
}

val networkModule: Module = module {
    factory { (get(named(Constants.BASE)) as Retrofit).create(VegaCommonSplitLotApi::class.java) }
}