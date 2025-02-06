package com.olam.warehouse.vegax.secretidcommon.ui

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.Observer
import com.afollestad.materialdialogs.MaterialDialog
import com.olam.warehouse.login.ui.succes.SuccessActivity
import com.olam.warehouse.login.utils.showErrorDialogWithFAQLink
import com.olam.warehouse.master.ui.scanner.ScannerActivity
import com.olam.warehouse.master.vega.entity.VegaDispatchLots
import com.olam.warehouse.master.vega.entity.VegaMaterial
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaDispatchLots
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeRminLots
import com.olam.warehouse.presentation.adapter.setUpAdapter
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.AppUtils
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.PreferenceHelper
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.extension.formatThreeDigits
import com.olam.warehouse.presentation.utils.extension.gone
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.presentation.utils.extension.visible
import com.olam.warehouse.vegax.secretid.R
import com.olam.warehouse.vegax.secretid.databinding.FragmentSelectLotBinding
import com.olam.warehouse.vegax.secretid.databinding.ItemLotCardLayoutBinding
import com.olam.warehouse.vegax.secretidcommon.data.domain.model.VegaSecretIdResponse
import com.olam.warehouse.vegax.secretidcommon.utils.LOT_LIST
import com.olam.warehouse.vegax.secretidcommon.utils.MODULE_SELECT
import org.koin.androidx.viewmodel.ext.android.viewModel

class VegaSecretSelectLotFragment : BaseFragment() {

    override val layoutResourceId = R.layout.fragment_select_lot
    private lateinit var binding: FragmentSelectLotBinding
    private var callBack: CallBack? = null
    private var secretLotList = mutableListOf<VegaCocoaDispatchLots>()
    private val vm: VegaCommonSecretIdViewModel by viewModel()
    private var lotId = ""
    private lateinit var selectedLot: VegaCocoaDispatchLots
    private var isSecretIdExist=false




    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentSelectLotBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? CallBack

    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUI()
        clickEvent()
    }

    private fun initUI() {
        enableDisableBtn(false)
        vm.lotList.observe(viewLifecycleOwner, Observer {
            updateUI(it)
        })

        vm.secretIdLot.observe(viewLifecycleOwner, Observer {
            secretIdResponse(it)
        })

    }

    private fun secretIdResponse(response: Resource<GenericReqAndResp<List<VegaSecretIdResponse>>>?) {
        response.let {
            when (it?.status) {
                Resource.Status.SUCCESS -> {
                    hideLoading()
                    when (it.data?.success) {
                        true -> {
                            it.data?.data.let {
                                if(!it.isNullOrEmpty()){
                                    isSecretIdExist= it[0].isSecretIdExists
                                    if(isSecretIdExist){
                                        showMaterialDialogWithMsg()
                                    }else{
                                        moveToSuccess(it[0].secretKey)
                                    }
                                }

                            }
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

    @SuppressLint("SuspiciousIndentation")
    private fun moveToSuccess(secretId:String) {
        val intent = Intent(requireContext(), SuccessActivity::class.java)
        intent.putExtra(AppUtils.TITLE, getString(R.string.success_msg))
        intent.putExtra(
            AppUtils.SUB_TITLE, getString(R.string.secret_id).plus(" : ").plus(secretId)
        )
        intent.putExtra(AppUtils.IS_SECRET_ID_QR, secretId.contains("SCRID"))
        intent.putExtra(AppUtils.SECRET_ID, secretId)

        startActivity(intent)
        requireActivity().finish()
    }


    private fun showMaterialDialogWithMsg() {
        MaterialDialog(requireContext()).show {
            message(R.string.secretid_msg)
            UIUtils.getMetirialCustomView(
                this,
                getString(com.olam.warehouse.presentation.R.string.confirm),
                getString(com.olam.warehouse.presentation.R.string.cancel),
                {
                    createSecretId()
                },
                { dismiss() })

        }
    }

    private fun updateUI(response: Resource<GenericReqAndResp<List<VegaCocoaDispatchLots>>>) {
        response.let {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    hideLoading()
                    when (it.data?.success) {
                        true -> {
                            secretLotList.clear()
                            val dataValue = it.data?.data!!
                            secretLotList.addAll(dataValue)
                            setupAdapter(secretLotList)

                        }
                        else -> showErrorDialogWithFAQLink(requireContext(), "${it.data?.message}")
                    }
                }
                Resource.Status.LOADING -> showLoading()
                Resource.Status.ERROR -> {
                    hideLoading()
                    showErrorDialogWithFAQLink(requireContext(), "${it.error}")
                }
            }
        }
    }


    private fun clickEvent() {

        binding.btAdd.setOnClickListener {
            lotId = binding.etEnterContainer.text.toString()
            if (lotId.isEmpty()) {
                Toast.makeText(requireContext(), "Enter Lot id", Toast.LENGTH_SHORT).show()
            } else {
                vm.getLotDetails(lotId,"", PreferenceHelper.get(Constants.WERKS, ""))

            }

        }
        binding.btnScan.setOnClickListener {
            moveToScan()
        }

        binding.btnProceed.setOnClickListener {
            createSecretId()
        }


    }

    private fun createSecretId() {
        val plant= PreferenceHelper.get(Constants.WERKS, "")
        val batchNumber= selectedLot.batchNumber
        val materialCode= selectedLot.materialCode.substring(6)
        vm.getSecretId(batchNumber,isSecretIdExist,materialCode,plant)

    }



    companion object {
        fun newInstance() = VegaSecretSelectLotFragment().putArgs {
            // putParcelableArrayList(UIUtils.LOT_DETAIL, lotList)
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
                    lotId=it
                    vm.getLotDetails(lotId,"", PreferenceHelper.get(Constants.WERKS, ""))
                    //binding.etEnterContainer.setText(it)
                }
            }
        }
    }

    interface CallBack {
        fun replaceFragment(
            fragment: String, list: ArrayList<VegaCocoaDispatchLots>
        )
    }


    private fun setupAdapter(data: MutableList<VegaCocoaDispatchLots>) {
        if (data.size > 0) {
            binding.rvLots.visible()
            binding.tvNoDataFound.gone()
        } else {
            binding.rvLots.gone()
            binding.tvNoDataFound.visible()
        }

        binding.rvLots.setUpAdapter(
            data,
            R.layout.item_lot_card_layout,
            ItemLotCardLayoutBinding::inflate,
            { it, pos, bindItem ->
                bindItem.tvScaleLotValue.text = it.batchNumber
                // bindItem.tvDate.text = it.da
                bindItem.tvStLocationValue.text = it.storageLocationCode
                bindItem.tvScaleWeightValue.text =
                    it.weight?.toDouble()?.formatThreeDigits().plus(" ").plus(it.unitOfMeasure)
                bindItem.tvDateValue.text = it.materialName
                bindItem.cbItem.isChecked = it.isChecked!!
                bindItem.flLl.setOnClickListener { view ->
                    secretLotList.forEach { it.isChecked = false }
                    secretLotList[pos].isChecked = !it.isChecked!!
                    selectedLot= secretLotList[pos]
                    binding.rvLots.adapter?.notifyDataSetChanged()
                    enableDisableBtn(secretLotList.any { it.isChecked!! })
                }


            }, itemClick = {

            })
    }


    fun updateLotList(list: ArrayList<VegaCocoaDispatchLots>) {
        secretLotList.clear()
        secretLotList.addAll(list)
        setupAdapter(list)
    }

    private fun enableDisableBtn(flag: Boolean) {
        if (flag) {
            binding.btnProceed.isEnabled = true
            binding.btnProceed.setBackgroundColor(requireContext().getColor(com.olam.warehouse.presentation.R.color.colorPrimaryOfi))
        } else {
            binding.btnProceed.isEnabled = false
            binding.btnProceed.setBackgroundColor(requireContext().getColor(com.olam.warehouse.presentation.R.color.light_grey))
        }
    }


}
