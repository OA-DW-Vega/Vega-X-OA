package com.olam.warehouse.odreceiving.ui.weigh

import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.Observer
import com.google.gson.Gson
import com.olam.warehouse.master.common.model.TransactionListPojo
import com.olam.warehouse.master.common.utils.getCurrentOriginEntity
import com.olam.warehouse.master.dorigin.entity.*
import com.olam.warehouse.master.dorigin.model.DOReceivingWithLineItems
import com.olam.warehouse.odreceiving.R
import com.olam.warehouse.odreceiving.databinding.FragmentDoReceivingTransactionListBinding
import com.olam.warehouse.odreceiving.ui.DOReceivingViewModel
import com.olam.warehouse.odreceiving.utils.POSNR
import com.olam.warehouse.odreceiving.utils.RECEIVING_DATA
import com.olam.warehouse.odreceiving.utils.WS01
import com.olam.warehouse.odreceiving.utils.getColor
import com.olam.warehouse.presentation.adapter.setUp
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.enums.ReceivingType
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.AppUtils.isOnline
import com.olam.warehouse.presentation.utils.PreferenceHelper
import com.olam.warehouse.presentation.utils.UIUtils.getActionBtnChangedView
import com.olam.warehouse.presentation.utils.extension.*
import com.olam.warehouse.vegax.App
import kotlinx.android.synthetic.main.item_do_receiving_transaction_details.view.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper

/**
 * Created by Keerthi Santhanam on 02/18/2020.a
 */
class DOReceivingTransactionListFragment : BaseFragment() {

    private lateinit var doTxnDetail: DOTxnDetail
    private lateinit var binding: FragmentDoReceivingTransactionListBinding
    override val layoutResourceId = R.layout.fragment_do_receiving_transaction_list
    private val vm: DOReceivingViewModel by viewModel()
    private var transactionsList = mutableListOf<DOTransactionDetail>()
    private var transList = mutableListOf<DOTxnDetail>()
    private val mSearchList = mutableListOf<DOTransactionDetail>()
    private val receivingData = DOReceiving()
    private val materialsList = mutableListOf<DOMaterial>()
    private val receivings = arrayListOf<DOReceivingWithLineItems>()

    companion object {
        fun newInstance() = DOReceivingTransactionListFragment().putArgs {
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        showCustomLoading()
        setHasOptionsMenu(true)
        binding = FragmentDoReceivingTransactionListBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("odreceiving/ui/weigh/DOReceivingTransactionListFragment").title("OD/Receiving")
            .with(tracker)
        initUI()
    }

    private fun initUI() {
        context?.let {
            getActionBtnChangedView(binding.llTitle, it, false)
        }
        vm.transactions.observe(viewLifecycleOwner, Observer { updateUI(it) })
        vm.transaction.observe(viewLifecycleOwner, Observer {
            updateUIItem(it)
        })
        vm.receiveWithLineItemLocal.observeOnce(this, Observer {
            if (it != null) {
                updateUI(it)
                val str: String = PreferenceHelper.get("txnData", "")
                if (str != "") {
                    val transPojo = Gson().fromJson(str, TransactionListPojo::class.java)
                    transPojo?.list?.let {
                        updateOfflineUI(data = it)
                    }
                }
            }
        })
        //vm.getReceivingWithLineItem()
//        vm.transactionsOffline.observe(viewLifecycleOwner, Observer {
//            updateOfflineUI(it)
//        })
        if (isOnline()) vm.getTransactions() else {
            vm.getReceivingWithLineItem()
//            vm.getTransactionsOffline()

        }
    }

    private fun updateUI(response: Resource<GenericReqAndResp<List<DOTransactionDetail>>>) {
        response.let {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    response.data?.data?.let { it1 ->
                        if (it1.isNotEmpty()) {
                            setUpTransList(it1)
                            binding.tvNoData.gone()
                            binding.rvTransaction.visible()
                        } else {
                            binding.tvNoData.visible()
                            binding.rvTransaction.gone()
                        }
                    }
                    hideLoading()
                }
                Resource.Status.LOADING -> showLoading()
                Resource.Status.ERROR -> {
                    hideLoading()
                    requireContext().toast(it.error.toString())
                }
            }
        }
    }

    private fun updateOfflineUI(data: List<DOTxnDetail>) {
        hideLoading()
        if (data.isNotEmpty()) {
            setUpTransListOffline(data)
            binding.tvNoData.gone()
            binding.rvTransaction.visible()
        } else {
            binding.tvNoData.visible()
            binding.rvTransaction.gone()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu.clear()
        super.onCreateOptionsMenu(menu, inflater)
        activity?.menuInflater?.inflate(com.olam.warehouse.presentation.R.menu.search_menu, menu)
        try {
            val search = menu.findItem(com.olam.warehouse.presentation.R.id.search)
            val searchView: SearchView =
                search?.actionView as SearchView
            searchView.setBackgroundColor(getColor(if(getCurrentOriginEntity().contains("OFI"))com.olam.warehouse.presentation.R.color.colorPrimaryOfi else com.olam.warehouse.presentation.R.color.green))
            searchView.queryHint = getString(R.string.search_transaction_item)
            searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    return false
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    newText.let {
                        if (newText?.isEmpty() == true) {
                            setUpAdapter(transactionsList)
                            binding.tvNoData.gone()
                            binding.rvTransaction.visible()
                        } else {
                            mSearchList.clear()
                            transactionsList.forEach { qtyLot ->
                                newText?.let { text ->
                                    if (qtyLot.lotTransactionId.contains(text) || qtyLot.materialName!!.contains(
                                            text,
                                            true
                                        ) || qtyLot.supplierName!!.contains(
                                            text,
                                            true
                                        ) || qtyLot.displaySupplierId!!.contains(
                                            text,
                                            true
                                        ) || qtyLot.creationDate!!.contains(text, true)
                                    ) {
                                        mSearchList.add(qtyLot)
                                    }
                                }
                            }
                            if (mSearchList.isNotEmpty()) {
                                setUpAdapter(mSearchList)
                                binding.tvNoData.gone()
                                binding.rvTransaction.visible()
                            } else {
                                binding.tvNoData.visible()
                                binding.rvTransaction.gone()
                            }
                        }
                    }
                    return true
                }
            })
        } catch (e: ClassCastException) {
            e.printStackTrace()
        }
    }

    private fun setUpTransListOffline(data: List<DOTxnDetail>) {
        vm.product.observe(viewLifecycleOwner, Observer { materials ->
            checkMaterialMatchOffline(materials, data)
        })
        vm.getProducts()
    }

    private fun checkMaterialMatchOffline(materials: List<DOMaterial>, transactions: List<DOTxnDetail>) {
        transactions.forEach { doDetail ->
            val hasMaterialId =
                materials.any { material -> material.materialCode == doDetail.materialId }
            if (hasMaterialId) {
                doDetail.unitsOfMeasure =
                    materials.first { material -> material.materialCode == doDetail.materialId }
                        .unitsOfMeasure
                /*if(receivings.size>0){
                receivings.forEachIndexed { index, doReceivingWithLineItems ->
                    if(doReceivingWithLineItems.lineItems.get(index).txnId!=doDetail.lotTransactionId){
                        transList.add(doDetail)
                    }
                }
                }else{*/
                    transList.add(doDetail)
                //}

            }
        }
        if(receivings.size>0) {
            var mTransactionOfflineList = receivings.map { it.lineItems.get(0).txnId }
            var OfflineList =transList.filter { !mTransactionOfflineList.contains(it.lotTransactionId) }
            setUpAdapter(null, OfflineList)
        }else{
            setUpAdapter(null, transList)
        }
       // setUpAdapter(null, transList)
    }

    private fun setUpTransList(data: List<DOTransactionDetail>) {
        vm.product.observe(viewLifecycleOwner, Observer { materials ->
            materialsList.clear()
            materialsList.addAll(materials)
            checkMaterialMatch(materials, data)
        })
        vm.getProducts()
    }

    private fun checkMaterialMatch(materials: List<DOMaterial>, transactions: List<DOTransactionDetail>) {
        transactions.forEach { doDetail ->
            val hasMaterialId =
                materials.any { material -> material.materialCode == doDetail.materialId }
            if (hasMaterialId) {
                doDetail.unitsOfMeasure =
                    materials.first { material -> material.materialCode == doDetail.materialId }
                        .unitsOfMeasure
                transactionsList.add(doDetail)
            }
        }
        setUpAdapter(transactionsList)
    }

    private fun setUpAdapter(data: List<DOTransactionDetail>? = null, dataOffline: List<DOTxnDetail>? = null) {
        if (data != null) {
            val transaction1 = data as MutableList<DOTransactionDetail>
            binding.rvTransaction.setUp(
                transaction1.asReversed(),
                R.layout.item_do_receiving_transaction_details, { it, pos ->
                    tvTransactionId.text = it.lotTransactionId
                    tvProduct.text = it.materialName
                    tvSupplier.text = it.supplierName
                    tvSupplierId.text = it.displaySupplierId
                    tvCreatedDate.text = it.creationDate
                    tvWeight.text = it.netweight
                    tvUom.text = it.weightUOM
                }, {
                    if (isOnline()) {
                        vm.getTransactionDetail("0", this.lotTransactionId)
                    }
                })
        } else if (dataOffline != null) {
            val transaction1 = dataOffline as MutableList<DOTxnDetail>
            binding.rvTransaction.setUp(
                transaction1.asReversed(),
                R.layout.item_do_receiving_transaction_details, { it, pos ->
                    tvTransactionId.text = it.lotTransactionId
                    tvProduct.text = it.materialName
                    tvSupplier.text = it.supplierName
                    tvSupplierId.text = it.displaySupplierId
                    tvCreatedDate.text = it.creationDate
                    tvWeight.text = it.netweight
                    tvUom.text = it.weightUOM
                }, {
                    /*if (isOnline()) {
                        vm.getTransactionDetail(this.lotTransactionId, this.lotTransactionId)
                    } else {

                    }*/
                    saveInDB(this)

                    /* else {
                    vm.getTransactionDetailOffline(mLotId)
                }*/
                })
        }

        //hideCustomLoading()

    }

    private fun updateUIItem(response: Resource<GenericReqAndResp<DOTxnDetail>>?) {
        response?.let {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    hideLoading()
                    it.data?.let { resp ->
                        when (resp.success) {
                            true -> {
                                val hasMaterialId =
                                    materialsList.any { material -> material.materialCode == resp.data.materialId }
                                if (hasMaterialId) {
                                    resp.data.unitsOfMeasure =
                                        materialsList.first { material -> material.materialCode == resp.data.materialId }
                                            .unitsOfMeasure
                                    saveInDB(resp.data)
                                }
                            }
                            else -> {
                            }
                        }
                    }
                }
                Resource.Status.LOADING -> showLoading()
                Resource.Status.ERROR -> {
                    hideLoading()
                }
            }
        }
    }

    private fun saveInDB(it: DOTxnDetail) {
        vm.getDOBags(it.lotTransactionId)
        vm.doBags.observeOnce(viewLifecycleOwner, Observer { doBags ->
            saveBag(it, doBags)
        })
    }

    private fun saveBag(
        it: DOTxnDetail,
        doBags: List<DOBag>
    ) {
        doTxnDetail = it
        val transactionDetail = DOTransactionDetail()
        transactionDetail.lotTransactionId = it.lotTransactionId
        transactionDetail.materialId = it.materialId
        transactionDetail.materialName = it.materialName
        transactionDetail.weightUOM = it.unitsOfMeasure
        transactionDetail.isLot = it.isLot
        transactionDetail.netweight = it.netweight
        transactionDetail.supplierId = it.supplierId
        transactionDetail.supplierName = it.supplierName
        if (it.transactionDetails != null && it.transactionDetails!!.size > 0) {
            transactionDetail.noOfBags = it.transactionDetails!![0].noOfBags
        }
        transactionDetail.unitsOfMeasure = it.unitsOfMeasure
        transactionDetail.isMapped = it.isMapped
        transactionDetail.displaySupplierId = it.displaySupplierId
        transactionDetail.creationDate = it.creationDate
        transactionDetail.emptyBagWeight = it.emptyBagWeight
        transactionDetail.bagType = it.bagType
        transactionDetail.weightConversionToKG = it.weightConversionToKG

        vm.saveTransactionDetail(transactionDetail = transactionDetail)
        if (it.transactionDetails != null && it.transactionDetails!!.size > 0) {

            val txnDetail = it.transactionDetails!!
            for (k in 0 until txnDetail.size) {
                txnDetail.get(k).bagList.let {
                    for (i in 0 until txnDetail.get(k).bagList.size) {
                        var doBag = DOBag()
                        doBag.transactionId = txnDetail.get(k).transactionId
                        doBag.lotTransactionId = doTxnDetail.lotTransactionId
                        doBag.bagQrCode = txnDetail.get(k).bagList[i].bagQrCode
                        doBag.weight = txnDetail.get(k).bagList[i].weight
                        doBag.invalidQrCode = txnDetail.get(k).bagList[i].invalidQrCode
                        doBag.bagMissed = txnDetail.get(k).bagList[i].bagMissed
                        doBag.oldQrCode = 0
                        doBag.newQrCode = 0
                        //            doBag.bagMissed = true

                        //            var bag = Bag()
                        //            bag.bagMissed = doBag.bagMissed
                        //            bag.bagQrCode = doBag.bagQrCode
                        //            bag.invalidQrCode = doBag.invalidQrCode
                        //            bag.weight = doBag.weight
                        //
                        //            bagList.add(bag)
                        if (doBags.isNullOrEmpty()) {
                            vm.saveBagDetail(bag = doBag)
                        } else {
                            if (!doBags.contains(doBag))
                                vm.saveBagDetail(bag = doBag)
                        }
                    }
                }
            }

        }

        moveToWeighPage(transactionDetail)

//        doTxnDetail.transactionDetails = mutableListOf(TransactionDetail(transactionDetail.noOfBags, transactionDetail.lotTransactionId, bagList))
    }

    private fun moveToWeighPage(data: DOTransactionDetail?) {
        receivingData.materialCode = data?.materialId
        receivingData.materialName = data?.materialName
        receivingData.supplierCode = data?.supplierId
        receivingData.supplierName = data?.supplierName
        receivingData.txnId = data?.lotTransactionId
        receivingData.uom = data?.unitsOfMeasure.toString()
        receivingData.wsGate = WS01
        receivingData.posnr = POSNR
        //receivingData.uom = data?.weightUOM!!
        receivingData.netWeight = data?.netweight!!.toDouble()
        receivingData.wtype = ReceivingType.SUPPLIER.type
        receivingData.creationDate = data.creationDate
        receivingData.bagType = data.bagType
        receivingData.emptyBagWeight = data.emptyBagWeight
        val intent = Intent(requireContext(), DOReceivingWeighActivity::class.java)
        intent.putExtra(RECEIVING_DATA, receivingData)
        intent.putExtra("doTxnDetail", doTxnDetail)
        startActivity(intent)
    }
    private fun updateUI(data: List<DOReceivingWithLineItems>?) {
        data?.let { receiving ->
            receivings.clear()
            receivings.addAll(receiving)
        }
    }
}
