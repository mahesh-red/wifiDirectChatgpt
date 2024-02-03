package com.mahe.wifidirectchatgpt

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.wifi.p2p.WifiP2pDevice
import android.net.wifi.p2p.WifiP2pDeviceList
import android.net.wifi.p2p.WifiP2pManager
import android.util.Log
import androidx.core.app.ActivityCompat

class WifiDirectBroadcastReceiver(
    private val wifiP2pManager: WifiP2pManager,
    private val channel: WifiP2pManager.Channel,
    private val onPeersChanged: (List<WifiP2pDevice>) -> Unit
) : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        when (intent?.action) {
            WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION -> {

                wifiP2pManager.requestPeers(channel) { peerList: WifiP2pDeviceList ->
                    val updatedList = peerList.deviceList.toList()
                    Log.d("DiscoverPeers", "Updated list at Broadcast: $updatedList")
                    updatePeersList(updatedList, onPeersChanged)
                }
            }
            // Handle other action cases as needed
        }
    }
}


/*class WifiDirectBroadcastReceiver(
    private val wifiP2pManager: WifiP2pManager,
    private val channel: WifiP2pManager.Channel,
    private val onPeersChanged: (List<WifiP2pDevice>) -> Unit
) : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        when (intent?.action) {
            WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION -> {
                wifiP2pManager.requestPeers(channel) { peerList: WifiP2pDeviceList ->
                    val updatedList = peerList.deviceList.toList()
                    Log.d("DiscoverPeers", "Updated list : $updatedList")
                    onPeersChanged(updatedList)
                }
            }
            // Handle other action cases as needed
        }
    }
}
*/