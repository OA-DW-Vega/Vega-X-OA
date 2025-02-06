package com.olam.warehouse.login.ui.notification.navigation

import android.app.Application
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat.startActivity
import com.google.gson.Gson
import com.olam.warehouse.login.di.injectVegaFeature
import com.olam.warehouse.master.VegaDatabase
import com.olam.warehouse.master.common.utils.getCurrentKey
import com.olam.warehouse.master.user.entity.UserRole
import com.olam.warehouse.navigation.features.VegaApproveGhanaNavigation
import com.olam.warehouse.navigation.features.VegaGhanaCashewOffloadingNavigation
import com.olam.warehouse.navigation.features.VegaGhanaCashewQualityNavigation
import com.olam.warehouse.navigation.features.VegaGhanaProcessingNavigation
import com.olam.warehouse.presentation.enums.UserRoles
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.PreferenceHelper
import com.olam.warehouse.presentation.utils.fromJson
import kotlinx.coroutines.*


/**
 * Created by Baskaran Kannan on 12/27/2022.
 */
class WorkFlowNavigation {


    //        .filter { key -> key.roleKey.equals(it) }
    fun navigationProcess(navigationId: String?, transactionId: String?, requireContext: Context) {
        runBlocking {
            withContext(Dispatchers.IO) {
                val roleData =
                    Gson().fromJson<List<UserRole>>(PreferenceHelper.get(Constants.USER_ROLES, ""))
                val naviflowItems = VegaDatabase.getInstance().vegaReceivingDao().getNavigationFlow()
                val navItem = naviflowItems.filter { it.processId.equals(navigationId) }
                val key = getCurrentKey().split("_")
                if (navItem.isNotEmpty()) {
                    when {
                        key[1].contains("GH") && key[2].contains("CASH") -> {
                            when {
                                navItem.get(0).processName?.contains("GRN_Supplier") == true -> {
                                    if (roleData.any { it.roleName.equals(UserRoles.GRN.role) }) {
                                        VegaApproveGhanaNavigation.dynamicStart?.let {
                                            it.putExtra(Constants.TRANSACTIONID, transactionId)
                                            startActivity(requireContext, it, null)
                                        }
                                    }

                                }

                                navItem.get(0).processName?.contains("Offloading_MTNR_WS") == true -> {
                                    if (roleData.any { it.roleName.equals(UserRoles.OFFLOADING.role) }) {
                                        VegaGhanaCashewOffloadingNavigation.dynamicStart?.let {
                                            it.putExtra(Constants.TRANSACTIONID, transactionId)
                                            startActivity(requireContext, it, null)
                                        }
                                    }
                                }

                                navItem.get(0).processName?.contains("Quality_Supplier") == true -> {
                                    if (roleData.any { it.roleName.equals(UserRoles.QUALITY.role) }) {
                                        VegaGhanaCashewQualityNavigation.dynamicStart?.let {
                                            it.putExtra(Constants.TRANSACTIONID, transactionId)
                                            it.putExtra(Constants.WEIGHMENT_TYPE, Constants.SUPPLIER)
                                            startActivity(requireContext, it, null)
                                        }
                                    }
                                }

                                navItem.get(0).processName?.contains("Quality_MTNR") == true -> {
                                    if (roleData.any { it.roleName.equals(UserRoles.QUALITY.role) }) {
                                        VegaGhanaCashewQualityNavigation.dynamicStart?.let {
                                            it.putExtra(Constants.TRANSACTIONID, transactionId)
                                            it.putExtra(Constants.WEIGHMENT_TYPE, Constants.MT_NR)
                                            startActivity(requireContext, it, null)
                                        }
                                    }
                                }

                                navItem.get(0).processName?.contains("RMIN") == true -> {
                                    if (roleData.any { it.roleName.equals(UserRoles.PROCESSING.role) }) {
                                        VegaGhanaProcessingNavigation.dynamicStart?.let {
                                            it.putExtra(Constants.TRANSACTIONID, "RMIN")
                                            startActivity(requireContext, it, null)
                                        }
                                    }
                                }

                                navItem.get(0).processName?.contains("FGRN") == true -> {
                                    if (roleData.any { it.roleName.equals(UserRoles.PROCESSING.role) }) {
                                        VegaGhanaProcessingNavigation.dynamicStart?.let {
                                            it.putExtra(Constants.TRANSACTIONID, "FRGN")
//                                        intent = it
                                            startActivity(requireContext, it, null)
                                        }
                                    }
                                }

                                else -> {}
                            }
                        }
                    }

                }
            }
        }
    }

    fun navigationProcessWithIntent(
        navigationId: String?,
        transactionId: String?,
        requireContext: Context,
        intent1: Intent
    ): Intent? {
        var intent: Intent? = intent1
        try {
            runBlocking {
                withContext(Dispatchers.IO) {
                    val roleData =
                        Gson().fromJson<List<UserRole>>(PreferenceHelper.get(Constants.USER_ROLES, ""))
                    val naviflowItems =
                        VegaDatabase.getInstance().vegaReceivingDao().getNavigationFlow()
                    val navItem = naviflowItems.filter { it.processId.equals(navigationId) }
                    val key = getCurrentKey().split("_")
                    if (navItem.isNotEmpty()) {
                        if (key[1].contains("GH") && key[2].contains("CASH")) {
                            when {
                                navItem.get(0).processName?.contains("GRN_Supplier") == true -> {
                                    if (roleData.any { it.roleName.equals(UserRoles.GRN.role) }) {
                                        VegaApproveGhanaNavigation.dynamicStart?.let {
                                            it.putExtra(Constants.TRANSACTIONID, transactionId)
                                            intent = it
                                            // startActivity(requireContext, it, null)
                                        }
                                    }
                                }

                                navItem.get(0).processName?.contains("Offloading_MTNR_WS") == true -> {
                                    if (roleData.any { it.roleName.equals(UserRoles.OFFLOADING.role) }) {
                                        VegaGhanaCashewOffloadingNavigation.dynamicStart?.let {
                                            it.putExtra(Constants.TRANSACTIONID, transactionId)
                                            intent = it
                                            // startActivity(requireContext, it, null)
                                        }
                                    }
                                }

                                navItem.get(0).processName?.contains("Quality_Supplier") == true -> {
                                    if (roleData.any { it.roleName.equals(UserRoles.QUALITY.role) }) {
                                        VegaGhanaCashewQualityNavigation.dynamicStart?.let {
                                            it.putExtra(Constants.TRANSACTIONID, transactionId)
                                            it.putExtra(Constants.WEIGHMENT_TYPE, Constants.SUPPLIER)
                                            intent = it
                                            // startActivity(requireContext, it, null)
                                        }
                                    }
                                }

                                navItem.get(0).processName?.contains("Quality_MTNR") == true -> {
                                    if (roleData.any { it.roleName.equals(UserRoles.QUALITY.role) }) {
                                        VegaGhanaCashewQualityNavigation.dynamicStart?.let {
                                            it.putExtra(Constants.TRANSACTIONID, transactionId)
                                            it.putExtra(Constants.WEIGHMENT_TYPE, Constants.MT_NR)
                                            intent = it
                                            // startActivity(requireContext, it, null)
                                        }
                                    }
                                }

                                navItem.get(0).processName?.contains("RMIN") == true -> {
                                    if (roleData.any { it.roleName.equals(UserRoles.PROCESSING.role) }) {
                                        VegaGhanaProcessingNavigation.dynamicStart?.let {
                                            it.putExtra(Constants.TRANSACTIONID, "RMIN")
                                            intent = it
                                            // startActivity(requireContext, it, null)
                                        }
                                    }
                                }

                                navItem.get(0).processName?.contains("FGRN") == true -> {
                                    if (roleData.any { it.roleName.equals(UserRoles.PROCESSING.role) }) {
                                        VegaGhanaProcessingNavigation.dynamicStart?.let {
                                            it.putExtra(Constants.TRANSACTIONID, "FRGN")
                                            intent = it
                                            // startActivity(requireContext, it, null)
                                        }
                                    }
                                }

                                else -> {}
                            }
                        }
                    }

                }
            }
        } catch (e: UninitializedPropertyAccessException) {
            return intent
        } catch (e: Exception) {
            return intent
        }
        return intent
    }

}
