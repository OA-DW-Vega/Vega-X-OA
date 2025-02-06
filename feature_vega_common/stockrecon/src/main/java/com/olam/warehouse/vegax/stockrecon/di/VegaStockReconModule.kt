package com.olam.warehouse.vegax.stockrecon.di

import com.olam.warehouse.master.VegaDatabase
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.vegax.stockrecon.data.api.VegaStockReconApi
import com.olam.warehouse.vegax.stockrecon.data.domian.usecase.VegaStockReconUseCase
import com.olam.warehouse.vegax.stockrecon.data.repo.VegaStockReconRepo
import com.olam.warehouse.vegax.stockrecon.data.repo.VegaStockReconRepoImpl
import com.olam.warehouse.vegax.stockrecon.vm.VegaStockReconViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.loadKoinModules
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit

fun injectStockReconFeature() = loadFeature

private val loadFeature by lazy {
    loadKoinModules(
        listOf(
            viewModelModule,
            networkModule,
            repositoryModule,
            loadDAOModules
        )
    )
}

val viewModelModule: Module = module {
    factory { VegaStockReconUseCase(get()) }
    viewModel { VegaStockReconViewModel(get(), get()) }
}

val networkModule: Module = module {
    factory { (get(named(Constants.BASE)) as Retrofit).create(VegaStockReconApi::class.java) }
}

val repositoryModule: Module = module {
    factory<VegaStockReconRepo> { VegaStockReconRepoImpl(get(), get()) }
}

val loadDAOModules: Module = module {
    factory { (get(named(Constants.VEGADATABASE)) as VegaDatabase).vegaStockReconDao() }
}
