package com.olam.warehouse.vegax.ginningwarehouse.ui.ui.incominglots.incomingmtn

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.customview.getCustomView
import com.olam.warehouse.login.ui.home.HomeBaseActivity
import com.olam.warehouse.login.ui.succes.SuccessActivity
import com.olam.warehouse.master.vegaCotton.vegaGinningWareHouse.entity.Mtn
import com.olam.warehouse.master.vegaCotton.vegaGinningWareHouse.entity.MtnGrades
import com.olam.warehouse.master.vegaCotton.vegaGinningWareHouse.entity.MtnWithGrades
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.ginningwarehouse.R
import com.olam.warehouse.vegax.ginningwarehouse.databinding.GinningIncomingMtnActivityBinding
import com.olam.warehouse.vegax.ginningwarehouse.ui.utils.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper
import java.util.*

class GinningIncomingMtnActivity : HomeBaseActivity() {

    companion object {
        const val SEARCH_HINT_TEXT = "Search MTN"
    }

    override val layoutResourceId = R.layout.ginning_incoming_mtn_activity
    private lateinit var binding: GinningIncomingMtnActivityBinding
    private val vm: GinningIncomingMtnViewModel by viewModel()
    private lateinit var mIncomingMtnAdapter: GinningIncomingMtnAdapter
    var mtnsOffline = mutableListOf<Mtn>()
    var mtnGradesList= ArrayList<MtnGrades>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = GinningIncomingMtnActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // injectGinningIncomingLOTsIncomingMtnFeature()
        initNavigationView()
        initUI()
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("ginningwarehouse/ui/incomingmtn/GinningIncomingMtnActivity")
            .title("Ginningwarehouse").with(tracker)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.ginning_search_menu, menu)

        val search = menu.findItem(R.id.search)
        val searchView: SearchView =
            search?.actionView as SearchView
        searchView.setBackgroundColor(
            ContextCompat.getColor(
                this,
                com.olam.warehouse.presentation.R.color.colorPrimaryOfi
            )
        )
        searchView.queryHint = SEARCH_HINT_TEXT
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                newText.let {
                    if (newText?.isEmpty() == true) {
                        vm.displayMtnList()
                    } else {
                        vm.filter(text = newText)
                    }
                }

                return true
            }
        })
        return true
    }

    private fun initUI() {
        binding.btnProceed.setOnClickListener {
            val selectedMtn = vm.getSelectedMtn()

            if (selectedMtn.mtn.classificationInProgress == 1) {
                showDialog(
                    resources.getString(R.string.verify_and_offload_status),
                    object : DialogClick {
                        override fun onPositive(dialog: DialogInterface) {
                            moveToVerifyBale(selectedMtn)
                        }

                        override fun onNegative(dialog: DialogInterface) {
                            offloadBales(selectedMtn)
                        }

                    },
                    postiveText = R.string.verify,
                    negativeText = R.string.offload,
                    isColor = false
                )
            } else {
                showOffloadTypeDialog()
            }
        }
        binding.incomingRecyclerView.layoutManager =
            LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        mIncomingMtnAdapter =
            GinningIncomingMtnAdapter(
                Collections.emptyList(),
                object : GinningIncomingMtnAdapter.UpdateMtnList {
                    override fun update(incomingMtn: MtnWithGrades) {
                        enableProceedButton()
                        vm.updateMtnList(incomingMtn)
                    }
                })
        binding.incomingRecyclerView.adapter = mIncomingMtnAdapter

        vm.mUIMtnList.observe(this, Observer {
            if (it.size > 0) {
                showRecyclerView()
                mIncomingMtnAdapter.updateData(it)
            } else {
                showEmptyView()
            }
        })

        vm.getMtnsOffline()
        vm.getMtnOffline.observe(this, Observer {
            mtnsOffline=it.toMutableList()
        })

        vm.getMtns()
        vm.getMtn.observe(this, Observer { /*updateMtn(it)*/item ->
            if (mtnsOffline.isEmpty()) {
                item.data?.data?.let {
                    it.forEach { mtn ->
                        vm.insertMtn(mtn)
                    }
                }
            } else {
                mtnsOffline.forEach { mtn ->
                    val mtnExistInServer = item.data?.data?.any { it.mtnNumber == mtn.mtnNumber }

                    mtnExistInServer?.let {
                        if (!it) {
                            vm.deleteMtnsByMtnId(mtn.mtnNumber)
                        }
                    }
                }
                item.data?.data?.forEach { item1 ->
                    if (!mtnsOffline.any { it.mtnNumber == item1.mtnNumber }) {
                        vm.insertMtn(item1)
                    }
                }
            }
            vm.getMtnsWithGradesOffline()
        })

        vm.getMtnWithGradesOffline.observe(this, Observer {
            updateMtnDetails(it)
        })

        vm.getMtnWithBalesOffline.observe(this, Observer {
            val mtnModel = vm.getMtnModel(it)
            val intent = Intent(this, GinningIncomingReviewBaleActivity::class.java)
            intent.putExtra(MTN, mtnModel)
            intent.putParcelableArrayListExtra(GRADES, mtnGradesList)
            startActivity(intent)
        })
        //if (isOnline()) {

        /*} else {
            vm.setMtnList(vm.getMtnsWithBales())
        }

        val mtnBales = vm.getOfflineMtnWithBales()
        if (mtnBales.size > 0) llOfflineSummary.visible() else llOfflineSummary.gone()

        llOfflineSummary.setOnClickListener {
            startActivity(Intent(this, IncomingMtnOfflineActivity::class.java))
        }*/
    }

    /*private fun updateMtn(data: Resource<GenericReqAndResp<List<Mtn>>>?) {
        data.let {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    hideLoading()


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
    }*/

    private fun enableProceedButton() {

        binding.btnProceed.setBackgroundColor(
            resources.getColor(com.olam.warehouse.presentation.R.color.colorPrimaryOfi)
        )
        binding.btnProceed.isEnabled = true

    }

    private fun showRecyclerView() {
        binding.tvIncomingMtnEmpty.visibility = View.INVISIBLE
        binding.incomingRecyclerView.visibility = View.VISIBLE
    }

    private fun showEmptyView() {
        binding.tvIncomingMtnEmpty.visibility = View.VISIBLE
        binding.incomingRecyclerView.visibility = View.INVISIBLE
    }

    private fun updateMtnDetails(response: List<MtnWithGrades>) {
       bindMtnList(response)
    }


    private fun bindMtnList(data: List<MtnWithGrades>?) {
        vm.setMtnList(data)
    }

    private fun showOffloadTypeDialog() {
        val dialog = MaterialDialog(this)
            .customView(
                R.layout.ginning_dialog_select_offload,
                scrollable = false,
                noVerticalPadding = true
            )
        val dialogView = dialog.getCustomView()
        val ivClose = dialogView.findViewById<ImageView>(R.id.ivClose)
        val btnOffloadProceed = dialogView.findViewById<Button>(R.id.btnOffloadProceed)
        val rbOffloadVerify = dialogView.findViewById<RadioButton>(R.id.rbOffloadAndVerify)
        val rbOffload = dialogView.findViewById<RadioButton>(R.id.rbOffload)
        val rgSelectOffload = dialogView.findViewById<RadioGroup>(R.id.rgSelectOffload)
        rbOffloadVerify.isChecked = true
        btnOffloadProceed?.setBackgroundColor(
            ContextCompat.getColor(
                this,
                com.olam.warehouse.presentation.R.color.colorPrimaryOfi
            )
        )
        btnOffloadProceed?.isEnabled = true
        rgSelectOffload?.setOnCheckedChangeListener { group, checkedId ->
            if (checkedId == R.id.rbOffloadAndVerify || checkedId == R.id.rbOffload) {
                btnOffloadProceed?.setBackgroundColor(
                    resources.getColor(com.olam.warehouse.presentation.R.color.colorPrimaryOfi)
                )
                btnOffloadProceed?.isEnabled = true
            }
        }

        btnOffloadProceed?.setOnClickListener {
            dialog.dismiss()
            val mtn = vm.getSelectedMtn()
            if (rbOffloadVerify?.isChecked == true) {
                vm.updateMtn(mtn.mtn)
                moveToVerifyBale(mtn)
            } else if (rbOffload?.isChecked == true) {
                offloadBales(mtn)
            }
        }

        ivClose?.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun offloadBales(mtn: MtnWithGrades) {
        vm.updateMtnBalesStatusVerified(mtn.mtn.mtnNumber)
        vm.getMtnWithBales(mtn.mtn.mtnNumber)
        mtnGradesList= mtn.grades as ArrayList<MtnGrades>

    }

    private fun moveToVerifyBale(mtn: MtnWithGrades) {
        val intent = Intent(this, GinningIncomingVerifyBaleActivity::class.java)
        intent.putExtra(MTN, mtn.mtn)
        intent.putParcelableArrayListExtra(GRADES, mtn.grades as ArrayList<MtnGrades>)
        intent.putExtra("ViewStatus", 1)
        startActivity(intent)
    }


    private fun moveToSuccessActivity(mtnNumber: String, materialDoc: String) {

        val intent = Intent(this, SuccessActivity::class.java)
        intent.putExtra(MTN_ID, mtnNumber)
        intent.putExtra(MATERIAL_DOC_ID, materialDoc)
        intent.putExtra(SUCCESS, OFFLOAD_SUCCESS)
        startActivity(intent)
    }


}
