package com.olam.warehouse.vegax.portwarehouse.ui.dispatch.directdispatch

import android.content.DialogInterface
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.olam.warehouse.login.ui.home.HomeBaseActivity
import com.olam.warehouse.master.vegaCotton.vegaPortWareHouse.entity.PortMtn
import com.olam.warehouse.master.vegaCotton.vegaPortWareHouse.entity.PortMtnBales
import com.olam.warehouse.presentation.data.domain.model.GenericMessage
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.utils.extension.toast
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.portwarehouse.R
import com.olam.warehouse.vegax.portwarehouse.databinding.ActivityDirectDispatchBinding
import com.olam.warehouse.vegax.portwarehouse.ui.dispatch.PortDispatchViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.emptyParametersHolder
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper

/**
 * Created by Baskaran Kannan on 4/1/2021.
 */
class DispatchDirectActivity : HomeBaseActivity() {
    private val vm: PortDispatchViewModel by viewModel { emptyParametersHolder() }
    override val layoutResourceId: Int =
        R.layout.activity_direct_dispatch
    private lateinit var binding: ActivityDirectDispatchBinding
    private var mtnWithGrade = ArrayList<PortMtn>()
    private lateinit var mIncomingMtnAdapter: DirectDispatchMtnAdapter
    private val mSearchList: MutableList<PortMtn> = mutableListOf()

    companion object {
        const val SEARCH_HINT_TEXT = "Search WB Item"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDirectDispatchBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initNavigationView()
        initUI()
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track()
            .screen("portwarehouse/ui/dispatch/directdispatch/DispatchDirectActivity")
            .title("Portwarehouse").with(tracker)
    }


    private fun initUI() {
        initAdapter()
        showLoading()
        vm.getMtns()
        vm.mtns.observe(this, Observer { updateDispatchUI(it) })
        vm.updateBales.observe(this, Observer { updateBalesUI(it) })
        binding.btnProceed.setOnClickListener { showDispatchConfirmDialog() }
    }

    private fun updateBalesUI(data: Resource<GenericReqAndResp<GenericMessage>>?) {
        data?.let {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    hideLoading()
                    toast(it.data?.data?.message.toString())
                    val selectedGrades = mtnWithGrade.filter { it.isChecked } as ArrayList
                    removeMtns(selectedGrades)
                }
                Resource.Status.LOADING -> {
                    showLoading()
                }
                Resource.Status.ERROR -> {
                    hideLoading()
                    toast(it.error.toString())
                }
            }
        }
    }

    private fun updateDispatchUI(data: Resource<GenericReqAndResp<List<PortMtn>>>?) {
        data?.let {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    hideLoading()
                    mtnWithGrade = it.data?.data as ArrayList<PortMtn>
                    mIncomingMtnAdapter.updateData(mtnWithGrade)
                    validateListSize()
                }
                Resource.Status.LOADING -> {
                    showLoading()
                }
                Resource.Status.ERROR -> {
                    hideLoading()
                    toast(it.error.toString())
                }
                else -> {
                }
            }
        }
    }

    private fun validateListSize() {
        if (mtnWithGrade.isEmpty()) {
            showEmptyView()
        } else {
            showMtnList()
        }
    }

    private fun initAdapter() {
        binding.rvMtns.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        mIncomingMtnAdapter = DirectDispatchMtnAdapter(
            ArrayList(),
            object : DirectDispatchMtnAdapter.UpdateMtnList {
                override fun update() {
                    onMtnCheck()
                }
            })
        binding.rvMtns.adapter = mIncomingMtnAdapter
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.search_menu_port, menu)

        val search = menu?.findItem(R.id.search)
        val searchView: SearchView =
            search?.actionView as SearchView
        searchView.setBackgroundColor(resources.getColor(R.color.green))
        searchView.queryHint = SEARCH_HINT_TEXT
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                newText.let {
                    if (newText?.isEmpty() == true) {
                        mIncomingMtnAdapter.updateData(mtnWithGrade)
                    } else {
                        filter(text = newText)
                    }
                }

                return true
            }
        })
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        item?.let {
            when (it.itemId) {
                android.R.id.home -> {
                    onBackPressed()
                }
            }
        }
        return false
    }

    private fun showMtnList() {
        binding.tvmtnEmpty.visibility = View.GONE
        binding.rvMtns.visibility = View.VISIBLE
    }

    private fun showEmptyView() {
        binding.tvmtnEmpty.visibility = View.VISIBLE
        binding.rvMtns.visibility = View.GONE
    }

    private fun disableButton() {
        binding.btnProceed.apply {
            setBackgroundColor(
                ContextCompat.getColor(context, R.color.light_grey)
            )
            isEnabled = false
        }
    }

    private fun enableButton() {
        binding.btnProceed.apply {
            setBackgroundColor(
                ContextCompat.getColor(context, R.color.green)
            )
            isEnabled = true
        }
    }

    private fun onMtnCheck() {
        val selectedGrades = mtnWithGrade.filter { it.isChecked }

        if (selectedGrades.isNotEmpty()) {
            enableButton()
        } else {
            disableButton()
        }
    }

    fun filter(text: String?) {
        mSearchList.clear()

        mtnWithGrade.forEach { mtn ->
            text?.let { text ->
                if (mtn.mtnNumber.contains(text)) {
                    mSearchList.add(mtn)
                }
            }
        }
        mIncomingMtnAdapter.updateData(mSearchList as ArrayList<PortMtn>)
    }

    private fun postBales() {
        val selectedGrades = mtnWithGrade.filter { it.isChecked } as ArrayList
        val selectedBales = ArrayList<PortMtnBales>()
        for (item in selectedGrades) {
            selectedBales.addAll(item.mtnBales as ArrayList)
        }
        showLoading()
        vm.updateBalesToDirectDispatch(selectedBales)

    }

    private fun removeMtns(list: ArrayList<PortMtn>) {
        val update = list.map { it.mtnNumber }
        mtnWithGrade.removeAll {
            it.mtnNumber in update
        }
        mIncomingMtnAdapter.updateData(mtnWithGrade)
        validateListSize()
    }

    private fun showDispatchConfirmDialog() {

        showDialog(resources.getString(R.string.confirm_direct_dispatch_mtn),
            object : DialogClick {
                override fun onPositive(dialog: DialogInterface) {
                    dialog.dismiss()
                    postBales()
                }

                override fun onNegative(dialog: DialogInterface) {
                    dialog.dismiss()
                }
            })
    }
}
