import os
import sys
from dotenv import load_dotenv

# Tambahkan direktori src ke sys.path
base_dir = os.path.abspath(os.path.dirname(__file__))
src_dir = os.path.join(base_dir, 'src')
sys.path.append(src_dir)

try:
    from OCR_KTP import extract_info_from_image
except ModuleNotFoundError:
    print("Error: Modul 'OCR_KTP' tidak ditemukan. Pastikan file 'OCR_KTP.py' ada di folder 'src'.")
    sys.exit(1)

# Load environment variables
load_dotenv()

def main():
    # Ganti dengan path ke gambar KTP Anda
    image_path = "D:/Aplikasi/OCR_KTP_API/KTP-NAWE.jpg"  # Path ke gambar KTP
    
    if not os.path.exists(image_path):
        print("Error: File gambar tidak ditemukan!")
        return
    
    try:
        # Panggil fungsi untuk mengekstrak informasi dari gambar KTP
        result = extract_info_from_image(image_path)
        
        # Tampilkan respons mentah dari API sebagai teks
        print("Hasil Ekstraksi Data KTP:")
        print("-" * 50)
        print(result)
            
    except Exception as e:
        print(f"Error: {str(e)}")
        print("Periksa pesan error untuk mengetahui penyebabnya.")

if __name__ == "__main__":
    main()