package com.olam.warehouse.vegax.secretidcommon.ui

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.lifecycle.Observer
import com.olam.warehouse.login.utils.showErrorDialogWithFAQLink
import com.olam.warehouse.master.vega.entity.VegaMaterial
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaDispatchLots
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeRminLots
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeSalesLots
import com.olam.warehouse.presentation.adapter.setUpAdapter
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.extension.formatThreeDigits
import com.olam.warehouse.presentation.utils.extension.gone
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.presentation.utils.extension.visible
import com.olam.warehouse.vegax.secretid.R
import com.olam.warehouse.vegax.secretid.databinding.FragmentLotListBinding
import com.olam.warehouse.vegax.secretid.databinding.ItemLotCardLayoutBinding
import com.olam.warehouse.vegax.secretidcommon.utils.listOfField
import org.koin.androidx.viewmodel.ext.android.viewModel


class VegaCommonSecretIdLotListFragment():BaseFragment() {

    override val layoutResourceId= R.layout.fragment_lot_list
    private lateinit var binding:FragmentLotListBinding
    private val vm: VegaCommonSecretIdViewModel by viewModel()
    private var lotsList = mutableListOf<VegaCocoaDispatchLots>()
    private var wareHouseList = mutableListOf<String>()
    private var alreadySelected = ArrayList<VegaCocoaDispatchLots>()
    private var filteredDispatchLotsList = mutableListOf<VegaCocoaDispatchLots>()
    private var listener: CallBack? = null



    interface CallBack {
        fun addedLots(
            lots: ArrayList<VegaCocoaDispatchLots>
        )
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = context as? CallBack
    }


    @SuppressLint("SuspiciousIndentation")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
      binding= FragmentLotListBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initExtra()
        initUi()
        clickEvent()
    }

    private fun initExtra() {
        val bundle= arguments?.getParcelableArrayList<VegaCocoaDispatchLots>(UIUtils.LOT_DETAIL)
        alreadySelected.addAll(bundle as ArrayList<VegaCocoaDispatchLots>)
    }

    private fun clickEvent() {
        binding.btnProceed.setOnClickListener {
            sendSelectedLots()
        }
    }

    companion object {
        fun newInstance(lotList:ArrayList<VegaCocoaDispatchLots>) = VegaCommonSecretIdLotListFragment().putArgs {
             putParcelableArrayList(UIUtils.LOT_DETAIL, lotList)
        }
    }

    private fun sendSelectedLots() {
        val data = lotsList.filter { it.isChecked } as ArrayList
        listener?.addedLots(data)
    }


    @SuppressLint("SuspiciousIndentation")
    private fun initUi() {

        vm.getAllStockByPlantList()
        vm.stockPlantList.observe(viewLifecycleOwner, Observer {
            updateUI(it)
        })

    }


    private fun updateUI(response: Resource<GenericReqAndResp<List<VegaCocoaDispatchLots>>>) {
        response.let {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    hideLoading()
                    when (it.data?.success) {
                        true -> {
                            lotsList.clear()
                            val dataValue = it.data?.data!!
                            lotsList.addAll(dataValue)


                            alreadySelected.forEach {
                                lotsList.forEach { lots->
                                    if(it.batchNumber == lots.batchNumber){
                                    lots.isAdded=true
                                }
                            } }
                           
                            updateSelectLotValues()
                            getWarehouseList()
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

    private fun getWarehouseList() {
        if (lotsList.isNotEmpty()) {
            wareHouseList.add(getString(R.string.all))
            wareHouseList.addAll(lotsList.listOfField(VegaCocoaDispatchLots::storageLocationCode).toSet())
            updateWareHouseSpinner()
        }
    }

    private fun updateWareHouseSpinner() {
        val stageAdapter =
            ArrayAdapter(requireContext(), R.layout.item_vega_wh, wareHouseList)
        stageAdapter.setDropDownViewResource(android.R.layout.simple_list_item_1)
        binding.spWareHouse.adapter = stageAdapter
        val defaultposition = 0
        binding.spWareHouse.setSelection(defaultposition)
        binding.spWareHouse.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {}

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                filteredDispatchLotsList.clear()
                if (position > 0) {
                    val storageLocation = wareHouseList[position]
                    val lotsList = lotsList.filter { it.storageLocationCode == storageLocation }
                    filteredDispatchLotsList.addAll(lotsList)
                    setupAdapter(filteredDispatchLotsList)
                } else {
                    setupAdapter(lotsList)
                    filteredDispatchLotsList.clear()
                }
            }
        }
    }

    private fun updateSelectLotValues() {
        hideLoading()
       setupAdapter(lotsList)
    }

    private fun setupAdapter(data: MutableList<VegaCocoaDispatchLots>) {
        if (data.size > 0) {
            binding.rvLots.visible()
            binding.tvNoData.gone()
        } else {
            binding.rvLots.gone()
            binding.tvNoData.visible()
        }

        binding.rvLots.setUpAdapter(
            data,
            R.layout.item_lot_card_layout,
            ItemLotCardLayoutBinding::inflate,
            { it, pos, bindItem ->

                bindItem.tvScaleLotValue.text = it.batchNumber
                bindItem.tvStLocationValue.text = it.storageLocationCode
                bindItem.tvScaleWeightValue.text =
                    it.weight?.toDouble()?.formatThreeDigits().plus(" ").plus(it.unitOfMeasure)
                bindItem.cbItem.isChecked = it.isChecked !!
                    //?: false
                bindItem.tvDateValue.text= it.materialName
                bindItem.flLl.setOnClickListener { view ->
                    lotsList.forEach { it.isChecked = false }
                    lotsList[pos].isChecked = !it.isChecked!!
                    binding.rvLots.adapter?.notifyDataSetChanged()
                }


            }, itemClick = {

            })
    }



}
