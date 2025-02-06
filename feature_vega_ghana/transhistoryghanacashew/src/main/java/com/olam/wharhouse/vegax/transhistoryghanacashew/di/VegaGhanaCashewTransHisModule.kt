package com.olam.wharhouse.vegax.transhistoryghanacashew.di

import com.olam.warehouse.presentation.utils.Constants
import com.olam.wharhouse.vegax.transhistoryghanacashew.data.api.VegaGhanaCashewTransHisApi
import com.olam.wharhouse.vegax.transhistoryghanacashew.data.domain.usecase.VegaGhanaCashewTransHisUsecase
import com.olam.wharhouse.vegax.transhistoryghanacashew.data.repo.VegaGhanaCashewTransHisRepo
import com.olam.wharhouse.vegax.transhistoryghanacashew.data.repo.VegaHistoryTransactionsGhanaCashewRepositoryImpl
import com.olam.wharhouse.vegax.transhistoryghanacashew.ui.VegaGhanaCashewTransHisViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.loadKoinModules
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit

/**
 * Created by Baskaran Kannan on 10/8/2022.
 */

fun injectGhanaCashewTransHisFeature() = loadFeature

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
    factory { VegaGhanaCashewTransHisUsecase(get()) }
    viewModel { VegaGhanaCashewTransHisViewModel(get(), get()) }
}

val repositoryModule = module {
    factory<VegaGhanaCashewTransHisRepo> { VegaHistoryTransactionsGhanaCashewRepositoryImpl(get(), get()) }
}

val networkModule: Module = module {
    factory { (get(named(Constants.BASE)) as Retrofit).create(VegaGhanaCashewTransHisApi::class.java) }

}
