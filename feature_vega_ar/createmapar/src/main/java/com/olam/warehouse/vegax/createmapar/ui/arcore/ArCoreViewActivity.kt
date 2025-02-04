package com.olam.warehouse.vegax.createmapar.ui.arcore

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.annotation.RawRes
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatTextView
import androidx.lifecycle.Observer
import com.google.ar.core.*
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.FrameTime
import com.google.ar.sceneform.Node
import com.google.ar.sceneform.Scene
import com.google.ar.sceneform.math.Quaternion
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.*
import com.google.ar.sceneform.ux.TransformableNode
import com.google.gson.Gson
import com.olam.warehouse.master.ui.BaseActivity
import com.olam.warehouse.master.ui.scanner.ScannerActivity
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.utils.*
import com.olam.warehouse.presentation.utils.extension.gone
import com.olam.warehouse.presentation.utils.extension.toast
import com.olam.warehouse.presentation.utils.extension.visible
import com.olam.warehouse.vegax.createmapar.R
import com.olam.warehouse.vegax.createmapar.data.domain.model.AnchorId
import com.olam.warehouse.vegax.createmapar.data.domain.model.ArLotDetails
import com.olam.warehouse.vegax.createmapar.databinding.ActivityArcoreBinding
import com.olam.warehouse.vegax.createmapar.ui.ArCoreMainActivity
import com.olam.warehouse.vegax.createmapar.ui.ArViewModel
import com.olam.warehouse.vegax.createmapar.utils.*
import kotlinx.android.synthetic.main.activity_arcore.*
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * Created by Baskaran Kannan on 6/15/2021.
 */
class ArCoreViewActivity : BaseActivity(), Scene.OnUpdateListener {

    private val vm: ArViewModel by viewModel()
    private var anchorIds: ArrayList<String> = ArrayList()
    private var arSession: Session? = null
    private val mTAG = ArCoreViewActivity::class.java.canonicalName
    private var arFragment: CustomArFragment = CustomArFragment()

    private enum class AppAnchorState {
        NONE, HOSTING, HOSTED, RESOLVING
    }

    private var appAnchorState = AppAnchorState.NONE
    private var anchor: Anchor? = null
    private var anchorList = arrayListOf<String>()
    private var lotList = arrayListOf<String>()
    private val offlineAnchorList = ArrayList<Anchor>()
    private val anchorNodeList: ArrayList<AnchorNode> = ArrayList()
    private val LinedNodeList: ArrayList<AnchorNode> = ArrayList()
    private var anchorNode: AnchorNode? = null
    private var mode: String = ""
    private var Lot_id = ""
    private var mIndex = 0
    var insertLotDialog: AlertDialog? = null
    var etLot: EditText? = null
    var etStLoc: EditText? = null
    var tvClose: AppCompatTextView? = null
    val gson = GsonUtils()
    var lotDetails: ArLotDetails? = null
    private var nodeForLine: Node? = Node()
    var lastAnchorNode: AnchorNode? = null

    override val layoutResourceId = R.layout.activity_arcore
    private lateinit var binding: ActivityArcoreBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //binding = ActivityArcoreBinding.inflate(layoutInflater)
        //setContentView(R.layout.activity_arcore)
        initExtra()

        vm.lotDeatils.observe(this, Observer { updateUI(it) })

        arFragment =
            supportFragmentManager.findFragmentById(R.id.fragmentArCore) as CustomArFragment
        /**
         * Touch listener to detect when a user touches the ArScene plane to place a model
         */
        arFragment.setOnTapArPlaneListener { hitResult, plane, motionEvent ->

            //Active only in Admin Mode
            if (mode.equals(CREATE_MAP, ignoreCase = true)) {
                Log.d("HIT_RESULT:", hitResult.toString())
                //                anchor = arFragment.getArSceneView().getSession().hostCloudAnchor(hitResult.createAnchor());
                val anchor1 = hitResult.createAnchor()
                setCloudAnchor(anchor1)
                //appAnchorState = AppAnchorState.HOSTING;
                //  showToast("Hosting...");
                anchor?.let { createCloudAnchorModel(it) }
            } else {
                showToast("Anchor can be hosted only in Admin mode")
            }
        }
        // arFragment.arSceneView.scene.addOnUpdateListener(this)
        arFragment.arSceneView.scene.addOnUpdateListener { frameTime ->
            if (appAnchorState != AppAnchorState.HOSTING && appAnchorState != AppAnchorState.RESOLVING) return@addOnUpdateListener
            val cloudAnchorState = anchor?.cloudAnchorState
            val frame = arFragment.arSceneView.arFrame
            if (appAnchorState == AppAnchorState.HOSTING) {
                if (cloudAnchorState?.isError == true) {
                    showToast(cloudAnchorState.toString())
                    appAnchorState = AppAnchorState.NONE
                } else if (cloudAnchorState == Anchor.CloudAnchorState.SUCCESS) {
                    mIndex++
                    appAnchorState = AppAnchorState.HOSTED
                    val anchorId = anchor?.cloudAnchorId
                    anchorList.add(anchorId.toString())
                    //showToast("Anchor hosted successfully. Anchor Id: " + anchorId);
                    if (mIndex == offlineAnchorList.size) {
                        val lotId: String = etStLoc?.text.toString().trim()
                        if (!lotId.equals("", ignoreCase = true)) {
                            PreferenceHelper.save(lotId, gson.toJson(anchorList))
                            // PreferenceHelper.save(lotId.plus(ANCHOR_NODES), gson.toJson(anchorNodeList))
                            //  tinydb.putListAnchorNode(lotId, (ArrayList<AnchorNode>) anchorNodeList);
                        } else {
                            showToast("Please enter the Lot Id")
                        }
                        //                    insertLotDialog.dismiss();
                        showToast("Anchor id's hosted successfully")
                        saveLotDetails()
                        hideCustomLoading()
                        finish()
                    } else {
                        syncHostAnchor(mIndex)
                    }
                }
            } else if (appAnchorState == AppAnchorState.RESOLVING) {
                if (frame != null) {
                    //get the trackables to ensure planes are detected
                    val var3 = frame.getUpdatedTrackables(Plane::class.java).iterator()
                    while (var3.hasNext()) {
                        val plane = var3.next() as Plane

                        //If a plane has been detected & is being tracked by ARCore
                        if (plane.trackingState == TrackingState.TRACKING) {
                            resolveAnchor()
                        }
                    }
                }

            }
            /* if (frame != null) {
                 //get the trackables to ensure planes are detected
                 val var3 = frame.getUpdatedTrackables(Plane::class.java).iterator()
                 while(var3.hasNext()) {
                     val plane = var3.next() as Plane
                     if (plane.trackingState == TrackingState.TRACKING) {
                         arSession = arFragment.arSceneView.session
                     }
                 }
             }*/

        }

        addQrCode.setOnClickListener { getLotID() }
        verifyQrCode.setOnClickListener { /*moveToScan()*/ drawLineAnchorNode() }

        /* ModelRenderable
             .builder()
             .setSource(this, R.raw.model)
             .build()
             .thenAccept { modelRenderable ->
                 this@MainActivity.modelRenderable = modelRenderable
             }*/
        /*val andy: CompletableFuture<ModelRenderable> = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            ModelRenderable.builder()
                .setSource(this, Uri.parse("model.sfb"))
                .build()
        } else {
            TODO("VERSION.SDK_INT < N")
        }


        CompletableFuture.allOf(andy)
            .handle { notUsed, throwable ->
                if (throwable != null) {
                    showToast("Unable to load renderables")
                    return@handle null
                }
                try {
                    modelRenderable = andy.get()
                } catch (ex: InterruptedException) {
                    showToast( "Unable to load renderables")
                } catch (ex: ExecutionException) {
                    showToast("Unable to load renderables")
                }
                null
            }*/
    }

    private fun saveLotDetails() {
        val lotDetails = lotDetails?.copy()
        lotDetails?.plant = getPlantDetails()
        val anchorIds = arrayListOf<AnchorId>()
        anchorList.forEach {
            val anchorId = AnchorId()
            anchorId.arValue = it
            anchorId.createdAt = DateUtils.getCurrentTimeInMills().toString()
            anchorId.createdBy = PreferenceHelper.get(Constants.USER_NAME, "")
            anchorId.updatedAt = DateUtils.getCurrentTimeInMills().toString()
            anchorId.updatedBy = PreferenceHelper.get(Constants.USER_NAME, "")
            anchorIds.add(anchorId)
        }
        lotDetails?.anchorIds = anchorIds
        lotDetails?.createdAt = DateUtils.getCurrentTimeInMills().toString()
        lotDetails?.createdBy = PreferenceHelper.get(Constants.USER_NAME, "")
        lotDetails?.updatedAt = DateUtils.getCurrentTimeInMills().toString()
        lotDetails?.updatedBy = PreferenceHelper.get(Constants.USER_NAME, "")
        lotDetails?.let { vm.saveLotDetails(it) }
    }

    private fun updateUI(response: Resource<ArLotDetails>?) {
        response?.let {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    hideCustomLoading()
                    finish()
                }
                Resource.Status.LOADING -> showCustomLoading()
                Resource.Status.ERROR -> {
                    toast(it.error.toString())
                    hideCustomLoading()
                }
            }
        }
    }

    private fun initExtra() {
        mode = intent.getStringExtra(MODE) ?: CREATE_MAP
        Lot_id = intent.getStringExtra(LOT_ID) ?: ""
        lotDetails = intent.getParcelableExtra(LOT) ?: ArLotDetails()
        anchorIds = intent.getStringArrayListExtra(ANCHOR_IDS) ?: ArrayList()
        if (mode.equals(RESOLVE_MAP, true)) {
            appAnchorState = AppAnchorState.RESOLVING
            addQrCode.gone()
            verifyQrCode.visible()
        } else {
            addQrCode.visible()
            verifyQrCode.gone()
            if (Lot_id.isNotEmpty()) {
                addQrCode.text = getString(R.string.add_st_loc)
                tvClose?.text = getString(R.string.add_st_loc)
                etStLoc?.visible()
                etLot?.gone()
            } else {
                addQrCode.text = getString(R.string.add_lot_id)
                tvClose?.text = getString(R.string.add_lot_id)
                etStLoc?.gone()
                etLot?.visible()
            }
        }
    }

    private fun resolveAnchor() {
        appAnchorState = AppAnchorState.NONE
        //anchorIds = Gson().fromJson<List<String>>(PreferenceHelper.get(Lot_id, "")) as ArrayList<String>? ?: ArrayList()
        anchorIds.forEach {
            if (it.isEmpty()) {
                toast("No anchor Id found")
                return@forEach
            }
            val resolvedAnchor = arFragment.arSceneView.session?.resolveCloudAnchor(it)
            resolvedAnchor?.let { it1 ->
                createCloudAnchorModelForResolve(
                    it1,
                    arFragment
                ) /*addLineBetweenHits(it1)*/
            }
        }
    }

    private fun setCloudAnchor(newAnchor: Anchor) {
        /*if (anchor != null){
            anchor.detach();
        }*/
        anchor = newAnchor
        offlineAnchorList.add(anchor!!)
        appAnchorState = AppAnchorState.NONE
    }

    private fun syncHostAnchor(index: Int) {
        if (anchor != null) {
            anchor =
                arFragment.arSceneView.session?.hostCloudAnchor(offlineAnchorList[index]/*, 365*/)
            appAnchorState = AppAnchorState.HOSTING
            // showToast((anchorIds.size-index).toString())
        } else {
            showToast("No anchor to host, Please create an anchor...")
        }
    }

    private fun createCloudAnchorModel(anchor: Anchor) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            ModelRenderable
                .builder()
                .setSource(this, Uri.parse("model.sfb"))
                .build()
                .thenAccept { modelRenderable: ModelRenderable? ->
//                    modelRenderable?.isShadowCaster = true
//                    modelRenderable?.isShadowReceiver = true
//                    modelRenderable?.renderPriority = ModelRenderable.RENDER_PRIORITY_FIRST
                    if (modelRenderable != null) {
                        placeCloudAnchorModel(
                            anchor,
                            modelRenderable
                        )
                    }
                }
            // if (appAnchorState == AppAnchorState.RESOLVING) addLineBetweenHits(anchor)
        }
    }

    private fun createCloudAnchorModelForResolve(
        anchor: Anchor,
        arFragment1: CustomArFragment
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            ModelRenderable
                .builder()
                .setSource(this, Uri.parse("model.sfb"))
                .build()
                .thenAccept { modelRenderable: ModelRenderable? ->
                    if (modelRenderable != null) {
                        placeCloudAnchorModelForResolve(anchor, modelRenderable, arFragment1)
                    }
                }
            /*val sampler = Texture.Sampler.builder()
                .setMinFilter(Texture.Sampler.MinFilter.LINEAR_MIPMAP_LINEAR)
                .setMagFilter(Texture.Sampler.MagFilter.LINEAR)
                .setWrapModeR(Texture.Sampler.WrapMode.REPEAT)
                .setWrapModeS(Texture.Sampler.WrapMode.REPEAT)
                .setWrapModeT(Texture.Sampler.WrapMode.REPEAT)
                .build();
            Texture.builder()
                .setSource { applicationContext.getAssets().open("login_bg.png") }
                .setSampler(sampler)
                .build().thenAccept { texture ->
                    // 2. make a material by the texture
                    MaterialFactory.makeTransparentWithTexture(applicationContext, texture)
                        .thenAccept { material ->
                            // 3. make a model by the material

                        }
                }*/
        }
    }


    private fun placeCloudAnchorModel(anchor: Anchor, modelRenderable: ModelRenderable) {
        anchorNode = AnchorNode(anchor)

        /*anchorNodeList.forEachIndexed { index, anchorNode ->
            if (index != 0) {
                drawLine(anchorNodeList[index - 1], anchorNodeList[index])
            }
        }*/
        /*AnchorNode cannot be zoomed in or moved
        So we create a TransformableNode with AnchorNode as the parent*/
        val transformableNode = TransformableNode(arFragment.transformationSystem)
        transformableNode.localRotation = Quaternion.axisAngle(Vector3(0f, 1f, 0f), 225f)
        /*if (modelOptionsSpinner.getSelectedItem().toString() == "Straight Arrow") {
            transformableNode.localRotation = Quaternion.axisAngle(Vector3(0,1f,0), 225f)
        }
        if (modelOptionsSpinner.getSelectedItem().toString() == "Right Arrow") {
            transformableNode.localRotation = Quaternion.axisAngle(
                Vector3(
                    0,
                    1f,
                    0
                ), 135f
            )
        }
        if (modelOptionsSpinner.getSelectedItem().toString() == "Left Arrow") {
            transformableNode.localRotation = Quaternion.axisAngle(
                Vector3(
                    0,
                    1f,
                    0
                ), 315f
            )
        }*/
        transformableNode.setParent(anchorNode)
        //adding the model to the transformable node
        transformableNode.renderable = modelRenderable
        //adding this to the scene
        arFragment.arSceneView.scene.addChild(anchorNode)
        /*if(lastAnchorNode!=null){
            drawLine(lastAnchorNode!!, anchorNode!!)
        }*/
        anchorNodeList.add(anchorNode!!)
        lastAnchorNode = anchorNode as AnchorNode

    }

    private fun placeCloudAnchorModelForResolve(
        anchor: Anchor,
        modelRenderable: ModelRenderable,
        arFragment1: CustomArFragment
    ) {
        anchorNode = AnchorNode(anchor)
        val transformableNode = TransformableNode(arFragment1.transformationSystem)
        transformableNode.localRotation = Quaternion.axisAngle(Vector3(0f, 1f, 0f), 225f)
        transformableNode.renderable = modelRenderable
        transformableNode.setParent(anchorNode)
        arFragment1.arSceneView.scene.addChild(anchorNode)
        anchorNodeList.add(anchorNode!!)
        lastAnchorNode = anchorNode as AnchorNode
    }


    private fun addLineBetweenHits(anchor1: Anchor) {

        val anchorNode = AnchorNode(anchor1)
        if (lastAnchorNode != null) {
            anchorNode.setParent(arFragment.arSceneView.scene)
            val point1: Vector3
            val point2: Vector3
            point1 = lastAnchorNode!!.worldPosition
            point2 = anchorNode.worldPosition

            /*
        First, find the vector extending between the two points and define a look rotation
        in terms of this Vector.
    */
            val difference =
                Vector3.subtract(point1, point2)
            val directionFromTopToBottom = difference.normalized()
            val rotationFromAToB =
                Quaternion.lookRotation(
                    directionFromTopToBottom,
                    Vector3.up()
                )
            MaterialFactory.makeOpaqueWithColor(applicationContext, Color(0f, 05f, 30f))
                .thenAccept { material ->
                    /* Then, create a rectangular prism, using ShapeFactory.makeCube() and use the difference vector
                                               to extend to the necessary length.  */
                    val model = ShapeFactory.makeCube(
                        Vector3(0.1f, 0.1f, difference.length()),
                        Vector3.zero(),
                        material
                    )
                    /* Last, set the world rotation of the node to the rotation calculated earlier and set the world position to
                                               the midpoint between the given points . */
                    val node = Node()
                    node.setParent(anchorNode)
                    node.renderable = model
                    node.worldPosition = Vector3.add(point1, point2).scaled(.5f)
                    node.worldRotation = rotationFromAToB
                }
        }
        lastAnchorNode = anchorNode
    }

    private fun showToast(s: String) {
        Toast.makeText(this, s, Toast.LENGTH_SHORT).show()
    }


    private fun drawLine(node1: AnchorNode, node2: AnchorNode) {
        //Draw a line between two AnchorNodes (adapted from https://stackoverflow.com/a/52816504/334402)
        val point1: Vector3 = node1.worldPosition
        val point2: Vector3 = node2.worldPosition

        //First, find the vector extending between the two points and define a look rotation
        //in terms of this Vector.
        val difference = Vector3.subtract(point1, point2)
        val directionFromTopToBottom = difference.normalized()
        val rotationFromAToB =
            Quaternion.lookRotation(
                directionFromTopToBottom,
                Vector3.up()
            )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            MaterialFactory.makeOpaqueWithColor(applicationContext, Color(0f, 05f, 30f))
                .thenAccept { material: Material? ->
                    /* Then, create a rectangular prism, using ShapeFactory.makeCube() and use the difference vector to extend to the necessary length.  */
                    val model =
                        ShapeFactory.makeCube(
                            Vector3(0.1f, 0.1f, difference.length()),
                            Vector3.zero(),
                            material
                        )
//                    val model = ShapeFactory.makeSphere(.1f, Vector3.zero(), material)
                    /* Last, set the world rotation of the node to the rotation calculated earlier and set the world position to the midpoint between the given points . */
                    val lineAnchor = node2.anchor
                    nodeForLine = Node()
                    nodeForLine?.setParent(node1)
                    nodeForLine?.renderable = model
                    nodeForLine?.worldPosition = Vector3.add(point1, point2).scaled(.5f)
                    nodeForLine?.worldRotation = rotationFromAToB
                }
        }
    }

    private fun getAndy(): Node? {
        val base = Node()
        base.renderable = modelRenderable
        val c: Context = this
        base.setOnTapListener { v, event ->
            Toast.makeText(
                c, "Andy touched.", Toast.LENGTH_LONG
            )
                .show()
        }
        return base
    }

    /**
     * Used to load model and set it on ArScene where a user Taps
     */
    private fun setModelOnUi(hitResult: HitResult) {
        loadModel(Uri.parse("model.sfb")) { modelRenderable ->
            //Used to get anchor point on scene where user tapped
            //var anchor = hitResult.createAnchor()
            //Created an anchor node to attach the anchor with its parent
            val anchorNode = AnchorNode(anchor)
            //Added arSceneView as parent to the anchorNode. So our anchors will bind to arSceneView.
            anchorNode.setParent(arFragment.arSceneView.scene)

            //TransformableNode for out model. So that it can be rotated, scaled etc using gestures
            val transformableNode = TransformableNode(arFragment.transformationSystem)
            //Assigned anchorNode as parent so that our model stays at the position where user taps
            transformableNode.setParent(anchorNode)
            //Assigned the resulted model received from loadModel method to transformableNode
            transformableNode.renderable = modelRenderable
            //Sets this node as selected node by default
            //adding this to the scene
            arFragment.arSceneView.scene.addChild(anchorNode)
            transformableNode.select()
        }
    }

    private var modelRenderable: ModelRenderable? = null

    /**
     * Used to laod models from 'raw' with a callback when loading is complete
     */
    fun loadModel(
        @SuppressLint("SupportAnnotationUsage") @RawRes model: Uri,
        callback: (ModelRenderable) -> Unit
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            ModelRenderable
                .builder()
                .setSource(this, model)
                .build()
                .thenAccept { modelRenderable ->
                    callback(modelRenderable)
                    this@ArCoreViewActivity.modelRenderable = modelRenderable
                }
        }
    }

    var placed = false
    override fun onUpdate(frameTime: FrameTime?) {
        /* val frame = arFragment.arSceneView.arFrame ?: return

         // If there is no frame, just return.

         // If there is no frame, just return.

         //Making sure ARCore is tracking some feature points, makes the augmentation little stable.

         //Making sure ARCore is tracking some feature points, makes the augmentation little stable.
         if (frame.camera.trackingState === TrackingState.TRACKING && !placed) {
             val pos = frame.camera.pose.compose(Pose.makeTranslation(0f, 0f, -0.3f))
             val anchor = arFragment.arSceneView.session!!.createAnchor(pos)
             val anchorNode =
                 AnchorNode(anchor)
             anchorNode.setParent(arFragment.arSceneView.scene)

             // Create the arrow node and add it to the anchor.
             val arrow = Node()
             arrow.setParent(anchorNode)
             arrow.setRenderable(modelRenderable)
            // placed = true //to place the arrow just once.
         }*/
        //get the frame from the scene for shorthand
        val frame = arFragment.arSceneView.arFrame
        if (frame != null) {
            //get the trackables to ensure planes are detected
            val var3 = frame.getUpdatedTrackables(Plane::class.java).iterator()
            while (var3.hasNext()) {
                val plane = var3.next() as Plane

                //If a plane has been detected & is being tracked by ARCore
                if (plane.trackingState == TrackingState.TRACKING) {

                    //Hide the plane discovery helper animation
                    arFragment.planeDiscoveryController.hide()


                    //Get all added anchors to the frame
                    val iterableAnchor = frame.updatedAnchors.iterator()

                    //place the first object only if no previous anchors were added
                    if (!iterableAnchor.hasNext()) {
                        //Perform a hit test at the center of the screen to place an object without tapping
                        val hitTest = frame.hitTest(frame.screenCenter().x, frame.screenCenter().y)

                        //iterate through all hits
                        val hitTestIterator = hitTest.iterator()
                        while (hitTestIterator.hasNext()) {
                            val hitResult = hitTestIterator.next()

                            //Create an anchor at the plane hit
                            val modelAnchor = plane.createAnchor(hitResult.hitPose)

                            //Attach a node to this anchor with the scene as the parent
                            val anchorNode = AnchorNode(modelAnchor)
                            anchorNode.setParent(arFragment.arSceneView.scene)

                            //create a new TranformableNode that will carry our object
                            val transformableNode =
                                TransformableNode(arFragment.transformationSystem)
                            transformableNode.setParent(anchorNode)
                            transformableNode.renderable = this@ArCoreViewActivity.modelRenderable

                            //Alter the real world position to ensure object renders on the table top. Not somewhere inside.
                            transformableNode.worldPosition = Vector3(
                                modelAnchor.pose.tx(),
                                modelAnchor.pose.compose(Pose.makeTranslation(0f, 0.05f, 0f)).ty(),
                                modelAnchor.pose.tz()
                            )
                        }
                    }
                }
            }
        }
    }

    private fun Frame.screenCenter(): Vector3 {
        val vw = findViewById<View>(android.R.id.content)
        return Vector3(vw.width / 2f, vw.height / 2f, 0f)
    }

    private fun moveToScan() {
        val intent = Intent(this, ScannerActivity::class.java)
        startActivityForResult(intent, Constants.SCAN_QR)
    }

    private fun drawLineAnchorNode() {
//        if(anchorIds.size == anchorNodeList.size) {
        anchorNodeList.forEachIndexed { index, anchorNode ->
            if (index != 0) {
                drawLine(anchorNodeList[index - 1], anchorNodeList[index])
            }
        }
//        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == Constants.SCAN_QR) {
            if (resultCode == Activity.RESULT_OK) {
                data?.extras?.getString(Constants.SCANNED_ID)?.trim()?.let {
                    moveToLotVerify()
                }
            }
        }
    }

    private fun moveToLotVerify() {
        val intent = Intent(this, ArCoreMainActivity::class.java)
        intent.putExtra(
            UIUtils.TYPE,
            getString(com.olam.warehouse.presentation.R.string.lot_details)
        )
        intent.putExtra(LOT_ID, lotDetails)
        startActivity(intent)
        finish()
    }

    private fun getLotID() {
        val factory = LayoutInflater.from(this)
        val lotDialog: View = factory.inflate(R.layout.popup_details, null)
        etLot = lotDialog.findViewById<EditText>(R.id.et_lot)
        etStLoc = lotDialog.findViewById<EditText>(R.id.etStLoc)
        tvClose = lotDialog.findViewById<AppCompatTextView>(R.id.tvClose)
        val btnLot = lotDialog.findViewById<Button>(R.id.btn_lot_id)
        insertLotDialog = AlertDialog.Builder(this).create()
        insertLotDialog?.setView(lotDialog)
        insertLotDialog?.show()
        if (mode.equals(CREATE_MAP, true)) {
            //if(Lot_id.isNotEmpty()){
            addQrCode.text = getString(R.string.add_st_loc)
            tvClose?.text = getString(R.string.add_st_loc)
            etStLoc?.visible()
            if (lotDetails?.storageLocationCode?.isNotEmpty() == true) etStLoc?.setText(lotDetails?.storageLocationCode.toString())
            etLot?.gone()
            /* }else{
                 addQrCode.text = getString(R.string.add_lot_id)
                 tvClose?.text = getString(R.string.add_lot_id)
                 etStLoc?.visible()
                 etLot?.visible()
             }*/
        }
        tvClose?.setOnClickListener { insertLotDialog?.dismiss() }
        // resultDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        btnLot.setOnClickListener { v: View? ->
            //  Toast.makeText(MainActivity.this,"Product Added In Cart", Toast.LENGTH_LONG).show();
            /*String lotId=etLot.getText().toString().trim();
                    if (lotId!=null && !lotId.equalsIgnoreCase("")){
                          tinydb.putListString(lotId,anchorList);
                    }else{
                        showToast("Please enter the Lot Id");
                    }
                    insertLotDialog.dismiss();
                    finish();*/insertLotDialog!!.dismiss()
            if (etStLoc?.text?.isNotEmpty() == true) lotDetails?.storageLocationCode =
                etStLoc?.text.toString()
            if (etLot?.text?.isNotEmpty() == true) lotDetails?.lotId = etLot?.text.toString()
            syncHostAnchor(mIndex)
            showCustomLoading("hosting anchor...")
            lotList =
                Gson().fromJson<List<String>>(
                    PreferenceHelper.get(
                        LOT_LIST,
                        ""
                    )
                ) as ArrayList<String>? ?: ArrayList()
            lotList.add(etLot?.text.toString())
            PreferenceHelper.save(LOT_LIST, gson.toJson(lotList))
        }
    }
}
