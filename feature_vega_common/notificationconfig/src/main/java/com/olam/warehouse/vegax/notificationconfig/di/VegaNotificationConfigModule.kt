package com.olam.warehouse.vegax.notificationconfig.di

import com.olam.warehouse.master.VegaDatabase
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.vegax.notificationconfig.data.api.VegaNotificationConfigAPI
import com.olam.warehouse.vegax.notificationconfig.data.domain.usecase.VegaNotificationConfigUseCase
import com.olam.warehouse.vegax.notificationconfig.data.repo.VegaNotificationConfigRepo
import com.olam.warehouse.vegax.notificationconfig.data.repo.VegaNotificationConfigRepoImpl
import com.olam.warehouse.vegax.notificationconfig.vm.VegaNotificationConfigViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.loadKoinModules
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit

fun injectNotificationConfigFeature() = loadFeature

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
    factory { VegaNotificationConfigUseCase(get()) }
    viewModel { VegaNotificationConfigViewModel(get(), get()) }
}

val networkModule: Module = module {
    factory { (get(named(Constants.BASE)) as Retrofit).create(VegaNotificationConfigAPI::class.java) }
}

val repositoryModule: Module = module {
    factory<VegaNotificationConfigRepo> { VegaNotificationConfigRepoImpl(get(), get()) }
}

val loadDAOModules: Module = module {
    factory { (get(named(Constants.VEGADATABASE)) as VegaDatabase).vegaNotificationConfigDao() }
}
