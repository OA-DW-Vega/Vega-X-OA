package com.olam.warehouse.login.ui.bottomsheet

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.olam.warehouse.login.databinding.BottomDialogUserProfileBinding
import com.olam.warehouse.login.databinding.VegaInventoryBottomSheetBinding
import com.olam.warehouse.master.common.utils.getCurrentKey
import com.olam.warehouse.master.common.utils.getCurrentUserName
import com.olam.warehouse.master.common.utils.getPlantDetails
import com.olam.warehouse.master.common.utils.getRoles
import com.olam.warehouse.master.user.model.Plant
import com.olam.warehouse.presentation.utils.extension.putArgs

class VegaUserProfileScreen: BottomSheetDialogFragment() {


    companion object {
        fun newInstance() = VegaUserProfileScreen().putArgs {

        }
    }

    private lateinit var binding: BottomDialogUserProfileBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = BottomDialogUserProfileBinding.inflate(layoutInflater)
        dialog?.setCanceledOnTouchOutside(false)
        return binding.root
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        return super.onCreateDialog(savedInstanceState).apply {
            // window?.setDimAmount(0.2f) // Set dim amount here
            setOnShowListener {
                val bottomSheet =
                    findViewById<View>(com.google.android.material.R.id.design_bottom_sheet) as FrameLayout
                bottomSheet.setBackgroundResource(android.R.color.transparent)
            }
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initUI()
    }

    private fun initUI() {
        binding.tvUserNameValue.text=getCurrentUserName()
        binding.tvPlantIdValue.text= getPlantDetails().plantId
        binding.tvKeyValue.text= getCurrentKey()
        binding.tvPlantNameValue.text= getPlantDetails().plantName
        var roleString = ""
        val role = getRoles(getCurrentKey())
        role.forEach {
            roleString += it.plus("\n")
        }
        binding.tvRoleValue.text=roleString
    }
}
