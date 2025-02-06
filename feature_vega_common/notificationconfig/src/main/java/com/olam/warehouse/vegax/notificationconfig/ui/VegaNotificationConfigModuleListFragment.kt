package com.olam.warehouse.vegax.notificationconfig.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import com.afollestad.materialdialogs.MaterialDialog
import com.olam.warehouse.login.ui.succes.SuccessActivity
import com.olam.warehouse.login.utils.showErrorDialogWithFAQLink
import com.olam.warehouse.master.common.utils.getNotifyModuleList
import com.olam.warehouse.master.user.model.NotifyModuleList
import com.olam.warehouse.master.vega.model.VegaStockReconGetAllAuditData
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.AppUtils
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.PreferenceHelper
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.presentation.utils.extension.toast
import com.olam.warehouse.vegax.notificationconfig.R
import com.olam.warehouse.vegax.notificationconfig.data.domain.model.VegaNotifyConfigModule
import com.olam.warehouse.vegax.notificationconfig.data.domain.model.VegaNotifyConfigPostReqResp
import com.olam.warehouse.vegax.notificationconfig.data.domain.model.VegaNotifyConfigUserDetails
import com.olam.warehouse.vegax.notificationconfig.data.domain.model.VegaNotifyConfigUserModuleConfigurationDetails
import com.olam.warehouse.vegax.notificationconfig.data.domain.model.VegaNotifyModuleList
import com.olam.warehouse.vegax.notificationconfig.databinding.FragmentVegaNotifyConfigModuleListBinding
import com.olam.warehouse.vegax.notificationconfig.ui.callback.VegaNotificationConfigCallbackListener
import com.olam.warehouse.vegax.notificationconfig.utils.BUNDLE_DATA
import com.olam.warehouse.vegax.notificationconfig.vm.VegaNotificationConfigViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class VegaNotificationConfigModuleListFragment : BaseFragment() {

    override val layoutResourceId = R.layout.fragment_vega_notify_config_module_list
    private lateinit var binding: FragmentVegaNotifyConfigModuleListBinding
    private val vm: VegaNotificationConfigViewModel by viewModel()
    private var titleList: List<String>? = null
    private var adapter: VegaNotificationConfigModuleListAdapter? = null
    private var callBack: VegaNotificationConfigCallbackListener? = null
    var bundleData = VegaNotifyConfigUserDetails()
    var selectedList = HashMap<String, List<VegaNotifyModuleList>>()
    var aleradySelectedString = HashMap<String, Boolean>()


    /*notify module list is fetched from master, for showing module in expandable list view*/
    var notifyModuleList = arrayListOf<NotifyModuleList>()

    /*already module config list is for set checked the already configured module for the particular user*/
    var userNotifyAlreadyModuleConfigList = arrayListOf<VegaNotifyConfigUserModuleConfigurationDetails>()


    companion object {
        fun newInstance(bundleData: VegaNotifyConfigUserDetails) = VegaNotificationConfigModuleListFragment().putArgs {
            putParcelable(BUNDLE_DATA, bundleData)
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentVegaNotifyConfigModuleListBinding.inflate(layoutInflater)
        initUI()
        return binding.root
    }

    private fun initUI() {
        initExtra()
        observer()
        clickListener()
//        setupExpandableListView()
    }

    private fun clickListener() {
        binding.tvSave.setOnClickListener {
            showSaveNotifyConfigConfirmationDialog()
        }
    }

    private fun initExtra() {
        notifyModuleList = getNotifyModuleList() as ArrayList<NotifyModuleList>
        bundleData = arguments?.getParcelable<VegaNotifyConfigUserDetails>(BUNDLE_DATA) as VegaNotifyConfigUserDetails
//        context?.toast(bundleData.userName)
    }

    private fun observer() {
        vm.getUserAlreadyModuleConfigList(bundleData.userName)
        vm.userAlreadyModuleConfigList.observe(viewLifecycleOwner, Observer { updateUserAlreadyModuleConfigList(it) })

        vm.userNotifyConfigResponse.observe(viewLifecycleOwner, Observer { postUserConfigResponse(it) })
    }

    /*api to fetch the already configured user list for user*/
    private fun updateUserAlreadyModuleConfigList(response: Resource<GenericReqAndResp<List<VegaNotifyConfigUserModuleConfigurationDetails>>>) {
        response.let {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    hideLoading()
                    userNotifyAlreadyModuleConfigList =
                        response.data?.data?.filter { it.notificationFlag == true } as ArrayList<VegaNotifyConfigUserModuleConfigurationDetails>
                    setupExpandableListView()
                }

                Resource.Status.LOADING -> showLoading()
                Resource.Status.ERROR -> {
                    hideLoading()
                    showErrorDialogWithFAQLink(requireContext(), it.error.toString())
                }
            }
        }
    }

    private fun postUserConfigResponse(response: Resource<GenericReqAndResp<VegaNotifyConfigPostReqResp>>) {
        response.let {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    hideLoading()
                    moveToSuccessPage()
                    /*context?.toast("Post success")*/
                }

                Resource.Status.LOADING -> showLoading()
                Resource.Status.ERROR -> {
                    hideLoading()
                    showErrorDialogWithFAQLink(requireContext(), it.error.toString())
                }
            }
        }
    }

    /*this is to collect the expandable parent list*/
    private fun getParentTitleList(): ArrayList<String> {
        var titleList = arrayListOf<String>()
        if (notifyModuleList.isNotEmpty()) {
            var title = notifyModuleList.filter { it.notificationProcessName?.isNotEmpty() == true }
                .map { it.notificationProcessName }
            for (item in title) {
                if (item?.contains(",") == true) {
                    titleList.addAll(item.split(",").map { it.trim() })
                } else {
                    item?.let { titleList.add(it.trim()) }
                }
            }
            titleList.distinct()
        }
        return titleList
    }

    /*this methods is to collect the child list*/
    private fun getExpandableListData(): HashMap<String, List<VegaNotifyModuleList>> {
        var data: HashMap<String, List<VegaNotifyModuleList>> = HashMap<String, List<VegaNotifyModuleList>>()
        var titleList = getParentTitleList().distinct()

        var parentPos = 0
        var childPos = 0
        for (item in titleList) {
            var mModuleList = arrayListOf<VegaNotifyModuleList>()
            parentPos++
            /*sometimes modules are under two or more different section, so we are filtering*/
            var moduleList = notifyModuleList.filter { it.notificationProcessName?.contains(item) == true }
            for(item1 in moduleList){
                var module = VegaNotifyModuleList()
                module.moduleName = item1.moduleName
                module.subModuleName = item
                mModuleList.add(module)
            }
            /*this is to set the already user config item*/
            if (userNotifyAlreadyModuleConfigList.isNotEmpty()) {
                /*this filter is to filter the exact module*/
                var alreadyConfigModuleList =
                    userNotifyAlreadyModuleConfigList.filter { it.subModuleName.contains(item) }
                if (alreadyConfigModuleList.isNotEmpty()) {
                    for (alreadyConfigItem in alreadyConfigModuleList) {
                        for (item in mModuleList) {
                            if (alreadyConfigItem.moduleName.contains(item.moduleName, true)) {
                                var pos = parentPos.toString().plus(childPos.toString())
                                item.isConfigured = true
//                                aleradySelectedString.put(pos, true)
                            }
                        }
                    }
                }
            }
            data[item] = mModuleList
        }
        return data
    }

    private fun setupExpandableListView() {
        val expandableListView = binding.expandableListView
        val listData = getExpandableListData()
        titleList = ArrayList(listData.keys)
        adapter = VegaNotificationConfigModuleListAdapter(requireContext(), titleList as ArrayList<String>, listData)
        expandableListView.setAdapter(adapter)
        expandableListView.setOnGroupExpandListener { groupPosition ->
//            Toast.makeText(
//                requireContext(),
//                (titleList as ArrayList<String>)[groupPosition] + " List Expanded.",
//                Toast.LENGTH_SHORT
//            ).show()
        }

        expandableListView.setOnGroupCollapseListener { groupPosition ->
//            Toast.makeText(
//                requireContext(),
//                (titleList as ArrayList<String>)[groupPosition] + " List Collapsed.",
//                Toast.LENGTH_SHORT
//            ).show()
        }

        expandableListView.setOnChildClickListener { parent, v, groupPosition, childPosition, id ->
//            Toast.makeText(
//                requireContext(),
//                "Clicked: " + (titleList as ArrayList<String>)[groupPosition] + " -> " + listData[(titleList as ArrayList<String>)[groupPosition]]!!.get(
//                    childPosition
//                ),
//                Toast.LENGTH_SHORT
//            ).show()
            true
        }
    }

    private fun preparePostConigData(): VegaNotifyConfigPostReqResp {
        var request = VegaNotifyConfigPostReqResp()
        var moduleList = arrayListOf<VegaNotifyConfigModule>()
        var keys = selectedList.keys
        for (item in keys) {
            var list = selectedList.get(item)/*?.filter { it.isConfigured == true }*/
            if (list?.isNotEmpty() == true) {
                for (listItem in list) {
                    var module = VegaNotifyConfigModule()
                    module.moduleName = listItem.moduleName
                    module.subModuleName = item
                    module.notificationFlag = listItem.isConfigured
                    module.deleteFlag = false
                    moduleList.add(module)
                }
            }
        }
        request.loggedInUser = PreferenceHelper.get(Constants.USER_NAME, "")
        request.notificationUser = bundleData.userName
        request.moduleDetails = moduleList
        return request
    }

    private fun showSaveNotifyConfigConfirmationDialog() {
        MaterialDialog(requireContext()).show {
            message(R.string.config_complete_confirmation)
            UIUtils.getMetirialCustomView(
                this,
                getString(com.olam.warehouse.presentation.R.string.confirm),
                getString(com.olam.warehouse.presentation.R.string.cancel),
                {
                    selectedList = adapter?.getSelectedList()!!
                    vm.postUserNotifyConfig(preparePostConigData())
                },
                { dismiss() })
        }
    }

    private fun moveToSuccessPage() {
        var sampleList = mutableListOf<VegaStockReconGetAllAuditData>();
        val intent = Intent(requireContext(), SuccessActivity::class.java)
        if (AppUtils.isOnline()) intent.putExtra(
            AppUtils.TITLE,
            getString(R.string.config_success)
        )
        startActivity(intent)
        requireActivity().finish()
    }
}
