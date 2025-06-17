import openai
import base64
import os
import json # Pastikan ini diimpor
from dotenv import load_dotenv
from src.resize_image import resize_image

load_dotenv()
openai.api_key = os.getenv("OPENAI_API_KEY")

def encode_image(image_path):
    with open(image_path, "rb") as image_file:
        return base64.b64encode(image_file.read()).decode('utf-8')

def extract_info_from_image(image_path):
    # Resize the image before processing
    resized_image_path = resize_image(image_path, max_width=650)
    
    # Encode the resized image to base64
    base64_image = encode_image(resized_image_path)

    # Hapus file sementara setelah di-encode jika resize_image menyimpannya sebagai file baru
    if resized_image_path != image_path:
        os.remove(resized_image_path) 
        print(f"Removed temporary resized image: {resized_image_path}")

    prompt = """
    Ekstrak informasi berikut dari gambar KTP ini dalam format JSON:
    - nik
    - nama
    - tempat_lahir
    - tanggal_lahir
    - jenis_kelamin
    - golongan_darah
    - alamat
    - rt
    - rw
    - kelurahan_atau_desa
    - kecamatan
    - agama
    - status_perkawinan
    - pekerjaan
    - kewarganegaraan

    Gunakan format ini untuk hasilnya:
    {
      "nik": "",
      "nama": "",
      "tempat_lahir": "",
      "tanggal_lahir": "",
      "jenis_kelamin": "",
      "golongan_darah": "",
      "alamat": "",
      "rt": "",
      "rw": "",
      "kelurahan_atau_desa": "",
      "kecamatan": "",
      "agama": "",
      "status_perkawinan": "",
      "pekerjaan": "",
      "kewarganegaraan": ""
    }
    Jika ada informasi yang tidak dapat diekstrak, gunakan "N/A" atau string kosong.
    **Pastikan output adalah JSON murni, tanpa teks tambahan sebelum atau sesudahnya.**

    Oh ya, untuk ekstraksinya tolong beri dalam Format Capitalized di tiap awal kata. Namun untuk alamat, gunakan kapitalisasi di awal kata jalan, perumahan, blok, dan nomor rumah.
    Jika ada huruf "no" setelah jalan, mohon dipisah. Karena kadang itu tercampur seperti "JL. SINGALODRANO 24/B" seharusnya menjadi "Jl. Singalodrano 24/B".
    """

    try:
        response = openai.chat.completions.create(
            model="gpt-4o",
            messages=[
                {
                    "role": "user",
                    "content": [
                        {"type": "text", "text": prompt},
                        {
                            "type": "image_url",
                            "image_url": {
                                "url": f"data:image/jpeg;base64,{base64_image}"
                            }
                        }
                    ]
                }
            ],
            max_tokens=1000
        )
        raw_content = response.choices[0].message.content
        print(f"OpenAI raw response content: {raw_content}") 

        # --- PERBAIKAN PENTING: Bersihkan output dari OpenAI agar JSON murni ---
        # Cari substring JSON yang sebenarnya (mulai dari '{' hingga '}')
        start_index = raw_content.find('{')
        end_index = raw_content.rfind('}')

        if start_index != -1 and end_index != -1 and end_index > start_index:
            json_string = raw_content[start_index : end_index + 1]
            try:
                json_data = json.loads(json_string)
                # Kembalikan string JSON yang sudah dipastikan valid dan bersih
                return json.dumps(json_data)
            except json.JSONDecodeError:
                print(f"Error: Cleaned OpenAI response is still not valid JSON. Cleaned content: {json_string}")
                return json.dumps({}) # Kembalikan JSON kosong jika tidak valid
        else:
            print(f"Error: Could not find valid JSON object in OpenAI response. Raw content: {raw_content}")
            return json.dumps({}) # Kembalikan JSON kosong jika tidak ada JSON yang valid
        # --- END OF PERBAIKAN ---

    except openai.APIError as e:
        print(f"OpenAI API Error: {e.status_code} - {e.response}")
        return json.dumps({"error": f"OpenAI API Error: {e.status_code}"})
    except openai.APITimeoutError as e:
        print(f"OpenAI API Timeout: {e}")
        return json.dumps({"error": "OpenAI API Timeout"})
    except openai.APIConnectionError as e:
        print(f"OpenAI API Connection Error: {e}")
        return json.dumps({"error": "OpenAI API Connection Error"})
    except Exception as e:
        print(f"An unexpected error occurred during OpenAI call: {e}")
        return json.dumps({"error": f"Unexpected error: {str(e)}"})