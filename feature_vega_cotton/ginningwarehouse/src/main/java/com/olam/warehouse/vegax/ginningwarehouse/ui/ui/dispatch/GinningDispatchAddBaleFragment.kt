package com.olam.warehouse.vegax.ginningwarehouse.ui.ui.dispatch

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.olam.warehouse.login.utils.showErrorDialogWithFAQLink
import com.olam.warehouse.master.ui.scanner.ScannerActivity
import com.olam.warehouse.master.vegaCotton.vegaGinningWareHouse.entity.Bale
import com.olam.warehouse.master.vegaCotton.vegaGinningWareHouse.entity.Grade
import com.olam.warehouse.master.vegaCotton.vegaGinningWareHouse.entity.VegaCottonGinningDispatchDelivery
import com.olam.warehouse.presentation.adapter.setUpAdapter
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.Constants.SCANNED_ID
import com.olam.warehouse.presentation.utils.DateUtils
import com.olam.warehouse.presentation.utils.DateUtils.getCurrentTimeInMills
import com.olam.warehouse.presentation.utils.PreferenceHelper
import com.olam.warehouse.presentation.utils.extension.*
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.ginningwarehouse.R
import com.olam.warehouse.vegax.ginningwarehouse.databinding.FragmentGinningDispatchAddBaleBinding
import com.olam.warehouse.vegax.ginningwarehouse.databinding.ItemGinningDispatchGradesBinding
import com.olam.warehouse.vegax.ginningwarehouse.ui.utils.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper

/**
 * Created by Baskaran Kannan on 3/20/2020.
 */
class GinningDispatchAddBaleFragment : BaseFragment() {

    private var callBack: CallBack? = null
    private var deliveryNo: String? = ""
    private lateinit var
            mAdapter: GinningDispatchAddBaleAdapter
    private var offlineList = arrayListOf<VegaCottonGinningDispatchDelivery>()
    private var grades = listOf<String>()
    private var mBaleList = mutableListOf<Bale>()
    private var mBaleId: String = ""
    private var existedDeliveries = mutableListOf<VegaCottonGinningDispatchDelivery>()
    private val vm: GinningDispatchViewModel by viewModel()
    override val layoutResourceId = R.layout.fragment_ginning_dispatch_add_bale
    private lateinit var binding: FragmentGinningDispatchAddBaleBinding


    companion object {
        fun newInstance(deliveryNo: String) = GinningDispatchAddBaleFragment().putArgs {
            putString(DELIVERY_NO, deliveryNo)
        }
    }

    interface CallBack {
        fun replaceFragment(moveFrag: String, deliveryNo: String)
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
        binding = FragmentGinningDispatchAddBaleBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        intiExtras()
        initUI()
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("ginningwarehouse/ui/dispatch/GinningDispatchAddBaleFragment")
            .title("Ginningwarehouse").with(tracker)
    }

    private fun intiExtras() {
        deliveryNo = arguments?.getString(DELIVERY_NO)
    }

    fun initUI() {

        mAdapter = GinningDispatchAddBaleAdapter { onViewBales(it) }
        binding.lotdetailsRecyclerview.layoutManager =
            LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        binding.lotdetailsRecyclerview.adapter = mAdapter

        binding.spnOrdernumber.setSelection(vm.lastSelectedPosition)
        binding.spnOrdernumber.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(p0: AdapterView<*>?) {
                }

                override fun onItemSelected(
                    p0: AdapterView<*>?,
                    p1: View?,
                    position: Int,
                    p3: Long
                ) {
                    if (position > 0) {
                        getDeliveryDetails(vm.mDeliveryList[position - 1])
                    }
                }
            }

        binding.txtDate.text = DateUtils.fromMillisToTimeString(getCurrentTimeInMills())
        binding.tvEmptyContent.text = getString(R.string.currently_no_order_to_dispatch)
        binding.btnScantodispatch.setOnClickListener { moveToScannerActivity() }
        binding.btnProceed.setOnClickListener { moveToConfirmPage() }
        binding.llDispatchSummary.setOnClickListener {
            callBack?.replaceFragment(
                FRGA_DISPATCH_OFFLINE,
                vm.mCurrentDelivery.deliveryNumber
            )
        }
        vm.fetchExistedDeliverys()
        //if (isOnline() && deliveryNo!!.isEmpty()) fetchDeliveryDetails() else fetchOfflineDeliveryDetails()

        /*vm.getBaleDetailsByBaleIdOffline.observe(viewLifecycleOwner, androidx.lifecycle.Observer { baleData ->


                if (baleData != null && !baleData.deliveryNumber.equals("")) {
                    if(!baleData.deliveryNumber.equals(mBaleData.deliveryNumber)) {
                        activity?.toast(getString(R.string.dispatch_bale_already_exist) + " " + baleData.deliveryNumber)
                    }
                }
               else {
                    updateBaleStatus(mBaleData)
                }

            })*/
        vm.fetchBales.observe(viewLifecycleOwner, Observer {
            it?.let {
                mBaleList = it.toMutableList()
            }
        })
        vm.fetchBaleDetails()
        vm.validateBale.observe(viewLifecycleOwner, androidx.lifecycle.Observer { response ->
            response?.let {
                when (it.status) {
                    Resource.Status.SUCCESS -> {

                        it.data?.let {

                            if (it.data !== null && it.data.baleID.isNotEmpty()) {
                                getAndUpdateBaleDetails(mBaleId, it.data)

                            } else {
                                showErrorDialogWithFAQLink(requireContext(), it.message)
                            }
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
        vm.offlineDeliveryDetailsVegaCotton.observe(
            viewLifecycleOwner,
            androidx.lifecycle.Observer {
                updateUI(it)

            })
        vm.existedDeliverys.observeOnce(viewLifecycleOwner, androidx.lifecycle.Observer {

            existedDeliveries = it.toMutableList()
            if (isOnline() && deliveryNo!!.isEmpty()) fetchDeliveryDetails() else fetchOfflineDeliveryDetails()

        })
        vm.deliveryDetailsVegaCotton.observe(
            viewLifecycleOwner,
            androidx.lifecycle.Observer { response ->
                response?.let {
                    when (it.status) {
                        Resource.Status.SUCCESS -> {
                            it.data?.let {
                                updateUI(it.data)

                                it.data.forEach { items ->
                                    items.gradeDTO.forEach { grade ->
                                        grade.deliveryNumber = items.deliveryNumber
                                        vm.insertOrReplaceGrade(grade)
                                    }

                                    var existedDeliveriesByNum =
                                        existedDeliveries.filter { it.deliveryNumber == items.deliveryNumber }

                                    if (existedDeliveriesByNum.isNotEmpty()) return@forEach
                                    items.userName = PreferenceHelper.get(Constants.USER_NAME, "")
                                    vm.insertOrReplaceDelivery(items)
                                }
                                //dao.insertOrReplaceDeliverys(it)
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

        vm.deliveryWithBales.observe(viewLifecycleOwner, androidx.lifecycle.Observer { balesData ->
            if (balesData != null && balesData.bales.size > 0) {
                if (balesData.bales != null) {
                    vm.mBaleList.addAll(balesData.bales)
                    balesData.bales.forEach {
                        if (vm.baleHashMap.containsKey(it.grade)) {
                            vm.baleHashMap[it.grade.toString()] =
                                balesData.bales.filter { it1 -> it1.grade.equals(it.grade) }
                                    .filter { it1 -> it1.deliveryNumber.equals(it.deliveryNumber) }
                                    .distinct()
                        } else {
                            vm.baleHashMap.put(
                                it.grade.toString(),
                                balesData.bales.filter { it1 -> it1.grade.equals(it.grade) }.filter { it1 ->
                                    it1.deliveryNumber.equals(
                                        it.deliveryNumber
                                    )
                                }.distinct()
                            )

                        }
                    }
                    //edit_baleID.text.clear()
                    setUpAdapter(vm.baleHashMap)
                } else {
                    setUpAdapter(HashMap<String, List<Bale>>())
                }
            } else {
                setUpAdapter(HashMap<String, List<Bale>>())
            }

        })

        vm.deliveryWithGrades.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            if (it != null && it.grades != null) {
                grades = it.grades.map { it.grade }
                /*txt_dispatchgrades.text =
                gradeData.grades.map { it.grade }.toString().replace("[", "").replace("]", "")*/
                if (it.grades.size > 0) {
                    binding.txtDispatchgradesnot.gone()
                    binding.rvDispatchGrades.visible()
                } else {
                    binding.txtDispatchgradesnot.visible()
                    binding.rvDispatchGrades.gone()
                }
                setUpGradeAdapter(it.grades)
                Log.d("receivingdata", it.deliveryVegaCotton.deliveryNumber)
            } else {

            }
        })

        vm.fetchDeliveryDetailsOffline()
        vm.deliveryDetailsOfflineVegaCotton.observe(
            viewLifecycleOwner,
            androidx.lifecycle.Observer {

                offlineList = it as ArrayList<VegaCottonGinningDispatchDelivery>
                if (offlineList.size > 0) binding.llDispatchSummary.visible() else binding.llDispatchSummary.gone()

            })
    }

    private fun fetchOfflineDeliveryDetails() {
        vm.fetchOfflineDeliveryDetails()
    }

    private fun moveToConfirmPage() {
        if (vm.mCurrentBaleList.size > 0) {
            callBack?.replaceFragment(FRGA_DISPATCH_CONFIRM, vm.mCurrentDelivery.deliveryNumber)
        }
    }

    private fun fetchDeliveryDetails() {

        //vm.fetchExistedDeliverys()

        vm.fetchDeliveryDetails()

    }


    private fun updateUI(response: List<VegaCottonGinningDispatchDelivery>?) {
        vm.setDelivery(response)
        updateSpinnerUI()
    }

    private fun updateSpinnerUI() {
        binding.spnOrdernumber.adapter =
            ArrayAdapter(
                requireContext(),
                com.olam.warehouse.presentation.R.layout.support_simple_spinner_dropdown_item,
                vm.Deliverys
            )

        if (deliveryNo?.isNotEmpty()!!) {
            var postion: Int = 0
            vm.mDeliveryList.forEachIndexed { index, item ->
                if (item.deliveryNumber.equals(deliveryNo)) postion = index
            }
            binding.spnOrdernumber.setSelection(postion + 1)
        }
    }

    private fun getDeliveryDetails(dispatchDeliveryVegaCotton: VegaCottonGinningDispatchDelivery) {
        vm.baleHashMap.clear()
        vm.mBaleList.clear()
        vm.mCurrentDelivery = dispatchDeliveryVegaCotton
        val containerNo =
            if (dispatchDeliveryVegaCotton.containerNumber.isNullOrEmpty()) "Not Available" else dispatchDeliveryVegaCotton.containerNumber
        val truckNo =
            if (dispatchDeliveryVegaCotton.truckNumber.isNullOrEmpty()) "Not Available" else dispatchDeliveryVegaCotton.truckNumber
        binding.txtContainerNumber.text = containerNo
        binding.txtTruckNumber.text = truckNo
        binding.txtWeight.text =
            dispatchDeliveryVegaCotton.maxWeightAllowed.toString().plus(" ").plus("KG")
        vm.getDeliveryWithGrades(dispatchDeliveryVegaCotton.deliveryNumber)


        vm.getDeliveryWithBales(dispatchDeliveryVegaCotton.deliveryNumber)




        binding.btnAdd.setOnClickListener {
            validateBaleId(binding.editBaleID.text.toString())
        }
        binding.editBaleID.onChange { changeButttonColor(it) }
    }

    private fun setUpGradeAdapter(grades: List<Grade>) {
        binding.rvDispatchGrades.setUpAdapter(
            grades as MutableList<Grade>,
            R.layout.item_ginning_dispatch_grades,
            ItemGinningDispatchGradesBinding::inflate,
            { it, pos, bindingItem ->
                bindingItem.tvGradeName.text = it.grade
                bindingItem.tvGradeWeight.text = it.weight.plus(" ").plus("KG")
            })
    }

    private fun changeButttonColor(value: String) {
        when (value.length) {
            10 -> {
                binding.btnAdd.isEnabled = true
                ViewCompat.setBackgroundTintList(binding.btnAdd,
                    context?.let {
                        ContextCompat.getColorStateList(
                            it,
                            com.olam.warehouse.presentation.R.color.colorPrimaryOfi
                        )
                    })
            }
            else -> ViewCompat.setBackgroundTintList(
                binding.btnAdd,
                context?.let {
                    ContextCompat.getColorStateList(
                        it,
                        com.olam.warehouse.presentation.R.color.colorSecondaryGrey
                    )
                }
            )
        }
    }

    private fun moveToScannerActivity() {
        startActivityForResult(
            Intent(context, ScannerActivity::class.java), ACTIVITY_REQUEST_CODE
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when {
            requestCode == ACTIVITY_REQUEST_CODE && resultCode == Activity.RESULT_OK -> {
                 data?.extras?.getString(SCANNED_ID)
              ?.trim()?.let {
                         validateBaleId(it)
                     }

            }
            requestCode == SELECTED_GRADE_ITEM && resultCode == Activity.RESULT_OK -> {
                val grade = data?.extras?.getString(SELECTED_GRADE)
                data?.extras?.getParcelableArrayList<Bale>(SELECTED_BALE)?.let {
                    var baleList = arrayListOf<Bale>()
                    /*val dat = vm.baleHashMap[it.grade]
                    dat?.let { it1 -> baleList.addAll(it1) }
                    val bale =
                        vm.baleHashMap[it.grade]?.find { it1 -> it1.baleID.equals(it.baleID) }
                    baleList.remove(bale)*/
                    baleList = it
                    vm.mBaleList = baleList
                    vm.baleHashMap[grade.toString()] = baleList
                    mAdapter.notifyDataSetChanged()
                }
            }
        }
    }

    private fun validateBaleId(baleId: String?) {
        baleId?.let {
            val isValid = vm.validateBaleId(baleId)
            when {
                isValid && vm.isAlreadyExistBale(baleId) -> activity?.toast(getString(R.string.dispatch_bale_already_exist) + " " + vm.mCurrentDelivery.deliveryNumber)
                isValid -> {
                    binding.editBaleID.text.clear()
                    if (isOnline())
                        moveToBaleDetails(baleId)
                    else moveToOfflineBale(baleId)
                }
                else -> activity?.toast(getString(R.string.error_bale_id))
            }


        }
    }

    private fun moveToOfflineBale(baleId: String) {
        var baleData = Bale()
        /*runBlocking {
            withContext(Dispatchers.IO) {
                baleData = vm.getBaleDetailsByIdOffline(baleId)

            }
        }*/
        baleData = mBaleList.find { it.baleID.equals(baleId) } ?: Bale()
        if (baleData != null) {
            if (baleData.baleID.isEmpty()) showErrorDialogWithFAQLink(
                    requireContext(),
                    getString(R.string.bale_not_available)
            )
            else if (!grades.contains(baleData.grade)) showErrorDialogWithFAQLink(
                    requireContext(),
                    getString(R.string.bale_grade_is) + baleData.grade + getString(R.string.diff_grades)
            )
            else if (grades.contains(baleData.grade)) {
                if (baleData.deliveryNumber.isEmpty())
                    updateBaleStatus(baleData)
                else showErrorDialogWithFAQLink(
                    requireContext(),
                    getString(R.string.dispatch_bale_already_exist) + " " + baleData.deliveryNumber
                )
//                edit_baleID.text.clear()
            }
        } else {
            showErrorDialogWithFAQLink(requireContext(), getString(R.string.bale_not_available))
//            edit_baleID.text.clear()
        }

    }

    private fun moveToBaleDetails(baleId: String) {
        vm.validateBale(baleId, vm.mCurrentDelivery.deliveryNumber)
        mBaleId = baleId

    }

    private fun getAndUpdateBaleDetails(baleId: String, data: Bale) {
        var baleData = Bale()
        /* runBlocking {
             withContext(Dispatchers.IO) {
                 baleData = vm.getBaleDetailsByIdOffline(baleId)

             }
         }*/
        baleData = mBaleList.find { it.baleID.equals(baleId) } ?: Bale()
        if (baleData != null && !baleData.deliveryNumber.equals("")) {
            if (!baleData.deliveryNumber.equals(data.deliveryNumber)) {
                activity?.toast(getString(R.string.dispatch_bale_already_exist) + " " + baleData.deliveryNumber)
            }
        } else {
            updateBaleStatus(data)
        }


    }

    private fun updateBaleStatus(bale: Bale?) {
        bale?.let {
            saveBale(it)
            vm.mBaleList.add(it)
            if (vm.baleHashMap.containsKey(it.grade)) {
                vm.baleHashMap[it.grade.toString()] =
                    vm.mBaleList.filter { it1 -> it1.grade.equals(it.grade) }
                        .filter { it1 -> it1.deliveryNumber.equals(it.deliveryNumber) }.distinct()
            } else {
                vm.baleHashMap.put(
                    it.grade.toString(),
                    vm.mBaleList.filter { it1 -> it1.grade.equals(it.grade) }.filter { it1 ->
                        it1.deliveryNumber.equals(
                            it.deliveryNumber
                        )
                    }.distinct()
                )
            }
        }
//        edit_baleID.text.clear()
        setUpAdapter(vm.baleHashMap)
    }

    private fun saveBale(it: Bale) {
        it.isUsed = true
        it.deliveryNumber = vm.mCurrentDelivery.deliveryNumber
        vm.saveBale(it)
    }

    private fun deleteBale(it: Bale) {
        it.isUsed = false
        it.deliveryNumber = ""
        vm.saveBale(it)
    }

    private fun setUpAdapter(baleHashMap: HashMap<String, List<Bale>>) {
//        edit_baleID.text.clear()
        if (baleHashMap.size > 0) binding.recyclerLinearlayout.visible() else binding.recyclerLinearlayout.gone()
        mAdapter.updateData(baleHashMap)
        enableProceedBtn(baleHashMap)
        vm.mCurrentBaleList = baleHashMap
    }

    private fun onViewBales(bales: List<Bale>) {
        val baleList = arrayListOf<Bale>()
        baleList.addAll(bales)
        val intent = Intent(requireContext(), GinningDispatchEditBaleActivity::class.java)
        intent.putExtra(DELIVERY_NO, vm.mCurrentDelivery.deliveryNumber)
        intent.putParcelableArrayListExtra(BALE_LIST, baleList)
        startActivityForResult(intent, SELECTED_GRADE_ITEM)
    }

    private fun enableProceedBtn(baleHashMap: HashMap<String, List<Bale>>) {

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
}
