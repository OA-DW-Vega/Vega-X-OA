package com.olam.warehouse.vegax.portwarehouse.ui.inventory

import android.annotation.SuppressLint
import android.app.Activity
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
import com.afollestad.materialdialogs.list.listItemsSingleChoice
import com.olam.warehouse.login.utils.showErrorDialogWithFAQLink
import com.olam.warehouse.master.vegaCotton.vegaPortWareHouse.entity.PortBale
import com.olam.warehouse.master.vegaCotton.vegaPortWareHouse.model.*
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.presentation.utils.extension.toast
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.portwarehouse.R
import com.olam.warehouse.vegax.portwarehouse.databinding.FragmentInventoryBaleListBinding
import com.olam.warehouse.vegax.portwarehouse.utils.PaginationScrollListener
import com.olam.warehouse.vegax.portwarehouse.utils.PortWHUtil
import com.olam.warehouse.vegax.portwarehouse.utils.PortWHUtil.CROP_YEARS
import com.olam.warehouse.vegax.portwarehouse.utils.PortWHUtil.FILTER_BALE
import com.olam.warehouse.vegax.portwarehouse.utils.PortWHUtil.FILTER_GRADE
import com.olam.warehouse.vegax.portwarehouse.utils.PortWHUtil.FILTER_MARK
import com.olam.warehouse.vegax.portwarehouse.utils.PortWHUtil.FILTER_PILES
import com.olam.warehouse.vegax.portwarehouse.utils.PortWHUtil.FILTER_YEAR
import com.olam.warehouse.vegax.portwarehouse.utils.PortWHUtil.GRADE
import com.olam.warehouse.vegax.portwarehouse.utils.PortWHUtil.GRADES
import com.olam.warehouse.vegax.portwarehouse.utils.PortWHUtil.MARK_LIST
import com.olam.warehouse.vegax.portwarehouse.utils.PortWHUtil.PILE_LIST
import com.olam.warehouse.vegax.portwarehouse.utils.PortWHUtil.SELECTED_GRADE
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.emptyParametersHolder
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper


class InventoryBaleListFragment : BaseFragment() {

    private val mAdapter = InventoryAdapter { startBaleDetail(it) }
    private var callBack: CallBack? = null
    private var mSortOption = 0
    private var baleNo = ""
    private var baleTypeList = ArrayList<String>()
    private var gradeList = ArrayList<String>()
    private var fullGradeList = ArrayList<BaleGrade>()
    private var mBaleTypeList = ArrayList<String>()
    private var mGradeList = ArrayList<String>()
    private var mMarkList = ArrayList<BaleMark>()
    private var mYearList = ArrayList<CropYears>()
    private var mPileList = ArrayList<PileModel>()
    private var pageNo = 0
    private var pageSize = 1000
    private var isLastPage: Boolean = false
    private var isLoadingPage: Boolean = false
    private var isSearched: Boolean = false
    private var isFilter: Boolean = false


    interface CallBack {
        fun replaceFragment(
            moveFrag: String,
            grades: ArrayList<String>
        )
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? CallBack
    }

    companion object {
        fun newInstance(
            grades: ArrayList<String>,
            grade: MutableList<BaleGrade>,
            mMarkList: ArrayList<BaleMark>,
            mYearList: ArrayList<CropYears>,
            mPileList: ArrayList<PileModel>
        ) = InventoryBaleListFragment().putArgs {
            putStringArrayList(SELECTED_GRADE, grades)
            putParcelableArrayList(GRADES, grade as ArrayList<BaleGrade>)
            putParcelableArrayList(MARK_LIST, mMarkList)
            putParcelableArrayList(CROP_YEARS, mYearList)
            putParcelableArrayList(PILE_LIST, mPileList)
        }

        const val SEARCH_HINT_BALE_TEXT = "Search Bale"
        const val BALE_DETAIL_REQUEST = 10
    }

    private val vm: InventoryGradeViewModel by viewModel { emptyParametersHolder() }

    override val layoutResourceId = R.layout.fragment_inventory_bale_list
    private lateinit var binding: FragmentInventoryBaleListBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentInventoryBaleListBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)
        initExtra()
        initUI()
        getInventoryList()
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("portwarehouse/ui/inventory/InventoryBaleListFragment")
            .title("Portwarehouse").with(tracker)
    }

    private fun startBaleDetail(bale: PortBale?) {
        val intent = Intent(context, BaleDetailActivity::class.java)
        intent.putExtra(PortWHUtil.INVENTORY_BALE, bale as Parcelable)
        intent.putExtra(PortWHUtil.BALE_STATUS, 0)
        startActivityForResult(intent, BALE_DETAIL_REQUEST)
    }

    private fun initExtra() {
        gradeList = arguments?.getStringArrayList(SELECTED_GRADE) as ArrayList<String>
        fullGradeList = arguments?.getParcelableArrayList<BaleGrade>(GRADES) as ArrayList<BaleGrade>
        mMarkList = arguments?.getParcelableArrayList<BaleMark>(MARK_LIST) as ArrayList<BaleMark>
        mYearList = arguments?.getParcelableArrayList<CropYears>(CROP_YEARS) as ArrayList<CropYears>
        mPileList = arguments?.getParcelableArrayList<PileModel>(PILE_LIST) as ArrayList<PileModel>
    }

    private fun initUI() {
        val llManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        binding.rvInventory.layoutManager = llManager
        binding.rvInventory.adapter = mAdapter
        binding.rvInventory.addOnScrollListener(object :
            PaginationScrollListener(llManager, pageSize) {
            override fun isLastPage(): Boolean {
                return isLastPage
            }

            override fun isLoading(): Boolean {
                return isLoadingPage
            }

            override fun loadMoreItems() {
                isLoadingPage = true
                pageNo += 1
                //you have to call load more items to get more data
                getInventoryBaleItem()
            }
        })



        binding.rgSort.setOnCheckedChangeListener { group, checkedId ->
            hideSortView()
            when (checkedId) {
                R.id.rbFirstIn -> context?.toast("First in")
                R.id.rbLastIn -> context?.toast("Last in")
            }
        }
        binding.llSortBy.setOnClickListener { //showSortView()
            showSortDialog()
            //sortByTabVisible()
        }

        binding.llCurrent.setOnClickListener {
            getInventoryList()
        }
        binding.sortBackground.setOnClickListener { hideSortView() }
        binding.llFilterBy.setOnClickListener {
            moveToFilterActivity()
            //hideSortView()
        }

        vm.fetchInventoryBaleList
            .observe(viewLifecycleOwner, Observer {
                hideLoading()
                when (it.status) {
                    Resource.Status.SUCCESS -> {
                        it.data?.let {
                            it.data.let {
                                bindOTList(
                                    it.inventoryBaleListDTO,
                                    it.baleCount
                                )
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

    private fun getInventoryList() {
        hideSortView()
        //currentTabVisible()
        mAdapter.removeItems()
        pageNo = 0
        baleNo = ""
        baleTypeList = ArrayList<String>()
        getInventoryBaleItem()
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

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        activity?.menuInflater?.inflate(R.menu.search_menu_port, menu)

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
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                newText.let {
                    if (newText.isEmpty() && isSearched) {
                        isSearched = false
                        mAdapter.removeItems()
                        baleNo = ""
                        pageNo = 0
                        baleTypeList = mBaleTypeList
                        gradeList = mGradeList
                        getInventoryBaleItem()
                    } else if (newText.length == 10) {
                        isSearched = true
                        mBaleTypeList = baleTypeList
                        mGradeList = gradeList
                        mAdapter.removeItems()
                        baleNo = newText
                        pageNo = 0
                        baleTypeList = ArrayList<String>()
                        gradeList = ArrayList<String>()
                        getInventoryBaleItem()
                    }
                }
                return true
            }
        })
    }


    private fun moveToFilterActivity() {
        isFilter = true
        val grades = fullGradeList.distinctBy { it.grade }.mapNotNull { it.grade }
        val mGrades = arrayListOf<Grade?>()
        grades.forEach {
            val grade = Grade()
            grade.grade = it
            mGrades.add(grade)
        }
        val intent = Intent(context, FilterActivity::class.java)
        intent.putParcelableArrayListExtra(
            GRADE,
            mGrades
        )
        intent.putStringArrayListExtra(SELECTED_GRADE, gradeList)
        intent.putParcelableArrayListExtra(MARK_LIST, mMarkList)
        intent.putParcelableArrayListExtra(CROP_YEARS, mYearList)
        intent.putParcelableArrayListExtra(PILE_LIST, mPileList)
        startActivityForResult(intent, Constants.ADD_TASK_REQUEST)
        //filterTabVisible()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == Constants.ADD_TASK_REQUEST) {
            if (resultCode == Activity.RESULT_OK) {
                isFilter = false
                hideSortView()
                //filterTabVisible()
                pageNo = 0
                baleNo = ""
                baleTypeList = data?.extras?.getStringArrayList(FILTER_BALE) as ArrayList<String>
                gradeList = data.extras?.getStringArrayList(FILTER_GRADE) as ArrayList<String>
                mMarkList =
                    data.extras?.getParcelableArrayList<BaleMark>(FILTER_MARK) as ArrayList<BaleMark>
                mYearList =
                    data.extras?.getParcelableArrayList<CropYears>(FILTER_YEAR) as ArrayList<CropYears>
                mPileList =
                    data.extras?.getParcelableArrayList<PileModel>(FILTER_PILES) as ArrayList<PileModel>
                mAdapter.removeItems()
                showLoading()
                baleTypeList =
                    mPileList.filter { it.isChecked }.map { it.classification } as ArrayList<String>
                val marks =
                    mMarkList.filter { it.isChecked }.map { item -> item.baleMark } as ArrayList<String>
                val years =
                    mYearList.filter { it.isChecked }.map { item -> item.cropYear } as ArrayList<String>
                vm.fetchInventoryBaleList(
                    baleNo,
                    baleTypeList,
                    gradeList,
                    pageNo,
                    pageSize,
                    marks,
                    years
                )
            }
        } else if (requestCode == BALE_DETAIL_REQUEST) {
            if (resultCode == Activity.RESULT_OK) {
                getInventoryList()
            }
        }
    }


    private fun getInventoryBaleItem() {
        val marks =
            mMarkList.filter { it.isChecked }.map { item -> item.baleMark } as ArrayList<String>
        val years =
            mYearList.filter { it.isChecked }.map { item -> item.cropYear } as ArrayList<String>
        baleTypeList =
            mPileList.filter { it.isChecked }.map { item -> item.classification } as ArrayList<String>
        showLoading()
        vm.fetchInventoryBaleList(baleNo, baleTypeList, gradeList, pageNo, pageSize, marks, years)

    }

    @SuppressLint("SetTextI18n")
    private fun bindOTList(it: List<PortBale>?, baleCount: Long?) {
        vm.setInventoryList(it)
        updateAdapter()
        binding.tvBaleCount.text = "Currently \n$baleCount bales"
    }

    private fun updateAdapter() {
        mAdapter.addItems(vm.mInventoryBaleList!!)

    }

    private fun showSortView() {
        binding.llSortContainer.visibility = View.VISIBLE
        binding.sortBackground.visibility = View.VISIBLE
    }

    private fun hideSortView() {
        binding.llSortContainer.visibility = View.GONE
        binding.sortBackground.visibility = View.GONE
    }

    private fun filterTabVisible() {
        binding.ivSortDown.visibility = View.INVISIBLE
        binding.ivCurrentDown.visibility = View.INVISIBLE
        binding.ivFilterDown.visibility = View.VISIBLE
        binding.llSortBy.setBackgroundColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.grey_light
            )
        )
        binding.llCurrent.setBackgroundColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.grey_light
            )
        )
        binding.tvSortBy.setTextColor(ContextCompat.getColor(requireContext(), R.color.dark_Grey))
        binding.tvBaleCount.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.dark_Grey
            )
        )
        binding.ivSortDownUp.setColorFilter(
            ContextCompat.getColor(
                requireContext(),
                R.color.dark_Grey
            )
        )
        binding.tvFilter.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
        binding.ivFilter.setColorFilter(ContextCompat.getColor(requireContext(), R.color.white))
        binding.llFilterBy.setBackgroundColor(
            ContextCompat.getColor(
                requireContext(),
                com.olam.warehouse.presentation.R.color.colorPrimaryOfi
            )
        )
        binding.ivFilterDown.setColorFilter(
            ContextCompat.getColor(
                requireContext(),
                com.olam.warehouse.presentation.R.color.colorPrimaryOfi
            )
        )
    }

    private fun currentTabVisible() {
        binding.ivSortDown.visibility = View.INVISIBLE
        binding.ivCurrentDown.visibility = View.VISIBLE
        binding.ivFilterDown.visibility = View.INVISIBLE
        binding.llCurrent.setBackgroundColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.orange_light
            )
        )
        binding.ivSortDownUp.setColorFilter(
            ContextCompat.getColor(
                requireContext(),
                R.color.dark_Grey
            )
        )
        binding.llSortBy.setBackgroundColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.grey_light
            )
        )
        binding.llFilterBy.setBackgroundColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.grey_light
            )
        )
        binding.tvSortBy.setTextColor(ContextCompat.getColor(requireContext(), R.color.dark_Grey))
        binding.tvBaleCount.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
        binding.tvFilter.setTextColor(ContextCompat.getColor(requireContext(), R.color.dark_Grey))
        binding.ivFilter.setColorFilter(ContextCompat.getColor(requireContext(), R.color.dark_Grey))
    }

    private fun sortByTabVisible() {
        binding.ivSortDown.visibility = View.VISIBLE
        binding.ivCurrentDown.visibility = View.INVISIBLE
        binding.ivFilterDown.visibility = View.INVISIBLE
        binding.llSortBy.setBackgroundColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.orange_light
            )
        )
        binding.ivSortDown.setColorFilter(
            ContextCompat.getColor(
                requireContext(),
                R.color.dark_Grey
            )
        )
        binding.ivSortDownUp.setColorFilter(ContextCompat.getColor(requireContext(), R.color.white))
        binding.tvSortBy.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
        binding.tvBaleCount.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.dark_Grey
            )
        )
        binding.llCurrent.setBackgroundColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.grey_light
            )
        )
        binding.llFilterBy.setBackgroundColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.grey_light
            )
        )
        binding.tvFilter.setTextColor(ContextCompat.getColor(requireContext(), R.color.dark_Grey))
        binding.ivFilter.setColorFilter(ContextCompat.getColor(requireContext(), R.color.dark_Grey))
    }

}
