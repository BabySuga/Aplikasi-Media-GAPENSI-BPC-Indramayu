# OCR KTP API

API ini digunakan untuk memproses gambar KTP (Kartu Tanda Penduduk) menggunakan teknik Optical Character Recognition (OCR). API ini dirancang untuk diintegrasikan dengan aplikasi Android, memungkinkan pengguna mengunggah gambar KTP dan menerima informasi yang diekstraksi dalam format terstruktur.

## Project Structure

```
OCR_KTP_API
├── src
│   ├── main.py          # Main entry point for the API
│   ├── OCR_KTP.py       # Logic for processing KTP images
│   ├── resize_image.py  # Function for resizing images
│   └── utils
│       └── __init__.py  # Utility functions and classes
├── requirements.txt     # Project dependencies
├── .env                 # Environment variables
└── README.md            # Project documentation
```

## Setup Instructions

1. **Clone the repository:**
   ```
   git clone <repository-url>
   cd OCR_KTP_API
   ```

2. **Create a virtual environment:**
   ```
   python -m venv venv
   venv\Scripts\activate  # On Windows
   ```

3. **Install dependencies:**
   ```
   pip install -r requirements.txt
   ```

4. **Set up environment variables:**
   Buat file `.env` di direktori root dan tambahkan variabel lingkungan yang diperlukan, seperti kredensial email:
   ```
   EMAIL_ADDRESS=your_email@example.com
   EMAIL_PASSWORD=your_email_password
   ```

## Usage

Untuk menjalankan API, gunakan perintah berikut:

```
uvicorn src.main:app --reload
```

API akan berjalan dan mendengarkan permintaan pada `http://127.0.0.1:8000`. Anda dapat menggunakan alat seperti Postman atau cURL untuk berinteraksi dengan endpoint API.

## API Endpoints

- **POST /upload-ktp/**: Mengunggah gambar KTP dan menerima informasi yang diekstraksi.
- **POST /submit-email/**: Mengirimkan email pengguna untuk registrasi.
- **GET /pending-registrations/**: Mendapatkan daftar registrasi yang masih dalam status "pending".
- **POST /approve-reject/**: Menyetujui atau menolak registrasi pengguna.

## Contributing

Kontribusi sangat diterima! Silakan kirim pull request atau buka issue untuk saran atau perbaikan.

## License

Proyek ini dilisensikan di bawah MIT License. Lihat file LICENSE untuk detail lebih lanjut.