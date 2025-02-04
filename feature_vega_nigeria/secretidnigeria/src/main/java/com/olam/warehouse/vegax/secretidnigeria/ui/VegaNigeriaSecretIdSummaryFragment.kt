package com.olam.warehouse.vegax.secretidnigeria.ui

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import com.microsoft.appcenter.utils.HandlerUtils.runOnUiThread
import com.olam.warehouse.login.databinding.ItemPrintLotCardPreviewBinding
import com.olam.warehouse.login.utils.bitmapToString
import com.olam.warehouse.login.utils.getBitmap
import com.olam.warehouse.login.utils.getBitmapFromView
import com.olam.warehouse.master.vega.entity.VegaQualityWBDetails
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeLot
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeSalesLots
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.ui.wifiprinter.WifiMainActivity
import com.olam.warehouse.presentation.utils.*
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.secretidnigeria.R
import com.olam.warehouse.vegax.secretidnigeria.data.domain.model.VegaNigeriaSecretId
import com.olam.warehouse.vegax.secretidnigeria.databinding.FragmentVegaNigeriaSecretidSummaryBinding
import com.olam.warehouse.vegax.secretidnigeria.utils.getColor
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper

class VegaNigeriaSecretIdSummaryFragment : BaseFragment() {

    override val layoutResourceId = R.layout.fragment_vega_nigeria_secretid_summary

    private lateinit var binding: FragmentVegaNigeriaSecretidSummaryBinding
    private val vm: VegaNigeriaSecretIdViewModel by viewModel()
    private var secretIdDetails = VegaNigeriaSecretId()
    private var secretId: String? = ""
    private var batchNo: String? = ""
    private var finalApproval: String? = ""
    private var challanNo: String? = ""
    private var flag: String? = ""
    private var lotItems = arrayListOf<VegaCoffeeLot>()
    private var isAccept: Boolean = false

    private var tallyPrintKeys = ArrayList<String>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentVegaNigeriaSecretidSummaryBinding.inflate(layoutInflater)
        return binding.root
    }

    companion object {
        fun newInstance() = VegaNigeriaSecretIdSummaryFragment().putArgs {}
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("qualitynigeria/ui/VegaNigeriaQualitySummaryFragment").title("Approve").with(tracker)
        initUI()
    }

    override fun onPause() {
        super.onPause()
        hideCustomLoading()
    }

    private fun initUI() {
        secretIdDetails =
            arguments?.getParcelable<VegaNigeriaSecretId>("LOT_LIST") as VegaNigeriaSecretId

        binding.tvApproveParamsWeighBID.text = secretIdDetails.weighBridgeId
        binding.tvBatchNo.text = batchNo

        binding.printLotCard.setOnClickListener { createLotCardBitMap() }
        binding.btnParamsProceed.setOnClickListener { activity?.onBackPressed() }
        vm.wbWeight.observe(viewLifecycleOwner, Observer { updateUI(it) })
        secretIdDetails.weighBridgeId.let { vm.getWbWeightDetails(it) }
    }

    private fun updateUI(response: Resource<GenericReqAndResp<VegaQualityWBDetails>>) {
        response.let {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    hideCustomLoading()
                    when (it.data?.success) {
                        true -> {
                            if (it.data?.data?.challan.toString().isNotEmpty()) {
                                binding.tvSecretId.text = it.data?.data?.challan.toString()
                                secretId = it.data?.data?.challan.toString()
                                binding.ivSampleIdQR.setImageBitmap(getBitmap(it.data?.data?.challan.toString()))
                            } else {
                                UIUtils.showErrorDialog(
                                    requireContext(),
                                    getString(R.string.no_data_found)
                                )
                                binding.printLotCard.setBackgroundColor(getColor(com.olam.warehouse.presentation.R.color.grey))
                                binding.printLotCard.isEnabled = false
                                binding.printLotCard.isClickable = false
                            }
                        }
                        else -> UIUtils.showErrorDialog(requireContext(), "${it.data?.message}")
                    }
                }
                Resource.Status.LOADING -> showCustomLoading()
                Resource.Status.ERROR -> {
                    hideCustomLoading()
                    UIUtils.showErrorDialog(requireContext(), "${it.error}")
                }
            }
        }
    }

    private fun createLotCardBitMap() {
        val lotList = ArrayList<VegaCoffeeSalesLots>()
        lotList.add(
            VegaCoffeeSalesLots(
                "",
                secretId.toString(),
                secretIdDetails.materialCode.toString(),
                secretIdDetails.materialName.toString(),
                "",
                "",
                "",
                "",
                "",
                secretIdDetails.unitsOfMeasure,
                "",
                secretIdDetails.netWeight


            )
        )
        tallyPrintKeys.clear()
        showCustomLoading()
        DoAsync {
            lotList.forEachIndexed { index, item ->
                val view = LayoutInflater.from(context)
                    .inflate(com.olam.warehouse.login.R.layout.item_print_lot_card_preview, null)
                val viewBinder = ItemPrintLotCardPreviewBinding.bind(view)

                viewBinder.ivPreview.setImageBitmap(getBitmap(item.batchNumber))
                viewBinder.tvLotValue.text = item.batchNumber
                viewBinder.tvLot.text = getString(R.string.secret_id)
                viewBinder.tvMaterialValue.visibility = View.GONE
                viewBinder.tvMaterial.visibility = View.GONE
                viewBinder.tvWeight.visibility = View.GONE

                tallyPrintKeys.add(
                    bitmapToString(
                        getBitmapFromView(
                            view, Color.WHITE
                        )
                    )
                )
            }
            runOnUiThread {
                val gson = GsonUtils()
                PreferenceHelper.save(Constants.BITMAP_KEYS, gson.toJson(tallyPrintKeys))
                startActivity(Intent(context, WifiMainActivity::class.java))
            }
        }.execute()
    }

}


