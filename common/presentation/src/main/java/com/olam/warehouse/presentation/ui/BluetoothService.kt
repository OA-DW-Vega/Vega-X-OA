package com.olam.warehouse.presentation.ui

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import android.os.Bundle
import android.os.Handler
import android.util.Log
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.Constants.STATE_CONNECTED
import com.olam.warehouse.presentation.utils.Constants.STATE_CONNECTING
import com.olam.warehouse.presentation.utils.Constants.STATE_LISTEN
import com.olam.warehouse.presentation.utils.Constants.STATE_NONE
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.*


class BluetoothService(handler: Handler) {

    // Debugging
    private val TAG = "BluetoothChatService"
    private val NAME_SECURE = "BluetoothChatSecure"
    private val NAME_INSECURE = "BluetoothChatInsecure"
    private val MY_UUID_SECURE = UUID.fromString("fa87c0d0-afac-11de-8a39-0800200c9a66")
    private val MY_UUID_INSECURE = UUID.fromString("8ce255c0-200a-11e0-ac64-0800200c9a66")

    // Member fields
    var mAdapter: BluetoothAdapter? = null
    var mHandler: Handler? = null
    private var mSecureAcceptThread: AcceptThread? = null
    private var mInsecureAcceptThread: AcceptThread? = null
    private var mConnectThread: ConnectThread? = null
    private var mConnectedThread: ConnectedThread? = null
    var mState: Int = 0
    var mNewState: Int = 0


    @Synchronized
    private fun updateUserInterfaceTitle() {
        mState = getState()
        mNewState = mState
        mHandler!!.obtainMessage(
            Constants.MESSAGE_STATE_CHANGE,
            mNewState,
            -1
        ).sendToTarget()
    }

    @Synchronized
    fun getState(): Int {
        return mState
    }

    init {
        mAdapter = BluetoothAdapter.getDefaultAdapter()
        mState = STATE_NONE
        mNewState = mState
        mHandler = handler
    }

    @Synchronized
    fun start() {

        // Cancel any thread attempting to make a connection
        if (mConnectThread != null) {
            mConnectThread?.cancel()
            mConnectThread = null
        }

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {
            mConnectedThread?.cancel()
            mConnectedThread = null
        }

        // Start the thread to listen on a BluetoothServerSocket
        if (mSecureAcceptThread == null) {
            mSecureAcceptThread =
                AcceptThread(true)
            mSecureAcceptThread!!.start()
        }
        if (mInsecureAcceptThread == null) {
            mInsecureAcceptThread =
                AcceptThread(false)
            mInsecureAcceptThread!!.start()
        }
        // Update UI title
        updateUserInterfaceTitle()
    }

    @Synchronized
    fun connect(device: BluetoothDevice, secure: Boolean) {
        // Cancel any thread attempting to make a connection
        if (mState == STATE_CONNECTING) {
            if (mConnectThread != null) {
                mConnectThread!!.cancel()
                mConnectThread = null
            }
        }

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {
            mConnectedThread!!.cancel()
            mConnectedThread = null
        }

        // Start the thread to connect with the given device
        mConnectThread = ConnectThread(device, secure)
        mConnectThread!!.start()
        // Update UI title
        updateUserInterfaceTitle()
    }

    @Synchronized
    fun connected(
        socket: BluetoothSocket?,
        device: BluetoothDevice,
        socketType: String
    ) {
        if (mConnectThread != null) {
            mConnectThread!!.cancel()
            mConnectThread = null
        }

        if (mConnectedThread != null) {
            mConnectedThread!!.cancel()
            mConnectedThread = null
        }

        if (mSecureAcceptThread != null) {
            mSecureAcceptThread!!.cancel()
            mSecureAcceptThread = null
        }
        if (mInsecureAcceptThread != null) {
            mInsecureAcceptThread!!.cancel()
            mInsecureAcceptThread = null
        }

        // Start the thread to manage the connection and perform transmissions
        mConnectedThread = ConnectedThread(
            socket!!,
            socketType
        )
        mConnectedThread!!.start()

        // Send the name of the connected device back to the UI Activity
        val msg =
            mHandler!!.obtainMessage(Constants.MESSAGE_DEVICE_NAME)
        val bundle = Bundle()
        bundle.putString(
            Constants.DEVICE_NAME,
            device.name
        )
        msg.data = bundle
        mHandler?.sendMessage(msg)
        // Update UI title
        updateUserInterfaceTitle()
    }

    @Synchronized
    fun stop() {
        Log.d("", "stop")
        if (mConnectThread != null) {
            mConnectThread!!.cancel()
            mConnectThread = null
        }
        if (mConnectedThread != null) {
            mConnectedThread!!.cancel()
            mConnectedThread = null
        }
        if (mSecureAcceptThread != null) {
            mSecureAcceptThread!!.cancel()
            mSecureAcceptThread = null
        }
        if (mInsecureAcceptThread != null) {
            mInsecureAcceptThread!!.cancel()
            mInsecureAcceptThread = null
        }
        mState = STATE_NONE
        // Update UI title
        updateUserInterfaceTitle()
    }

    fun write(out: ByteArray?) {
        // Create temporary object
        var r: ConnectedThread
        // Synchronize a copy of the ConnectedThread
        synchronized(this) {
            if (mState != STATE_CONNECTED) return
            r = mConnectedThread!!
        }
        // Perform the write unsynchronized
        r.write(out)
    }

    private fun connectionFailed() {
        // Send a failure message back to the Activity
        val msg =
            mHandler!!.obtainMessage(Constants.MESSAGE_TOAST)
        val bundle = Bundle()
        bundle.putString(
            Constants.TOAST,
            "Unable to connect device"
        )
        msg.data = bundle
        mHandler?.sendMessage(msg)
        mState = STATE_NONE
        // Update UI title
        updateUserInterfaceTitle()

        // Start the service over to restart listening mode
        this@BluetoothService.start()
    }

    private fun connectionLost() {
        // Send a failure message back to the Activity
        val msg =
            mHandler!!.obtainMessage(Constants.MESSAGE_TOAST)
        val bundle = Bundle()
        bundle.putString(
            Constants.TOAST,
            "Device connection was lost"
        )
        msg.data = bundle
        mHandler?.sendMessage(msg)
        mState = STATE_NONE
        // Update UI title
        updateUserInterfaceTitle()

        // Start the service over to restart listening mode
        this@BluetoothService.start()
    }


    /**
     * This thread runs while listening for incoming connections. It behaves
     * like a server-side client. It runs until a connection is accepted
     * (or until cancelled).
     */
    inner class AcceptThread(secure: Boolean) : Thread() {
        // The local server socket
        private val mmServerSocket: BluetoothServerSocket?
        private val mSocketType: String
        override fun run() {
            Log.d(
                " ", "Socket Type: " + mSocketType +
                        "BEGIN mAcceptThread" + this
            )
            name = "AcceptThread$mSocketType"
            var socket: BluetoothSocket? = null

            // Listen to the server socket if we're not connected
            while (mState != STATE_CONNECTED) {
                socket = try {
                    // This is a blocking call and will only return on a
                    // successful connection or an exception
                    mmServerSocket!!.accept()
                } catch (e: IOException) {
                    Log.e(
                        " ",
                        "Socket Type: " + mSocketType + "accept() failed",
                        e
                    )
                    break
                }

                // If a connection was accepted
                if (socket != null) {
                    synchronized(this) {
                        when (mState) {
                            STATE_LISTEN, STATE_CONNECTING ->                                 // Situation normal. Start the connected thread.
                                connected(
                                    socket, socket.remoteDevice,
                                    mSocketType
                                )
                            STATE_NONE, STATE_CONNECTED ->                                 // Either not ready or already connected. Terminate new socket.
                                try {
                                    socket.close()
                                } catch (e: IOException) {
                                    e.printStackTrace()
                                }
                        }
                    }
                }
            }

        }

        fun cancel() {
            try {
                mmServerSocket!!.close()
            } catch (e: IOException) {
                Log.e(
                    " ",
                    "Socket Type" + mSocketType + "close() of server failed",
                    e
                )
            }
        }

        init {
            var tmp: BluetoothServerSocket? = null
            mSocketType = if (secure) "Secure" else "Insecure"

            // Create a new listening server socket
            try {
                tmp = if (secure) {
                    mAdapter?.listenUsingRfcommWithServiceRecord(
                        NAME_SECURE,
                        MY_UUID_SECURE
                    )
                } else {
                    mAdapter?.listenUsingInsecureRfcommWithServiceRecord(
                        NAME_INSECURE, MY_UUID_INSECURE
                    )
                }
            } catch (e: IOException) {
                Log.e(
                    " ",
                    "Socket Type: " + mSocketType + "listen() failed",
                    e
                )
            }
            mmServerSocket = tmp
            mState = STATE_LISTEN
        }
    }

    inner class ConnectThread(private val mmDevice: BluetoothDevice, secure: Boolean) :
        Thread() {
        private val mmSocket: BluetoothSocket?
        private val mSocketType: String
        override fun run() {
            Log.i(
                " ",
                "BEGIN mConnectThread SocketType:$mSocketType"
            )
            name = "ConnectThread$mSocketType"

            // Always cancel discovery because it will slow down a connection
            mAdapter?.cancelDiscovery()

            // Make a connection to the BluetoothSocket
            try {
                // This is a blocking call and will only return on a
                // successful connection or an exception
                mmSocket!!.connect()
            } catch (e: IOException) {
                // Close the socket
                try {
                    mmSocket!!.close()
                } catch (e2: IOException) {
                    Log.e(
                        " ", "unable to close() " + mSocketType +
                                " socket during connection failure", e2
                    )
                }
                connectionFailed()
                return
            }

            // Reset the ConnectThread because we're done
            synchronized(this) { mConnectThread = null }

            // Start the connected thread
            connected(mmSocket, mmDevice, mSocketType)
        }

        fun cancel() {
            try {
                mmSocket!!.close()
            } catch (e: IOException) {
                Log.e(
                    "",
                    "close() of connect $mSocketType socket failed",
                    e
                )
            }
        }

        init {
            var tmp: BluetoothSocket? = null
            mSocketType = if (secure) "Secure" else "Insecure"

            // Get a BluetoothSocket for a connection with the
            // given BluetoothDevice
            try {
                tmp = if (secure) {
                    mmDevice.createRfcommSocketToServiceRecord(
                        MY_UUID_SECURE
                    )
                } else {
                    mmDevice.createInsecureRfcommSocketToServiceRecord(
                        MY_UUID_INSECURE
                    )
                }
            } catch (e: IOException) {
                Log.e(
                    "",
                    "Socket Type: " + mSocketType + "create() failed",
                    e
                )
            }
            mmSocket = tmp
            mState = STATE_CONNECTING
        }
    }

    inner class ConnectedThread(socket: BluetoothSocket, socketType: String) :
        Thread() {
        private val mmSocket: BluetoothSocket
        private val mmInStream: InputStream?
        private val mmOutStream: OutputStream?
        override fun run() {
            Log.i("", "BEGIN mConnectedThread")
            val buffer = ByteArray(1024)
            var bytes: Int

            // Keep listening to the InputStream while connected
            while (mState == STATE_CONNECTED) {
                try {
                    // Read from the InputStream
                    bytes = mmInStream!!.read(buffer)

                    // Send the obtained bytes to the UI Activity
                    //obtainMessage(int what, int arg1, int arg2, Object obj)
                    mHandler?.obtainMessage(
                            Constants.MESSAGE_READ,
                            bytes,
                            -1,
                            buffer
                        )
                        ?.sendToTarget()
                } catch (e: IOException) {
                    Log.e(" ", "disconnected", e)
                    connectionLost()
                    break
                }
            }
        }

        /**
         * Write to the connected OutStream.
         *
         * @param buffer The bytes to write
         */
        fun write(buffer: ByteArray?) {
            try {
                mmOutStream!!.write(buffer)

                // Share the sent message back to the UI Activity
                mHandler?.obtainMessage(
                        Constants.MESSAGE_WRITE,
                        -1,
                        -1,
                        buffer
                    )
                    ?.sendToTarget()
            } catch (e: IOException) {
                Log.e(" ", "Exception during write", e)
            }
        }

        fun cancel() {
            try {
                mmSocket.close()
            } catch (e: IOException) {
                Log.e(" ", "close() of connect socket failed", e)
            }
        }

        init {
            Log.d(" ", "create ConnectedThread: $socketType")
            mmSocket = socket
            var tmpIn: InputStream? = null
            var tmpOut: OutputStream? = null

            // Get the BluetoothSocket input and output streams
            try {
                tmpIn = socket.inputStream
                tmpOut = socket.outputStream
            } catch (e: IOException) {
                e.printStackTrace()
            }
            mmInStream = tmpIn
            mmOutStream = tmpOut
            mState = STATE_CONNECTED
        }
    }

}
