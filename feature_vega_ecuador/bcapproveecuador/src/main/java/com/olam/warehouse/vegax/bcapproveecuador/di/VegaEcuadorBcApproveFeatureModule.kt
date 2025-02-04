package com.olam.warehouse.vegax.bcapproveecuador.di

import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.vegax.bcapproveecuador.data.api.VegaEcuadorBcApproveOffloadingApi
import com.olam.warehouse.vegax.bcapproveecuador.data.domain.usecase.VegaEcuadorBcApproveUseCase
import com.olam.warehouse.vegax.bcapproveecuador.data.repo.VegaEcuadorBcApproveRepository
import com.olam.warehouse.vegax.bcapproveecuador.data.repo.VegaEcuadorBcApproveRepositoryImpl
import com.olam.warehouse.vegax.bcapproveecuador.ui.VegaEcuadorBcApproveViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.loadKoinModules
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit

/**
 * Created by Keerthi Santhanam on 7/12/2020.
 */
fun injectEcuadorBcApproveFeature() = loadFeature

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
    factory {VegaEcuadorBcApproveUseCase(get()) }
    viewModel { VegaEcuadorBcApproveViewModel(get(), get()) }
}

val networkModule: Module = module {
    factory { (get(named(Constants.BASE)) as Retrofit).create(VegaEcuadorBcApproveOffloadingApi::class.java) }
}

val repositoryModule: Module = module {
    factory<VegaEcuadorBcApproveRepository> { VegaEcuadorBcApproveRepositoryImpl(get(), get(), get()) }
}



