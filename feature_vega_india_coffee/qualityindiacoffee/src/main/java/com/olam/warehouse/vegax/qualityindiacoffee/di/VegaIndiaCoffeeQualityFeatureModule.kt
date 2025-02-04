package com.olam.warehouse.vegax.qualityindiacoffee.di

import com.olam.warehouse.master.DODatabase
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.vegax.qualityindiacoffee.data.api.VegaIndiaCoffeeQualityApi
import com.olam.warehouse.vegax.qualityindiacoffee.data.domain.usecase.VegaIndiaCoffeeQualityUseCase
import com.olam.warehouse.vegax.qualityindiacoffee.data.repo.VegaIndiaCoffeeQualityRepository
import com.olam.warehouse.vegax.qualityindiacoffee.data.repo.VegaQualityRepositoryImpl
import com.olam.warehouse.vegax.qualityindiacoffee.ui.VegaIndiaCoffeeQualityViewModel
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
    factory { VegaIndiaCoffeeQualityUseCase(get()) }
    viewModel { VegaIndiaCoffeeQualityViewModel(get(), get()) }
}

val networkModule: Module = module {
    factory { (get(named(Constants.BASE)) as Retrofit).create(VegaIndiaCoffeeQualityApi::class.java) }
}

val repositoryModule: Module = module {
    factory<VegaIndiaCoffeeQualityRepository> { VegaQualityRepositoryImpl(get(), get()) }
}

val localModule = module {
    //single(named(DODATABASE)) { DODatabase.buildDatabase(androidContext()) }
    factory { (get(named(Constants.DODATABASE)) as DODatabase).doQualityDao() }
}
