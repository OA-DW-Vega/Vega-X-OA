package com.olam.warehouse.odquality.di

import com.olam.warehouse.odquality.data.api.DOQualityApi
import com.olam.warehouse.odquality.data.domain.usecase.DOQualityUseCase
import com.olam.warehouse.odquality.data.repo.DOQualityRepository
import com.olam.warehouse.odquality.data.repo.DOQualityRepositoryImpl
import com.olam.warehouse.odquality.ui.DOQualityViewModel
import com.olam.warehouse.presentation.utils.Constants
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.loadKoinModules
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit

/**
 * Created by Baskaran Kannan on 12/26/2019.
 */
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
    factory { DOQualityUseCase(get()) }
    viewModel { DOQualityViewModel(get(), get()) }
}

val networkModule: Module = module {
    factory { (get(named(Constants.BASE)) as Retrofit).create(DOQualityApi::class.java) }
}

val repositoryModule: Module = module {
    factory<DOQualityRepository> { DOQualityRepositoryImpl(get(), get()) }
}
