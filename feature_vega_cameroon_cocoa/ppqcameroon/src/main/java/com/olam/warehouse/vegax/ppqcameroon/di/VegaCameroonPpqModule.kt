package com.olam.warehouse.vegax.ppqcameroon.di

import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.vegax.ppqcameroon.data.api.VegaCameroonPpqApi
import com.olam.warehouse.vegax.ppqcameroon.data.domain.usecase.VegaCameroonPpqUsecase
import com.olam.warehouse.vegax.ppqcameroon.data.repo.VegaCameroonPpqRepository
import com.olam.warehouse.vegax.ppqcameroon.data.repo.VegaCameroonPpqRepositoryImpl
import com.olam.warehouse.vegax.ppqcameroon.ui.VegaCameroonPpqViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.loadKoinModules
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit


/**
 * Created by Baskaran Kannan on 8/7/2020.
 */

fun injectCameroonPpqFeature() = loadFeature

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
    factory { VegaCameroonPpqUsecase(get()) }
    viewModel { VegaCameroonPpqViewModel(get(), get()) }
}

val networkModule: Module = module {
    factory { (get(named(Constants.BASE)) as Retrofit).create(VegaCameroonPpqApi::class.java) }
}

val repositoryModule: Module = module {
    factory<VegaCameroonPpqRepository> { VegaCameroonPpqRepositoryImpl(get(), get()) }
}
