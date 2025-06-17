package com.example.android_user

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.graphics.scale
import com.google.android.material.textfield.TextInputEditText
import okhttp3.Call
import okhttp3.Callback
import okhttp3.FormBody
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.Response
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import kotlin.math.min

class Register : AppCompatActivity() {
    private lateinit var imageUpload: ImageView
    private lateinit var buttonCamera: ImageButton
    private lateinit var loginText: TextView
    private var selectedImageUri: Uri? = null
    private var cameraImageUri: Uri? = null
    private var currentUserId: String? = null
    private lateinit var loadingDialog: AlertDialog
    private lateinit var loadingMessageTextView: TextView

    companion object {
        private const val TAG = "RegisterActivity"
        private const val MAX_IMAGE_SIZE_KB_CONSTANT = 300 // Maksimal ukuran file 300KB
        private const val MAX_IMAGE_DIMENSION_CONSTANT = 1000 // Maksimal lebar/tinggi 1000px
    }

    private val pickMedia = registerForActivityResult(PickVisualMedia()) { uri ->
        if (uri != null) {
            selectedImageUri = uri
            imageUpload.setImageURI(selectedImageUri)
            Log.d(TAG, "PhotoPicker: Gambar dipilih. URI: $selectedImageUri")
            uploadImage()
        } else {
            Log.d(TAG, "PhotoPicker: Pemilihan gambar dibatalkan.")
            Toast.makeText(this, "Pemilihan gambar dibatalkan.", Toast.LENGTH_SHORT).show()
        }
    }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                Log.d(TAG, "requestPermissionLauncher: Izin kamera diberikan.")
                openCameraIntent()
            } else {
                Log.w(TAG, "requestPermissionLauncher: Izin kamera ditolak.")
                Toast.makeText(this, "Izin kamera dibutuhkan untuk mengambil foto.", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        Log.d(TAG, "onCreate: RegisterActivity dimulai.")

        imageUpload = findViewById(R.id.image_upload)
        buttonCamera = findViewById(R.id.button_camera)
        loginText = findViewById(R.id.login_text)

        setupLoadingDialog()

        imageUpload.setOnClickListener {
            Log.d(TAG, "onClick: Tombol 'Pilih dari Galeri' ditekan.")
            openGallery()
        }
        buttonCamera.setOnClickListener {
            Log.d(TAG, "onClick: Tombol 'Ambil dari Kamera' ditekan.")
            checkCameraPermissionAndOpenCamera()
        }

        loginText.setOnClickListener {
            Log.d(TAG, "onClick: Teks 'Sudah punya akun?' ditekan, mengarahkan ke Login.")
            startActivity(Intent(this, Login::class.java))
            finish()
        }
    }

    private fun setupLoadingDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_loading, null)
        loadingMessageTextView = dialogView.findViewById(R.id.loading_message)
        loadingDialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(false)
            .create()
        loadingDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
    }

    private fun showLoadingDialog(message: String) {
        loadingMessageTextView.text = message
        loadingDialog.show()
    }

    private fun dismissLoadingDialog() {
        if (loadingDialog.isShowing) {
            loadingDialog.dismiss()
        }
    }

    private fun openGallery() {
        pickMedia.launch(PickVisualMediaRequest(PickVisualMedia.ImageOnly))
        Log.d(TAG, "openGallery: PhotoPicker diluncurkan.")
    }

    private fun checkCameraPermissionAndOpenCamera() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> {
                Log.d(TAG, "checkCameraPermissionAndOpenCamera: Izin kamera sudah diberikan, membuka kamera.")
                openCameraIntent()
            }
            ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.CAMERA
            ) -> {
                Log.d(TAG, "checkCameraPermissionAndOpenCamera: Menampilkan rasional untuk izin kamera.")
                AlertDialog.Builder(this)
                    .setTitle("Izin Kamera Diperlukan")
                    .setMessage("Aplikasi membutuhkan akses kamera untuk mengambil foto KTP Anda.")
                    .setPositiveButton("OK") { _, _ ->
                        requestPermissionLauncher.launch(Manifest.permission.CAMERA)
                    }
                    .setNegativeButton("Batal", null)
                    .show()
            }
            else -> {
                Log.d(TAG, "checkCameraPermissionAndOpenCamera: Meminta izin kamera.")
                requestPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }

    private fun openCameraIntent() {
        val photoFile: File?
        try {
            val fileName = "KTP_Camera_${System.currentTimeMillis()}.jpg"
            val storageDir = getExternalFilesDir(null)

            if (storageDir == null) {
                Log.e(TAG, "openCameraIntent: getExternalFilesDir returned null. Storage not available?")
                showErrorDialog("Penyimpanan tidak tersedia untuk file kamera.")
                return
            }

            if (!storageDir.exists()) {
                val created = storageDir.mkdirs()
                Log.d(TAG, "openCameraIntent: Direktori penyimpanan dibuat: $storageDir, berhasil: $created")
                if (!created) {
                    Log.e(TAG, "openCameraIntent: Gagal membuat direktori penyimpanan.")
                    showErrorDialog("Gagal membuat direktori untuk file kamera.")
                    return
                }
            } else {
                Log.d(TAG, "openCameraIntent: Direktori penyimpanan sudah ada: $storageDir")
            }

            photoFile = File(storageDir, fileName)
            Log.d(TAG, "openCameraIntent: Path file gambar: ${photoFile.absolutePath}")

        } catch (ex: IOException) {
            Log.e(TAG, "openCameraIntent: Error creating image file", ex)
            showErrorDialog("Gagal membuat file gambar.")
            return
        }

        photoFile.also {
            try {
                cameraImageUri = FileProvider.getUriForFile(
                    this,
                    "${applicationContext.packageName}.fileprovider",
                    it
                )
                Log.d(TAG, "openCameraIntent: URI FileProvider dibuat: $cameraImageUri")

                val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                intent.putExtra(MediaStore.EXTRA_OUTPUT, cameraImageUri)
                intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

                val resolveInfoList = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)
                for (resolveInfo in resolveInfoList) {
                    val packageName = resolveInfo.activityInfo.packageName
                    // Perbaikan untuk memastikan grantUriPermission di Android 11+
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) { // Android 11+
                        grantUriPermission(packageName, cameraImageUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    } else { // Android 10-
                        grantUriPermission(packageName, cameraImageUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    Log.d(TAG, "openCameraIntent: Diberikan izin URI ke package: $packageName")
                }

                cameraLauncher.launch(intent)
                Log.d(TAG, "openCameraIntent: Intent pengambilan kamera diluncurkan.")
            } catch (e: Exception) {
                Log.e(TAG, "openCameraIntent: Gagal meluncurkan intent kamera atau membuat URI FileProvider.", e)
                showErrorDialog("Gagal membuka kamera: ${e.message}")
            }
        }
    }

    private val cameraLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            selectedImageUri = cameraImageUri

            if (selectedImageUri != null) {
                imageUpload.setImageURI(selectedImageUri)
                Log.d(TAG, "cameraLauncher: Foto dari kamera berhasil diambil. Menggunakan URI output: $selectedImageUri")
                uploadImage()
            } else {
                Log.e(TAG, "cameraLauncher: cameraImageUri adalah null setelah pengambilan foto.")
                Toast.makeText(this, "Gagal mendapatkan foto resolusi penuh dari kamera.", Toast.LENGTH_SHORT).show()
                val fileToDelete = cameraImageUri?.path?.let { File(it) }
                if (fileToDelete != null && fileToDelete.exists()) {
                    fileToDelete.delete()
                    Log.d(TAG, "cameraLauncher: File kamera sementara dihapus karena URI hasil null/invalid.")
                }
            }
        } else {
            Log.d(TAG, "cameraLauncher: Pengambilan foto dibatalkan (RESULT_CANCELED).")
            cameraImageUri?.let { uri ->
                try {
                    // Attempt to delete the file using FileProvider URI's path
                    val fileToDelete = File(uri.path!!) // This path might be wrong for content:// URIs
                    if (fileToDelete.exists()) {
                        fileToDelete.delete()
                        Log.d(TAG, "cameraLauncher: File kamera sementara dihapus karena pembatalan.")
                    } else {
                        // For content:// URIs, use contentResolver.delete
                        contentResolver.delete(uri, null, null)
                        Log.d(TAG, "cameraLauncher: ContentResolver delete for URI: $uri because file not found at path.")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "cameraLauncher: Gagal menghapus file kamera sementara: ${e.message}", e)
                }
            }
            Toast.makeText(this, "Pengambilan foto dibatalkan.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun uploadImage() {
        if (selectedImageUri == null) {
            showErrorDialog("Tidak ada gambar yang dipilih untuk diunggah.")
            Log.w(TAG, "uploadImage: selectedImageUri adalah null, tidak dapat mengunggah.")
            return
        }

        showLoadingDialog("Mengunggah gambar KTP dan memproses OCR... (Ini bisa memakan waktu)")

        val file: File
        try {
            val originalInputStream = contentResolver.openInputStream(selectedImageUri!!)
            val originalBitmap = BitmapFactory.decodeStream(originalInputStream)
            originalInputStream?.close()

            val compressedBitmap = compressAndResizeBitmap(originalBitmap, MAX_IMAGE_DIMENSION_CONSTANT, MAX_IMAGE_SIZE_KB_CONSTANT)

            file = File(cacheDir, "temp_upload_compressed_file.jpg")
            val outputStream = FileOutputStream(file)
            compressedBitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
            outputStream.flush()
            outputStream.close()

            Log.d(TAG, "uploadImage: File sementara terkompresi dibuat: ${file.absolutePath}, ukuran: ${file.length() / 1024} KB")
        } catch (e: IOException) {
            dismissLoadingDialog()
            Log.e(TAG, "uploadImage: Gagal memproses atau mengompres file gambar: ${e.message}", e)
            showErrorDialog("Gagal memproses file gambar.")
            return
        } catch (e: Exception) {
            dismissLoadingDialog()
            Log.e(TAG, "uploadImage: Error tidak terduga saat kompresi: ${e.message}", e)
            showErrorDialog("Terjadi kesalahan saat mengompres gambar.")
            return
        }

        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("file", file.name, file.asRequestBody("image/jpeg".toMediaTypeOrNull()))
            .build()

        val request = Request.Builder()
            .url("${Config.BASE_URL}/upload-ktp/")
            .post(requestBody)
            .build()

        Log.d(TAG, "uploadImage: Mengirim permintaan upload KTP ke ${Config.BASE_URL}/upload-ktp/")
        OkHttpClient().newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    dismissLoadingDialog()
                    file.delete()
                    Log.e(TAG, "onFailure: Upload KTP gagal: ${e.message}", e)
                    showErrorDialog("Upload KTP gagal: ${e.message}")
                }
            }
            override fun onResponse(call: Call, response: Response) {
                val responseBodyString = response.body?.string()
                runOnUiThread {
                    dismissLoadingDialog()
                    file.delete()
                    if (response.isSuccessful) {
                        val jsonResponse = responseBodyString?.let {
                            try {
                                JSONObject(it)
                            } catch (e: Exception) {
                                Log.e(TAG, "onResponse: Gagal parsing JSON response body: ${e.message}", e)
                                null
                            }
                        }
                        currentUserId = jsonResponse?.getString("user_id")

                        if (currentUserId != null) {
                            Log.d(TAG, "onResponse: Upload KTP berhasil. User ID: $currentUserId")
                            Toast.makeText(this@Register, "KTP berhasil diunggah!", Toast.LENGTH_SHORT).show()
                            showEmailDialog()
                        } else {
                            Log.e(TAG, "onResponse: User ID null dari respons server: $responseBodyString")
                            showErrorDialog("Upload KTP berhasil, tetapi user ID tidak ditemukan dari server.")
                        }
                    } else {
                        val errorDetail = responseBodyString ?: response.message
                        Log.e(TAG, "onResponse: Upload KTP gagal. Code: ${response.code}, Detail: $errorDetail")
                        showErrorDialog("Upload KTP gagal (${response.code}): $errorDetail")
                    }
                }
            }
        })
    }

    private fun compressAndResizeBitmap(bitmap: Bitmap, maxDimension: Int, maxKb: Int): Bitmap {
        var resizedBitmap = bitmap
        val width = bitmap.width
        val height = bitmap.height

        if (width > maxDimension || height > maxDimension) {
            val ratio = min(maxDimension.toFloat() / width, maxDimension.toFloat() / height)
            val newWidth = (width * ratio).toInt()
            val newHeight = (height * ratio).toInt()
            resizedBitmap = bitmap.scale(newWidth, newHeight, true)
            Log.d(TAG, "compressAndResizeBitmap: Bitmap di-resize ke ${newWidth}x${newHeight}")
        } else {
            Log.d(TAG, "compressAndResizeBitmap: Bitmap tidak perlu di-resize. Dimensi: ${width}x${height}")
        }

        var quality = 100
        val byteArrayOutputStream = java.io.ByteArrayOutputStream()
        resizedBitmap.compress(Bitmap.CompressFormat.JPEG, quality, byteArrayOutputStream)
        var compressedBytes = byteArrayOutputStream.toByteArray()

        while (compressedBytes.size / 1024 > maxKb && quality > 0) {
            byteArrayOutputStream.reset()
            quality -= 5
            resizedBitmap.compress(Bitmap.CompressFormat.JPEG, quality, byteArrayOutputStream)
            compressedBytes = byteArrayOutputStream.toByteArray()
            Log.d(TAG, "compressAndResizeBitmap: Kompresi kualitas: $quality, Ukuran: ${compressedBytes.size / 1024} KB")
        }

        Log.d(TAG, "compressAndResizeBitmap: Final quality: $quality, Final size: ${compressedBytes.size / 1024} KB")
        return BitmapFactory.decodeByteArray(compressedBytes, 0, compressedBytes.size)
    }

    private fun showEmailDialog() {
        Log.d(TAG, "showEmailDialog: Menampilkan dialog input email dan nama perusahaan.")
        if (currentUserId == null) {
            Log.e(TAG, "showEmailDialog: currentUserId adalah null, tidak dapat menampilkan dialog email.")
            showErrorDialog("User ID tidak ada. Silakan unggah KTP lagi.")
            return
        }

        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_email, null)
        val emailInput = dialogView.findViewById<TextInputEditText>(R.id.email_input)
        val namaPerusahaanInput = dialogView.findViewById<TextInputEditText>(R.id.namaPerusahaan)
        val submitButton = dialogView.findViewById<Button>(R.id.submit_button)

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(false)
            .create()

        submitButton.setOnClickListener {
            val email = emailInput.text.toString().trim()
            val namaPerusahaan = namaPerusahaanInput.text.toString().trim()
            var isValid = true

            if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                emailInput.error = "Masukkan email yang valid"
                isValid = false
                Log.w(TAG, "showEmailDialog: Validasi email gagal.")
            }
            if (namaPerusahaan.isEmpty()) {
                namaPerusahaanInput.error = "Nama perusahaan tidak boleh kosong"
                isValid = false
                Log.w(TAG, "showEmailDialog: Validasi nama perusahaan gagal.")
            }

            if (isValid) {
                sendEmailAndCompanyToBackend(email, namaPerusahaan, dialog)
            }
        }
        dialog.show()
    }

    private fun sendEmailAndCompanyToBackend(email: String, namaPerusahaan: String, emailDialog: AlertDialog) {
        val capitalizedNamaPerusahaan = namaPerusahaan.toTitleCase()
        Log.d(TAG, "sendEmailAndCompanyToBackend: Mengirim email '$email' dan perusahaan '$capitalizedNamaPerusahaan' untuk User ID '$currentUserId' ke ${Config.BASE_URL}/submit-email/")

        showLoadingDialog("Mengirim data registrasi... Mohon tunggu.")

        val requestBody = FormBody.Builder()
            .add("user_id", currentUserId!!)
            .add("email", email)
            .add("nama_perusahaan", capitalizedNamaPerusahaan)
            .build()

        val request = Request.Builder()
            .url("${Config.BASE_URL}/submit-email/")
            .post(requestBody)
            .build()

        OkHttpClient().newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    dismissLoadingDialog()
                    Log.e(TAG, "onFailure: Gagal mengirim data email/perusahaan: ${e.message}", e)
                    showErrorDialog("Gagal mengirim data email/perusahaan: ${e.message}")
                }
            }
            override fun onResponse(call: Call, response: Response) {
                val responseBodyString = response.body?.string()
                runOnUiThread {
                    dismissLoadingDialog()
                    if (response.isSuccessful) {
                        emailDialog.dismiss()
                        Log.d(TAG, "onResponse: Data email/perusahaan berhasil dikirim. Response: $responseBodyString")
                        Toast.makeText(this@Register, "Email dan perusahaan berhasil dikirim!", Toast.LENGTH_SHORT).show()
                        showConfirmationDialog()
                    } else {
                        val errorDetail = responseBodyString ?: response.message
                        Log.e(TAG, "onResponse: Gagal mengirim data email/perusahaan. Code: ${response.code}, Detail: $errorDetail")
                        showErrorDialog("Gagal mengirim data email/perusahaan (${response.code}): $errorDetail")
                    }
                }
            }
        })
    }

    private fun showConfirmationDialog() {
        Log.d(TAG, "showConfirmationDialog: Menampilkan dialog konfirmasi registrasi.")
        val dialogView = layoutInflater.inflate(R.layout.dialog_confirmation, null)
        val okButton = dialogView.findViewById<Button>(R.id.ok_button)
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(false)
            .create()

        okButton.setOnClickListener {
            dialog.dismiss()
            Log.d(TAG, "onClick: Tombol 'OK' di dialog konfirmasi ditekan, mengarahkan ke Login.")
            startActivity(Intent(this, Login::class.java))
            finish()
        }
        dialog.show()
    }

    private fun showErrorDialog(message: String) {
        Log.e(TAG, "showErrorDialog: Menampilkan dialog error: $message")
        AlertDialog.Builder(this)
            .setMessage(message)
            .setPositiveButton("OK", null)
            .show()
    }

    // Helper function to capitalize with PT./CV. standardization
    private fun String.toTitleCase(): String {
        val trimmed = this.trim()
        if (trimmed.isEmpty()) return ""

        val lowerInput = trimmed.lowercase()
        val words = trimmed.split(" ").filter { it.isNotEmpty() }

        return when {
            lowerInput.startsWith("pt") -> {
                val remainingWords = words.drop(1).joinToString(" ") { word ->
                    word.lowercase().replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
                }
                "PT. ${remainingWords.ifEmpty { "" }}"
            }
            lowerInput.startsWith("cv") -> {
                val remainingWords = words.drop(1).joinToString(" ") { word ->
                    word.lowercase().replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
                }
                "CV. ${remainingWords.ifEmpty { "" }}"
            }
            else -> {
                words.joinToString(" ") { word ->
                    word.lowercase().replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
                }
            }
        }
    }
}