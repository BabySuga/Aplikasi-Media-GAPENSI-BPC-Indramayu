package com.example.android_user

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.ContactsContract
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class Kontak : BaseActivity() {

    private val REQUEST_CODE_WRITE_CONTACTS = 100

    @SuppressLint("UseKtx")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_kontak)

        // Set window insets for edge-to-edge display
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Contact information
        val phoneNumber = "+6281224584489"
        val whatsappNumber = "+6281224584489"
        val facebookUrl = "https://www.facebook.com/profile.php?id=61577068515789"
        val instagramUrl = "https://www.instagram.com/bpcgapensiindramayu/"
        val websiteUrl = "https://gapensi.or.id/bpc/kabupatenindramayu"
        val emailAddress = "gapensi.indramayoe@gmail.com"

        // Find LinearLayouts
        val linearWhatsApp = findViewById<LinearLayout>(R.id.linear_whatsapp)
        val linearCall = findViewById<LinearLayout>(R.id.linear_call)
        val linearFacebook = findViewById<LinearLayout>(R.id.linear_facebook)
        val linearInstagram = findViewById<LinearLayout>(R.id.linear_instagram)
        val linearWebsite = findViewById<LinearLayout>(R.id.linear_website)
        val linearEmail = findViewById<LinearLayout>(R.id.linear_email)

        // WhatsApp click listener
        linearWhatsApp.setOnClickListener {
            try {
                val whatsappUri = "https://wa.me/$whatsappNumber".toUri()
                val intent = Intent(Intent.ACTION_VIEW, whatsappUri)
                startActivity(intent)
            } catch (e: Exception) {
                Toast.makeText(this, "WhatsApp not installed", Toast.LENGTH_SHORT).show()
            }
        }

        // Save contact click listener
        linearCall.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_CONTACTS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(android.Manifest.permission.WRITE_CONTACTS),
                    REQUEST_CODE_WRITE_CONTACTS
                )
            } else {
                saveContact(phoneNumber)
            }
        }

        // Facebook click listener
        linearFacebook.setOnClickListener {
            openUrl(facebookUrl)
        }

        // Instagram click listener
        linearInstagram.setOnClickListener {
            openUrl(instagramUrl)
        }

        // Website click listener
        linearWebsite.setOnClickListener {
            openUrl(websiteUrl)
        }

        // Email click listener
        linearEmail.setOnClickListener {
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = "mailto:$emailAddress".toUri()
                putExtra(Intent.EXTRA_SUBJECT, "Contact Gapensi")
            }
            try {
                startActivity(intent)
            } catch (e: Exception) {
                Toast.makeText(this, "No email app installed", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Handle permission result for saving contact
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_WRITE_CONTACTS) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                saveContact("+6281224584489")
            } else {
                Toast.makeText(this, "Permission denied to save contact", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Function to save contact
    private fun saveContact(@Suppress("SameParameterValue") phoneNumber: String) {
        val intent = Intent(Intent.ACTION_INSERT).apply {
            type = ContactsContract.Contacts.CONTENT_TYPE
            putExtra(ContactsContract.Intents.Insert.PHONE, phoneNumber)
            putExtra(ContactsContract.Intents.Insert.NAME, "Gapensi Admin")
        }
        try {
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(this, "Unable to open contacts app", Toast.LENGTH_SHORT).show()
        }
    }

    // Function to open URL in browser
    private fun openUrl(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, url.toUri())
        try {
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(this, "No browser app installed", Toast.LENGTH_SHORT).show()
        }
    }
}