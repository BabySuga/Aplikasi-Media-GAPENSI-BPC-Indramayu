# Aplikasi Media GAPENSI BPC Indramayu

**Aplikasi Media GAPENSI BPC Indramayu** adalah sebuah platform digital yang dirancang untuk mempermudah anggota dan pengurus Gabungan Pelaksana Konstruksi Nasional Indonesia (GAPENSI) Badan Pengurus Cabang Indramayu dalam mengakses informasi organisasi dan mengelola data keanggotaan. Aplikasi ini dilengkapi dengan fitur registrasi anggota otomatis menggunakan teknologi Optical Character Recognition (OCR) KTP, dashboard pengelolaan anggota untuk admin, serta penyebaran informasi terkini.

## Fitur Utama

- **Registrasi Anggota Otomatis**  
  Memungkinkan pendaftaran anggota baru dengan pemindaian KTP menggunakan OCR untuk ekstraksi data otomatis.

- **Manajemen Anggota (Admin)**  
  Dashboard khusus admin untuk menyetujui/menolak pendaftaran, mengedit, menghapus, dan mengekspor data anggota terdaftar.

- **Informasi Organisasi**  
  Menampilkan profil organisasi, struktur kepengurusan, daftar anggota, galeri kegiatan, dan informasi fasilitas.

- **Pengumuman Terkini**  
  Menyajikan pengumuman dan berita terbaru dari GAPENSI BPC Indramayu.

- **Akses Kontak**  
  Menyediakan informasi kontak penting dan tautan ke media sosial organisasi.

- **Login & Registrasi Pengguna**  
  Sistem login dan registrasi yang aman dan mudah diakses.

## Teknologi yang Digunakan

Proyek ini dikembangkan menggunakan kombinasi teknologi mobile dan backend yang kuat untuk memastikan fungsionalitas dan skalabilitas:

- **Aplikasi Android**: Dikembangkan menggunakan Kotlin.
- **Backend (OCR & API)**: Dibangun dengan Python (FastAPI/Flask).
- **Admin Page**: Antarmuka berbasis web (HTML, CSS, JavaScript).
- **Basis Data**: Firabase Realtime Database
- **Penyimpanan Cloud**: Amazon AWS S3

## Persyaratan Sistem

### Untuk Aplikasi Android
- Android Studio Meerkat Feature Drop | 2024.3.2 Patch 1 atau versi lebih baru.
- Perangkat Android (fisik/emulator) dengan API Level 34+.

### Untuk Backend (OCR_KTP_API)
- Python 3.8+
- pip

## Daftar Dependensi

### Backend (Python)

| Komponen | Nama Library / Modul | Deskripsi |
|----------|----------------------|-----------|
| Web Framework | `fastapi` | Framework modern untuk API |
| ASGI Server | `uvicorn` | Server ASGI untuk FastAPI |
| Pemrosesan Gambar | `opencv-python` | Library pemrosesan gambar |
| OCR | `pytesseract` | Wrapper untuk Tesseract OCR |
| Manipulasi Gambar | `Pillow` | Pengolahan gambar |
| AWS Integration | `boto3` | SDK AWS |
| Env Vars | `python-dotenv` | Memuat variabel dari .env |
| Data Structures | `numpy` | Komputasi numerik |
| HTTP Requests | `requests` | Library HTTP |
| Image Processing | `scikit-image` | Algoritma gambar |
| Web Framework (alt) | `Flask` | Micro framework alternatif |
| CORS Handling | `Flask-Cors` | Kelola CORS |
| Data Validation | `pydantic` | Validasi dengan type hints |
| Prod Server | `gunicorn` | Server WSGI |
| Data Analysis | `pandas` | Analisis data |
| Excel Support | `openpyxl`, `xlrd` | Dukungan file Excel |
| Progress Bar | `tqdm` | Visualisasi progress |
| Plotting | `matplotlib` | Visualisasi data |
| Machine Learning | `tensorflow`, `scikit-learn` | Framework ML |
| Image Utils | `imutils` | Utility OpenCV |

### Aplikasi Android (Kotlin)

| Komponen | Nama Library / Modul | Versi | Deskripsi |
|----------|----------------------|--------|-----------|
| Kotlin Ext | `androidx.core:core-ktx` | 1.10.1 | Extensions AndroidX |
| UI Compat | `androidx.appcompat:appcompat` | 1.6.1 | Kompatibilitas UI |
| Material UI | `com.google.android.material:material` | 1.9.0 | Komponen UI Material |
| Layout | `androidx.constraintlayout:constraintlayout` | 2.1.4 | Layout berbasis batasan |
| HTTP Client | `com.squareup.retrofit2:retrofit` | 2.9.0 | Klien REST API |
| JSON Converter | `converter-gson` | 2.9.0 | JSON to Object |
| Gambar | `com.github.bumptech.glide:glide` | 4.12.0 | Load gambar |
| OkHttp | `com.squareup.okhttp3:okhttp` | 4.9.0 | HTTP client efisien |
| ViewPager | `androidx.viewpager2:viewpager2` | 1.0.0 | Pager view Android |
| Firebase DB | `firebase-database-ktx` | (gradle) | Firebase Realtime DB |
| Firebase Auth | `firebase-auth-ktx` | (gradle) | Firebase Authentication |
| Firebase Analytics | `firebase-analytics-ktx` | (gradle) | Data analitik |
| Unit Test | `junit:junit` | 4.13.2 | Framework pengujian |
| Android Test | `androidx.test.ext:junit` | 1.1.5 | JUnit Android |
| UI Test | `espresso-core` | 3.5.1 | Pengujian UI Android |

## Instalasi dan Konfigurasi

### 1. Mengkloning Repositori

```bash
git clone https://github.com/BabySuga/Aplikasi-Media-GAPENSI-BPC-Indramayu.git
cd Aplikasi-Media-GAPENSI-BPC-Indramayu
```
### 2. Konfigurasi dan Menjalankan Backend `(OCR_KTP_API)`
```bash
cd D:\Aplikasi\OCR_KTP_API
python -m venv venv
# Aktifkan venv
source venv/bin/activate  # Linux/macOS
.\venv\Scripts\activate   # Windows

pip install -r requirements.txt
```
Buat file `.env`
```bash
AWS_ACCESS_KEY_ID=your_access_key
AWS_SECRET_ACCESS_KEY=your_secret_key
AWS_REGION=ap-southeast-1
S3_BUCKET_NAME=your_s3_bucket_name
DATABASE_URL=your_database_connection_string
```
Menjalankan Backend
```bash
# FastAPI
uvicorn main:app --host 0.0.0.0 --port 8000

# atau Flask
python app.py
```
### 3. Konfigurasi dan Menjalankan Aplikasi Android
- Buka Android Studio Meerkat | 2024.3.2 Patch 1
- Pilih `File > Open`, arahkan ke `D:\Aplikasi\Android_user`
- Sinkronkan Gradle dan pastikan BASE_URL pada Config.kt mengarah ke backend Anda:
  
  ```bash
  object Config {
      const val BASE_URL = "http://10.0.2.2:8000"
  }
  ```
  
- Jalankan aplikasi di emulator atau perangkat fisik.
### 4. Menjalankan Admin Page
- Navigasi ke `D:\Aplikasi\Admin_Page`
- Buka `index.html` dengan browser
- Pastikan URL API pada JavaScript sesuai dengan backend Anda `(API_BASE_URL).`


Salam Hangat,

Developer
