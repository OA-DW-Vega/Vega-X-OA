package com.olam.warehouse.login.di

import com.olam.warehouse.login.data.api.UsersApi
import com.olam.warehouse.login.data.domain.usecase.UserUseCase
import com.olam.warehouse.login.data.repository.UserRepository
import com.olam.warehouse.login.data.repository.UserRepositoryImpl
import com.olam.warehouse.login.vm.LoginViewModel
import com.olam.warehouse.master.AppDatabase
import com.olam.warehouse.master.common.data.api.AppCenterApi
import com.olam.warehouse.master.common.data.api.AppCenterUrlApi
import com.olam.warehouse.presentation.data.api.AuthApi
import com.olam.warehouse.presentation.data.api.DoAuthApi
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.Constants.DATABASE
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.loadKoinModules
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit

fun injectFeature() = loadFeature

private val loadFeature by lazy {
    loadKoinModules(
        listOf(
            viewModelModule,
            networkModule,
            repositoryModule,
            localModule
        )
    )
}

val viewModelModule: Module = module {
    factory { UserUseCase(get()) }
    viewModel { LoginViewModel(get(), get()) }
}

val networkModule: Module = module {
    factory { (get(named(Constants.KEYCLOAK_URL)) as Retrofit).create(AuthApi::class.java) }
    factory { (get(named(Constants.BASE)) as Retrofit).create(UsersApi::class.java) }
    factory { (get(named(Constants.BASE_DO)) as Retrofit).create(DoAuthApi::class.java) }
    factory { (get(named(Constants.APPCENTER)) as Retrofit).create(AppCenterApi::class.java) }
    factory { (get(named(Constants.APPCENTER_URL)) as Retrofit).create(AppCenterUrlApi::class.java) }
}

val repositoryModule: Module = module {
    factory<UserRepository> { UserRepositoryImpl(get(), get(), get()) }
}

val localModule = module {
    single(named(DATABASE)) { AppDatabase.buildDatabase(androidContext()) }
    factory { (get(named(DATABASE)) as AppDatabase).userDao() }
}
