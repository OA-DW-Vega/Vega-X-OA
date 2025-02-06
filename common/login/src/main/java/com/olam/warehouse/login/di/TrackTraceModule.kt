package com.olam.warehouse.login.di

import com.olam.warehouse.login.data.api.TrackTraceApi
import com.olam.warehouse.login.data.domain.usecase.TrackTraceUseCase
import com.olam.warehouse.login.data.repository.TrackTraceRepository
import com.olam.warehouse.login.data.repository.TrackTraceRepositoryImpl
import com.olam.warehouse.login.ui.transaction.TransactionViewModel
import com.olam.warehouse.login.vm.TrackTraceViewModel
import com.olam.warehouse.master.VegaDatabase
import com.olam.warehouse.master.common.data.domain.MasterUseCase
import com.olam.warehouse.presentation.utils.Constants
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.loadKoinModules
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit

fun injectTrackTraceFeature() = loadFeature

private val loadFeature by lazy {
    loadKoinModules(
        listOf(
            //viewTrackTraceModelTrans,
            trackTraceViewModelModule,
            trackTraceRepoModule,
            trackTraceNetworkModule/*,
            loadTTDAOModules,
            ttUseCaseMaster*/
        )
    )
}

val viewTrackTraceModelTrans: Module = module {
    viewModel { TransactionViewModel(get(), get()) }
}

val ttUseCaseMaster: Module = module {
    factory {
        MasterUseCase(
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get()
        )
    }
}

val trackTraceViewModelModule: Module = module {
    viewModel { TrackTraceViewModel(get(), get()) }
    factory { TrackTraceUseCase(get()) }
}

val trackTraceRepoModule: Module = module {
    factory<TrackTraceRepository> { TrackTraceRepositoryImpl(get(), get()) }
}

val trackTraceNetworkModule: Module = module {
    factory { (get(named(Constants.BASE)) as Retrofit).create(TrackTraceApi::class.java) }
}



val loadTTDAOModules: Module = module {
    factory { (get(named(Constants.VEGADATABASE)) as VegaDatabase).vegaTrackTraceDao() }
}

