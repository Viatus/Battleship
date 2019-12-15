package com.example.battleship.UI

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import com.example.battleship.R
import com.example.battleship.databinding.ActivityMatchBinding
import com.example.battleship.game.ShotResult
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.connection.*
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_match.*
import kotlin.text.Charsets.UTF_8
import android.os.Handler
import androidx.core.view.ViewCompat
import androidx.fragment.app.FragmentStatePagerAdapter
import com.example.battleship.UI.snackbar.ShotResultSnackbar
import com.example.battleship.database.DatabaseHelper
import com.example.battleship.database.model.BattleshipMatchResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.lang.ref.WeakReference
import java.util.concurrent.TimeUnit
import kotlin.concurrent.timer


class MatchActivity : AppCompatActivity() {

    private val REQUIRED_PERMISSIONS = arrayOf<String>(
        Manifest.permission.BLUETOOTH,
        Manifest.permission.BLUETOOTH_ADMIN,
        Manifest.permission.ACCESS_WIFI_STATE,
        Manifest.permission.CHANGE_WIFI_STATE,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )

    private val REQUEST_CODE_REQUIRED_PERMISSIONS = 1

    private lateinit var mPager: ViewPager
    private lateinit var binding: ActivityMatchBinding
    private lateinit var pagerAdapter: FragmentStatePagerAdapter


    private var codeName = "Temporal name"

    private lateinit var opponentEndpointId: String


    private val STRATEGY = Strategy.P2P_POINT_TO_POINT
    private val TAG = "Battleship"

    private var isConnected = false

    private var isSearching = false

    private var isOnScreen = false
    private var mCountTimeTask: CountTimeTask? = null

    var playerTurn = true
    private var isWaitingTurnInfo = false
    private var isOtherPlayerReady = false
    private var isPlayerReady = false

    private var opponentShot = Pair(-1, -1)
    var playerShot = Pair(-1, -1)
    private var playerShotResult = ShotResult.MISS
    private var payload: String = ""

    private lateinit var connectionsClient: ConnectionsClient;

    class CountTimeTask : AsyncTask<Int, Int, Void>() {

        var activityRef: WeakReference<MatchActivity>? = null

        fun link(act: MatchActivity) {
            activityRef = WeakReference(act)
        }

        fun unLink() {
            activityRef = null
        }

        override fun doInBackground(vararg params: Int?): Void? {
            var secondsPassed = params[0]
            if (secondsPassed != null) {
                while (!isCancelled) {
                    if (activityRef?.get()?.isOnScreen == true) {
                        try {
                            TimeUnit.SECONDS.sleep(1)
                        } catch (e: InterruptedException) {
                            break
                        }
                        publishProgress(secondsPassed++)
                    }
                }
            }

            return null
        }

        override fun onProgressUpdate(vararg values: Int?) {
            super.onProgressUpdate(*values)
            activityRef?.get()?.textview_time_passed?.text = java.lang.StringBuilder(
                "${values[0]?.div(3600) ?: 0}:${values[0]?.rem(3600)?.div(60)
                    ?: 0}:${values[0]?.rem(60) ?: 0}"
            ).toString()
        }
    }

    override fun onRetainCustomNonConfigurationInstance(): Any? {
        mCountTimeTask?.unLink()
        return mCountTimeTask
    }


    private val connectionLifecycleCallback = object : ConnectionLifecycleCallback() {
        override fun onConnectionInitiated(p0: String, p1: ConnectionInfo) {
            Log.i(TAG, "onConnectionInitiated: showing dialog for accepting connection")
            val builder = AlertDialog.Builder(this@MatchActivity)
            builder
                .setTitle("Accept connection")
                .setPositiveButton("Accept") { dialog, which ->
                    Nearby.getConnectionsClient(this@MatchActivity)
                        .acceptConnection(p0, payloadCallback)
                }
                .setNegativeButton("Reject") { dialog, which ->
                    Nearby.getConnectionsClient(this@MatchActivity)
                        .rejectConnection(p0)
                }.show()
        }

        override fun onConnectionResult(p0: String, p1: ConnectionResolution) {
            when (p1.status.statusCode) {
                ConnectionsStatusCodes.STATUS_OK -> {
                    Snackbar.make(
                        coordinator_layout,
                        "Connected",
                        Snackbar.LENGTH_SHORT
                    ).show()
                    connectionsClient.stopAdvertising()
                    connectionsClient.stopDiscovery()
                    isConnected = true
                    isSearching = false
                    opponentEndpointId = p0
                }
                ConnectionsStatusCodes.STATUS_CONNECTION_REJECTED -> Snackbar.make(
                    coordinator_layout,
                    "Connection rejected",
                    Snackbar.LENGTH_SHORT
                ).show()
                ConnectionsStatusCodes.STATUS_ERROR -> Snackbar.make(
                    coordinator_layout,
                    "Error occurred",
                    Snackbar.LENGTH_SHORT
                ).show()
            }
        }

        override fun onDisconnected(p0: String) {
            isConnected = false
            isPlayerReady = false
            isOtherPlayerReady = false
            Snackbar.make(
                coordinator_layout,
                "Disconnected",
                Snackbar.LENGTH_SHORT
            ).show()
            binding.disconnectButton.isEnabled = false
            binding.connectButton.isEnabled = true
            binding.hostButton.isEnabled = true
        }
    }

    private val endpointDiscoveryCallback = object : EndpointDiscoveryCallback() {
        override fun onEndpointFound(endpointId: String, info: DiscoveredEndpointInfo) {
            Log.i(TAG, "onEndpointFound: endpoint found, connecting")
            connectionsClient.requestConnection(codeName, endpointId, connectionLifecycleCallback)
            playerTurn = false
            isWaitingTurnInfo = false
        }

        override fun onEndpointLost(endpointId: String) {}
    }

    private val payloadCallback = object : PayloadCallback() {
        override fun onPayloadReceived(p0: String, p1: Payload) {
            payload = String(p1.asBytes()!!, UTF_8)
            Log.i(TAG, "onPayloadReceived: data received : $payload")
            if (payload == "ready") {
                isOtherPlayerReady = true
                Snackbar.make(
                    coordinator_layout,
                    "Your opponent is ready",
                    Snackbar.LENGTH_SHORT
                ).show()
                playerTurn = isPlayerReady
                if (playerTurn) {
                    mPager.currentItem = 1
                    mCountTimeTask = CountTimeTask()
                    mCountTimeTask?.execute(0)
                } else {
                    mPager.currentItem = 0
                }
            } else {
                if (playerTurn) {
                    when (payload) {
                        "DESTR" -> {
                            playerShotResult = ShotResult.DESTROYED
                        }
                        "MISS" -> {
                            playerShotResult = ShotResult.MISS
                            playerTurn = false
                            mPager.currentItem = 0
                        }
                        "HIT" -> {
                            playerShotResult = ShotResult.HIT
                        }
                    }
                    ShotResultSnackbar.make(mPager, playerShotResult).show()
                    if (mPager.currentItem != 1) {
                        mPager.currentItem = 1
                    }
                    val adapter = mPager.adapter as MyViewPagerAdapter
                    val opponentFragment =
                        adapter.registeredFragments[1] as OpponentFieldFragment
                    opponentFragment.updateAfterShotResult(
                        playerShot.first,
                        playerShot.second,
                        playerShotResult
                    )
                    if (opponentFragment.opponentGrid.getNumberOfShips() == -10) {
                        val playerFragment =
                            adapter.registeredFragments[0] as PlayerFieldFragment
                        val opponentFieldFragment =
                            adapter.registeredFragments[1] as OpponentFieldFragment
                        val parts = textview_time_passed.text.split(":")
                        CoroutineScope(Dispatchers.IO).launch {
                            addMatchResultToStatistic(
                                this@MatchActivity,
                                BattleshipMatchResult.BATTLESHIP_WIN,
                                playerFragment.playerGrid.toString(),
                                opponentFieldFragment.opponentGrid.toString(),
                                parts[0].toInt() * 3600 + parts[1].toInt() * 60 + parts[2].toInt(),
                                opponentEndpointId
                            )
                        }
                        val builder = AlertDialog.Builder(this@MatchActivity)
                        builder
                            .setTitle("Congratulations! You won!")
                            .setPositiveButton("Okay") { dialog, which ->
                                finish()
                            }.show()
                    }
                } else {
                    if (payload.matches("[0-9]+ [0-9]+".toRegex())) {
                        val parts = payload.split(" ")
                        opponentShot = Pair(parts[0].toInt(), parts[1].toInt())
                        val adapter = mPager.adapter as MyViewPagerAdapter
                        val playerFragment =
                            adapter.registeredFragments[0] as PlayerFieldFragment
                        if (mPager.currentItem != 0) {
                            mPager.currentItem = 0
                        }
                        val shotResult =
                            playerFragment.getShot(opponentShot.first, opponentShot.second)
                        connectionsClient.sendPayload(
                            opponentEndpointId,
                            Payload.fromBytes(
                                shotResult.toString().toByteArray()
                            )
                        )
                        playerTurn = shotResult == ShotResult.MISS
                        if (playerTurn) {
                            mPager.currentItem = 1
                        }
                        if (playerFragment.playerGrid.getNumberOfShips() == 0) {
                            val builder = AlertDialog.Builder(this@MatchActivity)
                            builder
                                .setTitle("Oh, it seems you lost! Good luck next time!")
                                .setPositiveButton("Okay") { dialog, which ->
                                    finish()
                                }.show()
                        }
                    }
                }
            }
        }

        fun addMatchResultToStatistic(
            context: Context,
            result: Int,
            playerGrid: String,
            opponentGrid: String,
            duration: Int,
            opponentName: String
        ) {
            val db = DatabaseHelper(context, null)
            db.insertMatchResult(
                result,
                playerGrid,
                opponentGrid,
                duration,
                opponentName
            )
        }

        override fun onPayloadTransferUpdate(p0: String, p1: PayloadTransferUpdate) {
            if (p1.status == PayloadTransferUpdate.Status.SUCCESS) {
                Log.i(TAG, "onPayloadTransferUpdate: data received succefully")
            }
        }
    }

    fun setPlayerReady() {
        isPlayerReady = true
        connectionsClient.sendPayload(
            opponentEndpointId,
            Payload.fromBytes(
                StringBuilder("ready").toString().toByteArray()
            )
        )
        if (isOtherPlayerReady) {
            mPager.currentItem = 0
            mCountTimeTask = CountTimeTask()
            mCountTimeTask?.execute(0)
        }
    }

    fun sendShotAtCurrentCoordinate() {
        connectionsClient.sendPayload(
            opponentEndpointId,
            Payload.fromBytes(
                StringBuilder("${playerShot.first} ${playerShot.second}").toString().toByteArray()
            )
        )
    }

    private fun startAdvertising() {
        connectionsClient.startAdvertising(
            codeName, packageName, connectionLifecycleCallback,
            AdvertisingOptions.Builder().setStrategy(STRATEGY).build()
        )
        playerTurn = true
    }

    private fun startDiscovery() {
        connectionsClient.startDiscovery(
            packageName,
            endpointDiscoveryCallback,
            DiscoveryOptions.Builder().setStrategy(STRATEGY).build()
        )
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (isOtherPlayerReady && isPlayerReady) {
            mCountTimeTask = lastNonConfigurationInstance as CountTimeTask?
            if (mCountTimeTask == null) {
                mCountTimeTask = CountTimeTask()
                mCountTimeTask?.execute(0)
            }
            mCountTimeTask?.link(this)
        }
        setContentView(R.layout.activity_match)
        val extras = intent.extras
        if (extras != null) {
            codeName = extras.getString("NICKNAME") ?: "NewPlayer"
        }

        binding = DataBindingUtil.setContentView(this, R.layout.activity_match)

        mPager = binding.pager

        pagerAdapter = MyViewPagerAdapter(
            supportFragmentManager,
            FragmentStatePagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT
        )
        mPager.adapter = pagerAdapter

        connectionsClient = Nearby.getConnectionsClient(this)

        binding.connectButton.setOnClickListener { connectButtonClicked() }
        binding.disconnectButton.setOnClickListener { disconnectButtonClicked() }
        binding.hostButton.setOnClickListener { hostButtonClicked() }
    }

    private fun connectButtonClicked() {
        startDiscovery()
        Snackbar.make(
            coordinator_layout,
            "Searching game",
            Snackbar.LENGTH_SHORT
        ).show()
        binding.disconnectButton.isEnabled = true
        binding.connectButton.isEnabled = false
        binding.hostButton.isEnabled = false
        isSearching = true
    }

    private fun hostButtonClicked() {
        startAdvertising()
        Snackbar.make(
            coordinator_layout,
            "hosting game",
            Snackbar.LENGTH_SHORT
        ).show()
        binding.disconnectButton.isEnabled = true
        binding.connectButton.isEnabled = false
        binding.hostButton.isEnabled = false
        isSearching = true
    }

    private fun disconnectButtonClicked() {
        if (isSearching) {
            connectionsClient.stopDiscovery()
            connectionsClient.stopAdvertising()
            isSearching = false
            Snackbar.make(
                coordinator_layout,
                "Search stopped",
                Snackbar.LENGTH_SHORT
            ).show()
        }
        if (isConnected) {
            connectionsClient.disconnectFromEndpoint(opponentEndpointId)
            isConnected = false
            Snackbar.make(
                coordinator_layout,
                "Disconnected",
                Snackbar.LENGTH_SHORT
            ).show()
        }

        binding.disconnectButton.isEnabled = false
        binding.connectButton.isEnabled = true
        binding.hostButton.isEnabled = true
    }

    override fun onStop() {
        super.onStop()
        isOnScreen = false
    }

    override fun onStart() {
        super.onStart()
        isOnScreen = true
        if (android.os.Build.VERSION.SDK_INT >= 23) {
            if (!hasPermissions(this, REQUIRED_PERMISSIONS)) {
                requestPermissions(REQUIRED_PERMISSIONS, REQUEST_CODE_REQUIRED_PERMISSIONS)
            }
        }
    }

    private fun hasPermissions(context: Context, permissions: Array<String>): Boolean {
        for (permission in permissions) {
            if (ContextCompat.checkSelfPermission(
                    context,
                    permission
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return false
            }
        }
        return true
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isSearching) {
            connectionsClient.stopDiscovery()
            connectionsClient.stopAdvertising()
        }
        if (isConnected) {
            connectionsClient.disconnectFromEndpoint(opponentEndpointId)
        }
        connectionsClient.stopAllEndpoints()
    }
}
