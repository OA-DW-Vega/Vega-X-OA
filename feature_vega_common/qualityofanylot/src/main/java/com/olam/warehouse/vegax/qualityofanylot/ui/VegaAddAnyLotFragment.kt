package com.olam.warehouse.vegax.qualityofanylot.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import com.olam.warehouse.login.utils.showErrorDialogWithFAQLink
import com.olam.warehouse.master.common.utils.getPlantDetails
import com.olam.warehouse.master.ui.scanner.ScannerActivity
import com.olam.warehouse.master.vega.entity.VegaDispatchLots
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaDispatchLots
import com.olam.warehouse.presentation.adapter.setUpAdapter
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.PreferenceHelper
import com.olam.warehouse.presentation.utils.extension.gone
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.presentation.utils.extension.visible
import com.olam.warehouse.vegax.qualityofanylot.R
import com.olam.warehouse.vegax.qualityofanylot.data.domain.model.VegaAnyLotListData
import com.olam.warehouse.vegax.qualityofanylot.data.domain.model.VegaAnyLotQualityPostRequest
import com.olam.warehouse.vegax.qualityofanylot.databinding.FragmentAddAnyLotBinding
import com.olam.warehouse.vegax.qualityofanylot.databinding.ItemVegaAnyLotQualityLotDetailsBinding
import com.olam.warehouse.vegax.qualityofanylot.utils.LOT_ID
import com.olam.warehouse.vegax.qualityofanylot.utils.TYPE_SELECT
import org.koin.androidx.viewmodel.ext.android.viewModel

class VegaAddAnyLotFragment : BaseFragment() {
    private lateinit var binding:FragmentAddAnyLotBinding
    private var batchNo: String = ""
    private val vm: VegaAnyLotQualityViewModel by viewModel()
    private var callBack: CallBack? = null
    var typeSelect=""
    private var secretLotList = mutableListOf<VegaCocoaDispatchLots>()



    override val layoutResourceId = R.layout.fragment_add_any_lot


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        binding = FragmentAddAnyLotBinding.inflate(layoutInflater)
        return binding.root
    }

    companion object {
        fun newInstance(type: String) = VegaAddAnyLotFragment().putArgs {
            putString(TYPE_SELECT, type)
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as CallBack
    }



    interface CallBack {
        fun replaceLotListFragment(
            type: String,
            batchNo:String
        )

        fun replaceQualityFragment(
            id: String,
            item: VegaAnyLotQualityPostRequest,
            type: String
        )


    }


    private fun setUpAdapter(lotList: MutableList<VegaCocoaDispatchLots>) {
        if (lotList.size > 0) {
            binding.rvLots.visible()
            binding.tvNoData.gone()
        } else {
            binding.rvLots.gone()
            binding.tvNoData.visible()
        }
        binding.rvLots.setUpAdapter(
            lotList,
            R.layout.item_vega_any_lot_quality_lot_details,
            ItemVegaAnyLotQualityLotDetailsBinding::inflate,
            { it, pos, bindItem ->
                if(!it.secretId.isNullOrEmpty()){
                    bindItem.tvBatchNo.text = it.secretId
                    bindItem.tvBatchLabel.text= getString(R.string.secret_id)

                    bindItem.tvMaterialName.gone()
                    bindItem.tvMaterial.gone()
                    bindItem.tvWeightLabel.gone()
                    bindItem.tvWeight.gone()
                    bindItem.tvDate.gone()
                    bindItem.tvInspectionLotNo.gone()

                }else{
                    bindItem.tvBatchNo.text = it.batchNumber
                    bindItem.tvMaterialName.text = it.materialName
                    bindItem.tvWeight.text = it.weight.plus(" ").plus(it.unitOfMeasure)
                    bindItem.tvDate.text = getString(R.string.st_location)
                    bindItem.tvInspectionLotNo.text = it.storageLocationCode
                }

                bindItem.flItem.setOnClickListener { view ->
                    val item = it
                    val LotList = VegaAnyLotQualityPostRequest()
                    LotList.supplierCode = item.vendor
                    LotList.supplierName = item.vendorName
                    LotList.materialName = bindItem.tvMaterialName.text.toString()
                    LotList.materialCode = item.materialCode
                    LotList.werks = item.plantId
                    LotList.batchNumber = item.batchNumber
                    LotList.storageLocationCode = item.storageLocationCode
                    LotList.secretKey= item.secretId
                    LotList.weight= item.weight
                    LotList.uom= item.unitOfMeasure
                    callBack?.replaceQualityFragment(item.batchNumber, LotList, typeSelect)
                }

            },
            {

            })
    }




    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
          initUI()
         clickEvent()
        initExtra()
    }

    private fun initUI() {
        vm.lotList.observe(viewLifecycleOwner, Observer {
            updateUI(it)
        })

    }

    private fun updateUI(response: Resource<GenericReqAndResp<List<VegaCocoaDispatchLots>>>?) {
        response.let {
            when (it?.status) {
                Resource.Status.SUCCESS -> {
                    hideLoading()
                    when (it.data?.success) {
                        true -> {
                            secretLotList.clear()
                            val dataValue = it.data?.data!!
                            secretLotList.addAll(dataValue)
                            setUpAdapter(secretLotList)

                        }
                        else -> showErrorDialogWithFAQLink(requireContext(), "${it.data?.message}")
                    }
                }
                Resource.Status.LOADING -> showLoading()
                Resource.Status.ERROR -> {
                    hideLoading()
                    showErrorDialogWithFAQLink(requireContext(), "${it.error}")
                }
                else -> {}
            }
        }

    }


    private fun initExtra() {
        typeSelect= arguments?.getString(TYPE_SELECT,"").toString()
    }


    private fun clickEvent() {
        binding.clScan.setOnClickListener {
          moveToScan()
        }

        binding.btAdd.setOnClickListener {
            if(binding.etEnterContainer.getText().toString().isNotEmpty()){
                batchNo=binding.etEnterContainer.getText().toString()
                vm.getLotDetails(batchNo,"", PreferenceHelper.get(Constants.WERKS, ""))

            }else{
                showSnack(getString(R.string.enter_lot_id))
            }
        }





    }
    private fun moveToScan() {
        val intent = Intent(requireContext(), ScannerActivity::class.java)
        startActivityForResult(intent, Constants.SCAN_QR)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == Constants.SCAN_QR) {
            if (resultCode == Activity.RESULT_OK) {
                data?.extras?.getString(Constants.SCANNED_ID)?.trim()?.let {
                    batchNo=it
                    vm.getLotDetails(batchNo,"", PreferenceHelper.get(Constants.WERKS, ""))

                }
            }
        }

    }




}
