package com.example.phone_screener// Make sure this package name matches yours

import android.Manifest
import android.content.Context
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.phone_screener.ui.theme.Phone_screenerTheme
import android.app.role.RoleManager
import androidx.compose.material.icons.filled.Person // NEW: Icon for contacts


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // This block sets the screen's content to be your Composable UI
        setContent {
            Phone_screenerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // This is where you call your main Composable function
                    DialerScreen()
                }
            }
        }
    }
}

// This is the main Composable function that defines the entire screen
@Composable
fun DialerScreen() {
    // This holds the state of the phone number.
    var phoneNumber by remember { mutableStateOf("") }
    val context = LocalContext.current
    // NEW: Launcher to get the result (phone number) from ContactsActivity
    val contactLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val selectedNumber = result.data?.getStringExtra("contact_number")
            if (selectedNumber != null) {
                phoneNumber = selectedNumber
            }
        }
    }
    val roleRequestLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            Toast.makeText(context, "App set as default phone app!", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Failed to set as default phone app.", Toast.LENGTH_SHORT).show()
        }
    }
    // NEW: Launcher for contact permission request
    val contactPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // Permission granted, launch ContactsActivity
            val intent = Intent(context, ContactsActivity::class.java)
            contactLauncher.launch(intent)
        } else {
            Toast.makeText(context, "Contacts permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    // This handles the permission request dialog
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            makePhoneCall(context, phoneNumber) // Permission granted, make call
        } else {
            Toast.makeText(context, "Permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    val dialerButtons = listOf(
        "1", "2", "3", "4", "5", "6", "7", "8", "9", "*", "0", "#"
    )

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Text display for the number
        Text(
            text = phoneNumber.ifEmpty { "Enter number" },
            modifier = Modifier.fillMaxWidth().weight(1f).wrapContentHeight(align = Alignment.CenterVertically),
            fontSize = 34.sp,
            textAlign = TextAlign.Center
        )

        // Grid for the number pad
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier.padding(horizontal = 24.dp)
        ) {
            items(dialerButtons) { buttonText ->
                DialerButton(
                    text = buttonText,
                    onClick = { phoneNumber += buttonText }
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Bottom row for Call and Backspace buttons
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
//            Spacer(modifier = Modifier.size(64.dp)) // Placeholder for balance
            IconButton(onClick = {
                // Check for contacts permission before launching ContactsActivity
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
                    val intent = Intent(context, ContactsActivity::class.java)
                    contactPermissionLauncher.launch(Manifest.permission.READ_CONTACTS)
                } else {
                    // Request contacts permission
                    contactPermissionLauncher.launch(Manifest.permission.READ_CONTACTS)
                }
            }) {
                Icon(Icons.Default.Person, contentDescription = "Contacts", modifier = Modifier.size(32.dp))
            }
            IconButton(onClick = {
                if (phoneNumber.isNotEmpty()) {
                    phoneNumber = phoneNumber.dropLast(1)
                }
            }) {
                Icon(Icons.Default.Clear, contentDescription = "bonda", modifier = Modifier.size(32.dp))
            }
            // Call Button
            FloatingActionButton(
                onClick = {
                    if (phoneNumber.isNotEmpty()) {
                        // Check if permission is already granted before making the call
                        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
                            makePhoneCall(context, phoneNumber)
                        } else {
                            // If permission is not granted, launch the request
                            permissionLauncher.launch(Manifest.permission.CALL_PHONE)
                        }
                    }
                },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Call, contentDescription = "Call")
            }

            // Backspace Button
            IconButton(onClick = {
                if (phoneNumber.isNotEmpty()) {
                    phoneNumber = ""
                }
            }) {
                Icon(Icons.Default.Delete, contentDescription = "Backspace", modifier = Modifier.size(32.dp))
            }
            Button(onClick = {
                val roleManager = context.getSystemService(Context.ROLE_SERVICE) as RoleManager
                val intent = roleManager.createRequestRoleIntent(RoleManager.ROLE_DIALER)
                roleRequestLauncher.launch(intent)
            }) {
                Text("Set as Default Phone App")
            }
        }
    }
}

// This is a reusable Composable for a single dialer button
@Composable
fun DialerButton(text: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.padding(8.dp).size(72.dp),
        shape = CircleShape,
    ) {
        Text(text = text, fontSize = 28.sp)
    }
}

// Helper function that creates and starts the call intent
fun makePhoneCall(context: Context, number: String) {
    val intent = Intent(Intent.ACTION_CALL, Uri.parse("tel:$number"))
    context.startActivity(intent)
}

// A preview function so you can see your UI in the Android Studio editor
@Preview(showBackground = true)
@Composable
fun DialerScreenPreview() {
    Phone_screenerTheme {
        DialerScreen()
    }
}