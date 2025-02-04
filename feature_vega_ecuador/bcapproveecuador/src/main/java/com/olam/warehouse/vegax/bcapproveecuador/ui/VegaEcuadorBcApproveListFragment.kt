package com.olam.warehouse.vegax.bcapproveecuador.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.Observer
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.list.listItemsSingleChoice
import com.google.gson.Gson
import com.olam.warehouse.login.ui.succes.SuccessActivity
import com.olam.warehouse.master.common.utils.getCurrentKey
import com.olam.warehouse.master.common.utils.getCurrentOriginEntity
import com.olam.warehouse.master.common.utils.getPlantDetails
import com.olam.warehouse.master.user.model.Plant
import com.olam.warehouse.master.vegaecuador.entity.VegaEcuadorBcApproveWBDetails
import com.olam.warehouse.master.vegaecuador.model.VegaEcuadorBcApproveDetails
import com.olam.warehouse.master.vegaecuador.model.VegaEcuadorBcApprovePost
import com.olam.warehouse.master.vegaecuador.model.VegaEcuadorBcApproveQualityDetails
import com.olam.warehouse.master.vegaecuador.model.VegaEcuadorBcApproveResponse
import com.olam.warehouse.presentation.adapter.setUp
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.*
import com.olam.warehouse.presentation.utils.extension.invisible
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.presentation.utils.extension.visible
import com.olam.warehouse.vegax.bcapproveecuador.R
import com.olam.warehouse.vegax.bcapproveecuador.databinding.FragmentVegaEcuadorBcApproveListBinding
import com.olam.warehouse.vegax.bcapproveecuador.utils.LIST_DETAILPAGE
import com.olam.warehouse.vegax.bcapproveecuador.utils.getColor
import com.olam.warehouse.vegax.bcapproveecuador.utils.showErrorDialog
import kotlinx.android.synthetic.main.item_ecuador_bcapprove_list.view.*
import org.koin.androidx.viewmodel.ext.android.viewModel


class VegaEcuadorBcApproveListFragment : BaseFragment() {
    override val layoutResourceId: Int =
        R.layout.fragment_vega_ecuador_bc_approve_list
    private lateinit var binding: FragmentVegaEcuadorBcApproveListBinding
    private var callBack: CallBack? = null
    private val vm: VegaEcuadorBcApproveViewModel by viewModel()
    private var BcApproveWblist = mutableListOf<VegaEcuadorBcApproveWBDetails>()
    private var filteredBcApproveWblist = mutableListOf<VegaEcuadorBcApproveWBDetails>()
    private var finalBcApproveWblist = mutableListOf<VegaEcuadorBcApproveWBDetails>()
    private var alreadySelected = mutableListOf<VegaEcuadorBcApproveWBDetails>()
    private var isMultipleAdd = true
    private val mSearchList = mutableListOf<VegaEcuadorBcApproveWBDetails>()
    private var plantList = mutableListOf<Plant>()
    private var selectedplantid:String=""

    companion object {
        fun newInstance() = VegaEcuadorBcApproveListFragment()
            .putArgs {
            }

        const val SEARCH_HINT_TEXT = "Search truck Item"
    }

    interface CallBack {
        fun replaceFragment(fragment: String, item:VegaEcuadorBcApproveWBDetails )
         }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? CallBack
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentVegaEcuadorBcApproveListBinding.inflate(inflater)
        setHasOptionsMenu(true)
        initUI()
        return binding.root
    }

    private fun initUI() {
       /* if (AppUtils.isOnline()) {
            vm.weighBridge.observe(viewLifecycleOwner, Observer { updateUI(it) })

            vm.getWeighBridgeDetail()

        }*/
        vm.weighBridge.observe(viewLifecycleOwner, Observer { updateUI(it) })
        binding.btnProceed.setOnClickListener {

            showConfirmationDialog(R.string.confirm_dispatch,R.string.proceed) }
        binding.tvSort.setOnClickListener {
            val data = BcApproveWblist
            BcApproveWblist = data.asReversed()
            setupAdapter(BcApproveWblist)
        }
        plantList = getMultiPlantList() as MutableList<Plant>
        if (!plantList.isEmpty()&&plantList.size == 1) {
            binding.tvPlant.text = plantList[0].plantId.plus(" - ")
                .plus(plantList[0].plantName)

        }
        binding.tvPlant.setOnClickListener {
                showPlantSelectionDialog(plantList)

        }

         }
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        //menu.clear()
        activity?.menuInflater?.inflate(com.olam.warehouse.presentation.R.menu.search_menu, menu)
        try {
            val search = menu.findItem(com.olam.warehouse.presentation.R.id.search)
            val searchView: SearchView =
                search?.actionView as SearchView
            searchView.setBackgroundColor(getColor(if(getCurrentOriginEntity().contains("OFI"))com.olam.warehouse.presentation.R.color.colorPrimaryOfi else com.olam.warehouse.presentation.R.color.green))
            searchView.queryHint = SEARCH_HINT_TEXT
            searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    return false
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    newText.let {
                        if (newText?.isEmpty() == true) {
                            setupAdapter(if (filteredBcApproveWblist.isEmpty()) BcApproveWblist else filteredBcApproveWblist)
                        } else {
                            mSearchList.clear()
                            BcApproveWblist.forEach { qtyWb ->
                                newText?.let { text ->
                                    if (qtyWb.grnNumber!!.contains(text)) {
                                        mSearchList.add(qtyWb)
                                    }
                                }
                            }
                            setupAdapter(mSearchList)
                        }
                    }
                    return true
                }
            })
        } catch (e: ClassCastException) {
            e.printStackTrace()
        }
    }

    private fun updateUI(response: Resource<GenericReqAndResp<List<VegaEcuadorBcApproveWBDetails>>>) {
        response.let {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    when (it.data?.success) {
                        true -> {
                            BcApproveWblist.clear()
                            val dataValue = it.data?.data!!
                            BcApproveWblist.addAll(dataValue.asReversed())
                            updateSelectLotValues()
                         }
                        else -> UIUtils.showErrorDialog(requireContext(), "${it.data?.message}")
                    }
                }
                Resource.Status.LOADING ->
                    showLoading()
                Resource.Status.ERROR -> {
                    BcApproveWblist.clear()
                    updateSelectLotValues()
                    hideLoading()
                    UIUtils.showErrorDialog(requireContext(), "${it.error}")
                }
            }
        }
    }
    private fun updateSelectLotValues() {

        val mapSelected = alreadySelected.map { it.batchNumber }.toString()
        if(BcApproveWblist.isNotEmpty())
            binding.tvTruckIDNo.text = getString(R.string.no_wb).plus(" ").plus(BcApproveWblist.size)
        else
            binding.tvTruckIDNo.text = getString(R.string.no_wb).plus(" ").plus("0")
        setupAdapter(BcApproveWblist)
        //updateWeight()
    }


    private fun setupAdapter(data: MutableList<VegaEcuadorBcApproveWBDetails>) {
        if (data.size > 0) {
            binding.rvLots.visible()
            binding.tvNoTruck.invisible()
        } else {
            binding.rvLots.invisible()
            binding.tvNoTruck.visible()
        }
        binding.rvLots.setUp(data, R.layout.item_ecuador_bcapprove_list, { it, pos ->
            tvLotId.text = it.poNumber
            tvsupplierid.text = it.supplierName
            tvGradeValue.text = it.batchNumber
            tvWeightValue.text = it.grnQty.toString().trim().plus(" ").plus(it.unitsOfMeasure)
            tvStLocationValue.text = it.wbid
            ivSelect.isChecked = it.isAdded
            ivSelect.setOnCheckedChangeListener { item, isChecked ->
                it.isAdded = isChecked
                if (!isMultipleAdd) {
                    if (isChecked)
                        removeChecked(pos, data)
                }

            }
            llLotItem.setOnClickListener {view->
                callBack?.replaceFragment(LIST_DETAILPAGE, data[pos])
            }
            ivSelect.setOnClickListener{ view ->
                if (data[pos].isChecked){
                    it.isAdded=false
                    it.isChecked=false
                    ivSelect.isChecked=false
                        removeChecked(pos, data)
                } else {
                    it.isChecked = true
                    it.isAdded=true
                    ivSelect.isChecked = true
                }


            }

        })
        hideLoading()
    }

    private fun removeChecked(item: Int, list: MutableList<VegaEcuadorBcApproveWBDetails>) {
        list[item].isAdded = false
        list[item].isChecked = false
        setupAdapter(list)
    }
    private fun sendSelectedLots() {
        if(!BcApproveWblist.isEmpty())
        {
            val data = BcApproveWblist.filter { it.isAdded == true } as ArrayList
            finalBcApproveWblist.clear()
            finalBcApproveWblist.addAll(data)
            vm.postApproval(
                    VegaEcuadorBcApprovePost(
                            key = getCurrentKey(),
                            plant = getPlantDetails(),
                            approvalDetailsList = prepareDeliveryList()
                    )
            )
            vm.approval.observe(viewLifecycleOwner, Observer { updateResponse(it) })
        }else
        {
            Toast.makeText(requireContext(), "Please select atleast one weighbridgeId", Toast.LENGTH_SHORT).show()
        }
    }
    private fun updateResponse(data: Resource<GenericReqAndResp<VegaEcuadorBcApproveResponse>>?) {

        data?.let {
            when (it.status) {
                Resource.Status.SUCCESS -> {

                    if(data.data?.success!!)
                    {
                        moveToSuccessPage()
                    }
                    else{
                        //retryBcApproveWblist
                        var result =data.data?.data!!
                         retry(result)
                       hideCustomLoading()
                    }
                   // Toast.makeText(context, it.s, Toast.LENGTH_SHORT).show()

                   // vm.updateSyncStatus(vegaCoffeeThirdPartyModelWithLots)
                }
                Resource.Status.LOADING -> showCustomLoading()
                Resource.Status.ERROR -> {
                    hideCustomLoading()
                    UIUtils.showErrorDialog(requireContext(), "${it.error}")
                    //prepareErrorData(data.data?.data?.wbId, false, it.error.toString())
                }
            }
        }
    }
    private fun showConfirmationDialog(msg:Int,ok: Int) {
        MaterialDialog(requireContext()).hide()
    MaterialDialog(requireContext()).show {
            message(msg)
        UIUtils.getMetirialCustomView(
            this,
            getString(ok),
            getString(R.string.cancel),
            {
                sendSelectedLots()
            },
            { dismiss() })
        }
    }

    private fun prepareDeliveryList(): List<VegaEcuadorBcApproveDetails> {
        val list = ArrayList<VegaEcuadorBcApproveDetails>()
        for (item in finalBcApproveWblist) {
            val deliveryDetail = VegaEcuadorBcApproveDetails()
            deliveryDetail.batchNumber = item.batchNumber
                deliveryDetail.materialCode = item.materialNumber
            deliveryDetail.plant = selectedplantid
            deliveryDetail.autoTransfer = ""
            deliveryDetail.discount = item.discount
            deliveryDetail.finalApproval = item.finalApproval
            deliveryDetail.grnQty = item.grnQty?.trim()
            deliveryDetail.item = item.item
            deliveryDetail.msg = ""
            deliveryDetail.paidWeight = ""
            deliveryDetail.priceCharacter = ""
            deliveryDetail.qchar = item.qchar
            deliveryDetail.qualityDetails = ArrayList<VegaEcuadorBcApproveQualityDetails>()
            deliveryDetail.receivingStorageLoc = ""
            deliveryDetail.sendingStorageLoc = ""
            deliveryDetail.status = false
            deliveryDetail.supplierCode = item.supplierCode
            deliveryDetail.uom = item.unitsOfMeasure
            deliveryDetail.waers = item.waers
            deliveryDetail.weighBridgeId = item.wbid
            deliveryDetail.poNumber = item.poNumber

            list.add(deliveryDetail)
        }
        return list
    }
    private fun moveToSuccessPage() {
        val intent = Intent(requireContext(), SuccessActivity::class.java)
        intent.putExtra(
            AppUtils.TITLE,
            "Approve Successfully"
        )
        intent.putExtra(AppUtils.SUB_TITLE, " ")
        startActivity(intent)
        requireActivity().finish()
    }
    private fun retry(retrylist:VegaEcuadorBcApproveResponse): MutableList<VegaEcuadorBcApproveWBDetails>
    {
        var Weighbridgeid:String=""
        var errormsg:String=""
         var list = ArrayList<VegaEcuadorBcApproveDetails>()
        list=retrylist.approvalDetailsList
        list.forEach {it1->
            if(it1.status)
            {
                var filterlist=     finalBcApproveWblist.distinctBy { it.wbid }.filter {   it.wbid!!.equals(it1.weighBridgeId)  }
                BcApproveWblist.removeAll(filterlist)
            }
            else
            {
                Weighbridgeid=Weighbridgeid+","+it1.weighBridgeId
                errormsg=it1.msg.toString()
            }

        }
        BcApproveWblist.forEach {
            it2->
            it2.isChecked=false
            it2.isAdded=false
        }
      showErrorDialog(requireContext(), "WeighBridge Id: "+Weighbridgeid.trim(',')+" "+errormsg)
        setupAdapter(BcApproveWblist)
       // showConfirmationDialog(R.string.confirm_dispatch,R.string.retry)
        return BcApproveWblist
    }
    private fun getMultiPlantList(): List<Plant> { val plants = Gson().fromJson<List<Plant>>(PreferenceHelper.get(Constants.MULTI_PLANT_LIST, ""))

        return plants
    }
    private fun showPlantSelectionDialog(it: List<Plant>) {

            val location = it.map { data -> data.plantId.plus(" - ").plus(data.plantName) }
            MaterialDialog(requireContext()).show {
                title(R.string.plant_popup)
                if (!plantList.isEmpty()&&plantList.size >= 1) {
                listItemsSingleChoice(items = location) { _, index, text ->
                    binding.tvPlant.text = text
                    if (AppUtils.isOnline()) {
                        vm.getWeighBridgeDetail(it[index].plantId)
                        selectedplantid=it[index].plantId
                    }
                }
                }
                positiveButton(text = UIUtils.getSpannedText(getString(R.string.ok), true))
          }
    }
}
