package com.olam.warehouse.login.di

import com.olam.warehouse.master.common.data.api.MasterApi
import com.olam.warehouse.master.common.data.domain.model.ODMasterUseCase
import com.olam.warehouse.presentation.data.api.AuthApi
import com.olam.warehouse.presentation.utils.Constants
import org.koin.core.context.loadKoinModules
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit

/**
 * Created by Baskaran Kannan on 12/31/2019.
 */
fun injectDOCommonFeature() = loadFeature

private val loadFeature by lazy {
    loadKoinModules(listOf(useCaseMasterDO, networkCommonModuleDO))
}
/*
val viewModelTrans: Module = module {
//    viewModel { TransactionViewModel(get(), get()) }
}*/

val useCaseMasterDO: Module = module {
    factory {
        ODMasterUseCase(
            get(),
            get()
        )
    }
}
val networkCommonModuleDO: Module = module(override = true) {
    factory { (get(named(Constants.KEYCLOAK_URL)) as Retrofit).create(AuthApi::class.java) }
    factory { (get(named(Constants.BASE_OD)) as Retrofit).create(MasterApi::class.java) }
}
