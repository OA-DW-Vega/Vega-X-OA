package com.olam.warehouse.presentation.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.afollestad.materialdialogs.MaterialDialog
import com.google.android.material.snackbar.Snackbar
import com.olam.warehouse.presentation.R
import com.olam.warehouse.presentation.ui.widget.CustomProgressBar
import com.olam.warehouse.presentation.utils.UIUtils.getMetirialCustomView
import com.olam.warehouse.presentation.utils.extension.action
import com.olam.warehouse.presentation.utils.extension.gone
import com.olam.warehouse.presentation.utils.extension.snack
import com.olam.warehouse.presentation.utils.extension.visible
import kotlinx.android.synthetic.main.activity_base_content.*
import kotlinx.android.synthetic.main.dialog_start_loading_layout.view.*
import kotlinx.android.synthetic.main.error_content.*

/**
 * Created by SangiliPandian C on 18-11-2019.
 */
abstract class BaseFragment : Fragment() {

    private var currentView: View? = null
    private var lastViewId: Int? = null
    private val progressBar = CustomProgressBar()

    @get:LayoutRes
    protected abstract val layoutResourceId: Int

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_base_content, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init(layoutResourceId)
    }


    private fun init(layoutId: Int) {
        if (vsContent != null) {
            vsContent.layoutResource = layoutId
            currentView = vsContent.inflate()
        }
        currentView?.let { loadView(it) }
    }

    private fun loadView(currentView: View) {
        lastViewId?.let { view?.findViewById<View>(it)?.gone() }
        lastViewId = currentView.id
        currentView.visible()
    }

    //fun hideLoading() = progressbar?.gone()

    // fun showLoading() = progressbar?.visible()

    fun isLoading() = progressbar != null

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
        tvErrorMsg?.text = errorMsg
        //tool_bar?.gone()
    }

    private fun loadErrorView(layoutId: Int) {
        if (vsError != null) {
            vsError.layoutResource = layoutId
            currentView?.gone()
            currentView = vsError.inflate()
        }
        currentView?.let { loadView(it) }
    }

    protected fun showDialog(msg: String) {
        MaterialDialog(requireContext()).show {
            message(text = msg)
            getMetirialCustomView(this, getString(R.string.close), "", { dismiss() }, { })
        }
    }

    protected fun showSnack(string: String) {
        view?.snack(string, Snackbar.LENGTH_INDEFINITE) {
            action(
                "OK", color = ContextCompat.getColor(
                    requireContext(),
                    R.color.red
                )
            ) { dismiss() }
        }
    }

    fun showDialog(
        message: String,
        listener: DialogClick,
        isRemarkNeed: Boolean, remarkValue: String = ""
    ) {
        val view = LayoutInflater.from(activity!!).inflate(R.layout.dialog_start_loading_layout, null)
        view.tvMsg.text = message
        view.etRemark.hint = getString(R.string.enter_remark)
        view.etRemark.visibility = if (isRemarkNeed) View.VISIBLE else View.GONE
        view.etRemark.setText(remarkValue)
        val alertBuilder = AlertDialog.Builder(activity!!).setView(view)
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
}
