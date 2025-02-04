package com.olam.warehouse.odreceiving.di

import com.olam.warehouse.odreceiving.data.api.DOReceivingApi
import com.olam.warehouse.odreceiving.data.domain.usecase.DOReceivingUseCase
import com.olam.warehouse.odreceiving.data.repo.DOReceivingRepository
import com.olam.warehouse.odreceiving.data.repo.DOReceivingRepositoryImpl
import com.olam.warehouse.odreceiving.ui.DOReceivingViewModel
import com.olam.warehouse.presentation.utils.Constants
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.loadKoinModules
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit

/**
 * Created by Baskaran Kannan on 12/26/2019.
 */

fun injectReceivingFeature() = loadFeatureDO

private val loadFeatureDO by lazy {
    loadKoinModules(
        listOf(
            viewModelModule,
            networkModule,
            repositoryModule
        )
    )
}

val viewModelModule: Module = module {
    factory { DOReceivingUseCase(get()) }
    viewModel { DOReceivingViewModel(get(), get()) }
}

val repositoryModule = module {
    factory { DOReceivingRepositoryImpl(get(), get(), get()) as DOReceivingRepository }
}

val networkModule: Module = module {
    factory { (get(named(Constants.BASE)) as Retrofit).create(DOReceivingApi::class.java) }

}

