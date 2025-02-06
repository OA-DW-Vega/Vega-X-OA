package com.olam.warehouse.vegax.portwarehouse.ui.incoming

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.Observer
import com.olam.warehouse.login.ui.home.HomeBaseActivity
import com.olam.warehouse.login.utils.showErrorDialogWithFAQLink
import com.olam.warehouse.master.vegaCotton.vegaPortWareHouse.entity.PortMtn
import com.olam.warehouse.master.vegaCotton.vegaPortWareHouse.entity.PortMtnGrades
import com.olam.warehouse.presentation.data.domain.model.GenericMessage
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.portwarehouse.databinding.ActivityIncomingReviewBaleBinding
import com.olam.warehouse.vegax.portwarehouse.ui.success.SuccessPortActivity
import com.olam.warehouse.vegax.portwarehouse.utils.PortWHUtil
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
class PortIncomingReviewBaleActivity : HomeBaseActivity() {

    private val vm: PortIncomingReviewViewModel by viewModel { emptyParametersHolder() }

    private var mtnNumber: String? = null
    private var mtn: PortMtn? = null
    private var grades: ArrayList<PortMtnGrades>? = null

    override val layoutResourceId: Int =
        com.olam.warehouse.vegax.portwarehouse.R.layout.activity_incoming_review_bale
    private lateinit var binding: ActivityIncomingReviewBaleBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityIncomingReviewBaleBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initExtras()
        initNavigationView()
        initUI()
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("portwarehouse/ui/incoming/PortIncomingReviewBaleActivity")
            .title("Portwarehouse").with(tracker)
    }

    private fun initExtras() {
        mtn = intent.getParcelableExtra(PortWHUtil.MTN)
        grades = intent.getParcelableArrayListExtra(PortWHUtil.GRADES)
        mtnNumber = mtn?.mtnNumber
    }

    private fun initUI() {

        binding.tvPlant.text = mtn?.suplierPlantDesc
        binding.tvWeight.text = mtn?.sourceNetWeight.plus(" ").plus(mtn?.uom)
        val grades = grades?.map { it.grade }
        vm.offloadBales.observe(this, Observer {
            handleResponse(it)
        })
        binding.tvGrades.text = grades.toString().replace("[", "").replace("]", "")
        binding.btnStoreInInventory.setOnClickListener {
            showLoading()

            if (PortWHUtil.isOnline()) {
                val mtnModel = vm.getMtnModel()
                mtnModel?.let { it1 -> vm.postMtnWithBales(it1) }

            } else {

                runBlocking {
                    withContext(Dispatchers.IO)
                    {
                        vm.updateOfflineMtnStatus(vm.getVerifiedBales())
                    }
                }

                moveToSuccess("")
            }
        }

        mtnNumber?.let { mtnNumber ->
            runBlocking {
                withContext(Dispatchers.IO)
                {
                    vm.setMtnWithBales(mtnNumber)
                }

            }


            binding.tvMtnNumber.text = mtnNumber

            vm.getTotalCount()?.let {
                binding.tvBaleReceived.text = it
            }
            vm.getBaleGoodCount()?.let {
                binding.tvBaleGood.text = it
            }
            vm.getBaleTieDamageCount()?.let {
                binding.tvBaleTieDamage.text = it
            }

            vm.getWetBaleCount()?.let {
                binding.tvWetBale.text = it
            }

            vm.getNoBaleTagCount()?.let {
                binding.tvNoBaleTag.text = it
            }

            vm.getBaleCottonCleanCount()?.let {
                binding.tvCottonClean.text = it
            }

            vm.getBaleCottonDirtyCount()?.let {
                binding.tvBaleCottonDirty.text = it
            }

            vm.getVerifiedCount()?.let {
                binding.tvBaleVerified.text = it
            }

            vm.getYetToVerifyCount()?.let {
                binding.tvBaleYetToVerify.text = it
            }
        }
    }

    private fun handleResponse(response: Resource<GenericReqAndResp<GenericMessage>>) {
        response.let {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    hideLoading()
                    when {
                        it.data?.data?.success!! -> {
                            mtnNumber?.let { mtn ->
                                runBlocking {
                                    withContext(Dispatchers.IO) {
                                        vm.deleteMtnsByMtnId(mtn)
                                    }
                                }

                                it.data?.data?.message?.let { message ->
                                    moveToSuccess(message.split("-")[1].trim())
                                }
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
                    showErrorDialogWithFAQLink(
                        this, it.error.toString()
                    )
                }
            }
        }
    }

    private fun moveToSuccess(materialDoc: String) {
        val intent = Intent(this, SuccessPortActivity::class.java)
        intent.putExtra(PortWHUtil.MTN_ID, mtnNumber)
        intent.putExtra(PortWHUtil.SUCCESS, PortWHUtil.OFFLOAD_SUCCESS)
        intent.putExtra(PortWHUtil.MATERIAL_DOC_ID, materialDoc)
        startActivity(intent)
    }
    private fun showErrorDialog(message: String) {
        showSingleDialog(message, object : DialogSingleClick {
            override fun onClick(dialog: DialogInterface) {
                dialog.dismiss()
            }
        })
    }
}

