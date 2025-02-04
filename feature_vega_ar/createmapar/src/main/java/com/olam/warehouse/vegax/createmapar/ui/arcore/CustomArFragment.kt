package com.olam.warehouse.vegax.createmapar.ui.arcore

import com.google.ar.core.Config
import com.google.ar.core.Session
import com.google.ar.sceneform.ux.ArFragment

/**
 * Created by Baskaran Kannan on 6/15/2021.
 */
class CustomArFragment : ArFragment() {
    override fun getSessionConfiguration(session: Session): Config {
        val config = Config(session)
        config.cloudAnchorMode = Config.CloudAnchorMode.ENABLED
//        config.focusMode = Config.FocusMode.AUTO
        session.configure(config)
        arSceneView.setupSession(session)
        return config
    }
}