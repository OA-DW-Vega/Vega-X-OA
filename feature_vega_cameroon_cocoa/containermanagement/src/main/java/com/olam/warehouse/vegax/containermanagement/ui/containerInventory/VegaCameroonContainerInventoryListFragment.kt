package com.olam.warehouse.vegax.containermanagement.ui.containerInventory

import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.view.*
import android.widget.Button
import android.widget.CheckBox
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.olam.warehouse.login.utils.showErrorDialogWithFAQLink
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.AppUtils
import com.olam.warehouse.presentation.utils.UIUtils.getActionBtnChangedView
import com.olam.warehouse.presentation.utils.extension.gone
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.containermanagement.R
import com.olam.warehouse.vegax.containermanagement.data.domain.model.ContainerInventory
import com.olam.warehouse.vegax.containermanagement.data.domain.model.VegaCameroonContainerInventoryModel
import com.olam.warehouse.vegax.containermanagement.databinding.FragmentVegaCameroonContainerInventoryListBinding
import com.olam.warehouse.vegax.containermanagement.ui.VegaCameroonContainerManagementViewModel
import com.olam.warehouse.vegax.containermanagement.utils.CONTAINER_DETAILS
import com.olam.warehouse.vegax.containermanagement.utils.getColor
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper

class VegaCameroonContainerInventoryListFragment : BaseFragment() {

    private lateinit var binding: FragmentVegaCameroonContainerInventoryListBinding
    private val vm: VegaCameroonContainerManagementViewModel by viewModel()
    override val layoutResourceId = R.layout.fragment_vega_cameroon_container_inventory_list
    private var callBack: CallBack? = null
    private var isDescends: Boolean = true
    private lateinit var alertDialog: AlertDialog
    private lateinit var mListener: OnContainerDetailsNavigationListener
    private var containerInventoryList = ArrayList<ContainerInventory>()
    private var offloading = mutableListOf<ContainerInventory>()
    private val mSearchList: MutableList<ContainerInventory> = mutableListOf()
    private var selectedFilter: ArrayList<String>? = null
    var filterAppliedData = ArrayList<ContainerInventory>()
    private var fullFilter = ArrayList<String>()


    private var mAdapter = VegaCameroonContainerInventoryAdapter {
        if (it != null) {
            moveToContainerdetail(it)
        }
    }

    var containerStatusFilterList = ArrayList<String>()
    var containerSizeFilterList = ArrayList<String>()
    var containerLifeFilterList = ArrayList<String>()

    interface CallBack {
        fun replaceFragment(moveFrag: String, bundle: Bundle)
        fun replaceFragment(moveFrag: String, bundle: Bundle, fullFilter: ArrayList<String>)
        fun filterList(list: ArrayList<String>) {}
    }

    interface OnContainerDetailsNavigationListener {
        fun navigateToDetails(bundle: Bundle)
    }

    companion object {
        fun newInstance() = VegaCameroonContainerInventoryListFragment().apply {}
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? CallBack
        mListener = context as OnContainerDetailsNavigationListener
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        binding = FragmentVegaCameroonContainerInventoryListBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUI()
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track()
            .screen("containermanagement/ui/containerInventory/VegaCameroonContainerInventoryListFragment")
            .title("Vega_Cameroon/Containermanagement").with(tracker)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        activity?.menuInflater?.inflate(R.menu.search_vega_cameroon_container_menu, menu)
        try {
            val search = menu.findItem(R.id.search)
            val searchView: SearchView =
                search?.actionView as SearchView
            searchView.setBackgroundColor(getColor(com.olam.warehouse.presentation.R.color.colorPrimaryOfi))
            searchView.queryHint = getString(com.olam.warehouse.presentation.R.string.search_container_id)
            searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    return false
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    newText.let {
                        if (newText?.isEmpty() == true) {
                            mAdapter.addItems(containerInventoryList)
                        } else {
                            mSearchList.clear()
                            containerInventoryList.forEach { containerItem ->
                                newText?.let { text ->
                                    if (containerItem.containerNum.contains(text)) {
                                        mSearchList.add(containerItem)
                                    }
                                }
                            }
                            mAdapter.addItems(mSearchList)
                        }
                    }
                    return true
                }
            })
        } catch (e: ClassCastException) {
            e.printStackTrace()
        }
    }

    private fun initUI() {
        context?.let {
            getActionBtnChangedView(binding.tvTruckNo, it, false)
            getActionBtnChangedView(binding.tvSearchLot, it, false)
        }
        binding.rvContainerId.layoutManager = LinearLayoutManager(this.context)
        binding.rvContainerId.adapter = mAdapter

        binding.tvSearchLot.gone()
        binding.tvSort.setOnClickListener {


            isDescends = !isDescends
            sortByWeight(containerInventoryList)
        }
        binding.tvFilter.setOnClickListener {
        }

        binding.tvFilter.setOnClickListener {
            showFilterDialog()
        }

        if (AppUtils.isOnline()) {
            vm.inventory.observe(viewLifecycleOwner, Observer { updateUIWithOnlineData(it) })
            vm.getContainerInventory("All")
        }
        binding.tvTotalCount.text = "Total No : "

    }

    fun showFilterDialog() {
        val inflater: LayoutInflater = this.layoutInflater
        var isSelected = false
        val dialogView: View = inflater.inflate(R.layout.item_vega_cameroon_container_filter, null)
        val tvCompleted = dialogView.findViewById<TextView>(R.id.tvCompleted)
        val tvNewContainer = dialogView.findViewById<TextView>(R.id.tvNewContainer)
        val cbMaterial = dialogView.findViewById<CheckBox>(R.id.cbMaterial)
        val btFilter: Button = dialogView.findViewById(R.id.btFilter)
        selectedFilter = ArrayList()


        tvCompleted.setOnClickListener {
            selectedFilter?.clear()
            selectedFilter?.add("Stuffing in Progress")
            isSelected = !isSelected
            backgroundColor(tvCompleted, isSelected)
        }
        tvNewContainer.setOnClickListener {
            selectedFilter?.clear()
            selectedFilter?.add("New Container")
            isSelected = !isSelected
            backgroundColor(tvNewContainer, isSelected)
        }
        cbMaterial.setOnCheckedChangeListener { buttonView, isChecked ->
            selectedFilter?.clear()
            if (isChecked) {
                selectedFilter?.add("Stuffing in Progress")
                selectedFilter?.add("New Container")
                isSelected = true
            } else {
                isSelected = false
            }
            backgroundColor(tvCompleted, isSelected)
            backgroundColor(tvNewContainer, isSelected)
        }
        btFilter.setOnClickListener {
            alertDialog.dismiss()
            applyFilter()
        }
        val dialogBuilder: AlertDialog.Builder = AlertDialog.Builder(requireContext())
        dialogBuilder.setOnDismissListener(object : DialogInterface.OnDismissListener {
            override fun onDismiss(arg0: DialogInterface) {
            }
        })
        dialogBuilder.setView(dialogView)
        alertDialog = dialogBuilder.create()
        alertDialog.show()
    }

    private fun applyFilter() {
        val filterData = ArrayList<ContainerInventory>()
        fullFilter.clear()
        filterAppliedData.clear()

        selectedFilter?.let { fullFilter.addAll(it) }

        containerInventoryList.forEach { container ->
            selectedFilter?.forEach { item ->
                when {
                    item.contains(container.status) -> {
                        filterData.add(container)
                    }
                }
            }
        }
        containerInventoryList.forEach { container ->
            if (filterData.size == 0) return@forEach
            val status = container.status

            if (selectedFilter?.size == 0 || filterData.map { it.status }.contains(status)) {
                filterAppliedData.add(container)
            }
        }
        val result = HashSet<ContainerInventory>()
        result.addAll(filterAppliedData)
        filterAppliedData = result.toMutableList() as ArrayList<ContainerInventory>
        mAdapter.addItems(if (fullFilter.size == 0) containerInventoryList else filterAppliedData)
    }

    private fun backgroundColor(text: TextView?, selected: Boolean) {
        ViewCompat.setBackground(
            text!!,
            ContextCompat.getDrawable(
                text.context,
                if (selected) com.olam.warehouse.presentation.R.drawable.item_selector_green
                else
                    com.olam.warehouse.presentation.R.drawable.item_deselected_white
            )
        )
        text.setTextColor(
            ContextCompat.getColor(
                text.context,
                if (selected) com.olam.warehouse.presentation.R.color.white
                else
                    com.olam.warehouse.presentation.R.color.black
            )
        )
    }


    private fun sortByWeight(list: List<ContainerInventory>) {
        val sortedList: List<ContainerInventory>
        if (isDescends) {
            sortedList = list
        } else {
            sortedList = list.reversed()
        }
        mAdapter.addItems(sortedList)
    }

    private fun updateUIWithOnlineData(data: Resource<GenericReqAndResp<VegaCameroonContainerInventoryModel>>) {
        data.let {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    hideLoading()
                    when (it.data?.success) {
                        true -> {
                            if (it.data?.data?.containerDTOs?.size ?: 0 > 0) {
                                containerInventoryList =
                                    it.data?.data?.containerDTOs?.filter {
                                        !it.status.equals("Deleted") && !it.status.equals(
                                            "Completed"
                                        )
                                    } as ArrayList<ContainerInventory>
                                binding.tvTotalCount.text =
                                    "Total No : ".plus(containerInventoryList.size.toString())


                                if (containerInventoryList.size > 0) {
                                    binding.rvContainerId.visibility = View.VISIBLE
                                    binding.tvNoData.visibility = View.GONE
                                    mAdapter.addItems(containerInventoryList)
                                } else {
                                    binding.rvContainerId.visibility = View.GONE
                                    binding.tvNoData.visibility = View.VISIBLE
                                }
                            } else {
                                binding.rvContainerId.visibility = View.GONE
                                binding.tvNoData.visibility = View.VISIBLE
                            }
                        }

                        else -> showErrorDialogWithFAQLink(requireContext(), "${it.data?.message}")

                    }

                }
                Resource.Status.LOADING -> {
                    showLoading()
                }
                Resource.Status.ERROR -> {
                    showErrorDialogWithFAQLink(requireContext(), it.error.toString())
                    hideLoading()
                }
                else -> {

                }

            }
        }
    }

    private fun moveToContainerdetail(containerDetails: ContainerInventory) {
        val bundle = Bundle().apply {
            putSerializable(CONTAINER_DETAILS, containerDetails)
        }
        mListener.navigateToDetails(bundle)
    }

    private fun getAllFilterValues() {
        for (containerItem in containerInventoryList) {
            for (status in containerItem.status) {
                containerStatusFilterList.add(status.toString())
            }
        }
        for (containerItem in containerInventoryList) {
            for (size in containerItem.containerSize) {
                containerStatusFilterList.add(size.toString())
            }
        }
    }

    fun getBack() {
        vm.getContainerInventory("All")
    }

}
