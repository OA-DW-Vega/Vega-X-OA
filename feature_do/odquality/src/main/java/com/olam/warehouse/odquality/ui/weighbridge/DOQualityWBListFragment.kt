package com.olam.warehouse.odquality.ui.weighbridge

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.olam.warehouse.master.dorigin.entity.DOQualityWBDetails
import com.olam.warehouse.odquality.R
import com.olam.warehouse.odquality.data.domain.model.SelectedBags
import com.olam.warehouse.odquality.databinding.FragmentDoQualityWeighBridgeListBinding
import com.olam.warehouse.odquality.ui.DOQualityViewModel
import com.olam.warehouse.odquality.utils.CHOOSE_QR_FOR_SAMPLING
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.AppUtils.isOnline
import com.olam.warehouse.presentation.utils.PreferenceHelper
import com.olam.warehouse.presentation.utils.UIUtils.getActionBtnChangedView
import com.olam.warehouse.presentation.utils.extension.*
import com.olam.warehouse.vegax.App
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper

/**
 * Created by Baskaran Kannan on 12/27/2019.
 */
class DOQualityWBListFragment : BaseFragment() {

    private var scanLevelId: Int = 0
    private var isBltEnabled: Boolean = false
    private var mAdapter =
        DOWeighBridgeListAdapter { it: DOQualityWBDetails?, isQrCodeLabelClicked: Boolean? ->
            moveBagdetail(
                it,
                isQrCodeLabelClicked
            )
        }
    private val vm: DOQualityViewModel by viewModel()
    private lateinit var mListener: OnWeighBridgeListener
    private lateinit var binding: FragmentDoQualityWeighBridgeListBinding
    override val layoutResourceId = R.layout.fragment_do_quality_weigh_bridge_list

    private var callBack: CallBack? = null

    interface CallBack {
        fun replaceFragment(
            moveFrag: String,
            wbDetails: DOQualityWBDetails?
        )
    }

    interface OnWeighBridgeListener {
        fun onWeighBridgeClick(wbDetails: DOQualityWBDetails?)
        fun setQualityWBList(it: List<DOQualityWBDetails>?)
        fun onQualityOfflineClick()
    }

    companion object {
        fun newInstance() = DOQualityWBListFragment().putArgs {
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mListener = context as OnWeighBridgeListener
        callBack = context as CallBack
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        if (isOnline()) showCustomLoading()
        binding = FragmentDoQualityWeighBridgeListBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("odquality/ui/weighbridge/DOQualityWBListFragment").title("OD/Quality")
            .with(tracker)
        isBltEnabled = false
        initUI()
    }

    private fun initUI() {
        context?.let {
            getActionBtnChangedView(binding.llTitle, it, false)
        }
        binding.rvWeighbridge.layoutManager = LinearLayoutManager(this.context)
        binding.rvWeighbridge.adapter = mAdapter
        vm.qualityOfflineList.observe(viewLifecycleOwner, Observer { enableOfflineLabel(it) })
        vm.getQualityOfflineListCount()
        if (isOnline()) {
            vm.weighBridgeOnline.observe(
                viewLifecycleOwner,
                Observer { updateUIWithOnlineData(it) })
            vm.getWeighBridgeDataOnline()
        } else {
            vm.weighBridge.observe(viewLifecycleOwner, Observer { updateUI(it) })
            vm.getWeighBridgeData()
        }
        binding.llQualityOffline.setOnClickListener { mListener.onQualityOfflineClick() }
    }

    private fun moveBagdetail(
        wbDetails: DOQualityWBDetails?,
        isQrCodeLabelClicked: Boolean?
    ) {
        onItemClick(wbDetails!!, isQrCodeLabelClicked)
    }

    override fun onResume() {
        super.onResume()
        setHasOptionsMenu(true)
    }

    fun updateAdapter(mQualityWBList: MutableList<DOQualityWBDetails>) {
        hideCustomLoading()
        mQualityWBList.let { data ->
            binding.rvWeighbridge.let {
                //if (mAdapter.itemCount != mQualityWBList.size) {
                if (data.size > 0 && isValidDataAvailable(data)) {
                    mAdapter.addItems(data)
                    binding.tvNoData.gone()
                    binding.rvWeighbridge.visible()
                } else {
                    binding.tvNoData.visible()
                    binding.rvWeighbridge.gone()
                }
            }
            //}
        }
    }

    private fun isValidDataAvailable(weighbridge: MutableList<DOQualityWBDetails>): Boolean {
        return weighbridge.any { data -> !data.qcStatus!!.contains("X") }
    }

    private fun onItemClick(
        wbDetails: DOQualityWBDetails,
        qrCodeLabelClicked: Boolean?
    ) {
        isBltEnabled = false
        scanLevelId = 0

        Log.i("isBltEnabledInQty", wbDetails.materialCode)
        Log.i("isBltEnabledInQtyWeighBridgeJd", wbDetails.weighBridgeId)

        vm.getSAPMaterials()
        vm.sapMaterial.observeOnce(viewLifecycleOwner, Observer {sapMaterials ->
            if (sapMaterials != null && sapMaterials.isNotEmpty()) {
                for (sapMaterial in sapMaterials) {
                    if (wbDetails.materialCode!!.contains(sapMaterial.materialCode) && sapMaterial.bltEnabled != null && sapMaterial.bltEnabled!!) {
                        isBltEnabled = sapMaterial.bltEnabled!!
                        scanLevelId = sapMaterial.scanLevelId!!

//                        activity?.toast(isBltEnabled.toString() + "," + scanLevelId.toString())

                        Log.i("isBltEnabledInQty", isBltEnabled.toString())
                        Log.i("isBltEnabledInQty", scanLevelId.toString())

                        // if qr sample done move to quality or else move to qr sample page
                        val selectedBagsStr = PreferenceHelper.get("selectedBags", "default")
                        if (selectedBagsStr != "default") {
                            // qr sample done already
                            val selectedBags = Gson().fromJson(selectedBagsStr, SelectedBags::class.java)
                            if (selectedBags.map != null && selectedBags.map.containsKey(wbDetails.weighBridgeId)
                                && selectedBags.map[wbDetails.weighBridgeId] != null
                                && selectedBags.map[wbDetails.weighBridgeId]?.isNotEmpty()!!) {

                                val s = selectedBags.map[wbDetails.weighBridgeId]
                                val count = s?.filter {
                                    it.isSelected!!
                                }?.count()

                                if (count!! > 0) {
                                    if (qrCodeLabelClicked != null && qrCodeLabelClicked) {
                                        callBack?.replaceFragment(CHOOSE_QR_FOR_SAMPLING, wbDetails)
                                    } else {
                                        mListener.onWeighBridgeClick(wbDetails)
                                    }
                                } else {
                                    if (scanLevelId == 3) {
                                        callBack?.replaceFragment(CHOOSE_QR_FOR_SAMPLING, wbDetails)
                                    } else {
                                        if (qrCodeLabelClicked != null && qrCodeLabelClicked) {
                                            callBack?.replaceFragment(CHOOSE_QR_FOR_SAMPLING, wbDetails)
                                        } else {
                                            mListener.onWeighBridgeClick(wbDetails)
                                        }
                                    }
                                }

//                                mListener.onWeighBridgeClick(wbDetails)
//                                break
//                                return@Observer
                            } else {
                                if (scanLevelId == 3) {
                                    callBack?.replaceFragment(CHOOSE_QR_FOR_SAMPLING, wbDetails)
                                } else {
                                    if (qrCodeLabelClicked != null && qrCodeLabelClicked) {
                                        callBack?.replaceFragment(CHOOSE_QR_FOR_SAMPLING, wbDetails)
                                    } else {
                                        mListener.onWeighBridgeClick(wbDetails)
                                    }
                                }
//                                break
//                                return@Observer
                            }
                        } else {
                            // qr sample not done already
                            if (scanLevelId == 3) {
                                callBack?.replaceFragment(CHOOSE_QR_FOR_SAMPLING, wbDetails)
                            } else {
                                if (qrCodeLabelClicked != null && qrCodeLabelClicked) {
                                    callBack?.replaceFragment(CHOOSE_QR_FOR_SAMPLING, wbDetails)
                                } else {
                                    mListener.onWeighBridgeClick(wbDetails)
                                }
                            }
                        }

                        break
                    }
                }
            }

            if (!isBltEnabled) {
                mListener.onWeighBridgeClick(wbDetails)
            }
        })
    }

    private fun updateUIWithOnlineData(response: Resource<List<DOQualityWBDetails>>) {
        when (response.status) {
            Resource.Status.SUCCESS -> {
                hideLoading()
                response.data?.let { it1 ->
                    if (it1.isNotEmpty()) {
                        val doWbIds = it1.filter { wb -> !wb.challan.isNullOrEmpty() && !wb.qcStatus!!.contains("X") }
                        doWbIds.distinctBy { Pair(it.weighBridgeId, it.weighBridgeId) }
                        mListener.setQualityWBList(doWbIds)
                        if (doWbIds.size > 0) {
                            mAdapter.addItems(doWbIds)
                            binding.tvNoData.gone()
                            binding.rvWeighbridge.visible()
                        } else {
                            binding.tvNoData.visible()
                            binding.rvWeighbridge.gone()
                        }

                    } else {
                        binding.tvNoData.visible()
                        binding.rvWeighbridge.gone()
                    }

                }
            }
            Resource.Status.LOADING -> {}/*showLoading()*/
            Resource.Status.ERROR -> {
                hideLoading()
                requireContext().toast(response.error.toString())
            }
        }
    }

    private fun enableOfflineLabel(response: List<DOQualityWBDetails>) {
        response.let {
            if (it.size > 0) {
                binding.llQualityOffline.visible()
            } else {
                binding.llQualityOffline.gone()
            }
        }
    }

    private fun updateUI(response: List<DOQualityWBDetails>?) {
        hideCustomLoading()
        response?.let { qualityDetail ->
            if (qualityDetail.isNotEmpty()) {
                //val doWbIds = qualityDetail.filter { wb -> !wb.challan.isNullOrEmpty() }
                val doWbIds = qualityDetail.filter { wb -> !wb.challan.isNullOrEmpty() && !wb.qcStatus!!.contains("X") }
                doWbIds.distinctBy { Pair(it.weighBridgeId, it.weighBridgeId) }
                mListener.setQualityWBList(doWbIds)
                if (doWbIds.size > 0) {
                    mAdapter.addItems(doWbIds)
                    binding.tvNoData.gone()
                    binding.rvWeighbridge.visible()
                } else {
                    binding.tvNoData.visible()
                    binding.rvWeighbridge.gone()
                }
            } else {
                binding.tvNoData.visible()
                binding.rvWeighbridge.gone()
            }
        }
    }
}
