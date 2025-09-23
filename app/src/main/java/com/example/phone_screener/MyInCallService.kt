package com.example.phone_screener
import android.telecom.Call
import android.telecom.InCallService
import android.util.Log

class MyInCallService : InCallService() {

    override fun onCallAdded(call: Call) {
        super.onCallAdded(call)
        Log.d("MyInCallService", "Call Added: $call")
        // TODO: A new call has been added.
        // This is where you would launch your own in-call UI activity.
        // You would pass call details to your activity.
    }

    override fun onCallRemoved(call: Call) {
        super.onCallRemoved(call)
        Log.d("MyInCallService", "Call Removed: $call")
        // TODO: The call has been removed (e.g., hung up).
        // This is where you would close your in-call UI.
    }
}