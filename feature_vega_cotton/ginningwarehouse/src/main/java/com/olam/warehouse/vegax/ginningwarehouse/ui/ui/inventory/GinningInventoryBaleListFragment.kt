package com.olam.warehouse.vegax.ginningwarehouse.ui.ui.inventory

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import android.view.*
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.list.listItemsMultiChoice
import com.afollestad.materialdialogs.list.listItemsSingleChoice
import com.olam.warehouse.login.utils.showErrorDialogWithFAQLink
import com.olam.warehouse.master.vegaCotton.vegaGinningWareHouse.entity.Bale
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.ginningwarehouse.R
import com.olam.warehouse.vegax.ginningwarehouse.databinding.ActivityGinningInventoryBinding
import com.olam.warehouse.vegax.ginningwarehouse.ui.data.model.BaleGrade
import com.olam.warehouse.vegax.ginningwarehouse.ui.utils.GRADES
import com.olam.warehouse.vegax.ginningwarehouse.ui.utils.INVENTORY_BALE
import com.olam.warehouse.vegax.ginningwarehouse.ui.utils.SELECTED_GRADE
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper


class GinningInventoryBaleListFragment : BaseFragment() {

    private val mAdapter = InventoryAdapter({ startBaleDetail(it) }, { updateBaleCount(it) })
    private var callBack: CallBack? = null

    private var baleNo = ""
    private var mBales = arrayListOf<Bale>()
    private var baleTypeList = ArrayList<String>()
    private var mBaleTypeList = ArrayList<String>()
    private var mGradeList = ArrayList<String>()
    private var mFullGradeList = ArrayList<BaleGrade>()
    private var mFilteredGrades = ArrayList<String>()
    private var mFilters = ArrayList<Int>()
    private var pageNo = 0
    private var isSearched: Boolean = false
    private var mSortOption = 0

    interface CallBack {
        /*fun replaceFragment(
            moveFrag: String,
            bale: Bale
        )*/
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? CallBack
    }

    companion object {
        fun newInstance(
            grades: ArrayList<String>,
            grade: MutableList<BaleGrade>
        ) = GinningInventoryBaleListFragment().putArgs {
            putStringArrayList(SELECTED_GRADE, grades)
            putParcelableArrayList(GRADES, grade as ArrayList<BaleGrade>)
        }

        const val SEARCH_HINT_BALE_TEXT = "Search Bale"
        const val BALE_DETAIL_REQUEST = 10
    }

    private val vm: InventoryViewModel by viewModel()

    override val layoutResourceId = R.layout.activity_ginning_inventory
    private lateinit var binding: ActivityGinningInventoryBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = ActivityGinningInventoryBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)
        initExtra()
        initUI()
        getInventoryList()
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("ginningwarehouse/ui/inventory/GinningInventoryBaleListFragment")
            .title("Ginningwarehouse").with(tracker)
    }

    private fun startBaleDetail(bale: Bale?) {
        val intent = Intent(context, GinningBaleDetailActivity::class.java)
        intent.putExtra(INVENTORY_BALE, bale as Parcelable)
        startActivity(intent)
    }

    private fun initExtra() {
        mGradeList = arguments?.getStringArrayList(SELECTED_GRADE) as ArrayList<String>
        mFullGradeList =
            arguments?.getParcelableArrayList<BaleGrade>(GRADES) as ArrayList<BaleGrade>
    }

    private fun initUI() {
        val llManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        binding.rvInventory.layoutManager = llManager
        binding.rvInventory.adapter = mAdapter
        binding.llSortBy.setOnClickListener {
            showSortDialog()
        }
        binding.llFilterBy.setOnClickListener {
            showFilterDialog()
        }

        vm.fetchInventoryBaleList .observe(viewLifecycleOwner, Observer {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    it.data?.let {
                        it.data.let {
                            bindOTList(it)
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

        })

    }

    private fun showFilterDialog() {
        val grades = mBales.distinctBy { it.grade }.mapNotNull { it.grade }
        val indexValues = arrayListOf<Int>()
        val indexes = grades.filterIndexed { index, grade ->
            if (mFilteredGrades.contains(grade)) indexValues.add(index) else true
        }
        context?.let {
            MaterialDialog(it).show {
                title(text = "Select Grades")
                listItemsMultiChoice(
                    items = grades,
                    initialSelection = indexValues.toIntArray()
                ) { dialog, indices, items ->
                    mFilters.clear()
                    indices.forEach { mFilters.add(it) }
                    mFilteredGrades.clear()
                    items.forEach { mFilteredGrades.add(it.toString()) }
                    val bales = mBales.filter { bale -> mFilteredGrades.contains(bale.grade) }
                    mAdapter.update(bales)
                }
                positiveButton(text = UIUtils.getSpannedText(getString(R.string.ok))) {
                    dismiss()
                }
            }
        }
    }

    private fun showSortDialog() {
        val sortOption = listOf("First in", "Last in")
        context?.let {
            MaterialDialog(it).show {
                title(text = getString(R.string.select_sorting_option))
                listItemsSingleChoice(
                    items = sortOption,
                    initialSelection = mSortOption
                ) { dialog, index, text ->
                    mSortOption = index
                }
                positiveButton(text = UIUtils.getSpannedText(getString(R.string.ok))) {
                    mAdapter.reverse()
                    dismiss()
                }
            }
        }
    }

    private fun getInventoryList() {
        hideSortView()
        currentTabVisible()
        mAdapter.removeItems()
        pageNo = 0
        baleNo = ""
        baleTypeList = ArrayList<String>()
        getInventoryBaleItem()
    }

    private fun updateBaleCount(it: Int) {
        binding.tvBaleCount.text = "Currently \n${it} bales"
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        activity?.menuInflater?.inflate(R.menu.ginning_search_menu, menu)
        val search = menu.findItem(R.id.search)
        val searchView: SearchView =
            search?.actionView as SearchView
        searchView.setBackgroundColor(
            ContextCompat.getColor(
                requireContext(),
                com.olam.warehouse.presentation.R.color.colorPrimaryOfi
            )
        )
        searchView.queryHint = SEARCH_HINT_BALE_TEXT
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                mAdapter.filter.filter(query)
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                mAdapter.filter.filter(newText)
                return false
            }
        })
    }

    private fun getInventoryBaleItem() {
        showLoading()
        vm.fetchInventoryBaleList()


    }



    @SuppressLint("SetTextI18n")
    private fun bindOTList(bales: List<Bale>?) {
        bales?.let {
            mBales.clear()
            mBales.addAll(bales)
            mFilteredGrades.clear()
            mFilteredGrades.addAll(mGradeList)
            val filterBales = it.filter { bale -> mGradeList.contains(bale.grade) }
            vm.setInventoryList(filterBales)
            updateAdapter()
            //tvBaleCount.text = "Currently \n${filterBales.size} bales"
        }
    }

    private fun updateAdapter() {
        vm.mInventoryBaleList?.let { mAdapter.addItems(it) }
    }

    private fun hideSortView() {
        binding.llSortContainer.visibility = View.GONE
        binding.sortBackground.visibility = View.GONE
    }

    private fun currentTabVisible() {
        binding.llCurrent.setBackgroundColor(
            ContextCompat.getColor(
                requireContext(),
                com.olam.warehouse.presentation.R.color.colorPrimaryOfi
            )
        )
       // binding.ivFilter.setColorFilter(ContextCompat.getColor(requireContext(), R.color.white))
        binding.tvBaleCount.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
    }

}
