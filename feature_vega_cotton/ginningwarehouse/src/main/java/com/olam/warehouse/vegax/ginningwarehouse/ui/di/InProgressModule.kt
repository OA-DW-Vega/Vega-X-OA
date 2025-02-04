package com.olam.warehouse.ginning.di
import com.olam.warehouse.ginning.ui.ginninginprogress.GinningInprogressViewModel
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.vegax.ginningwarehouse.ui.data.api.GinningInprogreessApi
import com.olam.warehouse.vegax.ginningwarehouse.ui.data.domain.VegaCottonGinningInprogressUseCase
import com.olam.warehouse.vegax.ginningwarehouse.ui.data.repo.GinningInprogressRepository
import com.olam.warehouse.vegax.ginningwarehouse.ui.data.repo.VegaGinningInprogressRepositoryImpl
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.loadKoinModules
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit


fun injectGinningInprogressFeature() = loadFeature

private val loadFeature by lazy {
    loadKoinModules(
        listOf(
            viewModelInProgressModule,
            networkInProgressModule,
            repositoryInProgressModule
        )
    )
}

val viewModelInProgressModule: Module = module {
    factory { VegaCottonGinningInprogressUseCase(get()) }
    viewModel { GinningInprogressViewModel(get(), get()) }
}

val networkInProgressModule: Module = module {
    factory { (get(named(Constants.BASE)) as Retrofit).create(GinningInprogreessApi::class.java) }
}

val repositoryInProgressModule: Module = module {
    factory<GinningInprogressRepository> { VegaGinningInprogressRepositoryImpl(get(), get()) }
}
