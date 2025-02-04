package com.olam.warehouse.login.ui.common

import android.os.Bundle
import com.google.gson.Gson
import com.olam.warehouse.login.R
import com.olam.warehouse.login.databinding.ActivityCocoaDispatchTypeBinding
import com.olam.warehouse.login.ui.home.HomeBaseActivity
import com.olam.warehouse.master.common.utils.getCurrentKey
import com.olam.warehouse.master.user.entity.UserRole
import com.olam.warehouse.navigation.features.*
import com.olam.warehouse.presentation.enums.UserRoles
import com.olam.warehouse.presentation.enums.UserRoles.Companion.valueOfEnum
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.PreferenceHelper
import com.olam.warehouse.presentation.utils.extension.gone
import com.olam.warehouse.presentation.utils.extension.visible
import com.olam.warehouse.presentation.utils.fromJson

/**
 * Created by Baskaran Kannan on 5/19/2020.
 */
class VegaCocoaDispatchTypeActivity : HomeBaseActivity() {

    var isMtnt = false
    var isSales = false
    var isExportSales = false
    var isThirdPartySales = false
    var isVirtual = false
    private var isThirdPartyOwnership: Boolean = false
    private var isThirdPartyTP: Boolean = false
    private var isThirdPartyOlam: Boolean = false
    override val layoutResourceId = R.layout.activity_cocoa_dispatch_type
    private lateinit var binding: ActivityCocoaDispatchTypeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val roleData = Gson().fromJson<List<UserRole>>(PreferenceHelper.get(Constants.USER_ROLES, ""))
            .filter { key -> key.roleKey.equals(PreferenceHelper.get(Constants.CURRENT_KEY, "")) }
        roleData.forEach { rol ->
            when (valueOfEnum(rol.roleName.trim())) {
                UserRoles.VIRTUAL_MTNT -> isVirtual = true
            }
        }

        /*if (isVirtual) {
            moveToMtnt()
            finish()
        }*/
        binding = ActivityCocoaDispatchTypeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initNavigationView()
        initUI()
    }

    private fun initUI() {
        binding.llMtnt.setOnClickListener { moveToMtnt() }
        binding.llSales.setOnClickListener { moveToSales() }
        binding.llExportSales.setOnClickListener { moveToExportSales() }
        binding.llThirdSales.setOnClickListener { moveToThirdSales() }

        val roleData = Gson().fromJson<List<UserRole>>(PreferenceHelper.get(Constants.USER_ROLES, ""))
            .filter { key -> key.roleKey.equals(PreferenceHelper.get(Constants.CURRENT_KEY, "")) }
        roleData.forEach { rol ->
            when (valueOfEnum(rol.roleName.trim())) {
                UserRoles.MTNT -> isMtnt = true
                UserRoles.SALES -> isSales = true
                UserRoles.EXPORTSALES -> isExportSales = true
                UserRoles.THIRDPARTY -> isThirdPartySales = true
                UserRoles.THIRDPARTY_OWNERSHIP -> isThirdPartyOwnership = true
                UserRoles.THIRDPARTY_TP_TO_TP -> isThirdPartyTP = true
                UserRoles.THIRDPARTY_TP_OLAM -> isThirdPartyOlam = true
            }
        }
        if (getCurrentKey().split("_")[2].contains("COCO"))
            if (getCurrentKey().split("_")[1].contains("GH")) {
                binding.tvDispatch.text = getString(R.string.select_mtnt)
            }
        if (isMtnt || isVirtual) binding.llMtnt.visible() else binding.llMtnt.gone()
        if (isSales && !isVirtual) binding.llSales.visible() else binding.llSales.gone()
        if (isExportSales && !isVirtual) binding.llExportSales.visible() else binding.llExportSales.gone()
        if (getCurrentKey().split("_")[1].contains("NI")) {
            if (isThirdPartyOlam || isThirdPartyTP) binding.llThirdSales.visible() else binding.llThirdSales.gone()
        } else {
            if (isVirtual && (isThirdPartyOlam || isThirdPartyOwnership || isThirdPartyTP)) binding.llThirdSales.visible() else binding.llThirdSales.gone()
        }
    }

    private fun moveToSales() {

        if (getCurrentKey().split("_")[2].contains("COCO")) {
            if (getCurrentKey().split("_")[1].contains("CM")) {
                VegaDispatchSalesCameroonCocoNavigation.dynamicStart?.let { startActivity(it) }
            } else if (getCurrentKey().split("_")[1].contains("NG")) {
                VegaDispatchNigeriaLocalSalesNavigation.dynamicStart?.let { startActivity(it) }
            } else {
                VegaDispatchSalesCocoNavigation.dynamicStart?.let { startActivity(it) }
            }
        } else if (getCurrentKey().split("_")[2].contains("COFF")) {
            if (getCurrentKey().split("_")[1].contains("NI")) {
                VegaDispatchNicaraguaNavigation.dynamicStart?.let { startActivity(it) }
            }
        } else {
            VegaSalesCoffeeNavigation.dynamicStart?.let { startActivity(it) }
        }


        /*else if (getCurrentKey().split("_")[2].contains("ARAB")) {
            if (getCurrentKey().split("_")[1].contains("ID")) {
                //VegaIndoCoffeeSalesCoffeeNavigation.dynamicStart?.let { startActivity(it) }
            }
        }*/
    }

    private fun moveToMtnt() {
        if (getCurrentKey().split("_")[2].contains("COCO"))
            if (getCurrentKey().split("_")[1].contains("CM")) {
                VegaDispatchMtntCameroonNavigation.dynamicStart?.let { startActivity(it) }
            } else if (getCurrentKey().split("_")[1].contains("GH")) {
                VegaGhanaCocoaDispatchMtntNavigation.dynamicStart?.let { startActivity(it) }
            } else if (getCurrentKey().split("_")[1].contains("NG")) {
                VegaNigeriaDispatchNavigation.dynamicStart?.let { startActivity(it) }
            } else {
                VegaDispatchCocoNavigation.dynamicStart?.let {
                    startActivity(
                        it.putExtra(
                            "isVirtual",
                            isVirtual
                        )
                    )
                }
            }
        else if (getCurrentKey().split("_")[2].contains("COFF")) {
            if (getCurrentKey().split("_")[1].contains("NI")) {
                VegaNicaraguaMtntNavigation.dynamicStart?.let { startActivity(it) }
            } else

                VegaDispatchMtntCoffeeNavigation.dynamicStart?.let { startActivity(it) }
        } else if (getCurrentKey().split("_")[2].contains("SESA"))
            VegaDispatchMtntSesameNavigation.dynamicStart?.let { startActivity(it) }
        else if (getCurrentKey().split("_")[1].contains("NG") && getCurrentKey().split("_")[2].contains(
                "CASH"
            )
        )
            VegaDispatchMtntSesameNavigation.dynamicStart?.let { startActivity(it) }
        else if (getCurrentKey().split("_")[2].contains("CASH")) {
            if (getCurrentKey().split("_")[1].contains("GH")) {
                VegaDispatchMtntGhanaNavigation.dynamicStart?.let { startActivity(it) }
            }
        } else if (getCurrentKey().split("_")[2].contains("ARAB")) {
            if (getCurrentKey().split("_")[1].contains("ID")) {
                VegaIndoCoffeeMtntNavigation.dynamicStart?.let { startActivity(it) }
            }
        }

    }

    private fun moveToExportSales() {
        if (getCurrentKey().split("_")[2].contains("COFF"))
            if (getCurrentKey().split("_")[1].contains("NI")) {
                VegaNicaraguaNavigationExportSales.dynamicStart?.let { startActivity(it) }
            } else {
                VegaExportSalesCoffeeNavigation.dynamicStart?.let { startActivity(it) }
            }
        else if (getCurrentKey().split("_")[2].contains("ARAB")) {
            if (getCurrentKey().split("_")[1].contains("ID")) {
                VegaIndoCoffeeSalesCoffeeNavigation.dynamicStart?.let { startActivity(it) }
            }
        } else if (getCurrentKey().split("_")[2].contains("COCO"))
            if (getCurrentKey().split("_")[1].contains("CM")) {
                VegaExportSalesCameroonNavigation.dynamicStart?.let { startActivity(it) }
            } else if (getCurrentKey().split("_")[1].contains("NG")) {
                VegaNigeriaCocoaExportSalesCoffeeNavigation.dynamicStart?.let { startActivity(it) }
            }
    }

    private fun moveToThirdSales() {

        if (getCurrentKey().split("_")[2].contains("COCO"))
            VegaCocoaThirdPartySalesNavigation.dynamicStart?.let { startActivity(it) }
        else if (getCurrentKey().split("_")[2].contains("COFF"))
            VegaThirdPartySalesCoffeeNavigation.dynamicStart?.let { startActivity(it) }
    }
}
