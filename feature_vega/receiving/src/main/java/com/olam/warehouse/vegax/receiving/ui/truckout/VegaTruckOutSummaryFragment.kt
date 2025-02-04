package com.olam.warehouse.vegax.receiving.ui.truckout

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.lifecycle.Observer
import com.afollestad.materialdialogs.MaterialDialog
import com.olam.warehouse.login.ui.succes.SuccessActivity
import com.olam.warehouse.master.common.utils.getCurrentKey
import com.olam.warehouse.master.common.utils.getPlantDetails
import com.olam.warehouse.master.vega.entity.VegaReceiving
import com.olam.warehouse.presentation.adapter.setUp
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.enums.Status
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.AppUtils
import com.olam.warehouse.presentation.utils.DateUtils
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.UIUtils.getActionBtnChangedView
import com.olam.warehouse.presentation.utils.extension.formatThreeDigits
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.presentation.utils.extension.visible
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.receiving.R
import com.olam.warehouse.vegax.receiving.data.domain.model.VegaReceivingPost
import com.olam.warehouse.vegax.receiving.data.domain.model.VegaReceivingResponse
import com.olam.warehouse.vegax.receiving.databinding.FragmentVegaTruckoutSummaryBinding
import com.olam.warehouse.vegax.receiving.ui.VegaReceivingViewModel
import com.olam.warehouse.vegax.receiving.utils.*
import kotlinx.android.synthetic.main.item_vega_bag_summary_details.view.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper
import java.io.ByteArrayOutputStream

/**
 * Created by Baskaran Kannan on 1/28/2020.
 */
class VegaTruckOutSummaryFragment : BaseFragment() {

    private var callBack: CallBack? = null
    private var receivingData = VegaReceiving()
    private var mReceiving = mutableListOf<VegaReceiving>()
    private var mBagsTarWeight: Double? = 0.0
    private val vm: VegaReceivingViewModel by viewModel()

    interface CallBack {
        fun replaceFragment(
            moveFrag: String,
            receivingData: VegaReceiving
        )
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? CallBack
    }

    private lateinit var binding: FragmentVegaTruckoutSummaryBinding
    override val layoutResourceId = R.layout.fragment_vega_truckout_summary

    companion object {
        fun newInstance(receivingData: VegaReceiving,
            mReceiving: ArrayList<VegaReceiving>) = VegaTruckOutSummaryFragment().putArgs {
            putParcelable(RECEIVING_DATA, receivingData)
            putParcelableArrayList(RECEIVING_POST_DATA, mReceiving)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentVegaTruckoutSummaryBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("receiving/ui/truckout/VegaTruckOutSummaryFragment").title("Receiving").with(tracker)
        initUI()
    }

    private fun initUI() {
        context?.let {
            getActionBtnChangedView(binding.tvTitle, it, false)
            getActionBtnChangedView(binding.btnConfirm, it, true)
        }
        receivingData = arguments?.getParcelable(RECEIVING_DATA)!!
        mReceiving = arguments?.getParcelableArrayList<VegaReceiving>(RECEIVING_POST_DATA)!!
        binding.tvMaterial.text = ": ".plus(receivingData.materialName)
        when (receivingData.weighBridgeType) {
            PROCURE -> {
                binding.tvdifference.text = SUPPLIER
            }
            else -> {
                binding.tvdifference.visibility = View.GONE
                binding.tvSupplierName.visibility = View.GONE
                binding.tvdifference.text = WAREHOUSE
            }
        }
        enableProceedBtn(receivingData.status)
        binding.tvTruckID.text =
            getString(R.string.truck_id).plus(": ").plus(receivingData.vehicleNumber ?: receivingData.weighBridgeId)
        binding.tvSupplierName.text = receivingData.supplierName ?: receivingData.supplierCode
        binding.tvWeight.text = receivingData.grossWeight.plus(" ").plus(receivingData.unitsOfMeasure)
        val times = receivingData.erdat?.split('(', ')')
        binding.tvDate.text = times?.get(1)?.let { it1 ->
            DateUtils.getUTCDateTime(
                it1,
                App.getAppContext()
            )
        }
        //if (receivingData.weighBridgeType == PROCURE) {
            binding.llBagDetails.visible()
            setBagDetails(mReceiving)
            var count=0
            mReceiving.forEach {
                count += it.bagCount!!.toInt()
                val weight = it.bagCount!!.toDouble() * it.bagTareWeight!!.toDouble()
                mBagsTarWeight = mBagsTarWeight!! + weight
            }
            binding.tvTotalNoofBags.text=count.toString()
        /*} else {
            mReceiving.clear()
            mReceiving.add(receivingData)
            binding.llBagDetails.gone()

        }*/
        binding.tvTruckGrossWeight.text = receivingData.grossWeight.plus(" ").plus(receivingData.unitsOfMeasure)
        binding.tvTruckTarWeight.text = receivingData.tareWeight.plus(" ").plus(receivingData.unitsOfMeasure)
        binding.tvNetWeight.text =
            getTotalWeight(receivingData.grossWeight!!, receivingData.tareWeight!!).toString().plus(" ")
                .plus(receivingData.unitsOfMeasure)

        binding.tvGross.text = receivingData.grossWeight
        binding.tvTarWeight.text = receivingData.tareWeight
        binding.tvBagTare.text = mBagsTarWeight?.formatThreeDigits().toString()
        val tarweight = receivingData.tareWeight?.toDouble()!! + mBagsTarWeight!!.toDouble()
        val netWeight = receivingData.grossWeight?.toDouble()!! - tarweight
        binding.tvTotalNetWeight.text =
            netWeight.formatThreeDigits().replace(",", "").plus(" ").plus(receivingData.unitsOfMeasure)
        val netData = binding.tvTotalNetWeight.text.toString().replace(",", "").split(" ")[0]
        binding.btnConfirm.setOnClickListener { if (validate(netData)) showConfirmDialog() else showSnack(getString(R.string.negative_weight_error)) }
        vm.receive.observe(viewLifecycleOwner, Observer { updateUI(it) })
        if (receivingData.imagePath?.isNotEmpty()!!) {
            binding.photoImageView.visible()
            binding.photoImageView.setImageBitmap(setScaledBitmap())
        }

    }

    private fun updateUI(data: Resource<GenericReqAndResp<VegaReceivingResponse>>?) {
        data?.let {
            when (it.status) {
                Resource.Status.SUCCESS -> prepareSuccessData(it.data?.data?.wbId, true, it.data?.message.toString())
                Resource.Status.LOADING -> showCustomLoading()
                Resource.Status.ERROR -> {
                    hideCustomLoading()
                    UIUtils.showErrorDialog(requireContext(), "${it.error}")
                    prepareErrorData(receivingData.weighBridgeId, false, it.error.toString())
                }
            }
        }
    }

    private fun showConfirmDialog() {
        MaterialDialog(requireContext()).show {
            message(R.string.confirm_truck_out_message)
            UIUtils.getMetirialCustomView(
                this,
                getString(R.string.confirm),
                getString(R.string.cancel),
                {
                    postReceiving()
                },
                { dismiss() })
        }
    }

    private fun validate(netweight: String): Boolean {
        return !netweight.contains("-")
    }

    private fun postReceiving() {
        /* val file = File(receivingData.imagePath.toString())
         val imageBody = MultipartBody.Part.createFormData("image", file.name)*/
        mReceiving.forEachIndexed { index, vegaReceiving ->
            vegaReceiving.bagWeight = (vegaReceiving.bagCount?.toDouble()?.let { it1 ->
                vegaReceiving.bagTareWeight?.toDouble()?.times(it1)
            }).toString()
            if (index == 0) {
                val charset = Charsets.UTF_8
                val byteArray = getBase64FromFile(receivingData.imagePath.toString())?.toByteArray(charset)
                vegaReceiving.imageString = byteArray?.let { it1 -> String(it1) }
            }
            vegaReceiving.netWeight = binding.tvTotalNetWeight.text.toString().replace(",", "").split(" ")[0]
        }
        if (AppUtils.isOnline()) {
            vm.postReceivingData(VegaReceivingPost(getCurrentKey(), getPlantDetails(), mReceiving))
        } else {
            prepareSuccessData(
                if (receivingData.tmpWbId.isEmpty()) getTmpId() else receivingData.tmpWbId,
                false,
                "Data cached offline"
            )
        }
    }

    private fun prepareSuccessData(wbId: String?, syncStatus: Boolean, msg: String) {
        hideLoading()
        receivingData.truckDirection = DIRECTIONOUT
        receivingData.status = if (syncStatus) Status.RECEVING_COMPLETED else Status.SYNC_PENDING
        receivingData.isSynced = syncStatus
        receivingData.syncStatusMsg = msg
        vm.saveReceiving(receivingData)
        if (!syncStatus) {
            mReceiving.forEach { it.tmpWbId = wbId ?: "" }
            vm.saveReceivingLineItems(mReceiving)
        }
        moveToSuccessPage(wbId)
    }

    private fun prepareErrorData(wbId: String?, syncStatus: Boolean, msg: String) {
        hideLoading()
        receivingData.truckDirection = DIRECTIONOUT
        receivingData.status = Status.SYNC_ERROR
        receivingData.isSynced = syncStatus
        receivingData.syncStatusMsg = msg
        vm.saveReceiving(receivingData)
        if (!syncStatus) {
            mReceiving.forEach { it.tmpWbId = wbId ?: "" }
            vm.saveReceivingLineItems(mReceiving)
        }
    }



    private fun setBagDetails(data: MutableList<VegaReceiving>?){
        binding.rvBagDetailSummary.setUp(data!!, R.layout.item_vega_bag_summary_details, { it, pos ->
            tvBagType.text=it.bagType
            tvNoofBags.text=it.bagCount
        })
    }



    private fun getTotalWeight(grossWeight:String,tarWeight:String):Double{
        var totalWeight=0.0
        try{ totalWeight=grossWeight.toDouble()-tarWeight.toDouble()
        }catch (e:NumberFormatException){
            e.printStackTrace()
        }
        return totalWeight
    }


    private fun moveToSuccessPage(wbId: String?) {
        val intent = Intent(requireContext(), SuccessActivity::class.java)
        if (AppUtils.isOnline()) intent.putExtra(
            AppUtils.TITLE,
            getString(R.string.success_truck_out)
        ) else intent.putExtra(
            AppUtils.TITLE,
            getString(R.string.success_truck_out_offline)
        )
        intent.putExtra(AppUtils.SUB_TITLE, getString(R.string.weigh_bridge_id_is).plus(wbId))
        startActivity(intent)
        requireActivity().finish()
    }

    fun setScaledBitmap(): Bitmap? {
        try {
            val imageViewWidth = 347
            val imageViewHeight = 413

            val bmOptions = BitmapFactory.Options()
            bmOptions.inJustDecodeBounds = true
            BitmapFactory.decodeFile(receivingData.imagePath, bmOptions)
            val bitmapWidth = bmOptions.outWidth
            val bitmapHeight = bmOptions.outHeight

            val scaleFactor = Math.min(bitmapWidth / imageViewWidth, bitmapHeight / imageViewHeight)

            bmOptions.inJustDecodeBounds = false
            bmOptions.inSampleSize = scaleFactor

            return BitmapFactory.decodeFile(receivingData.imagePath, bmOptions)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null

    }

    fun getBase64FromFile(path: String?): String? {
        var bmp: Bitmap? = null
        var baos: ByteArrayOutputStream? = null
        var baat: ByteArray? = null
        var encodeString: String? = null
        try {
            bmp = BitmapFactory.decodeFile(path)
            baos = ByteArrayOutputStream()
            bmp.compress(Bitmap.CompressFormat.JPEG, 70, baos)
            baat = baos.toByteArray()
            encodeString = Base64.encodeToString(baat, Base64.DEFAULT)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
        return encodeString
    }

    private fun enableProceedBtn(status: Status) {

        when (status) {
            Status.SYNC_PENDING -> {
                binding.btnConfirm.isEnabled = true
                ViewCompat.setBackgroundTintList(
                    binding.btnConfirm,
                    context?.let {
                        ContextCompat.getColorStateList(
                            it,
                            com.olam.warehouse.presentation.R.color.colorPrimaryOfi
                        )
                    }
                )
            }
            Status.RECEVING_COMPLETED -> {
                binding.btnConfirm.isEnabled = false
                ViewCompat.setBackgroundTintList(
                    binding.btnConfirm,
                    context?.let { ContextCompat.getColorStateList(it, com.olam.warehouse.presentation.R.color.grey) }
                )
            }
        }
    }

}
