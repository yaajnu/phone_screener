package com.example.phone_screener

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.phone_screener.ui.theme.Phone_screenerTheme

class IncomingCallActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val incomingNumber = intent.getStringExtra("INCOMING_NUMBER") ?: "Unknown"

        setContent {
            Phone_screenerTheme {
                IncomingCallScreen(
                    callerNumber = incomingNumber,
                    onAccept = {
                        // TODO: Add logic to accept call via TelecomManager
                        finish() // Close the screen for now
                    },
                    onDecline = {
                        // TODO: Add logic to decline call via TelecomManager
                        finish() // Close the screen for now
                    }
                )
            }
        }
    }
}

@Composable
fun IncomingCallScreen(callerNumber: String, onAccept: () -> Unit, onDecline: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.DarkGray),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Incoming Call", fontSize = 24.sp, color = Color.White)
            Spacer(modifier = Modifier.height(24.dp))
            Text(callerNumber, fontSize = 36.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Spacer(modifier = Modifier.height(150.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                // Decline Button
                FloatingActionButton(
                    onClick = onDecline,
                    shape = CircleShape,
                    containerColor = Color.Red,
                    modifier = Modifier.size(72.dp)
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Decline Call", tint = Color.White)
                }

                // Accept Button
                FloatingActionButton(
                    onClick = onAccept,
                    shape = CircleShape,
                    containerColor = Color.Green,
                    modifier = Modifier.size(72.dp)
                ) {
                    Icon(Icons.Default.Call, contentDescription = "Accept Call", tint = Color.White)
                }
            }
        }
    }
}

@Preview
@Composable
fun IncomingCallScreenPreview() {
    Phone_screenerTheme {
        IncomingCallScreen(callerNumber = "123-456-7890", onAccept = {}, onDecline = {})
    }
}