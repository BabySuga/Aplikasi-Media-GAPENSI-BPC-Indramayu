import os
from PIL import Image

def resize_image(image_path, max_width=650):
    img = Image.open(image_path)
    
    if img.width > max_width:
        ratio = max_width / float(img.width)
        new_height = int(img.height * ratio)
        img = img.resize((max_width, new_height), Image.LANCZOS)
        
        # Simpan gambar yang diubah ukurannya ke path baru di direktori yang sama
        resized_path = os.path.join(
            os.path.dirname(image_path), 
            f"resized_{os.path.basename(image_path)}"
        )
        resized_path = os.path.normpath(resized_path) # Normalkan path untuk konsistensi
        
        img.save(resized_path)
        print(f"Gambar berhasil diubah. Resolusi baru: {img.width}x{img.height}")
        return resized_path
    else:
        print("Gambar tidak perlu diubah.")
        return image_path