package com.olam.warehouse.vegax.ginningwarehouse.ui.di


import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.vegax.ginningwarehouse.data.domain.VegaCottonPileUseCase
import com.olam.warehouse.vegax.ginningwarehouse.ui.data.api.GinningPileApi
import com.olam.warehouse.vegax.ginningwarehouse.ui.data.repo.GinningPileRepository
import com.olam.warehouse.vegax.ginningwarehouse.ui.data.repo.VegaGinningPileRepositoryImpl
import com.olam.warehouse.vegax.ginningwarehouse.ui.ui.pile.GinningPileAddBaleViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.loadKoinModules
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit


fun injectGinningPileFeature() = loadFeature

private val loadFeature by lazy {
    loadKoinModules(
        listOf(
            viewModelPileModule,
            networkPileModule,
            repositoryPileModule
        )
    )
}

val viewModelPileModule: Module = module {
    factory { VegaCottonPileUseCase(get()) }
    viewModel { GinningPileAddBaleViewModel(get(), get()) }
}

val networkPileModule: Module = module {
    factory { (get(named(Constants.BASE)) as Retrofit).create(GinningPileApi::class.java) }
}

val repositoryPileModule: Module = module {
    factory<GinningPileRepository> { VegaGinningPileRepositoryImpl(get(), get()) }
}
