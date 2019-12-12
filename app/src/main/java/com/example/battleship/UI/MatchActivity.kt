package com.example.battleship.UI

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
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

    var timerHandler = Handler()
    var startTime: Long = 0

    var timerRunnable: Runnable = object : Runnable {

        override fun run() {
            val millis = System.currentTimeMillis() - startTime
            var seconds = (millis / 1000).toInt()
            val minutes = seconds / 60
            seconds = seconds % 60

            //timerTextView.setText(String.format("%d:%02d", minutes, seconds))

            timerHandler.postDelayed(this, 500);
        }
    }


    private lateinit var mPager: ViewPager
    private lateinit var binding: ActivityMatchBinding
    private lateinit var pagerAdapter: FragmentStatePagerAdapter


    private val codeName = "Temporal name"

    private lateinit var opponentEndpointId: String


    private val STRATEGY = Strategy.P2P_POINT_TO_POINT
    private val TAG = "Battleship"

    private var isConnected = false

    private var isSearching = false

    var playerTurn = true
    private var isWaitingTurnInfo = false
    private var isOtherPlayerReady = false
    private var isPlayerReady = false

    private var opponentShot = Pair(-1, -1)
    var playerShot = Pair(-1, -1)
    private var playerShotResult = ShotResult.MISS
    private var payload: String = ""

    private lateinit var connectionsClient: ConnectionsClient;

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
                }
            } else {
                if (playerTurn) {
                    when (payload) {
                        "DESTR" -> {
                            playerShotResult = ShotResult.DESTROYED
                            Snackbar.make(
                                coordinator_layout,
                                "Ship was destroyed!",
                                Snackbar.LENGTH_SHORT
                            ).show()
                        }
                        "MISS" -> {
                            playerShotResult = ShotResult.MISS
                            playerTurn = false
                            Snackbar.make(
                                coordinator_layout,
                                "Ha! You missed!",
                                Snackbar.LENGTH_SHORT
                            ).show()
                        }
                        "HIT" -> {
                            playerShotResult = ShotResult.HIT
                            Snackbar.make(
                                coordinator_layout,
                                "Enemy ship is on fire!",
                                Snackbar.LENGTH_SHORT
                            ).show()
                        }
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
                        val builder = AlertDialog.Builder(this@MatchActivity)
                        builder
                            .setTitle("Congratulations! You won!")
                            .setPositiveButton("Okay") { dialog, which ->
                                finish()
                            }.show()
                    }
                } else {
                    if (payload.matches("[0-9]+ [0-9]+".toRegex())) {
                        mPager.currentItem = 0
                        val parts = payload.split(" ")
                        opponentShot = Pair(parts[0].toInt(), parts[1].toInt())
                        val adapter = mPager.adapter as MyViewPagerAdapter
                        val playerFragment =
                            adapter.registeredFragments[0] as PlayerFieldFragment
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

        override fun onPayloadTransferUpdate(p0: String, p1: PayloadTransferUpdate) {
            Log.i(TAG, "onPayloadTransferUpdate: data received")
            if (p1.status == PayloadTransferUpdate.Status.SUCCESS) {

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
    }

    fun sendShotAtCurrentCoordinate() {
        connectionsClient.sendPayload(
            opponentEndpointId,
            Payload.fromBytes(
                StringBuilder("${playerShot.first} ${playerShot.second}").toString().toByteArray()
            )
        )
        Snackbar.make(
            coordinator_layout,
            "Shot sent",
            Snackbar.LENGTH_SHORT
        ).show()
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
        setContentView(R.layout.activity_match)

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
    }

    private fun connectButtonClicked() {
        //startAdvertising()
        startDiscovery()
        Snackbar.make(
            coordinator_layout,
            "Searching game",
            Snackbar.LENGTH_SHORT
        ).show()
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
    }

    override fun onStart() {
        super.onStart()

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
