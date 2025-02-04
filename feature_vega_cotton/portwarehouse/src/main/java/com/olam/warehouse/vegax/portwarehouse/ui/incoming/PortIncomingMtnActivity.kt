package com.olam.warehouse.vegax.portwarehouse.ui.incoming

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.widget.*
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.customview.getCustomView
import com.olam.warehouse.login.ui.home.HomeBaseActivity
import com.olam.warehouse.master.ui.scanner.ScannerActivity
import com.olam.warehouse.master.vegaCotton.vegaPortWareHouse.entity.MtnWithGrades
import com.olam.warehouse.master.vegaCotton.vegaPortWareHouse.entity.PortMtn
import com.olam.warehouse.master.vegaCotton.vegaPortWareHouse.entity.PortMtnGrades
import com.olam.warehouse.master.vegaCotton.vegaPortWareHouse.model.MtnNumber
import com.olam.warehouse.portwarehouse.data.model.Deliverylist
import com.olam.warehouse.portwarehouse.data.model.Deliverypostlist
import com.olam.warehouse.portwarehouse.ui.incoming.OnItemClickListener
import com.olam.warehouse.portwarehouse.ui.incoming.PortIncomingMtnCustomAdapter
import com.olam.warehouse.portwarehouse.ui.success.SuccessPortActivity
import com.olam.warehouse.portwarehouse.utils.PortWHUtil
import com.olam.warehouse.presentation.data.domain.model.GenericMessage
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.extension.gone
import com.olam.warehouse.presentation.utils.extension.visible
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.portwarehouse.R
import com.olam.warehouse.vegax.portwarehouse.databinding.IncomingMtnActivityBinding
import com.olam.warehouse.vegax.portwarehouse.di.injectPortIncomingFeaturee
import com.olam.warehouse.vegax.portwarehouse.ui.incoming.offline.PortIncommingMtnOfflineActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.emptyParametersHolder
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper
import java.util.*

/**
 * Created by Baskaran Kannan on 4/5/2021.
 */
class PortIncomingMtnActivity : HomeBaseActivity(), OnItemClickListener {

    companion object {
        const val SEARCH_HINT_TEXT = "Search MTN"
    }

    override val layoutResourceId = R.layout.incoming_mtn_activity

    private lateinit var binding: IncomingMtnActivityBinding
    private val vm: PortIncomingMtnViewModel by viewModel { emptyParametersHolder() }
    private lateinit var mIncomingMtnAdapter: PortIncomingMtnAdapter
    var heroList = ArrayList<MtnNumber>()
    private lateinit var listview:RecyclerView
    lateinit var mIncomingCustomMtnAdapter: PortIncomingMtnCustomAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = IncomingMtnActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        injectPortIncomingFeaturee()
        initNavigationView()
        initUI()
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("portwarehouse/ui/incoming/PortIncomingMtnActivity")
            .title("Portwarehouse").with(tracker)
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
                    }, postiveText = R.string.verify, negativeText = R.string.offload, isColor = false
                )
            } else {
                showOffloadTypeDialog()
            }
        }
        binding.incomingRecyclerView.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        mIncomingMtnAdapter =
            PortIncomingMtnAdapter(Collections.emptyList(), object : PortIncomingMtnAdapter.UpdateMtnList {
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


        var mtnBales = runBlocking {
            withContext(Dispatchers.IO) {
                vm.getOfflineMtnWithBales()
            }
        }

        if (PortWHUtil.isOnline()) {
            showChangeLangDialog()
        } else {
            val list = runBlocking {
                withContext(Dispatchers.IO) {
                    vm.getMtnsWithBales()

                }
            }
            vm.setMtnList(list)
        }

        if (mtnBales.size > 0) binding.llOfflineSummary.visible() else binding.llOfflineSummary.gone()

        binding.llOfflineSummary.setOnClickListener {
            startActivity(Intent(this, PortIncommingMtnOfflineActivity::class.java))
        }

        vm.listMtns.observe(this, Observer { updateMtnDetails(response = it) })
        vm.listMtnWithGrades.observe(this, Observer { updateMtnListDetails(data = it) })
    }


    private fun enableProceedButton() {

        binding.btnProceed.setBackgroundColor(
            resources.getColor(R.color.green)
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

    private fun updateMtnDetails(response: Resource<GenericReqAndResp<List<PortMtn>>>) {
        hideLoading()
        response.let {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    hideLoading()
                    it.data?.data?.let { it1 -> bindMtnList(it1) }
                }
                Resource.Status.LOADING -> {
                    showLoading()
                }
                Resource.Status.ERROR -> {
                    hideLoading()
                    UIUtils.showErrorDialog(
                        this, it.error.toString()
                    )
                }
            }
        }
    }


    private fun bindMtnList(data: List<PortMtn>) {
        try {
            val mtns = runBlocking {
                withContext(Dispatchers.IO) {
                    vm.getMtnsOffline()
                }
            }
            if (mtns.isEmpty()) {
                data.let {
                    it.forEach { mtn ->
                        runBlocking {
                            withContext(Dispatchers.IO) {
                                vm.insertMtn(mtn)
                            }
                        }
                    }
                }
            } else {
                mtns.forEach { mtn ->
                    val mtnExistInServer = data.any { it.mtnNumber == mtn.mtnNumber }
                    mtnExistInServer.let {
                        if (!it) {
                            runBlocking {
                                withContext(Dispatchers.IO) {
                                    vm.deleteMtnsByMtnId(mtn.mtnNumber)
                                }
                            }

                        }
                    }
                }
                data.forEach { serverMtn ->
                    val isMtnExist = mtns.any { it.mtnNumber == serverMtn.mtnNumber }
                    if (!isMtnExist) {
                        runBlocking {
                            withContext(Dispatchers.IO) {

                                vm.insertMtn(serverMtn)
                            }
                        }

                    }
                }
            }
        } catch (Ex: Exception) {
            Log.d("exception", Ex.toString())
        }
        val data = runBlocking {
            withContext(Dispatchers.IO) {
                vm.getMtnsWithGradesOffline()
            }
        }


        vm.setMtnList(data)
    }

    private fun updateMtnListDetails(data: List<MtnWithGrades>?) {
        vm.setMtnList(data)
    }

    private fun showOffloadTypeDialog() {
        val dialog = MaterialDialog(this)
            .customView(R.layout.dialog_select_offload, scrollable = false, noVerticalPadding = true)
        val dialogView = dialog.getCustomView()
        val ivClose = dialogView.findViewById<ImageView>(R.id.ivClose)
        val btnOffloadProceed = dialogView.findViewById<Button>(R.id.btnOffloadProceed)
        val rbOffloadVerify = dialogView.findViewById<RadioButton>(R.id.rbOffloadAndVerify)
        val rbOffload = dialogView.findViewById<RadioButton>(R.id.rbOffload)
        val rgSelectOffload = dialogView.findViewById<RadioGroup>(R.id.rgSelectOffload)
        rbOffloadVerify.isChecked = true
        btnOffloadProceed?.setBackgroundColor(resources.getColor(R.color.green))
        btnOffloadProceed?.isEnabled = true
        rgSelectOffload?.setOnCheckedChangeListener { group, checkedId ->
            if (checkedId == R.id.rbOffloadAndVerify || checkedId == R.id.rbOffload) {
                btnOffloadProceed?.setBackgroundColor(
                    resources.getColor(R.color.green)
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
        showLoading()
        if (PortWHUtil.isOnline()) {
                runBlocking {
                    withContext(Dispatchers.IO) {
                        val mtnBales = vm.getMtnWithBales(mtn.mtn.mtnNumber)
                        val mtnModel = vm.getMtnModel(mtnBales)
                        vm.postMtnWithBales(mtnModel)
                    }
                }

            vm.offloadBales.observe(this, Observer { handleResponse(it, mtn.mtn.mtnNumber) })
        } else {
            runBlocking {
                withContext(Dispatchers.IO) {
                    vm.updateOfflineMtnStatus(mtn.mtn.mtnNumber)
                }
            }
            moveToSuccessActivity(mtn.mtn.mtnNumber, "")
        }
    }

    private fun moveToVerifyBale(mtn: MtnWithGrades) {
        val intent = Intent(this, PortIncomingVerifyBaleActivity::class.java)
        intent.putExtra(PortWHUtil.MTN, mtn.mtn)
        intent.putParcelableArrayListExtra(PortWHUtil.GRADES, mtn.grades as ArrayList<PortMtnGrades>)
        intent.putExtra("ViewStatus", 1)
        startActivity(intent)
    }

    private fun handleResponse(
        response: Resource<GenericReqAndResp<GenericMessage>>,
        mtnNumber: String
    ) {
        response.let {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    hideLoading()
                    when {
                        it.data?.data?.success!! -> {
                            val selectedMtn = vm.getSelectedMtn()
                            runBlocking {
                                withContext(Dispatchers.IO) {
                                    vm.deleteMtnsByMtnId(selectedMtn.mtn.mtnNumber)
                                }
                            }
                            it.data?.data?.message?.let { message ->
                                moveToSuccessActivity(mtnNumber, message.split("-")[1].trim())
                            }
                        }
                        else -> showErrorDialog(it.data?.data?.message.toString())
                    }
                }
                Resource.Status.LOADING -> {
                    showLoading()
                }
                Resource.Status.ERROR -> {
                    hideLoading()
                    UIUtils.showErrorDialog(
                        this, it.error.toString()
                    )
                }
            }
        }
    }

    private fun showErrorDialog(message: String) {
        showSingleDialog(message, object : DialogSingleClick {
            override fun onClick(dialog: DialogInterface) {
                dialog.dismiss()
            }

        })
    }


    private fun moveToSuccessActivity(mtnNumber: String, materialDoc: String) {

        val intent = Intent(this, SuccessPortActivity::class.java)
        intent.putExtra(PortWHUtil.MTN_ID, mtnNumber)
        intent.putExtra(PortWHUtil.MATERIAL_DOC_ID, materialDoc)
        intent.putExtra(PortWHUtil.SUCCESS, PortWHUtil.OFFLOAD_SUCCESS)
        startActivity(intent)
    }

    fun showChangeLangDialog() {
        heroList.clear()
        val dialogBuilder: AlertDialog.Builder = AlertDialog.Builder(this)
        val inflater: LayoutInflater =
            this.applicationContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val dialogView: View = inflater.inflate(R.layout.custom_mtn_dialog, null)
        dialogBuilder.setView(dialogView)
        val edittext = dialogView.findViewById(R.id.edit1) as EditText
        val button = dialogView.findViewById(R.id.button_add) as Button
        val btn_scanBale = dialogView.findViewById(R.id.btn_scanBale) as Button
        listview = dialogView.findViewById(R.id.listview) as RecyclerView
        listview.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        button.setOnClickListener {
            if (!edittext.text.isNullOrEmpty()) {
                    val mat = heroList.filter { it1 -> it1.baleID!!.contains(edittext.text.toString()) }
                        if (mat.size>0) {
                            Toast.makeText(this, "Already Added", Toast.LENGTH_SHORT).show()
                        } else {
                            var baleid: MtnNumber = MtnNumber()
                            baleid.baleID = edittext.text.toString()
                            heroList.add(baleid)
                            mIncomingCustomMtnAdapter = PortIncomingMtnCustomAdapter(this,heroList)
                            listview.adapter = mIncomingCustomMtnAdapter
                            mIncomingCustomMtnAdapter.updateData(heroList)
                            edittext.text.clear()
                        }
            }
            else
            {
                PortWHUtil.showErrorDialog(this,"Please enter valid OBD Number")
            }

        }
        btn_scanBale.setOnClickListener {
            moveToScan()
            vm.incomingMtns.observe(this, Observer {Toast.makeText(this, it.toString(), Toast.LENGTH_SHORT).show() })

        }
        dialogBuilder.setTitle("Enter MTN's Number")
        dialogBuilder.setPositiveButton("Done", DialogInterface.OnClickListener { dialog, whichButton ->
            if (heroList.isNotEmpty()) {
                val postedLot = arrayListOf<Deliverypostlist>()
                heroList.forEach {
                    val lot = Deliverypostlist()
                    lot.deliveryNumber = it.baleID
                    postedLot.add(lot)
                }
                vm.getMtns(Deliverylist(postedLot))
            } else {
                Toast.makeText(this, "MTN Number is empty. please enter valid MTN number", Toast.LENGTH_SHORT).show()
            finish()
            }
        })
        dialogBuilder.setNegativeButton("Cancel", DialogInterface.OnClickListener { dialog, whichButton ->
            heroList.clear()
            finish()
        })
        val b: AlertDialog = dialogBuilder.create()
        b.show()
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == Constants.ADD_TASK_REQUEST) {
            if (resultCode == Activity.RESULT_OK) {
                var scannedresult=data?.extras?.getString(Constants.SCANNED_ID)?.trim()
                validateBaleId(scannedresult)


                           }
        }
    }
    private fun moveToScan() {
        val intent = Intent(this, ScannerActivity::class.java)
        startActivityForResult(intent, Constants.ADD_TASK_REQUEST)
    }
    private fun validateBaleId(baleId: String?){
        baleId?.let {
            val isValid = vm.validateBaleId(baleId)
            if (isValid) {
                    if (!baleId.isNullOrEmpty()) {
                        val mat = heroList.filter { it1 -> it1.baleID!!.contains(baleId) }
                        if (mat.size>0) {
                            Toast.makeText(this, "Already Added", Toast.LENGTH_SHORT).show()
                        } else {
                            var baleid: MtnNumber = MtnNumber()
                            baleid.baleID = baleId
                            baleid.isScaned=true
                            heroList.add(baleid)
                            mIncomingCustomMtnAdapter = PortIncomingMtnCustomAdapter(this,heroList)
                            listview.adapter = mIncomingCustomMtnAdapter
                            mIncomingCustomMtnAdapter.updateData(heroList)
                        }
                    }
            }
            else
            {
                PortWHUtil.showErrorDialog(this,"Please enter valid OBD Number")
            }
        }
    }
    private fun showHoldDialog(mtn: MtnNumber) {

        showDialog(resources.getString(R.string.delete_msg),
            object : HomeBaseActivity.DialogClick {
                override fun onPositive(dialog: DialogInterface) {
                    mIncomingCustomMtnAdapter.removedata(mtn)
                }

                override fun onNegative(dialog: DialogInterface) {
                    dialog.dismiss()
                }
            })
    }



    override fun onItemClicked(mtn: MtnNumber,isdeleted:Boolean) {
        if(!isdeleted)
        showHoldDialog(mtn)
        else
            showDialog(getString(R.string.Remove_BalaID))



    }
}
