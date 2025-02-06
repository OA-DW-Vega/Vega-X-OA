package com.olam.warehouse.vegax.bagissuenigeriacocoa.di

import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.vegax.bagissuenigeriacocoa.data.api.VegaNigeriaCocoaBagIssueApi
import com.olam.warehouse.vegax.bagissuenigeriacocoa.data.domain.usecase.VegaNigeriaCocoaBagIssueUseCase
import com.olam.warehouse.vegax.bagissuenigeriacocoa.data.repo.VegaNigeriaCocoaBagIssueRepository
import com.olam.warehouse.vegax.bagissuenigeriacocoa.data.repo.VegaNigeriaCocoaBagIssueRepositoryImpl
import com.olam.warehouse.vegax.bagissuenigeriacocoa.ui.VegaNigeriaCocoaBagIssueViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.loadKoinModules
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit

/**
 * Created by Keerthi Santhanam on 6/29/2020.
 */
fun injectNigeriaCocoaBagIssueFeature() = loadFeature

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
    factory { VegaNigeriaCocoaBagIssueUseCase(get()) }
    viewModel { VegaNigeriaCocoaBagIssueViewModel(get(), get()) }
}

val networkModule: Module = module {
    factory { (get(named(Constants.BASE)) as Retrofit).create(VegaNigeriaCocoaBagIssueApi::class.java) }
}

val repositoryModule: Module = module {
    factory<VegaNigeriaCocoaBagIssueRepository> { VegaNigeriaCocoaBagIssueRepositoryImpl(get(), get()) }
}
