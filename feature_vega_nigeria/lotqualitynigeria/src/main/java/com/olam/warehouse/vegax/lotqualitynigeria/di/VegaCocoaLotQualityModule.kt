package com.olam.warehouse.vegax.lotqualitynigeria.di

import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.vegax.lotqualitynigeria.data.api.VegaCocoaLotQualityApi
import com.olam.warehouse.vegax.lotqualitynigeria.data.domain.usecase.VegaCocoaLotQualityUsecase
import com.olam.warehouse.vegax.lotqualitynigeria.data.repo.VegaCocoaLotQualityRepository
import com.olam.warehouse.vegax.lotqualitynigeria.data.repo.VegaCocoaLotQualityRepositoryImpl
import com.olam.warehouse.vegax.lotqualitynigeria.ui.VegaCocoaLotQualityViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.loadKoinModules
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit

/**
 * Created by Muskan Jain on 20/09/2021.
 */

fun injectCocoaLotQualityFeature() = loadFeature

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
    factory { VegaCocoaLotQualityUsecase(get()) }
    viewModel { VegaCocoaLotQualityViewModel(get(), get()) }
}

val networkModule: Module = module {
    factory { (get(named(Constants.BASE)) as Retrofit).create(VegaCocoaLotQualityApi::class.java) }
}

val repositoryModule: Module = module {
    factory<VegaCocoaLotQualityRepository> {
        VegaCocoaLotQualityRepositoryImpl(
            get(),
            get(),
            get()
        )
    }
}
