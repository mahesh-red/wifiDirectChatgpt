package com.mahe.wifidirectchatgpt

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.wifi.p2p.WifiP2pConfig
import android.net.wifi.p2p.WifiP2pDevice
import android.net.wifi.p2p.WifiP2pDeviceList
import android.net.wifi.p2p.WifiP2pManager
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mahe.wifidirectchatgpt.ui.theme.WifiDirectChatgptTheme

class MainActivity : ComponentActivity() {
    private lateinit var wifiP2pManager: WifiP2pManager
    private lateinit var channel: WifiP2pManager.Channel
    private lateinit var receiver: WifiDirectBroadcastReceiver

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        wifiP2pManager = getSystemService(Context.WIFI_P2P_SERVICE) as WifiP2pManager
        channel = wifiP2pManager.initialize(this, mainLooper, null)

        receiver = WifiDirectBroadcastReceiver(wifiP2pManager, channel) { updatedList ->
            handlePeersChanged(updatedList)
        }


        setContent {
            WifiDirectChatgptTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    val  context= LocalContext.current
                    P2pDiscoveryScreen(wifiP2pManager, channel, receiver,context)


                }
            }
        }
    }
    //c
    override fun onResume() {
        super.onResume()
        registerReceiver(receiver, IntentFilter(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION))
    }
    //c

    override fun onPause() {
        super.onPause()
        unregisterReceiver(receiver)
    }

    private fun handlePeersChanged(updatedList: List<WifiP2pDevice>) {
        // Handle the updated list of peers here
        // You can update UI or perform any other actions
        Log.d("MainActivity", "Updated list in MainActivity: $updatedList")
    }
}

/////





