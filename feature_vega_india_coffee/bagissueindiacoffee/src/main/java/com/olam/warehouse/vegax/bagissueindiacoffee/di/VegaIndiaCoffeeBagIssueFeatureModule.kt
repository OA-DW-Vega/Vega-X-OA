package com.olam.warehouse.vegax.bagissueindiacoffee.di

import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.vegax.bagissueindiacoffee.data.api.VegaIndiaCoffeeBagIssueApi
import com.olam.warehouse.vegax.bagissueindiacoffee.data.domain.usecase.VegaIndiaCoffeeBagIssueUseCase
import com.olam.warehouse.vegax.bagissueindiacoffee.data.repo.VegaIndiaCoffeeBagIssueRepository
import com.olam.warehouse.vegax.bagissueindiacoffee.data.repo.VegaIndiaCoffeeBagIssueRepositoryImpl
import com.olam.warehouse.vegax.bagissueindiacoffee.ui.VegaIndiaCoffeeBagIssueViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.loadKoinModules
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit

/**
 * Created by Keerthi Santhanam on 6/29/2020.
 */
fun injectIndiaCoffeeBagIssueFeature() = loadFeature

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
    factory { VegaIndiaCoffeeBagIssueUseCase(get()) }
    viewModel { VegaIndiaCoffeeBagIssueViewModel(get(), get()) }
}

val networkModule: Module = module {
    factory { (get(named(Constants.BASE)) as Retrofit).create(VegaIndiaCoffeeBagIssueApi::class.java) }
}

val repositoryModule: Module = module {
    factory<VegaIndiaCoffeeBagIssueRepository> { VegaIndiaCoffeeBagIssueRepositoryImpl(get(), get()) }
}
