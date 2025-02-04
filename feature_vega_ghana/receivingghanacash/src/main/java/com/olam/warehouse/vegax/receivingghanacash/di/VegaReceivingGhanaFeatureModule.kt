package com.olam.warehouse.vegax.receivingghanacash.di

import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.vegax.receivingghanacash.data.api.VegaReceivingGhanaApi
import com.olam.warehouse.vegax.receivingghanacash.data.api.VegaReceivingGhanaMtntApi
import com.olam.warehouse.vegax.receivingghanacash.data.domain.usecase.VegaReceivingGhanaMtntUseCase
import com.olam.warehouse.vegax.receivingghanacash.data.domain.usecase.VegaReceivingGhanaReceivingUseCase
import com.olam.warehouse.vegax.receivingghanacash.data.repo.VegaReceivingGhanaMtntRepository
import com.olam.warehouse.vegax.receivingghanacash.data.repo.VegaReceivingGhanaMtntRepositoryImpl
import com.olam.warehouse.vegax.receivingghanacash.data.repo.VegaReceivingGhanaRepository
import com.olam.warehouse.vegax.receivingghanacash.data.repo.VegaReceivingGhanaRepositoryImpl
import com.olam.warehouse.vegax.receivingghanacash.ui.VegaReceivingGhanaMtntViewModel
import com.olam.warehouse.vegax.receivingghanacash.ui.VegaReceivingGhanaViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.loadKoinModules
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit

/**
 * Created by Baskaran Kannan on 1/28/2020.
 */

fun injectVegaReceivingGhanaFeature() = loadFeature

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
    factory { VegaReceivingGhanaReceivingUseCase(get()) }
    factory { VegaReceivingGhanaMtntUseCase(get()) }
    viewModel { VegaReceivingGhanaViewModel(get(), get()) }
    viewModel { VegaReceivingGhanaMtntViewModel(get(), get()) }
}

val repositoryModule = module {
    factory { VegaReceivingGhanaRepositoryImpl(get(), get(), get()) as VegaReceivingGhanaRepository }
    factory { VegaReceivingGhanaMtntRepositoryImpl(get(), get()) as VegaReceivingGhanaMtntRepository }
}

val networkModule: Module = module {
    factory { (get(named(Constants.BASE)) as Retrofit).create(VegaReceivingGhanaApi::class.java) }
    factory { (get(named(Constants.BASE)) as Retrofit).create(VegaReceivingGhanaMtntApi::class.java) }

}
