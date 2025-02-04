package com.olam.warehouse.vegax.ppqsesame.di

import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.vegax.ppqsesame.data.api.VegaSesamePpqApi
import com.olam.warehouse.vegax.ppqsesame.data.domain.usecase.VegaSesamePpqUsecase
import com.olam.warehouse.vegax.ppqsesame.data.repo.VegaSesamePpqRepository
import com.olam.warehouse.vegax.ppqsesame.data.repo.VegaSesamePpqRepositoryImpl
import com.olam.warehouse.vegax.ppqsesame.ui.VegaSesamePpqViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.loadKoinModules
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit


/**
 * Created by Baskaran Kannan on 8/7/2020.
 */

fun injectSesamePpqFeature() = loadFeature

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
    factory { VegaSesamePpqUsecase(get()) }
    viewModel { VegaSesamePpqViewModel(get(), get()) }
}

val networkModule: Module = module {
    factory { (get(named(Constants.BASE)) as Retrofit).create(VegaSesamePpqApi::class.java) }
}

val repositoryModule: Module = module {
    factory<VegaSesamePpqRepository> { VegaSesamePpqRepositoryImpl(get(), get(), get(), get()) }
}
