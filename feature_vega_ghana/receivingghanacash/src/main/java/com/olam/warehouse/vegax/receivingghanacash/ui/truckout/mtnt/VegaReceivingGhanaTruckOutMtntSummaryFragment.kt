package com.olam.warehouse.vegax.receivingghanacash.ui.truckout.mtnt

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import com.afollestad.materialdialogs.MaterialDialog
import com.olam.warehouse.login.ui.succes.SuccessActivity
import com.olam.warehouse.master.common.utils.getCurrentKey
import com.olam.warehouse.master.common.utils.getPlantDetails
import com.olam.warehouse.master.vega.entity.VegaMtnt
import com.olam.warehouse.presentation.adapter.setUp
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.enums.Status
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.AppUtils
import com.olam.warehouse.presentation.utils.DateUtils
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.extension.formatThreeDigits
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.presentation.utils.extension.visible
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.receivingghanacash.R
import com.olam.warehouse.vegax.receivingghanacash.data.domain.model.VegaReceivingGhanaMtntPost
import com.olam.warehouse.vegax.receivingghanacash.data.domain.model.VegaReceivingGhanaMtntResponse
import com.olam.warehouse.vegax.receivingghanacash.databinding.FragmentVegaReceivingGhanaTruckoutMtntSummaryBinding
import com.olam.warehouse.vegax.receivingghanacash.ui.VegaReceivingGhanaMtntViewModel
import com.olam.warehouse.vegax.receivingghanacash.utils.DIRECTIONOUT
import com.olam.warehouse.vegax.receivingghanacash.utils.MTNTDATA
import com.olam.warehouse.vegax.receivingghanacash.utils.MTNT_POST_DATA
import com.olam.warehouse.vegax.receivingghanacash.utils.getTmpId
import kotlinx.android.synthetic.main.item_vega_receiving_ghana_bag_summary_details.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper
import java.io.ByteArrayOutputStream

/**
 * Created by Baskaran Kannan on 2/14/2020.
 */
class VegaReceivingGhanaTruckOutMtntSummaryFragment : BaseFragment() {


    private var mMtntList = mutableListOf<VegaMtnt>()
    private var mtntData = VegaMtnt()
    private var mBagsTarWeight: Double? = 0.0

    private val vm: VegaReceivingGhanaMtntViewModel by viewModel()
    private lateinit var binding: FragmentVegaReceivingGhanaTruckoutMtntSummaryBinding
    override val layoutResourceId = R.layout.fragment_vega_receiving_ghana_truckout_mtnt_summary

    companion object {
        fun newInstance(mtntData: VegaMtnt, mMtntList: ArrayList<VegaMtnt>) =
            VegaReceivingGhanaTruckOutMtntSummaryFragment().putArgs {
                putParcelable(MTNTDATA, mtntData)
                putParcelableArrayList(MTNT_POST_DATA, mMtntList)
            }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentVegaReceivingGhanaTruckoutMtntSummaryBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("receiving/ui/truckout/mtnt/VegaTruckOutMtntSummaryFragment").title("Receiving")
            .with(tracker)
        initUI()
    }

    private fun initUI() {
        mMtntList = arguments?.getParcelableArrayList<VegaMtnt>(MTNT_POST_DATA)!!
        mtntData = arguments?.getParcelable<VegaMtnt>(MTNTDATA)!!

        binding.tvMaterial.text = ": ".plus(mtntData.materialName)
        binding.tvTruckID.text =
            getString(R.string.truck_id).plus(": ").plus(mtntData.vehicleNumber ?: mtntData.weighBridgeId)
        binding.tvSupplierName.text = mtntData.supplierName ?: mtntData.supplierCode
        binding.tvWeight.text = mtntData.tareWeight.plus(" ").plus(mtntData.unitsOfMeasure)
        val times = mtntData.erdat?.split('(', ')')
        binding.tvDate.text = times?.get(1)?.let { it1 ->
            DateUtils.getUTCDateTime(
                it1,
                App.getAppContext()
            )
        }
        binding.llBagDetails.visible()
        setBagDetails(mMtntList)
        var count = 0
        mMtntList.forEach {
            count += it.bagCount!!.toInt()
            val weight = it.bagCount!!.toDouble() * it.bagTareWeight!!.toDouble()
            mBagsTarWeight = mBagsTarWeight!! + weight
        }
        binding.tvTotalNoofBags.text = count.toString()

        binding.tvTruckGrossWeight.text = mtntData.grossWeight.plus(" ").plus(mtntData.unitsOfMeasure)
        binding.tvTruckTarWeight.text = mtntData.tareWeight.plus(" ").plus(mtntData.unitsOfMeasure)
        binding.tvNetWeight.text =
            getTotalWeight(mtntData.grossWeight!!, mtntData.tareWeight!!).toString().plus(" ")
                .plus(mtntData.unitsOfMeasure)

        binding.tvGross.text = mtntData.grossWeight
        binding.tvTarWeight.text = mtntData.tareWeight
        binding.tvBagTare.text = mBagsTarWeight?.formatThreeDigits().toString()
        val tarweight = mtntData.tareWeight?.toDouble()!! + mBagsTarWeight!!.toDouble()
        val netWeight = mtntData.grossWeight?.toDouble()!! - tarweight
        binding.tvTotalNetWeight.text =
            netWeight.formatThreeDigits().replace(",", "").plus(" ").plus(mtntData.unitsOfMeasure)
        val netData = binding.tvTotalNetWeight.text.toString().replace(",", "").split(" ")[0]
        binding.btnConfirm.setOnClickListener { if (validate(netData)) showConfirmDialog() else showSnack(getString(R.string.negative_weight_error)) }
        vm.mtnt.observe(viewLifecycleOwner, Observer { updateUI(it) })
        if (mtntData.imagePath?.isNotEmpty()!!) {
            binding.photoImageView.visible()
            binding.photoImageView.setImageBitmap(setScaledBitmap())
        }

    }

    private fun updateUI(data: Resource<GenericReqAndResp<VegaReceivingGhanaMtntResponse>>?) {
        data?.let {
            when (it.status) {
                Resource.Status.SUCCESS -> prepareSuccessData(it.data?.data?.wbId, true, it.data?.message.toString())
                Resource.Status.LOADING -> showCustomLoading()
                Resource.Status.ERROR -> {
                    hideCustomLoading()
                    prepareErrorData(mtntData.weighBridgeId, false, it.error.toString())
                    UIUtils.showErrorDialog(requireContext(), "${it.error}")
                }
            }
        }
    }

    private fun showConfirmDialog() {
        MaterialDialog(requireContext()).show {
            message(R.string.confirm_truck_out_message)
            UIUtils.getMetirialCustomView(
                this,
                getString(com.olam.warehouse.presentation.R.string.confirm),
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
        mMtntList.forEachIndexed { index, vegaMtnt ->
            vegaMtnt.bagWeight = (vegaMtnt.bagCount?.toDouble()?.let { it1 ->
                vegaMtnt.bagTareWeight?.toDouble()?.times(it1)
            }).toString()

            if (index == 0) {
                val charset = Charsets.UTF_8
                val byteArray = getBase64FromFile(mtntData.imagePath.toString())?.toByteArray(charset)
                vegaMtnt.imageString = byteArray?.let { it1 -> String(it1) }
            }
            vegaMtnt.netWeight = binding.tvTotalNetWeight.text.toString().replace(",", "").split(" ")[0]
        }
        if (AppUtils.isOnline()) {
            vm.postMtntData(VegaReceivingGhanaMtntPost(getCurrentKey(), getPlantDetails(), mMtntList))
        } else {
            prepareSuccessData(
                if (mtntData.tmpWbId.isEmpty()) getTmpId() else mtntData.tmpWbId,
                false,
                "Cached offline"
            )
        }
    }

    private fun prepareSuccessData(wbId: String?, syncStatus: Boolean, msg: String) {
        hideLoading()
        mtntData.weighBridgeId = wbId ?: ""
//        mtntData.tmpWbId = wbId ?: ""
        mtntData.truckDirection = DIRECTIONOUT
        mtntData.status = if (syncStatus) Status.MTNT_COMPLETED else Status.SYNC_PENDING
        mtntData.isSynced = syncStatus
        mtntData.syncStatusMsg = msg
        vm.saveMtnt(mtntData)
        if (!syncStatus) {
            mMtntList.forEach { it.tmpWbId = wbId ?: "" }
            vm.saveMtntLineItems(mMtntList)
        }
        moveToSuccessPage(wbId)
    }

    private fun prepareErrorData(wbId: String?, syncStatus: Boolean, msg: String) {
        hideLoading()
//        mtntData.tmpWbId = wbId ?: ""
        mtntData.truckDirection = DIRECTIONOUT
        mtntData.status = Status.SYNC_ERROR
        mtntData.isSynced = syncStatus
        mtntData.syncStatusMsg = msg
        vm.saveMtnt(mtntData)
        if (!syncStatus) {
            mMtntList.forEach { it.tmpWbId = wbId ?: "" }
            vm.saveMtntLineItems(mMtntList)
        }
    }


    private fun setBagDetails(data: MutableList<VegaMtnt>?) {
        binding.rvBagDetailSummary.setUp(data!!, R.layout.item_vega_receiving_ghana_bag_summary_details, { it, pos ->
            tvBagType.text = it.bagType
            tvNoofBags.text = it.bagCount
        })
    }

    private fun getTotalWeight(grossWeight: String, tarWeight: String): Double {
        var totalWeight = 0.0
        try {
            totalWeight = grossWeight.toDouble() - tarWeight.toDouble()
        } catch (e: NumberFormatException) {
            e.printStackTrace()
        }
        return totalWeight
    }

    fun setScaledBitmap(): Bitmap? {
        try {
            val imageViewWidth = 347
            val imageViewHeight = 413

            val bmOptions = BitmapFactory.Options()
            bmOptions.inJustDecodeBounds = true
            BitmapFactory.decodeFile(mtntData.imagePath, bmOptions)
            val bitmapWidth = bmOptions.outWidth
            val bitmapHeight = bmOptions.outHeight

            val scaleFactor = Math.min(bitmapWidth / imageViewWidth, bitmapHeight / imageViewHeight)

            bmOptions.inJustDecodeBounds = false
            bmOptions.inSampleSize = scaleFactor

            return BitmapFactory.decodeFile(mtntData.imagePath, bmOptions)
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

}
