package com.olam.warehouse.vegax.portwarehouse.ui.dispatch.mtntdispatch

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
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
import com.olam.warehouse.master.vegaCotton.vegaPortWareHouse.entity.MtnBale
import com.olam.warehouse.master.vegaCotton.vegaPortWareHouse.entity.MtnDispatchDelivery
import com.olam.warehouse.master.vegaCotton.vegaPortWareHouse.entity.MtnGrade
import com.olam.warehouse.master.vegaCotton.vegaPortWareHouse.model.DeliveryWithBales
import com.olam.warehouse.master.vegaCotton.vegaPortWareHouse.model.MtnDeliveryWithGrades
import com.olam.warehouse.presentation.adapter.setUpAdapter
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.DateUtils
import com.olam.warehouse.presentation.utils.PreferenceHelper
import com.olam.warehouse.presentation.utils.extension.*
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.portwarehouse.R
import com.olam.warehouse.vegax.portwarehouse.databinding.FragmentMtnDispatchAddBaleBinding
import com.olam.warehouse.vegax.portwarehouse.databinding.ItemMtnDispatchGradesBinding
import com.olam.warehouse.vegax.portwarehouse.ui.dispatch.MtnDispatchViewModel
import com.olam.warehouse.vegax.portwarehouse.utils.PortWHUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper

/**
 * Created by Baskaran Kannan on 3/30/2021.
 */
class MtnDispatchAddBaleFragment : BaseFragment() {

    private var callBack: CallBack? = null
    private var deliveryNo: String? = ""
    private lateinit var mAdapter: MtnDispatchAddBaleAdapter
    private var offlineList = arrayListOf<MtnDispatchDelivery>()
    private var grades = listOf<String>()

    private val vm: MtnDispatchViewModel by viewModel()


    override val layoutResourceId = R.layout.fragment_mtn_dispatch_add_bale
    private lateinit var binding: FragmentMtnDispatchAddBaleBinding

    companion object {
        fun newInstance(deliveryNo: String) = MtnDispatchAddBaleFragment().putArgs {
            putString(PortWHUtil.DELIVERY_NO, deliveryNo)
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
        binding = FragmentMtnDispatchAddBaleBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        intiExtras()
        initUI()
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track()
            .screen("portwarehouse/ui/dispatch/mtntdispatch/MtnDispatchAddBaleFragment")
            .title("Portwarehouse").with(tracker)
    }

    private fun intiExtras() {
        deliveryNo = arguments?.getString(PortWHUtil.DELIVERY_NO)
    }

    fun initUI() {

        mAdapter = MtnDispatchAddBaleAdapter { onViewBales(it) }
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

        binding.txtDate.text = DateUtils.fromMillisToTimeString(DateUtils.getCurrentTimeInMills())
        //tvEmpty.text = getString(R.string.currently_no_order_to_dispatch)
        binding.btnScantodispatch.setOnClickListener { moveToScannerActivity() }
        binding.btnProceed.setOnClickListener { moveToConfirmPage() }
        binding.llDispatchSummary.setOnClickListener {
            callBack?.replaceFragment(
                PortWHUtil.FRGA_DISPATCH_OFFLINE,
                vm.mCurrentDelivery.deliveryNumber
            )
        }

        if (PortWHUtil.isOnline() && deliveryNo!!.isEmpty()) fetchDeliveryDetails() else fetchOfflineDeliveryDetails()
        enableOffloneField()

        vm.deliveyDetails.observe(viewLifecycleOwner, Observer { updateDispatchUI(it) })
        vm.validateBale.observe(viewLifecycleOwner, Observer { updateBaleUI(it) })
    }

    private fun updateBaleUI(data: Resource<GenericReqAndResp<MtnBale>>?) {
        data?.let {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    hideLoading()
                    if (it.data?.data !== null && it.data?.data!!.baleID.isNotEmpty())
                        updateBaleStatus(it.data?.data)
                    else activity?.toast(it.data?.message.toString())
                }
                Resource.Status.LOADING -> {
                    showLoading()
                }
                Resource.Status.ERROR -> {
                    hideLoading()
                    showErrorDialogWithFAQLink(requireContext(), it.error.toString())
                }
                else -> {
                }
            }
        }
    }

    private fun fetchOfflineDeliveryDetails() {
        var deliveryList = emptyList<MtnDispatchDelivery>()
        runBlocking {
            withContext(Dispatchers.IO) {
                deliveryList = vm.fetchOfflineDeliveryList()
            }
        }
        updateUI(deliveryList)
    }

    private fun moveToConfirmPage() {
        if (vm.mCurrentBaleList.size > 0) {
            callBack?.replaceFragment(PortWHUtil.FRGA_DISPATCH_CONFIRM, vm.mCurrentDelivery.deliveryNumber)
        }
    }

    private fun fetchDeliveryDetails() {
        vm.fetchDeliveryDetails()
    }

    private fun updateDispatchUI(data: Resource<GenericReqAndResp<List<MtnDispatchDelivery>>>?) {
        data?.let {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    hideLoading()
                    updateUI(it.data?.data)
                }
                Resource.Status.LOADING -> {
                    showLoading()
                }
                Resource.Status.ERROR -> {
                    hideLoading()
                    showErrorDialogWithFAQLink(requireContext(), it.error.toString())
                }
                else -> {
                }
            }
        }
    }

    private fun updateUI(response: List<MtnDispatchDelivery>?) {
        response?.forEach { items ->
            items.gradeDTO.forEach { grade ->
                grade.deliveryNumber = items.deliveryNumber
                runBlocking {
                    withContext(Dispatchers.IO) {
                        vm.insertOrReplaceGrade(grade)
                    }
                }
            }
            val isDeliveryExist = runBlocking {
                withContext(Dispatchers.IO) {
                    vm.isDeliveryExist(items.deliveryNumber).isNotEmpty()
                }
            }
            if (isDeliveryExist) return@forEach
            items.userName = PreferenceHelper.get(Constants.USER_NAME, "")
            runBlocking {
                withContext(Dispatchers.IO) {
                    vm.insertOrReplaceDelivery(items)
                }
            }

        }
        val deliveryList = runBlocking {
            withContext(Dispatchers.IO) {
                vm.fetchOfflineDeliveryList()
            }
        }
        vm.setDelivery(deliveryList)
        updateSpinnerUI()
    }

    private fun updateSpinnerUI() {
        binding.spnOrdernumber.adapter =
            ArrayAdapter(
                requireContext(),
                com.olam.warehouse.presentation.R.layout.simple_spinner_item,
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

    private fun getDeliveryDetails(dispatchDelivery: MtnDispatchDelivery) {
        vm.baleHashMap.clear()
        vm.mBaleList.clear()
        vm.mCurrentDelivery = dispatchDelivery
        val containerNo =
            if (dispatchDelivery.containerNumber?.isEmpty()!!) "Not Available" else dispatchDelivery.containerNumber
        val truckNo =
            if (dispatchDelivery.truckNumber?.isEmpty()!!) "Not Available" else dispatchDelivery.truckNumber
        binding.txtContainerNumber.text = containerNo
        binding.txtTruckNumber.text = truckNo
        binding.txtWeight.text = dispatchDelivery.maxWeightAllowed.toString().plus(" ").plus("KG")
        var gradeData: MtnDeliveryWithGrades = MtnDeliveryWithGrades()
        var balesData: DeliveryWithBales = DeliveryWithBales()
        runBlocking {
            withContext(Dispatchers.IO) {
                gradeData = vm.getDeliveryWithGrades(dispatchDelivery.deliveryNumber)
                balesData = vm.getDeliveryWithBales(dispatchDelivery.deliveryNumber)
            }
        }

        if (gradeData != null && gradeData.grades != null) {
            grades = gradeData.grades.map { it.grade }
            /*txt_dispatchgrades.text =
            gradeData.grades.map { it.grade }.toString().replace("[", "").replace("]", "")*/
            if (gradeData.grades.size > 0) {
                binding.txtDispatchgradesnot.gone()
                binding.rvDispatchGrades.visible()
            } else {
                binding.txtDispatchgradesnot.visible()
                binding.rvDispatchGrades.gone()
            }
            setUpGradeAdapter(gradeData.grades)
        } else {
            binding.txtDispatchgradesnot.visible()
            binding.rvDispatchGrades.gone()
        }

        if (balesData != null && balesData.bales != null) {
            if (balesData.bales.size > 0) {
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
                            balesData.bales.filter { it1 -> it1.grade.equals(it.grade) }
                                .filter { it1 ->
                                    it1.deliveryNumber.equals(
                                        it.deliveryNumber
                                    )
                                }.distinct()
                        )
                    }
                }
                setUpAdapter(vm.baleHashMap)
            } else {
                setUpAdapter(HashMap<String, List<MtnBale>>())
            }
        }
        binding.btnAdd.setOnClickListener { validateBaleId(binding.editBaleID.text.toString()) }
        binding.editBaleID.onChange { changeButttonColor(it) }
    }

    private fun setUpGradeAdapter(grades: List<MtnGrade>) {
        binding.rvDispatchGrades.setUpAdapter(
            grades as MutableList<MtnGrade>,
            R.layout.item_mtn_dispatch_grades,
            ItemMtnDispatchGradesBinding::inflate,
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
            Intent(context, ScannerActivity::class.java), PortWHUtil.ACTIVITY_REQUEST_CODE
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when {
            requestCode == PortWHUtil.ACTIVITY_REQUEST_CODE && resultCode == Activity.RESULT_OK -> {
                data?.extras?.getString(Constants.SCANNED_ID)
                    ?.trim()?.let {
                        validateBaleId(it)
                    }
            }
            requestCode == PortWHUtil.SELECTED_GRADE_ITEM && resultCode == Activity.RESULT_OK -> {
                data?.extras?.getParcelable<MtnBale>(PortWHUtil.SELECTED_BALE)?.let {
                    val baleList = arrayListOf<MtnBale>()
                    val baleList1 = vm.baleHashMap[it.grade]
                    baleList1?.let { it1 -> baleList.addAll(it1) }
                    val bale =
                        vm.baleHashMap[it.grade]?.find { it1 -> it1.baleID.equals(it.baleID) }
                    baleList.remove(bale)
                    vm.mBaleList.remove(bale)
                    vm.baleHashMap[it.grade.toString()] = baleList
                    mAdapter.notifyDataSetChanged()
                    bale?.let { it1 -> deleteBale(it1) }
                }
            }
        }
    }

    private fun validateBaleId(baleId: String?) {
        baleId?.let {
            val isValid = vm.validateBaleId(baleId)
            when {
                isValid && vm.isAlreadyExistBale(baleId) -> activity?.toast(getString(R.string.bale_already_exist))
                isValid -> if (PortWHUtil.isOnline()) moveToBaleDetails(baleId) else moveToOfflineBale(baleId)
                else -> activity?.toast(getString(R.string.error_bale_id))
            }
        }
    }

    private fun moveToOfflineBale(baleId: String) {
        var baleData = MtnBale()
        runBlocking {
            withContext(Dispatchers.IO) {
                baleData = vm.getBaleDetails(baleId)
            }
        }
        if (baleData != null) {
            if (baleData.baleID.isEmpty()) activity?.toast(getString(R.string.bale_not_inventory))
            else if (!grades.contains(baleData.grade)) activity?.toast(getString(R.string.bale_out_grades))
            else if (grades.contains(baleData.grade)) updateBaleStatus(baleData)
        } else activity?.toast(getString(R.string.bale_not_inventory))
    }

    private fun moveToBaleDetails(baleId: String) {
        vm.validateBale(baleId, vm.mCurrentDelivery.deliveryNumber)

    }

    private fun updateBaleStatus(bale: MtnBale?) {
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
        setUpAdapter(vm.baleHashMap)
    }

    private fun saveBale(it: MtnBale) {
        it.isUsed = true
        it.deliveryNumber = vm.mCurrentDelivery.deliveryNumber
        runBlocking {
            withContext(Dispatchers.IO) {
                vm.saveBaleDetails(it)
            }
        }
    }

    private fun deleteBale(it: MtnBale) {
        it.isUsed = false
        it.deliveryNumber = ""
        runBlocking {
            withContext(Dispatchers.IO) {
                vm.saveBaleDetails(it)
            }
        }
    }

    private fun setUpAdapter(baleHashMap: HashMap<String, List<MtnBale>>) {
        binding.editBaleID.text.clear()
        if (baleHashMap.size > 0) binding.recyclerLinearlayout.visible() else binding.recyclerLinearlayout.gone()
        mAdapter.updateData(baleHashMap)
        enableProceedBtn(baleHashMap)
        vm.mCurrentBaleList = baleHashMap
    }

    private fun onViewBales(bales: List<MtnBale>) {
        val baleList = arrayListOf<MtnBale>()
        baleList.addAll(bales)
        val intent = Intent(requireContext(), MtnDispatchEditBaleActivity::class.java)
        intent.putExtra(PortWHUtil.DELIVERY_NO, vm.mCurrentDelivery.deliveryNumber)
        intent.putParcelableArrayListExtra(PortWHUtil.BALE_LIST, baleList)
        startActivityForResult(intent, PortWHUtil.SELECTED_GRADE_ITEM)
    }

    private fun enableProceedBtn(baleHashMap: HashMap<String, List<MtnBale>>) {

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

    private fun enableOffloneField() {
        runBlocking {
            withContext(Dispatchers.IO) {
                offlineList = vm.fetchOfflineDeliveryDetails() as ArrayList<MtnDispatchDelivery>
            }
        }
        if (offlineList.size > 0) binding.llDispatchSummary.visible() else binding.llDispatchSummary.gone()
    }
}
