package com.example.phone_screener

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.ContactsContract
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.phone_screener.ui.theme.Phone_screenerTheme

// Data class to hold contact information
data class Contact(val name: String, val number: String)

class ContactsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Phone_screenerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ContactsScreen()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactsScreen() {
    val context = LocalContext.current
    val activity = (context as? Activity)
    var contacts by remember { mutableStateOf<List<Contact>>(emptyList()) }
    var permissionGranted by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            permissionGranted = true
            Toast.makeText(context, "Permission Granted", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Permission Denied", Toast.LENGTH_SHORT).show()
        }
    }

    // This effect runs once when the composable is first displayed
    LaunchedEffect(key1 = permissionGranted) {
        // Check if permission is already granted
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            permissionGranted = true
        } else {
            // Request permission if not granted
            permissionLauncher.launch(Manifest.permission.READ_CONTACTS)
        }

        if (permissionGranted) {
            contacts = getPhoneContacts(context)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Contacts") },
                navigationIcon = {
                    IconButton(onClick = { activity?.finish() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (permissionGranted) {
            if (contacts.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No contacts found.")
                }
            } else {
                LazyColumn(modifier = Modifier.padding(paddingValues)) {
                    items(contacts) { contact ->
                        ContactItem(contact = contact) { selectedContact ->
                            // When a contact is clicked, send the number back
                            val resultIntent = Intent()
                            resultIntent.putExtra("contact_number", selectedContact.number.filter { it.isDigit() })
                            activity?.setResult(Activity.RESULT_OK, resultIntent)
                            activity?.finish()
                        }
                    }
                }
            }
        } else {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Permission required to view contacts.")
            }
        }
    }
}

@Composable
fun ContactItem(contact: Contact, onClick: (Contact) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick(contact) }
            .padding(16.dp)
    ) {
        Text(text = contact.name, style = MaterialTheme.typography.bodyLarge)
        Text(text = contact.number, style = MaterialTheme.typography.bodyMedium)
    }
    Divider()
}

// Function to query the phone's contacts
private fun getPhoneContacts(context: android.content.Context): List<Contact> {
    val contactsList = mutableListOf<Contact>()
    val contentResolver = context.contentResolver
    val cursor = contentResolver.query(
        ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
        null, null, null,
        ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC"
    )

    cursor?.use {
        val nameIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
        val numberIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)

        while (it.moveToNext()) {
            val name = if (nameIndex != -1) it.getString(nameIndex) else "N/A"
            val number = if (numberIndex != -1) it.getString(numberIndex) else "N/A"
            contactsList.add(Contact(name, number))
        }
    }
    return contactsList
}