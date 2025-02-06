package com.olam.warehouse.vegax.ginningwarehouse.ui.ui.pile

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.InputFilter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.olam.warehouse.login.utils.showErrorDialogWithFAQLink
import com.olam.warehouse.master.ui.scanner.ScannerActivity
import com.olam.warehouse.master.vegaCotton.vegaGinningWareHouse.entity.GinningPileStorageLocationModel
import com.olam.warehouse.master.vegaCotton.vegaGinningWareHouse.entity.PileBale
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.ui.widget.CustomFilterDialog
import com.olam.warehouse.presentation.ui.widget.RecyclerViewItemClickListener
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.extension.gone
import com.olam.warehouse.presentation.utils.extension.onChange
import com.olam.warehouse.presentation.utils.extension.toast
import com.olam.warehouse.presentation.utils.extension.visible
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.ginningwarehouse.R
import com.olam.warehouse.vegax.ginningwarehouse.databinding.FragmentGiningPileMgntBinding
import com.olam.warehouse.vegax.ginningwarehouse.ui.utils.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper

class GinningPileAddBaleFragment : BaseFragment(), RecyclerViewItemClickListener {

    private var isFromScan = false
    private var callBack: CallBack? = null
    private var storageId: String = ""
    private var listPiles = arrayListOf<GinningPileStorageLocationModel>()
    override val layoutResourceId = R.layout.fragment_gining_pile_mgnt
    private val vm: GinningPileAddBaleViewModel by viewModel()
    private lateinit var mAdapter: GinningPileAddBaleAdapter
    private var storageData = ArrayList<GinningPileStorageLocationModel>()
    private var existedBales = mutableListOf<PileBale>()
    private lateinit var binding: FragmentGiningPileMgntBinding

    interface CallBack {
        fun replaceConfirmFragment(storageId: GinningPileStorageLocationModel)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? CallBack
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentGiningPileMgntBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        showLoading()
        vm.getStorageLocationList()
        vm.getPileLocations.observe(viewLifecycleOwner, Observer { response ->
            response?.let {
                when (it.status) {
                    Resource.Status.SUCCESS -> {
                        it.data?.let {
                            updatePileListUI(it.data)
                        }

                        hideLoading()
                    }
                    Resource.Status.LOADING -> showLoading()
                    Resource.Status.ERROR -> {
                        hideLoading()
                        showErrorDialogWithFAQLink(requireContext(), it.error.toString())
                    }
                }
            }

        })

        initUI()
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("ginningwarehouse/ui/pile/GinningPileAddBaleFragment")
            .title("Ginningwarehouse").with(tracker)

    }

    private fun updatePileListUI(data: List<GinningPileStorageLocationModel>?) {
        storageData.addAll(data!!)
        listPiles = data as ArrayList<GinningPileStorageLocationModel>
        val location =
            data.map { storage -> storage.storageLocationCode.toString() + " - " + storage.classification } as ArrayList
        //sp_pile.setTitle("Select Pile ID")
        storageId = data[0].storageLocationCode
        binding.spPile.setOnClickListener { spinnerClick(location) }
    }


    private fun initUI() {
        setBaleAdapter()
        binding.clScan.setOnClickListener { moveToScanPage() }
        binding.btnProceed.setOnClickListener { moveToConfirm() }
        binding.etEnterContainer.onChange { setAddButtonColor(vm.validateBaleId(it)) }
        binding.etEnterContainer.filters =
            arrayOf(InputFilter.AllCaps(), InputFilter.LengthFilter(11))
        binding.btAdd.setOnClickListener {
            val baleId = binding.etEnterContainer.text.toString().trim()
            validateScannedData(baleId)
        }
        vm.getExistedBaleList()



        vm.getExistedBaleList.observe(viewLifecycleOwner, Observer {
            existedBales = it.toMutableList()
        })

        vm.validateBale.observe(viewLifecycleOwner, Observer { response ->
            response?.let {
                when (it.status) {
                    Resource.Status.SUCCESS -> {
                        it.data?.let {
                            it.data.let { it1 -> it1.let { it2 -> saveListBaletoDB(it2) } }
                        }

                        hideLoading()
                    }
                    Resource.Status.LOADING -> showLoading()
                    Resource.Status.ERROR -> {
                        hideLoading()
                        showErrorDialogWithFAQLink(requireContext(), it.error.toString())
                    }
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

    private fun saveListBaletoDB(listBale: List<PileBale>) {
        listBale.forEach { it.storageLocationTo = vm.mCurrentPiles.storageLocationCode }

        vm.saveListBaletoDB(listBale)

        getBaleByStorage(vm.mCurrentPiles.storageLocationCode)
    }

    private fun saveListBale(balesData: List<PileBale>) {
        if (balesData.size > 0) {
            balesData.forEach {
                if (vm.mBaleHashMap.containsKey(it.grade)) {
                    vm.mBaleHashMap[it.grade.toString()] =
                        balesData.filter { it1 -> it1.grade.equals(it.grade) }
                            .filter { it1 -> it1.storageLocationTo.equals(storageId) }
                } else {
                    vm.mBaleHashMap.put(
                        it.grade.toString(),
                        balesData.filter { it1 -> it1.grade.equals(it.grade) }.filter { it1 ->
                            it1.storageLocationTo.equals(
                                storageId
                            )
                        })
                }
            }
            setUpAdapter(vm.mBaleHashMap)
        } else {
            setUpAdapter(HashMap<String, List<PileBale>>())
        }
    }

    private fun setUpAdapter(baleHashMap: HashMap<String, List<PileBale>>) {
        binding.etEnterContainer.text.clear()
        if (baleHashMap.size > 0) {
            binding.rvBales.visible()
            binding.tvNoSummary.gone()
        } else {
            binding.rvBales.gone()
            binding.tvNoSummary.visible()
        }
        mAdapter.updateData(baleHashMap)
        enableProceedBtn(baleHashMap)
        vm.mCurrentBaleList = baleHashMap
    }

    private fun enableProceedBtn(baleHashMap: HashMap<String, List<PileBale>>) {

        if (baleHashMap.size > 0) {
            binding.btnProceed.isEnabled = true
            ViewCompat.setBackgroundTintList(
                binding.btnProceed,
                context?.let {
                    ContextCompat.getColorStateList(
                        it,
                        com.olam.warehouse.presentation.R.color.colorPrimaryOfi
                    )
                }
            )
        } else {
            binding.btnProceed.isEnabled = false
            ViewCompat.setBackgroundTintList(
                binding.btnProceed,
                context?.let {
                    ContextCompat.getColorStateList(
                        it,
                        com.olam.warehouse.presentation.R.color.colorSecondaryGrey
                    )
                }
            )
        }
    }

    private fun getBaleByStorage(locationId: String) {
        vm.mBaleHashMap.clear()
        val list = runBlocking {
            withContext(Dispatchers.IO)
            {
                vm.getBaleListByStorageId(locationId)
            }
        }
        saveListBale(list)


    }

    private fun setBaleAdapter() {

        mAdapter = GinningPileAddBaleAdapter { startBaleDetail(it) }
        binding.rvBales.layoutManager =
            LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        binding.rvBales.adapter = mAdapter
    }

    private fun saveBaleInDb(bale: PileBale) {
        /* runBlocking {
             withContext(Dispatchers.IO) {
                 vm.insertBaleToStorage(bale)
             }
         }*/
    }

    private fun startBaleDetail(baleList: List<PileBale>?) {
        val intent = Intent(requireContext(), GinningPileBaleInfoActivity::class.java)
        intent.putParcelableArrayListExtra(PILE_LIST, baleList as ArrayList<PileBale>)
        startActivityForResult(intent, SELECTED_GRADE_ITEM)
    }

    private fun setAddButtonColor(flag: Boolean) {
        val color =
            if (flag) resources.getColor(com.olam.warehouse.presentation.R.color.colorPrimaryOfi) else resources.getColor(
                com.olam.warehouse.presentation.R.color.colorSecondaryGrey
            )
        binding.btAdd.setBackgroundColor(color)
    }

    private fun moveToScanPage() {
        isFromScan = true
        startActivityForResult(
            Intent(activity, ScannerActivity::class.java), ACTIVITY_REQUEST_CODE
        )
    }

    private fun validateScannedData(baleId: String) {

        val isValid = vm.validateBaleId(baleId)
        var isBaleExisted =!(existedBales.filter { it.baleID == baleId }.isEmpty())
        when {
            isValid && isBaleExisted-> activity?.toast(getString(R.string.bale_already_mapped_pile))
            isValid -> makeBaleValidationCall(baleId)
            else -> activity?.toast(getString(R.string.error_bale_id))
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
                data?.extras?.getParcelableArrayList<PileBale>(SELECTED_BALE)?.let {
                    var baleList = arrayListOf<PileBale>()
                    /*val dat = vm.mBaleHashMap[it.grade]
                    dat?.let { it1 -> baleList.addAll(it1) }
                    val bale =
                        vm.mBaleHashMap[it.grade]?.find { it1 -> it1.baleID.equals(it.baleID) }
                    baleList.remove(bale)*/
                    baleList = it
                    vm.mBaleHashMap[grade.toString()] = baleList
                    mAdapter.notifyDataSetChanged()
                }
            }
        }
    }

    fun applyFilterValues(bale: PileBale) {
        val data = vm.mCurrentBaleList
        val item = data[bale.grade] as ArrayList
        item.remove(bale)
        data.put(bale.grade.toString(), item)
        mAdapter.updateData(data)
    }

    fun spinnerClick(items: ArrayList<String>) {
        customDialog = CustomFilterDialog(items, requireContext(), this)
        customDialog?.show()
        customDialog?.setCanceledOnTouchOutside(false)


    }



    private var customDialog: CustomFilterDialog? = null
    override fun clickOnItem(data: Int, isWh: Boolean) {
        binding.spPile.text =
            storageData[data].storageLocationCode + " - " + storageData[data].classification
        storageId = storageData[data].storageLocationCode
        getBaleByStorage(storageData[data].storageLocationCode)
        vm.mCurrentPiles = storageData[data]
        if (customDialog != null) {
            customDialog!!.dismiss()
        }
    }
}
