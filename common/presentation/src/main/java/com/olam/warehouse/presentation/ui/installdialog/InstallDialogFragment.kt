package com.olam.warehouse.presentation.ui.installdialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import com.olam.warehouse.navigation.FeatureManager
import com.olam.warehouse.presentation.R
import com.olam.warehouse.presentation.databinding.InstallFeatureDialogBinding
import com.olam.warehouse.presentation.utils.extension.animateBetween
import com.olam.warehouse.presentation.utils.extension.animateProgress
import com.olam.warehouse.presentation.utils.extension.observeExt
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * Created by SangiliPandian C on 26-11-2019.
 */
private const val ARG_FEATURE_ID = "com.jeppeman.jetpackplayground.ARG_FEATURE_ID"
private const val ARG_FEATURE_ACTION_ID = "com.jeppeman.jetpackplayground.ARG_FEATURE_ACTION_ID"

fun createInstallDialogFragment(feature: String): InstallDialogFragment = InstallDialogFragment().apply {
    arguments = bundleOf(ARG_FEATURE_ID to feature)
}

fun createInstallDialogFragment(feature: Int): InstallDialogFragment = InstallDialogFragment().apply {
    arguments = bundleOf(ARG_FEATURE_ACTION_ID to feature)
}

class InstallDialogFragment : DialogFragment() {

    private val installDialogViewModel: InstallDialogViewModelImpl by viewModel()
    private lateinit var binding: InstallFeatureDialogBinding

    private fun progressTo(to: Int) {
        binding.progressValueText.animateProgress(binding.loader.progress, to, 300)
        binding.loader.animateBetween(binding.loader.progress, to, 333)
    }

    private fun handleDownloadingState(state: FeatureManager.InstallState.Downloading) {
        binding.progressText.text = getString(R.string.install_dialog_step_downloading)
        progressTo(state.progress)
    }

    private fun handleInstallingState(state: FeatureManager.InstallState.Installing) {
        binding.progressText.text = getString(R.string.install_dialog_installing)
        progressTo(state.progress)
    }

    private fun handleInstalledState(state: FeatureManager.InstallState.Installed) {
        binding.progressText.text = getString(R.string.install_dialog_installed)
        progressTo(100)
        view?.postDelayed({
            if (fragmentManager != null) {
                dismiss()
            }
        }, 500)
    }

    private fun handleFailedState(state: FeatureManager.InstallState.Failed) {
        binding.progressText.text = getString(R.string.install_dialog_failed, state.code.toString())
    }

    private fun handleUserConfirmationRequired(state: FeatureManager.InstallState.RequiresUserConfirmation) {
        activity?.startIntentSender(state.sender, null, 0, 0, 0)
    }

    private fun onInstallStateChanged(state: FeatureManager.InstallState?) {
        if (state != null) {
            dialog?.setTitle(getString(R.string.install_dialog_title, state.featureInfo.name))
            binding.installDialogContainer.transitionToEnd()
        }

        when (state) {
            is FeatureManager.InstallState.RequiresUserConfirmation -> handleUserConfirmationRequired(state)
            is FeatureManager.InstallState.Downloading -> handleDownloadingState(state)
            is FeatureManager.InstallState.Installing -> handleInstallingState(state)
            is FeatureManager.InstallState.Installed -> handleInstalledState(state)
            is FeatureManager.InstallState.Failed -> handleFailedState(state)
            else -> {}
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.DialogTheme)

        if (savedInstanceState == null) {
            val featureId = arguments?.getString(ARG_FEATURE_ID)
            val featureActionId = arguments?.getInt(ARG_FEATURE_ACTION_ID)
            when {
                featureId != null -> installDialogViewModel.installFeature(featureId)
                featureActionId != null -> installDialogViewModel.installFeature(featureActionId)
                else -> dismiss()
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = InstallFeatureDialogBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        installDialogViewModel.installState.observeExt(viewLifecycleOwner, ::onInstallStateChanged)

        dialog?.window?.setWindowAnimations(R.style.DialogAnimation)
        dialog?.setCanceledOnTouchOutside(false)
    }
}
