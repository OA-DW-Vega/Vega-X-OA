package com.olam.warehouse.vegax.quality.di

import com.olam.warehouse.master.DODatabase
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.vegax.quality.data.api.VegaQualityApi
import com.olam.warehouse.vegax.quality.data.domain.usecase.VegaQualityUseCase
import com.olam.warehouse.vegax.quality.data.repo.VegaQualityRepository
import com.olam.warehouse.vegax.quality.data.repo.VegaQualityRepositoryImpl
import com.olam.warehouse.vegax.quality.ui.VegaQualityViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.loadKoinModules
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit

fun injectQualityFeature() = loadFeature

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
    factory { VegaQualityUseCase(get()) }
    viewModel { VegaQualityViewModel(get(), get()) }
}

val networkModule: Module = module {
    factory { (get(named(Constants.BASE)) as Retrofit).create(VegaQualityApi::class.java) }
}

val repositoryModule: Module = module {
    factory<VegaQualityRepository> { VegaQualityRepositoryImpl(get(), get()) }
}

val localModule = module {
    //single(named(DODATABASE)) { DODatabase.buildDatabase(androidContext()) }
    factory { (get(named(Constants.DODATABASE)) as DODatabase).doQualityDao() }
}
