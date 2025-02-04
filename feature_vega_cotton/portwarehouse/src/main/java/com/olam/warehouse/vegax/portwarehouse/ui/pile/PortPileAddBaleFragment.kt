package com.olam.warehouse.portwarehouse.ui.pile

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.InputFilter
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.olam.warehouse.master.ui.scanner.ScannerActivity
import com.olam.warehouse.master.vegaCotton.vegaPortWareHouse.entity.PortPileBale
import com.olam.warehouse.master.vegaCotton.vegaPortWareHouse.entity.PortPileStorageLocationModel
import com.olam.warehouse.portwarehouse.utils.PortWHUtil.ACTIVITY_REQUEST_CODE
import com.olam.warehouse.portwarehouse.utils.PortWHUtil.PILE_LIST
import com.olam.warehouse.portwarehouse.utils.PortWHUtil.SELECTED_BALE
import com.olam.warehouse.portwarehouse.utils.PortWHUtil.SELECTED_GRADE
import com.olam.warehouse.portwarehouse.utils.PortWHUtil.SELECTED_GRADE_ITEM
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.ui.widget.CustomFilterDialog
import com.olam.warehouse.presentation.ui.widget.RecyclerViewItemClickListener
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.extension.gone
import com.olam.warehouse.presentation.utils.extension.onChange
import com.olam.warehouse.presentation.utils.extension.visible
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.portwarehouse.R
import kotlinx.android.synthetic.main.fragment_port_pile_mgnt.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper

class PortPileAddBaleFragment : BaseFragment(), RecyclerViewItemClickListener {

    private var isFromScan = false
    private var callBack: CallBack? = null
    private var storageId: String = ""
    override val layoutResourceId = R.layout.fragment_port_pile_mgnt
    private val vm: PortPileAddBaleViewModel by viewModel()
    private lateinit var mAdapter: PortPileAddBaleAdapter
    private var storageData = ArrayList<PortPileStorageLocationModel>()
    private var mBaleId: String = ""

    interface CallBack {
        fun replaceConfirmFragment(storageId: PortPileStorageLocationModel)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? CallBack
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("portwarehouse/ui/pile/PortPileAddBaleFragment")
            .title("Portwarehouse").with(tracker)
        showLoading()
        vm.getStorageLocationList()
        vm.getStorageLocationList
            .observe(viewLifecycleOwner, Observer {
                hideLoading()
                when (it.status) {
                    Resource.Status.SUCCESS -> {
                        it.data?.let {
                            it.data.let { data ->
                                storageData.addAll(data)
                                val location =
                                    data.map { storage -> storage.storageLocationCode.toString() + " - " + storage.classification } as ArrayList
                                //sp_pile.setTitle("Select Pile ID")
                                data.map { storage -> storage.storageLocationCode + " - " + storage.storageLocationName } as ArrayList
                                //sp_pile.text = location[0]
                                if (data.size > 0) {
                                    storageId = data[0].storageLocationCode
                                    sp_pile.setOnClickListener { spinnerClick(location) }
                                }
                            }
                        }

                        hideLoading()
                    }
                    Resource.Status.LOADING -> showLoading()
                    Resource.Status.ERROR -> {
                        hideLoading()
                        UIUtils.showErrorDialog(requireContext(), it.error.toString())
                    }
                }
            })

        initUI()
    }

    private fun initUI() {
        setBaleAdapter()
        clScan.setOnClickListener { moveToScanPage() }
        btnProceed.setOnClickListener { moveToConfirm() }
        etEnterContainer.onChange { setAddButtonColor(vm.validateBaleId(it)) }
        etEnterContainer.filters = arrayOf(InputFilter.AllCaps(), InputFilter.LengthFilter(11))
        btAdd.setOnClickListener {
            val baleId = etEnterContainer.text.toString().trim()
            validateScannedData(baleId)
        }
        vm.getBaleListByStorageId.observe(this, Observer {

            saveListBale(it)
        })

        vm.validateBaleWithAPI
            .observe(viewLifecycleOwner, Observer {
                hideLoading()
                when (it.status) {
                    Resource.Status.SUCCESS -> {
                        it.data?.let {
                            it.data.let { data ->
                                saveListBaletoDB(data)
                            }
                        }

                        hideLoading()
                    }
                    Resource.Status.LOADING -> showLoading()
                    Resource.Status.ERROR -> {
                        hideLoading()
                        UIUtils.showErrorDialog(requireContext(), it.error.toString())
                    }
                }
            })

    }

    private fun moveToConfirm() {
        callBack?.replaceConfirmFragment(vm.mCurrentPiles)
    }

    private fun makeBaleValidationCall(baleId: String) {
        vm.validateBaleWithAPI(baleId, storageId)

    }

    private fun saveListBaletoDB(listBale: PortPileBale) {
        listBale.storageLocationTo = vm.mCurrentPiles.storageLocationCode
        runBlocking {
            withContext(Dispatchers.IO) {
                vm.insertBaleToStorage(listBale)
            }
        }
        getBaleByStorage(vm.mCurrentPiles.storageLocationCode)
    }

    private fun saveListBale(balesData: List<PortPileBale>) {
        if (balesData.size > 0) {
            balesData.forEach {
                if (vm.baleHashMap.containsKey(it.grade)) {
                    vm.baleHashMap[it.grade.toString()] =
                        balesData.filter { it1 -> it1.grade.equals(it.grade) }
                            .filter { it1 -> it1.storageLocationTo.equals(storageId) }
                } else {
                    vm.baleHashMap.put(
                        it.grade.toString(),
                        balesData.filter { it1 -> it1.grade.equals(it.grade) }.filter { it1 ->
                            it1.storageLocationTo.equals(
                                storageId
                            )
                        })
                }
            }
            setUpAdapter(vm.baleHashMap)
        } else {
            setUpAdapter(HashMap<String, List<PortPileBale>>())
        }
    }

    private fun setUpAdapter(baleHashMap: HashMap<String, List<PortPileBale>>) {
        etEnterContainer.text.clear()
        if (baleHashMap.size > 0) {
            rvBales.visible()
            tvNoSummary.gone()
        } else {
            rvBales.gone()
            tvNoSummary.visible()
        }
        mAdapter.updateData(baleHashMap)
        enableProceedBtn(baleHashMap)
        vm.mCurrentBaleList = baleHashMap
    }

    private fun enableProceedBtn(baleHashMap: HashMap<String, List<PortPileBale>>) {

        if (baleHashMap.size > 0) {
            btnProceed.isEnabled = true
            ViewCompat.setBackgroundTintList(
                btnProceed,
                context?.let { ContextCompat.getColorStateList(it, R.color.green) }
            )
        } else {
            btnProceed.isEnabled = false
            ViewCompat.setBackgroundTintList(
                btnProceed,
                context?.let { ContextCompat.getColorStateList(it, R.color.grey) }
            )
        }
    }

    private fun getBaleByStorage(locationId: String) {
        vm.baleHashMap.clear()
        vm.getBaleListByStorageId(locationId)


    }

    private fun setBaleAdapter() {

        mAdapter = PortPileAddBaleAdapter { startBaleDetail(it) }
        rvBales.layoutManager =
            LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        rvBales.adapter = mAdapter
    }

    private fun saveBaleInDb(bale: PortPileBale) {
        /*runBlocking {
            withContext(Dispatchers.IO) {
                vm.insertBaleToStorage(bale)
            }
        }*/
    }

    private fun startBaleDetail(baleList: List<PortPileBale>?) {
        val intent = Intent(requireContext(), PortPileBaleInfoActivity::class.java)
        intent.putParcelableArrayListExtra(PILE_LIST, baleList as ArrayList<PortPileBale>)
        startActivityForResult(intent, SELECTED_GRADE_ITEM)
    }

    private fun setAddButtonColor(flag: Boolean) {
        val color =
            if (flag) resources.getColor(R.color.green) else resources.getColor(R.color.grey)
        btAdd.setBackgroundColor(color)
    }

    private fun moveToScanPage() {
        isFromScan = true
        startActivityForResult(
            Intent(activity, ScannerActivity::class.java), ACTIVITY_REQUEST_CODE
        )
    }

    private fun validateScannedData(baleId: String) {
        val it = runBlocking {
            withContext(Dispatchers.IO) {
                vm.isAlreadyExistBale(baleId)
            }
        }

        mBaleId = baleId
        val isValid = vm.validateBaleId(mBaleId)
        when {
            isValid && it > 0 -> showDialog(getString(R.string.bale_already_mapped_pile))
            isValid -> makeBaleValidationCall(mBaleId)
            else -> showDialog(getString(R.string.error_bale_id))
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when {
            requestCode == ACTIVITY_REQUEST_CODE && resultCode == Activity.RESULT_OK -> {
                isFromScan = false
                data?.extras?.getString(Constants.SCANNED_ID)?.trim()
                    ?.let { validateScannedData(it) }
            }
            requestCode == SELECTED_GRADE_ITEM && resultCode == Activity.RESULT_OK -> {
                val grade = data?.extras?.getString(SELECTED_GRADE)
                data?.extras?.getParcelableArrayList<PortPileBale>(SELECTED_BALE)?.let {
                    var baleList = arrayListOf<PortPileBale>()
                    /*val dat = vm.baleHashMap[it.grade]
                    dat?.let { it1 -> baleList.addAll(it1) }
                    val bale =
                        vm.baleHashMap[it.grade]?.find { it1 -> it1.baleID.equals(it.baleID) }
                    baleList.remove(bale)*/
                    baleList = it
                    vm.baleHashMap[grade.toString()] = baleList
                    mAdapter.notifyDataSetChanged()
                }
            }
        }
    }

    fun spinnerClick(items: ArrayList<String>) {
        customDialog = CustomFilterDialog(items, context!!, this)
        customDialog?.show()
        customDialog?.setCanceledOnTouchOutside(false)
    }

    private var customDialog: CustomFilterDialog? = null
    override fun clickOnItem(data: Int, isWh: Boolean) {
        sp_pile.text =
            storageData[data].storageLocationCode + " - " + storageData[data].classification
        storageId = storageData[data].storageLocationCode
        getBaleByStorage(storageData[data].storageLocationCode)
        vm.mCurrentPiles = storageData[data]
        if (customDialog != null) {
            customDialog!!.dismiss()
        }
    }
}
