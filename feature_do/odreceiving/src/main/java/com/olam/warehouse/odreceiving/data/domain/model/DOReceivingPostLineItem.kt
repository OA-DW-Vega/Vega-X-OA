package com.olam.warehouse.odreceiving.data.domain.model

import com.olam.warehouse.master.dorigin.entity.DOReceivingLineItem
import com.olam.warehouse.master.user.model.Plant

/**
 * Created by Baskaran Kannan on 1/21/2020.
 */
data class DOReceivingPostLineItem(val key: String, val plant: Plant, val weighDetails: List<DOReceivingLineItem>)
