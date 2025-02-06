package com.olam.wharhouse.vegax.transactionhistory.di

import com.olam.warehouse.presentation.utils.Constants
import com.olam.wharhouse.vegax.transactionhistory.data.api.VegaTransHistoryApi
import com.olam.wharhouse.vegax.transactionhistory.data.domain.usecase.VegaTransactionHistoryUsecase
import com.olam.wharhouse.vegax.transactionhistory.data.repo.VegaHistoryTransactionsRepositoryImpl
import com.olam.wharhouse.vegax.transactionhistory.data.repo.VegaTransHisRepo
import com.olam.wharhouse.vegax.transactionhistory.ui.VegaTransHisViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.loadKoinModules
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit

/**
 * Created by Baskaran Kannan on 10/8/2022.
 */

fun injectTransHisFeature() = loadFeature

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
    factory {
        VegaTransactionHistoryUsecase(
            get()
        )
    }
    viewModel { VegaTransHisViewModel(get(), get()) }
}

val repositoryModule = module {
    factory<VegaTransHisRepo> {
       VegaHistoryTransactionsRepositoryImpl(
            get(),
            get()
        )
    }
}

val networkModule: Module = module {
    factory { (get(named(Constants.BASE)) as Retrofit).create(VegaTransHistoryApi::class.java) }

}
