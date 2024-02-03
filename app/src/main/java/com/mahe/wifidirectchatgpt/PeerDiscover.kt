package com.mahe.wifidirectchatgpt

import android.Manifest
import android.content.Context
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.wifi.p2p.WifiP2pConfig
import android.net.wifi.p2p.WifiP2pDevice
import android.net.wifi.p2p.WifiP2pDeviceList
import android.net.wifi.p2p.WifiP2pManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
//import android.widget.Button
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import com.mahe.wifidirectchatgpt.ui.theme.WifiDirectChatgptTheme


private fun discoverPeers(wifiP2pManager: WifiP2pManager, channel: WifiP2pManager.Channel,
                          onListUpdated: (List<WifiP2pDevice>) -> Unit,context: Context) {
    val maxRetries = 3
    var retryCount = 0


    fun startDiscovery() {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        if(ActivityCompat.checkSelfPermission(context,Manifest.permission.NEARBY_WIFI_DEVICES)!=PackageManager.PERMISSION_GRANTED){


        }
        wifiP2pManager.discoverPeers(channel, object : WifiP2pManager.ActionListener {
            override fun onSuccess() {
                // Discovery initiated successfully
                requestAvailablePeers(wifiP2pManager, channel,onListUpdated ,context)
                Log.d("DiscoverPeers","Discovery successful")
            }

            override fun onFailure(reasonCode: Int) {
                // Discovery failed
                when (reasonCode) {
                    WifiP2pManager.P2P_UNSUPPORTED ->
                        Log.e("DiscoverPeers", "P2P is not supported on this device")
                    WifiP2pManager.ERROR ->
                        Log.e("DiscoverPeers", "General error during discovery")
                    WifiP2pManager.BUSY -> {
                        if (retryCount < maxRetries) {
                            retryCount++
                            Log.w("DiscoverPeers", "Framework is busy. Retrying...")
                            // Retry after a short delay
                            Handler(Looper.getMainLooper()).postDelayed({
                                startDiscovery()
                            }, 1000) // Adjust the delay as needed
                        } else {
                            Log.e("DiscoverPeers", "Max retries reached. Unable to start discovery.")
                        }
                    }
                    else ->
                        Log.e("DiscoverPeers", "Unknown error: $reasonCode")
                }
            }
        })
    }

    startDiscovery()
}

fun updatePeersList(peerList: List<WifiP2pDevice>, onListUpdated: (List<WifiP2pDevice>) -> Unit) {
    onListUpdated(peerList)
}

private fun requestAvailablePeers(wifiP2pManager: WifiP2pManager, channel: WifiP2pManager.Channel,
                                  onListUpdated: (List<WifiP2pDevice>) -> Unit,context: Context) {
    if (ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED
    ) {
        // TODO: Consider calling
        //    ActivityCompat#requestPermissions
        // here to request the missing permissions, and then overriding
        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
        //                                          int[] grantResults)
        // to handle the case where the user grants the permission. See the documentation
        // for ActivityCompat#requestPermissions for more details.
        return
    }
    wifiP2pManager.requestPeers(channel) { peerList: WifiP2pDeviceList ->
        // Update the UI with the list of peers
//        val updateList=peerList.deviceList.toList()
       val updateList=peerList.deviceList.toList()
        Log.d("DiscoverPeers","Updated list at request availablepeers : $updateList")
        updatePeersList(updateList, onListUpdated)
    }


   /* val UpdatedList = listOf(
        WifiP2pDevice().apply {
            deviceName = "Device 1"
            deviceAddress = "AA:BB:CC:DD:EE:01"
            status = WifiP2pDevice.AVAILABLE
        },
        WifiP2pDevice().apply {
            deviceName = "Device 2"
            deviceAddress = "AA:BB:CC:DD:EE:02"
            status = WifiP2pDevice.AVAILABLE
        },
        WifiP2pDevice().apply {
            deviceName = "Device 3"
            deviceAddress = "AA:BB:CC:DD:EE:03"
            status = WifiP2pDevice.AVAILABLE
        }
        // Add more devices as needed
    )
    updatePeersList(UpdatedList, onListUpdated)
}*/
}

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
fun P2pDiscoveryScreen(
    wifiP2pManager: WifiP2pManager,
    channel: WifiP2pManager.Channel,
    receiver: WifiDirectBroadcastReceiver,
    context: Context
) {
    var peerList by remember { mutableStateOf(emptyList<WifiP2pDevice>()) }
    val isGranted=ActivityCompat.checkSelfPermission(
        context,
        Manifest.permission.NEARBY_WIFI_DEVICES
    )==PackageManager.PERMISSION_GRANTED

    val NearByWifiDevicesPermission = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted: Boolean ->
            if (isGranted) {
                discoverPeers(wifiP2pManager, channel, onListUpdated = { updatedList ->
                    peerList = updatedList
                }, context)
            } else {
                // Handle the case where permission is not granted
            }
        })




    DisposableEffect(Unit) {
        val intentFilter = IntentFilter()
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION)
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION)
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION)
        context.registerReceiver(receiver, intentFilter)

        // Start discovery when the Composable is first launched
        discoverPeers(wifiP2pManager, channel, onListUpdated = {
            peerList = it
        }, context)

        onDispose {
            context.unregisterReceiver(receiver)
        }
    }


    Column {
        Text("Available Peers:")
        Spacer(modifier = Modifier.height(8.dp))

        if (peerList.isEmpty()) {
            Text("No peers available.")
        } else {
            peerList.forEach { device ->
                Text(
                    text = "Device: ${device.deviceName}",
                    modifier = Modifier.clickable {
                        connectToDevice(wifiP2pManager, channel, device, context)
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            if (!isGranted){
                NearByWifiDevicesPermission.launch(
                    Manifest.permission.NEARBY_WIFI_DEVICES

                )
                Log.d("mahesh ","not granted")
            }
            else{
                Log.d("mahesh","granted")
                // Manually trigger a new discovery when the button is clicked
                discoverPeers(wifiP2pManager, channel, onListUpdated = { updatedList ->
                    peerList = updatedList
                }, context)
            }

        }) {
            Text("Discover Peers")
        }
    }
}






fun connectToDevice(wifiP2pManager: WifiP2pManager, channel: WifiP2pManager.Channel, device: WifiP2pDevice,context: Context) {
    val config = WifiP2pConfig().apply {
        deviceAddress = device.deviceAddress
    }

    if (ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED
    ) {
        // TODO: Consider calling
        //    ActivityCompat#requestPermissions
        // here to request the missing permissions, and then overriding
        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
        //                                          int[] grantResults)
        // to handle the case where the user grants the permission. See the documentation
        // for ActivityCompat#requestPermissions for more details.
        return
    }
    wifiP2pManager.connect(channel, config, object : WifiP2pManager.ActionListener {
        override fun onSuccess() {
            Log.d("connection success",device.deviceName)
            // Connection initiated successfully
        }

        override fun onFailure(reason: Int) {
            Log.d("connection fail","failed to connect")
            // Connection failed
        }
    })
}


/*
@Composable
fun P2pDiscoveryScreen(
    wifiP2pManager: WifiP2pManager,
    channel: WifiP2pManager.Channel,
    receiver: WifiDirectBroadcastReceiver
) {

    val peerList by remember { mutableStateOf(emptyList<WifiP2pDevice>()) }
    val context= LocalContext.current

    val isgranted = ActivityCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED


    val FineLocationPermissionResultLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
        }
    )

    DisposableEffect(Unit) {
        val intentFilter = IntentFilter()
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION)
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION)
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION)
//        LocalContext.current.registerReceiver(receiver, intentFilter)
        context.registerReceiver(receiver,intentFilter)

        onDispose {
//            LocalContext.current.unregisterReceiver(receiver)
            context.unregisterReceiver(receiver)
        }
    }

    Column {
        Text("Available Peers:")
        Spacer(modifier = Modifier.height(8.dp))

        if (peerList.isEmpty()) {
            Text("No peers available.")
        } else {
            peerList.forEach { device ->
                Text(
                    text = "Device: ${device.deviceName}",
                    modifier = Modifier.clickable {
                        connectToDevice(wifiP2pManager, channel, device, context = context)
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))


        if (!isgranted) {
            Button(onClick = {
                FineLocationPermissionResultLauncher.launch(
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
                //navController.navigate(CallHisScreen.UploadCAllHistoryScreen.name)
            }, shape = RoundedCornerShape(20.dp)) {
                Text(text = "Give Fine Location Permission")


            }}
            else Button(onClick = {
            discoverPeers(wifiP2pManager, channel, context = context)
        }) {
            Text("Discover Peers")
        }
    }
}






///

private fun discoverPeers(wifiP2pManager: WifiP2pManager, channel: WifiP2pManager.Channel,context: Context) {
    val maxRetries = 3
    var retryCount = 0

    fun startDiscovery() {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        wifiP2pManager.discoverPeers(channel, object : WifiP2pManager.ActionListener {
            override fun onSuccess() {
                // Discovery initiated successfully
                requestAvailablePeers(wifiP2pManager, channel,context)
            }

            override fun onFailure(reasonCode: Int) {
                // Discovery failed
                when (reasonCode) {
                    WifiP2pManager.P2P_UNSUPPORTED ->
                        Log.e("DiscoverPeers", "P2P is not supported on this device")
                    WifiP2pManager.ERROR ->
                        Log.e("DiscoverPeers", "General error during discovery")
                    WifiP2pManager.BUSY -> {
                        if (retryCount < maxRetries) {
                            retryCount++
                            Log.w("DiscoverPeers", "Framework is busy. Retrying...")
                            // Retry after a short delay
                            Handler(Looper.getMainLooper()).postDelayed({
                                startDiscovery()
                            }, 1000) // Adjust the delay as needed
                        } else {
                            Log.e("DiscoverPeers", "Max retries reached. Unable to start discovery.")
                        }
                    }
                    else ->
                        Log.e("DiscoverPeers", "Unknown error: $reasonCode")
                }
            }
        })
    }

    startDiscovery()
}


fun connectToDevice(wifiP2pManager: WifiP2pManager, channel: WifiP2pManager.Channel, device: WifiP2pDevice,context: Context) {
    val config = WifiP2pConfig().apply {
        deviceAddress = device.deviceAddress
    }

    if (ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED
    ) {
        // TODO: Consider calling
        //    ActivityCompat#requestPermissions
        // here to request the missing permissions, and then overriding
        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
        //                                          int[] grantResults)
        // to handle the case where the user grants the permission. See the documentation
        // for ActivityCompat#requestPermissions for more details.
        return
    }
    wifiP2pManager.connect(channel, config, object : WifiP2pManager.ActionListener {
        override fun onSuccess() {
            // Connection initiated successfully
        }

        override fun onFailure(reason: Int) {
            // Connection failed
        }
    })
}



///
@Composable
fun FineLocationPerm(){
    val context = LocalContext.current
    val isgranted = ActivityCompat.checkSelfPermission(
        context,
        Manifest.permission.READ_CALL_LOG
    ) == PackageManager.PERMISSION_GRANTED
    val FineLocationPermissionResultLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->


        }
    )
}
*/