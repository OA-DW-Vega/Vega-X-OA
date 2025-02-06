package com.olam.warehouse.login.ui.quickpinaccess.createpin

import android.animation.ObjectAnimator
import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.graphics.drawable.AnimatedVectorDrawable
import android.hardware.fingerprint.FingerprintManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import androidx.annotation.RequiresApi
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import com.olam.warehouse.login.R
import com.olam.warehouse.login.databinding.ActivityCreatePinBinding
import com.olam.warehouse.login.ui.home.HomeBaseActivity
import com.olam.warehouse.login.ui.quickpinaccess.createpin.pinlockview.IndicatorDots
import com.olam.warehouse.login.ui.quickpinaccess.createpin.pinlockview.PinLockListener
import com.olam.warehouse.login.utils.animate
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.PreferenceHelper
import com.olam.warehouse.presentation.utils.UIUtils.EXTRA_FONT_NUM
import com.olam.warehouse.presentation.utils.UIUtils.EXTRA_FONT_TEXT
import com.olam.warehouse.presentation.utils.UIUtils.EXTRA_SET_PIN

/**
 * Created by Baskaran Kannan on 11/24/2020.
 */
class VegaCreatePinActivity : HomeBaseActivity() {
    val TAG = "VegaCreatePinActivity"

    override val layoutResourceId = R.layout.activity_create_pin
    private lateinit var binding: ActivityCreatePinBinding
    val RESULT_BACK_PRESSED: Int = RESULT_FIRST_USER

    //    public static final int RESULT_TOO_MANY_TRIES = RESULT_FIRST_USER + 1;
    private val PIN_LENGTH = 4
    private val FINGER_PRINT_KEY = "FingerPrintKey"
    private val KEY_PIN = "pin"

//    private var mCipher: Cipher? = null
//    private var mKeyStore: KeyStore? = null
//    private var mKeyGenerator: KeyGenerator? = null
    private var mCryptoObject: FingerprintManager.CryptoObject? = null
    private var mFingerprintManager: FingerprintManager? = null
    private var mKeyguardManager: KeyguardManager? = null
    private var mSetPin = false
    private var mFirstPin = ""
    //    private int mTryCount = 0;

    //    private int mTryCount = 0;
    private var showFingerprint: AnimatedVectorDrawable? = null
    private var fingerprintToTick: AnimatedVectorDrawable? = null
    private var fingerprintToCross: AnimatedVectorDrawable? = null
    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo

    fun getIntent(context: Context?, setPin: Boolean): Intent? {
        val intent = Intent(context, VegaCreatePinActivity::class.java)
        intent.putExtra(EXTRA_SET_PIN, setPin)
        return intent
    }

    fun getIntent(context: Context?, fontText: String?, fontNum: String?): Intent {
        val intent = Intent(context, VegaCreatePinActivity::class.java)
        intent.putExtra(EXTRA_FONT_TEXT, fontText)
        intent.putExtra(EXTRA_FONT_NUM, fontNum)
        return intent
    }

    fun getIntent(
        context: Context?,
        setPin: Boolean,
        fontText: String?,
        fontNum: String?
    ): Intent? {
        val intent = getIntent(context, fontText, fontNum)
        intent.putExtra(EXTRA_SET_PIN, setPin)
        return intent
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//       requestWindowFeature(Window.FEATURE_NO_TITLE)
        binding = ActivityCreatePinBinding.inflate(layoutInflater)
        setContentView(binding.root)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            showFingerprint = getDrawable(R.drawable.show_fingerprint) as AnimatedVectorDrawable?
            fingerprintToTick = getDrawable(R.drawable.fingerprint_to_tick) as AnimatedVectorDrawable?
            fingerprintToCross = getDrawable(R.drawable.fingerprint_to_cross) as AnimatedVectorDrawable?
        }
        mSetPin = intent.getBooleanExtra(EXTRA_SET_PIN, false)
        if (mSetPin) {
            changeLayoutForSetPin()
        } else {
            val pin = getPinFromSharedPreferences()
            if (pin == "") {
                changeLayoutForSetPin()
                mSetPin = true
            } else {
                promptInfo = createPromptInfo()
                biometricPrompt = createBiometricPrompt()

            }
        }
        val pinLockListener: PinLockListener = object : PinLockListener {
            override fun onComplete(pin: String?) {
                if (mSetPin) {
                    pin?.let { setPin(it) }
                } else {
                    pin?.let { checkPin(it) }
                }
            }

            override fun onEmpty() {
                if (mSetPin) {
                    PreferenceHelper.save(Constants.QUICK_PIN, "")
                }
            }

            override fun onPinChange(pinLength: Int, intermediatePin: String?) {
                Log.d(TAG, "Pin changed, new length $pinLength with intermediate pin $intermediatePin")
            }

        }
        binding.pinlockView.attachIndicatorDots(binding.indicatorDots)
        binding.pinlockView.setPinLockListener(pinLockListener)
        binding.pinlockView.setPinLength(PIN_LENGTH)
        binding.indicatorDots.setIndicatorType(IndicatorDots.IndicatorType.FILL_WITH_ANIMATION)
        checkForFont()
        binding.fingerView.setOnClickListener {
            biometricPrompt.authenticate(promptInfo)
        }
    }

    private fun createPromptInfo(): BiometricPrompt.PromptInfo {
        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(getString(R.string.bio_auth)) // e.g. "Sign in"
            //.setSubtitle(getString(R.string.prompt_info_subtitle)) // e.g. "Biometric for My App"
//           .setDescription(getString(R.string.prompt_info_description)) // e.g. "Confirm biometric to continue"
            .setConfirmationRequired(false)
            .setNegativeButtonText(getString(R.string.quick_pinlock_title)) // e.g. "Use Account Password"
            // .setDeviceCredentialAllowed(true) // Allow PIN/pattern/password authentication.
            // Also note that setDeviceCredentialAllowed and setNegativeButtonText are
            // incompatible so that if you uncomment one you must comment out the other
            .build()
        return promptInfo
    }

    private fun checkForFont() {
        val intent: Intent = intent
        if (intent.hasExtra(EXTRA_FONT_TEXT)) {
            val font = intent.getStringExtra(EXTRA_FONT_TEXT)
            font?.let { setTextFont(it) }
        }
        if (intent.hasExtra(EXTRA_FONT_NUM)) {
            val font = intent.getStringExtra(EXTRA_FONT_NUM)
            font?.let { setNumFont(it) }
        }
    }

    private fun setTextFont(font: String) {
        try {
            val typeface = Typeface.createFromAsset(assets, font)
            binding.title.typeface = typeface
            binding.attempts.typeface = typeface
            binding.fingerText.typeface = typeface
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun setNumFont(font: String) {
        try {
            val typeface = Typeface.createFromAsset(assets, font)
            binding.pinlockView.setTypeFace(typeface)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    //Create the generateKey method that we’ll use to gain access to the Android keystore and generate the encryption key//
  /*  @RequiresApi(Build.VERSION_CODES.M)
    @Throws(FingerprintException::class)
    private fun generateKey() {
        try {
            // Obtain a reference to the Keystore using the standard Android keystore container identifier (“AndroidKeystore”)//
            mKeyStore = KeyStore.getInstance("AndroidKeyStore")

            //Generate the key//
            mKeyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")

            //Initialize an empty KeyStore//
            mKeyStore!!.load(null)

            //Initialize the KeyGenerator//
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                mKeyGenerator!!.init(
                    KeyGenParameterSpec.Builder(
                        FINGER_PRINT_KEY,
                        KeyProperties.PURPOSE_ENCRYPT or
                                KeyProperties.PURPOSE_DECRYPT
                    )
                        .setBlockModes(KeyProperties.BLOCK_MODE_CBC) //Configure this key so that the user has to confirm their identity with a fingerprint each time they want to use it//
                        .setUserAuthenticationRequired(true)
                        .setEncryptionPaddings(
                            KeyProperties.ENCRYPTION_PADDING_PKCS7
                        )
                        .build()
                )
            }

            //Generate the key//
            mKeyGenerator!!.generateKey()
        } catch (exc: KeyStoreException) {
            throw FingerprintException(exc)
        } catch (exc: NoSuchAlgorithmException) {
            throw FingerprintException(exc)
        } catch (exc: NoSuchProviderException) {
            throw FingerprintException(exc)
        } catch (exc: InvalidAlgorithmParameterException) {
            throw FingerprintException(exc)
        } catch (exc: CertificateException) {
            throw FingerprintException(exc)
        } catch (exc: IOException) {
            throw FingerprintException(exc)
        }
    }

    //Create a new method that we’ll use to initialize our mCipher//
    fun initCipher(): Boolean {
        try {
            //Obtain a mCipher instance and configure it with the properties required for fingerprint authentication//
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                mCipher = Cipher.getInstance(
                    KeyProperties.KEY_ALGORITHM_AES + "/"
                            + KeyProperties.BLOCK_MODE_CBC + "/"
                            + KeyProperties.ENCRYPTION_PADDING_PKCS7
                )
            }
        } catch (e: NoSuchAlgorithmException) {
            Log.e(TAG, "Failed to get Cipher")
            return false
        } catch (e: NoSuchPaddingException) {
            Log.e(TAG, "Failed to get Cipher")
            return false
        }
        return try {
            mKeyStore!!.load(null)
            val key = mKeyStore!!.getKey(
                FINGER_PRINT_KEY,
                null
            ) as SecretKey
            mCipher!!.init(Cipher.ENCRYPT_MODE, key)
            //Return true if the mCipher has been initialized successfully//
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to init Cipher")
            false
        }
    }*/

    private fun writePinToSharedPreferences(pin: String) {
        PreferenceHelper.save(Constants.QUICK_PIN, pin)
    }

    private fun getPinFromSharedPreferences(): String? {
        return PreferenceHelper.get(Constants.QUICK_PIN, "")
    }

    private fun setPin(pin: String) {
        if (mFirstPin == "") {
            mFirstPin = pin
            binding.title.text = getString(R.string.pinlock_secondPin)
            binding.pinlockView.resetPinLockView()
        } else {
            if (pin == mFirstPin) {
                writePinToSharedPreferences(pin)
                setResult(RESULT_OK)
                finish()
            } else {
                shake()
                binding.title.text = getString(R.string.pinlock_tryagain)
                binding.pinlockView.resetPinLockView()
                mFirstPin = ""
            }
        }
    }

    private fun checkPin(pin: String) {
        if (pin.equals(getPinFromSharedPreferences())) {
            setResult(RESULT_OK)
            finish()
        } else {
            shake()

//            mTryCount++;
            binding.attempts.text = getString(R.string.pinlock_wrongpin)
            binding.pinlockView.resetPinLockView()

//            if (mTryCount == 1) {
//                binding.attempts.setText(getString(R.string.pinlock_firsttry));
//                binding.pinlockView.resetPinLockView();
//            } else if (mTryCount == 2) {
//                binding.attempts.setText(getString(R.string.pinlock_secondtry));
//                binding.pinlockView.resetPinLockView();
//            } else if (mTryCount > 2) {
//                setResult(RESULT_TOO_MANY_TRIES);
//                finish();
//            }
        }
    }

    private fun shake() {
        val objectAnimator: ObjectAnimator = ObjectAnimator.ofFloat(
            binding.pinlockView, "translationX",
            0f, 25f, -25f, 25f, -25f, 15f, -15f, 6f, -6f, 0f
        ).setDuration(1000)
        objectAnimator.start()
    }

    private fun changeLayoutForSetPin() {
        binding.fingerView.visibility = View.GONE
        binding.fingerText.visibility = View.GONE
        binding.attempts.visibility = View.GONE
        binding.title.text = getString(R.string.pinlock_settitle)
    }

    private fun createBiometricPrompt(): BiometricPrompt {
        val executor = ContextCompat.getMainExecutor(this)

        val callback = @RequiresApi(Build.VERSION_CODES.P)
        object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                Log.d(TAG, "$errorCode :: $errString")
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                Log.d(TAG, "Authentication failed for an unknown reason")
            }

            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                Log.d(TAG, "Authentication was successful")
                setResult(RESULT_OK)
                fingerprintToTick?.let { animate(binding.fingerView, it) }
                val handler = Handler()
                handler.postDelayed({ finish() }, 750)
            }
        }

        //The API requires the client/Activity context for displaying the prompt view
        val biometricPrompt = BiometricPrompt(this, executor, callback)
        return biometricPrompt
    }

    /*private fun checkForFingerPrint() {
        val fingerPrintListener: FingerPrintListener = object : FingerPrintListener {
            override fun onSuccess() {
                setResult(RESULT_OK)
                fingerprintToTick?.let { animate(binding.fingerView, it) }
                val handler = Handler()
                handler.postDelayed({ finish() }, 750)
            }

            override fun onFailed() {
                fingerprintToCross?.let { animate(binding.fingerView, it) }
                val handler = Handler()
                handler.postDelayed({ showFingerprint?.let { animate(binding.fingerView, it) } }, 750)
            }

            override fun onError(errorString: CharSequence?) {
                toast(errorString.toString())
            }

            override fun onHelp(helpString: CharSequence?) {
                toast(helpString.toString())
            }
        }

        // If you’ve set your app’s minSdkVersion to anything lower than 23, then you’ll need to verify that the device is running Marshmallow
        // or higher before executing any fingerprint-related code
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val fingerprintManager =
                getSystemService(Context.FINGERPRINT_SERVICE) as FingerprintManager?
            if (fingerprintManager != null && fingerprintManager.isHardwareDetected) {
                //Get an instance of KeyguardManager and FingerprintManager//
                mKeyguardManager = getSystemService(KEYGUARD_SERVICE) as KeyguardManager?
                mFingerprintManager = getSystemService(FINGERPRINT_SERVICE) as FingerprintManager?

                //Check whether the user has granted your app the USE_FINGERPRINT permission//
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.USE_FINGERPRINT)
                    != PackageManager.PERMISSION_GRANTED
                ) {
                    // If your app doesn't have this permission, then display the following text//
 //                Toast.makeText(EnterPinActivity.this, "Please enable the fingerprint permission", Toast.LENGTH_LONG).show();
                    binding.fingerView.setVisibility(View.GONE)
                    //                    binding.fingerText.setVisibility(View.GONE);
                    return
                }

                //Check that the user has registered at least one fingerprint//
                if (!mFingerprintManager!!.hasEnrolledFingerprints()) {
                    // If the user hasn’t configured any fingerprints, then display the following message//
 //                Toast.makeText(EnterPinActivity.this,
 //                        "No fingerprint configured. Please register at least one fingerprint in your device's Settings",
 //                        Toast.LENGTH_LONG).show();
                    binding.fingerView.setVisibility(View.GONE)
                    //                    binding.fingerText.setVisibility(View.GONE);
                    return
                }

                //Check that the lockscreen is secured//
                if (!mKeyguardManager!!.isKeyguardSecure) {
                    // If the user hasn’t secured their lockscreen with a PIN password or pattern, then display the following text//
 //                Toast.makeText(EnterPinActivity.this, "Please enable lockscreen security in your device's Settings", Toast.LENGTH_LONG).show();
                    binding.fingerView.setVisibility(View.GONE)
                    //                    binding.fingerText.setVisibility(View.GONE);
                    return
                } else {
                    try {
                        generateKey()
                        if (initCipher()) {
                            //If the mCipher is initialized successfully, then create a CryptoObject instance//
                            mCryptoObject = FingerprintManager.CryptoObject(mCipher!!)

                            // Here, I’m referencing the FingerprintHandler class that we’ll create in the next section. This class will be responsible
                            // for starting the authentication process (via the startAuth method) and processing the authentication process events//
                            val helper = FingerprintHandler(this)
                            helper.startAuth(mFingerprintManager!!, mCryptoObject)
                            helper.setFingerPrintListener(fingerPrintListener)
                        }
                    } catch (e: FingerprintException) {
                        Log.wtf(TAG, "Failed to generate key for fingerprint.", e)
                    }
                }
            } else {
                binding.fingerView.setVisibility(View.GONE)
                //                binding.fingerText.setVisibility(View.GONE);
            }
        } else {
            binding.fingerView.setVisibility(View.GONE)
            //            binding.fingerText.setVisibility(View.GONE);
        }
    }*/

    override fun onBackPressed() {
        setResult(RESULT_BACK_PRESSED)
        super.onBackPressed()
    }


    private class FingerprintException(e: Exception?) : Exception(e)
}
