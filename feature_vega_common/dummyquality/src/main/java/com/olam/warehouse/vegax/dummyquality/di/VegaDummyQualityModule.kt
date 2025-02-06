package com.olam.warehouse.vegax.dummyquality.di

import com.olam.warehouse.master.VegaDatabase
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.Constants.VEGADATABASE
import com.olam.warehouse.vegax.dummyquality.data.api.VegaCommonDummyQualityApi
import com.olam.warehouse.vegax.dummyquality.data.domain.usecase.VegaCommonDummyQualityUseCase
import com.olam.warehouse.vegax.dummyquality.data.repo.VegaCommonDummyQualityRepository
import com.olam.warehouse.vegax.dummyquality.data.repo.VegaCommonDummyQualityRepositoryImp
import com.olam.warehouse.vegax.dummyquality.ui.VegaCommonDummyQualityViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.loadKoinModules
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit

/**
 * Created by Ramesh Rm on 9/22/2022.
 */
fun injectDummyQualityFeature() = loadFeature

private val loadFeature by lazy {
    loadKoinModules(
        listOf(
            viewModelModule,
            networkModule,
            repositoryModule,
            localDaoModule
        )
    )
}
val localDaoModule : Module = module {
    factory {  (get(named(VEGADATABASE)) as VegaDatabase).vegaDummyQualityDao()}
}
val viewModelModule: Module = module {
    factory { VegaCommonDummyQualityUseCase(get()) }
    viewModel { VegaCommonDummyQualityViewModel(get(), get()) }
}


val networkModule: Module = module {
    factory { (get(named(Constants.BASE)) as Retrofit).create(VegaCommonDummyQualityApi::class.java) }
}

val repositoryModule: Module = module {
    factory<VegaCommonDummyQualityRepository> { VegaCommonDummyQualityRepositoryImp(get(), get()) }
}
