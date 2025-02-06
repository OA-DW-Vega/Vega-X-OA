package com.olam.warehouse.login.di

import com.olam.warehouse.login.ui.transaction.TransactionViewModel
import com.olam.warehouse.master.common.data.api.GinningTransMasterApi
import com.olam.warehouse.master.common.data.api.MasterApi
import com.olam.warehouse.master.common.data.api.PortTransMasterApi
import com.olam.warehouse.master.common.data.domain.MasterUseCase
import com.olam.warehouse.presentation.data.api.AuthApi
import com.olam.warehouse.presentation.data.api.TruckManageOffflineApi
import com.olam.warehouse.presentation.utils.Constants
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.loadKoinModules
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit

/**
 * Created by Baskaran Kannan on 12/31/2019.
 */
fun injectCommonFeature() = loadFeature

private val loadFeature by lazy {
    loadKoinModules(listOf(viewModelTrans, useCaseMaster, networkCommonModule))
}

val viewModelTrans: Module = module {
    viewModel { TransactionViewModel(get(), get()) }
}

val useCaseMaster: Module = module {
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
val networkCommonModule: Module = module {
    factory { (get(named(Constants.KEYCLOAK_URL)) as Retrofit).create(AuthApi::class.java) }
    factory { (get(named(Constants.BASE)) as Retrofit).create(MasterApi::class.java) }
    factory { (get(named(Constants.BASE)) as Retrofit).create(GinningTransMasterApi::class.java) }
    factory { (get(named(Constants.BASE)) as Retrofit).create(PortTransMasterApi::class.java) }
    factory { (get(named(Constants.TRUCK_MANAGE)) as Retrofit).create(TruckManageOffflineApi::class.java) }
}
