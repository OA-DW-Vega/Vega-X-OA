package com.olam.warehouse.login.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.list.listItemsMultiChoice
import com.google.gson.Gson
import com.olam.warehouse.login.R
import com.olam.warehouse.login.databinding.ConfigActivityBinding
import com.olam.warehouse.login.databinding.ItemReceptionBinding
import com.olam.warehouse.login.ui.home.HomeActivity
import com.olam.warehouse.login.vm.LoginViewModel
import com.olam.warehouse.master.ui.BaseActivity
import com.olam.warehouse.master.user.entity.UserRole
import com.olam.warehouse.master.user.model.Reception
import com.olam.warehouse.navigation.SplitInstall
import com.olam.warehouse.presentation.adapter.setUp
import com.olam.warehouse.presentation.enums.UserRoles
import com.olam.warehouse.presentation.enums.UserRoles.Companion.valueOfEnum
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.PreferenceHelper
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.extension.toast
import com.olam.warehouse.presentation.utils.fromJson
import kotlinx.android.synthetic.main.item_reception.view.*
import org.koin.androidx.viewmodel.ext.android.viewModel

class ConfigActivity : BaseActivity() {

    private var conapny_code: String? = ""
    var companyCodes = listOf<String>()
    var keys = listOf<String>()
    var selectedKeys = mutableListOf<String>()
    val receptionItems = mutableListOf<Reception>()

    private lateinit var binding: ConfigActivityBinding
    private lateinit var bindingItem: ItemReceptionBinding

    private val vm: LoginViewModel by viewModel()
    override val layoutResourceId = R.layout.config_activity

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ConfigActivityBinding.inflate(layoutInflater)
        bindingItem = ItemReceptionBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initUI()
    }

    private fun initUI() {
        supportActionBar?.hide()
        binding.btnSubmit.setOnClickListener { moveToHomeActivity() }

        // Company code Spinner
        companyCodes = Gson().fromJson<List<String>>(PreferenceHelper.get(Constants.COMPANY_CODE, ""))
        val recTypeAdapter =
            ArrayAdapter(binding.spCompanyCode.context, android.R.layout.simple_list_item_1, companyCodes)
        binding.spCompanyCode.adapter = recTypeAdapter
        binding.spCompanyCode.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(p0: AdapterView<*>?) {}
                override fun onItemSelected(p0: AdapterView<*>?, p1: View?, pos: Int, p3: Long) {
                    conapny_code = companyCodes[pos]
                    updateAdapter()
                }
            }

        // Reception Type RecyclerView
        //updateAdapter()
    }

    private fun updateAdapter() {
        receptionItems.clear()
        val items = Gson().fromJson<List<String>>(PreferenceHelper.get(Constants.RECEPTION_TYPES, ""))
        val products = Gson().fromJson<List<String>>(PreferenceHelper.get(Constants.PRODUCTS, ""))
        keys = Gson().fromJson<List<String>>(PreferenceHelper.get(Constants.KEYS, ""))
        items.forEach {
            val reception = Reception()
            val productsList = arrayListOf<String>()
            keys.forEach { it1 ->
                if (it1.contains(it.plus("_").plus(conapny_code))) {
                    productsList.add(it1.split("_")[2])
                }
            }
            reception.products = productsList
            reception.receptionName = it
            if (productsList.size > 0) receptionItems.add(reception)
        }
        binding.rvReceptionType.setUp(receptionItems, R.layout.item_reception, { it, pos ->
            tvReceptionItem.text = it.receptionName
            when (it.selectedProducts.size) {
                0 -> tvProductItem.text = if (it.products?.size!! == 1) it.products!![0] else it.product
                else -> tvProductItem.text = it.selectedProducts.toString().replace("[", "").replace("]", "")
            }
            cbReception?.isChecked = it.isChecked!!
            cbReception.setOnCheckedChangeListener { _, isChecked ->
                receptionItems[pos].selectedProducts.clear()
                if (isChecked) {
                    if (it.products?.size == 1) {
                        it.isChecked = true
                        it.selectedProducts.add(it.product.toString())
                        //binding.rvReceptionType.adapter?.notifyDataSetChanged()
                    } else {
                        MaterialDialog(context).show {
                            listItemsMultiChoice(items = it.products) { dialog, index, items ->
                                items.forEach { text ->
                                    keys.forEach { it1 ->
                                        if (it1.contains(
                                                it.receptionName?.plus("_").plus(conapny_code).plus(
                                                    "_".plus(
                                                        text.toString()
                                                    )
                                                )
                                            )
                                        ) {
                                            it.isChecked = true
                                            it.product = text.toString()
                                            it.selectedProducts.add(text.toString())
                                        }
                                    }
                                }
                                if (!it.isChecked!!) {
                                    toast(context.getString(R.string.feature_not_avail))
                                    it.isChecked = false
                                    it.product = ""
                                }
                            }
                            positiveButton(text = UIUtils.getSpannedText(getString(R.string.ok), true)) {
                                binding.rvReceptionType.adapter?.notifyDataSetChanged()
                            }

                        }
                    }
                } else {
                    it.isChecked = false
                    it.product = ""
                }
            }
        })
    }

    private fun moveToHomeActivity() {
        selectedKeys.clear()
        receptionItems.forEach {
            if (it.isChecked!!) {
                it.selectedProducts.forEach { product ->
                    keys.forEach { key ->
                        if (key.contains(it.receptionName.plus("_").plus(conapny_code).plus("_").plus(product)))
                            selectedKeys.add(key)
                    }
                }
            }
        }
        if (selectedKeys.size.equals(0)) {
            toast(getString(R.string.feature_not_avail))
            return
        }
        val gson = Gson()
        PreferenceHelper.save(Constants.SELECTED_KEYS, gson.toJson(selectedKeys))
        selectedKeys.forEachIndexed { index, it ->
            if (it.split("_")[0].contains("DO")) {
                val list = mutableListOf<String>()
                val roleData = Gson().fromJson<List<UserRole>>(PreferenceHelper.get(Constants.USER_ROLES, ""))
                    .filter { key -> key.roleKey.equals(it) }
                roleData.forEach { rol -> list.add(rol.roleName) }
                list.forEach {
                    when (valueOfEnum(it.replace(" ", "_"))) {
                        UserRoles.SUPPLIER -> SplitInstall.download(this, "doreceiving", dolistener)
                        UserRoles.QUALITY -> SplitInstall.download(this, "odquality", dolistener)
                    }
                }
            } else if (it.split("_")[0].contains("VEGA")) {
                val list = mutableListOf<String>()
                val roleData = Gson().fromJson<List<UserRole>>(PreferenceHelper.get(Constants.USER_ROLES, ""))
                    .filter { key -> key.roleKey.equals(it) }
                roleData.forEach { rol -> list.add(rol.roleName) }
                SplitInstall.download(this, "vegaoffline", vegalistener)
                list.forEach { it1 ->
                    when (valueOfEnum(it1)) {
                        UserRoles.SUPPLIER -> SplitInstall.download(this, "receiving", vegalistener)
                        UserRoles.QUALITY -> SplitInstall.download(this, "quality", vegalistener)
                        UserRoles.OFFLOADING -> SplitInstall.download(this, "offloading", vegalistener)
                        UserRoles.APPROVE -> SplitInstall.download(this, "approve", vegalistener)
                        UserRoles.MTNT -> {
                            when {
                                it.split("_")[2].contains("CASH") -> SplitInstall.download(
                                    this,
                                    "dispatch",
                                    vegalistener
                                )
                                it.split("_")[2].contains("COCO") -> SplitInstall.download(
                                    this,
                                    "mtntcocoa",
                                    vegalistener
                                )
                            }

                        }
                        UserRoles.SALES -> {
                            when {
                                it.split("_")[2].contains("COCO") -> SplitInstall.download(
                                    this,
                                    "salescocoa",
                                    vegalistener
                                )
                            }

                        }
                        UserRoles.PROCESSING -> {
                            when {
                                it.split("_")[2].contains("CASH") -> SplitInstall.download(
                                    this,
                                    "processing",
                                    vegalistener
                                )
                                it.split("_")[2].contains("COCO") -> SplitInstall.download(
                                    this,
                                    "processingcocoa",
                                    vegalistener
                                )
                                it.split("_")[2].contains("COFF") -> SplitInstall.download(
                                    this,
                                    "processingcoffee",
                                    vegalistener
                                )
                            }
                        }
                        UserRoles.GATEENTRY -> SplitInstall.download(
                            this,
                            "gateentry",
                            vegalistener
                        )
                        UserRoles.GATEENTRYAPPROVAL -> SplitInstall.download(
                            this,
                            "gateentryapproval",
                            vegalistener
                        )
                        UserRoles.INVENTORY -> {
                            when {
                                it.split("_")[2].contains("CASH") -> SplitInstall.download(
                                    this,
                                    "inventory",
                                    vegalistener
                                )
                                it.split("_")[2].contains("COCO") -> SplitInstall.download(
                                    this,
                                    "inventorycocoa",
                                    vegalistener
                                )
                                it.split("_")[2].contains("COFF") -> SplitInstall.download(
                                    this,
                                    "inventorycoffee",
                                    vegalistener
                                )
                            }

                        }
                        UserRoles.SWEEPINGS -> SplitInstall.download(this, "sweepingcocoa", vegalistener)
                        UserRoles.PCH -> SplitInstall.download(this, "pch", vegalistener)
                        UserRoles.GRN -> {
                            when {
                                it.split("_")[1].contains("EC") -> SplitInstall.download(
                                    this,
                                    "grnecuador",
                                    vegalistener
                                )
                            }
                        }
                        UserRoles.INVOICE -> SplitInstall.download(this, "invoicenicaragua", vegalistener)
                    }
                }
            }

            if (index.equals(selectedKeys.size - 1)) {
                //injectDOFeature()
                PreferenceHelper.save(Constants.CURRENT_KEY, "")
                startActivity(Intent(this, HomeActivity::class.java))
                finish()
            }
        }

    }

    private val dpivclistener: () -> Unit = {}

    private val dolistener: () -> Unit = {}

    private val vegalistener: () -> Unit = { }

}
