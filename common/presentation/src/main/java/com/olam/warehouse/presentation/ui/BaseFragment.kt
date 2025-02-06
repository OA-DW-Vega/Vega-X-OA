package com.olam.warehouse.presentation.ui

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.ViewCompat
import androidx.fragment.app.Fragment
import com.afollestad.materialdialogs.MaterialDialog
import com.google.android.material.snackbar.Snackbar
import com.olam.warehouse.presentation.R
import com.olam.warehouse.presentation.databinding.DialogRadioOptionBinding
import com.olam.warehouse.presentation.databinding.DialogStartLoadingLayoutBinding
import com.olam.warehouse.presentation.databinding.FragmentBaseContentBinding
import com.olam.warehouse.presentation.ui.widget.CustomProgressBar
import com.olam.warehouse.presentation.utils.UIUtils.getMetirialCustomView
import com.olam.warehouse.presentation.utils.extension.*

/**
 * Created by SangiliPandian C on 18-11-2019.
 */
abstract class BaseFragment : Fragment() {

    private var currentView: View? = null
    private var lastViewId: Int? = null
    private val progressBar = CustomProgressBar()
    private lateinit var binding: FragmentBaseContentBinding
    private var permissionCallBack: PermissionObserve? = null

    companion object {
        const val ALL_PERMISSIONS = 10
        val REQUIRED_CAMERA_PERMISSIONS =
            mutableListOf (
                Manifest.permission.CAMERA
            ).apply {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    add(Manifest.permission.READ_MEDIA_IMAGES)
                    add(Manifest.permission.READ_MEDIA_VIDEO)
                    add(Manifest.permission.READ_MEDIA_AUDIO)
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                }
            }.toTypedArray()
    }

    @get:LayoutRes
    protected abstract val layoutResourceId: Int

    interface PermissionObserve {
        fun grandValue(grandValue: Boolean)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentBaseContentBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init(layoutResourceId)
    }


    private fun init(layoutId: Int) {
        /*if (vsContent != null) {
            vsContent.layoutResource = layoutId
            currentView = vsContent.inflate()
        }*/
        currentView?.let { loadView(it) }
    }

    private fun loadView(currentView: View) {
        lastViewId?.let { view?.findViewById<View>(it)?.gone() }
        lastViewId = currentView.id
        currentView.visible()
    }

    //fun hideLoading() = progressbar?.gone()

    // fun showLoading() = progressbar?.visible()

//    fun isLoading() = progressbar != null

    fun showLoading() {
        when {
            progressBar.dialog == null -> progressBar.showLoading(requireContext())
            !progressBar.dialog!!.isShowing -> progressBar.showLoading(requireContext())
        }
    }

    fun hideLoading() = progressBar.dialog?.dismiss()

    fun showCustomLoading(text: String = getString(R.string.loading_text)) {
        when {
            progressBar.dialog == null -> progressBar.show(requireContext(), text)
            !progressBar.dialog!!.isShowing -> progressBar.show(requireContext(), text)
        }
    }

    fun hideCustomLoading() = progressBar.dialog?.dismiss()

    protected fun setContentViewVisibility(visibility: Boolean) {
        if (visibility) currentView?.visible() else currentView?.gone()
    }

    protected fun setErrorContentView(errorMsg: String) {
        loadErrorView(R.layout.error_content)
//        tvErrorMsg?.text = errorMsg
        //tool_bar?.gone()
    }

    private fun loadErrorView(layoutId: Int) {
        /* if (vsError != null) {
             vsError.layoutResource = layoutId
             currentView?.gone()
             currentView = vsError.inflate()
         }*/
        currentView?.let { loadView(it) }
    }

    protected fun showDialog(msg: String) {
        MaterialDialog(requireContext()).show {
            message(text = msg)
            getMetirialCustomView(this, getString(R.string.close), "", { dismiss() }, { })
        }
    }

    protected fun showOkDialog(msg: String) {
        MaterialDialog(requireContext()).show {
            message(text = msg)
            getMetirialCustomView(this, getString(R.string.ok), "", { dismiss() }, { dismiss() })
        }
    }

    protected fun showSnack(string: String) {
        view?.snack(string, Snackbar.LENGTH_INDEFINITE) {
            changeFont()
            action(
                "OK", color = ContextCompat.getColor(
                    requireContext(),
                    R.color.red
                )
            ) { dismiss() }
        }
    }
    fun Snackbar.changeFont()
    {
        val tv = view.findViewById(com.google.android.material.R.id.snackbar_text) as TextView
        val typeface = ResourcesCompat.getFont(requireActivity(), R.font.gilroy_medium)
        tv.typeface = typeface
    }

    fun showDialog(
        message: String,
        listener: DialogClick,
        isRemarkNeed: Boolean, remarkValue: String = ""
    ) {
        //val view = LayoutInflater.from(activity!!).inflate(R.layout.dialog_start_loading_layout, null)
        val view = DialogStartLoadingLayoutBinding.inflate(layoutInflater)
        view.tvMsg.text = message
        view.etRemark.hint = getString(R.string.enter_remark)
        view.etRemark.visibility = if (isRemarkNeed) View.VISIBLE else View.GONE
        view.etRemark.setText(remarkValue)
        val alertBuilder = AlertDialog.Builder(requireActivity()).setView(view.root)
        val alertDialog = alertBuilder.create()
        view.btConform.setOnClickListener {
            listener.onPositive(if (isRemarkNeed) view.etRemark.text.toString() else "")
            if (isRemarkNeed) {
                if (view.etRemark.text.toString().isNotEmpty())
                    alertDialog.dismiss()
            } else alertDialog.dismiss()
        }
        view.btCancel.setOnClickListener {
            alertDialog.dismiss()
        }
        ViewCompat.setBackgroundTintList(
            view.btCancel,
            ContextCompat.getColorStateList(
                activity?.applicationContext!!,
                R.color.colorPrimaryOfi
            )
        )
        alertDialog.show()
    }

    fun showDialogWithRadioChoice(listener: DialogClick){
        var pos= -1
        val view = DialogRadioOptionBinding.inflate(layoutInflater)
        val alertBuilder = AlertDialog.Builder(requireActivity()).setView(view.root)
        val alertDialog = alertBuilder.create()

        view.rb1.setOnCheckedChangeListener { buttonView, isChecked ->  if(isChecked) pos=0 }
        view.rb2.setOnCheckedChangeListener { buttonView, isChecked ->  if(isChecked) pos=1 }

        view.btConform.setOnClickListener {
            listener.onPositive(if (pos>=0) pos.toString() else "-1")
            if (pos>=0) {
                    alertDialog.dismiss()
            }
        }
        view.btCancel.setOnClickListener {
            alertDialog.dismiss()
        }
        alertDialog.show()
    }

    fun showDialogWithInput(
        message: String,
        listener: DialogClick,isInputNeeded: Boolean,
        hint: String, remarkValue: String = ""
    ) {
        //val view = LayoutInflater.from(activity!!).inflate(R.layout.dialog_start_loading_layout, null)
        val view = DialogStartLoadingLayoutBinding.inflate(layoutInflater)
        view.tvMsg.text = message
        view.etRemark.hint = hint
        view.etRemark.visibility = View.VISIBLE
        view.etRemark.setText(remarkValue)
        val alertBuilder = AlertDialog.Builder(requireActivity()).setView(view.root)
        val alertDialog = alertBuilder.create()
        view.btConform.setOnClickListener {
            listener.onPositive(if (isInputNeeded) view.etRemark.text.toString() else "")
            if (isInputNeeded) {
                if (view.etRemark.text.toString().isNotEmpty())
                    alertDialog.dismiss()
            } else alertDialog.dismiss()
        }
        view.btCancel.setOnClickListener {
            alertDialog.dismiss()
        }
        alertDialog.show()
    }


    interface DialogClick {
        fun onPositive(remark: String)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (progressBar.dialog != null) {
            progressBar.dialog?.dismiss()
            progressBar.dialog = null
        }
    }

    fun setupAllPermissions(REQUIRED_PERMISSIONS: Array<String>, listner: PermissionObserve) {
        permissionCallBack = listner
        if(!hasPermissions(this.requireContext(), *REQUIRED_PERMISSIONS)){
            ActivityCompat.requestPermissions(this.requireActivity(),
                REQUIRED_PERMISSIONS, ALL_PERMISSIONS)
        }
    }

    fun hasPermissions(context: Context, vararg permissions: String): Boolean {
        try {
           return permissions.all {
                ActivityCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
            }
        }catch (e:Exception){
            return false
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
           ALL_PERMISSIONS -> {
                if (grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                    activity?.toast("PERMISSION_STATE Granted")
                    permissionCallBack?.grandValue(true)
                }else {
                    activity?.toast("PERMISSION_STATE Denied")
                    permissionCallBack?.grandValue(false)
                }
            }
            else -> super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }
}
