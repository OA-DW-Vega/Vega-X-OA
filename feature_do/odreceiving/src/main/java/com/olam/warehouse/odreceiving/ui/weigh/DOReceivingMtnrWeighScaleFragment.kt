package com.olam.warehouse.odreceiving.ui.weigh

import android.os.Bundle
import com.olam.warehouse.master.dorigin.entity.DOReceiving
import com.olam.warehouse.odreceiving.R
import com.olam.warehouse.odreceiving.utils.RECEIVING_DATA
import com.olam.warehouse.odreceiving.utils.RECEIVING_MTN
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.UIUtils.getActionBtnChangedView
import com.olam.warehouse.presentation.utils.extension.putArgs
import kotlinx.android.synthetic.main.fragment_do_receiving_mtnr_weigh_scale.*

/**
 * Created by Baskaran Kannan on 12/30/2019.
 */
class DOReceivingMtnrWeighScaleFragment : BaseFragment() {

    override val layoutResourceId = R.layout.fragment_do_receiving_mtnr_weigh_scale

    companion object {
        fun newInstance(data: DOReceiving, lots: String) =
            DOReceivingMtnrWeighScaleFragment().putArgs {
                putParcelable(RECEIVING_DATA, data)
                putString(RECEIVING_MTN, lots)
            }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        context?.let {
            getActionBtnChangedView(llTitle, it, false)
            getActionBtnChangedView(btnProceed, it, true)
        }
    }
}
