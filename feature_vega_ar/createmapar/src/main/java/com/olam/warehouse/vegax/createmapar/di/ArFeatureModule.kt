package com.olam.warehouse.vegax.createmapar.di

import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.vegax.createmapar.data.api.ArApi
import com.olam.warehouse.vegax.createmapar.data.domain.usecase.ArUsecase
import com.olam.warehouse.vegax.createmapar.data.repo.ArRepository
import com.olam.warehouse.vegax.createmapar.data.repo.ArRepositoryImpl
import com.olam.warehouse.vegax.createmapar.ui.ArViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.loadKoinModules
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit

/**
 * Created by Baskaran Kannan on 6/15/2021.
 */
fun injectArFeature() = loadFeature

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
    factory { ArUsecase(get()) }
    viewModel { ArViewModel(get(), get()) }
}

val networkModule: Module = module {
    factory { (get(named(Constants.BASE)) as Retrofit).create(ArApi::class.java) }
}

val repositoryModule: Module = module {
    factory<ArRepository> { ArRepositoryImpl(get()) }
}
