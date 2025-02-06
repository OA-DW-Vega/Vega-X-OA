package com.olam.warehouse.presentation.data.remote

/**
 * Created by SangiliPandian C on 17-11-2019.
 */

import kotlinx.coroutines.CoroutineDispatcher

class AppDispatchers(
    val main: CoroutineDispatcher,
    val io: CoroutineDispatcher
)
