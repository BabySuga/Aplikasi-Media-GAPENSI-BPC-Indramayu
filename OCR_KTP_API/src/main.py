import sys
import os
import json
import traceback
sys.path.append(os.path.abspath(os.path.join(os.path.dirname(__file__), '..')))

from fastapi import FastAPI, UploadFile, File, Form, HTTPException
from fastapi.responses import JSONResponse
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel
import smtplib
from email.mime.text import MIMEText
from src.OCR_KTP import extract_info_from_image
from src.resize_image import resize_image
import firebase_admin
from firebase_admin import credentials, db, storage
from datetime import datetime
from dotenv import load_dotenv
from passlib.context import CryptContext
import boto3 # Untuk AWS S3
from PIL import Image 
import io

# --- KONFIGURASI KEAMANAN PASSWORD ---
pwd_context = CryptContext(schemes=["bcrypt"], deprecated="auto")

# Load environment variables
load_dotenv()

# Inisialisasi Firebase Realtime Database
cred_path = 'gapensi-firebase-firebase-adminsdk-fbsvc-f4e05a3366.json'
if not os.path.exists(cred_path):
    print(f"Error: Firebase credential file not found at {cred_path}", file=sys.stderr)
    sys.exit(1)

cred = credentials.Certificate(cred_path)
firebase_admin.initialize_app(cred, {
    'databaseURL': 'https://gapensi-firebase-default-rtdb.asia-southeast1.firebasedatabase.app/',
    'storageBucket': 'gapensi-firebase.appspot.com'
})

# Inisialisasi AWS S3
AWS_REGION = os.getenv('AWS_REGION')
S3_BUCKET = os.getenv('AWS_S3_BUCKET')
if AWS_REGION and S3_BUCKET:
    s3_client = boto3.client(
        's3',
        region_name=AWS_REGION
    )
else:
    print("Warning: AWS_REGION or AWS_S3_BUCKET not set. S3 functionality may fail.", file=sys.stderr)
    s3_client = None

app = FastAPI()

origins = ["*"]

app.add_middleware(
    CORSMiddleware,
    allow_origins=origins,
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# --- Pydantic Models ---
class MemberUpdateRequest(BaseModel):
    user_id: str
    nama_perusahaan: str
    username: str
    ktp_info: dict

class ApprovalRequest(BaseModel):
    user_id: str
    action: str
    username: str = None
    password: str = None

class LoginRequest(BaseModel):
    username: str
    password: str

# --- Fungsi Pengiriman Email ---
def send_email(to_email, username, password):
    try:
        msg = MIMEText(
            f"Selamat! Pendaftaran Anda untuk Aplikasi Media Gapensi BPC Indramayu telah disetujui.\n\n"
            f"Berikut adalah kredensial login Anda:\n"
            f"Username: {username}\n"
            f"Password: {password}\n\n"
            f"Gunakan kredensial ini untuk masuk ke Aplikasi Media Gapensi BPC Indramayu melalui aplikasi mobile.\n"
            f"Jika Anda mengalami masalah saat login, silakan hubungi admin melalui email gapensi.indramayoe@gmail.com atau WhatsApp di +6281224584489.\n\n"
            f"Terima kasih,\n"
            f"Tim Gapensi BPC Indramayu"
        )
        msg['Subject'] = 'Akun Anda Telah Disetujui - Aplikasi Media Gapensi BPC Indramayu melalui aplikasi mobile'
        msg['From'] = os.getenv('EMAIL_SENDER')
        msg['To'] = to_email

        if not os.getenv('EMAIL_SENDER') or not os.getenv('EMAIL_PASSWORD'):
            print("Error: EMAIL_SENDER or EMAIL_PASSWORD not set in environment variables.", file=sys.stderr)
            return

        with smtplib.SMTP('smtp.gmail.com', 587) as server:
            server.starttls()
            server.login(os.getenv('EMAIL_SENDER'), os.getenv('EMAIL_PASSWORD'))
            server.send_message(msg)
        print(f"Email sent successfully to {to_email}")
    except Exception as e:
        print(f"Failed to send email to {to_email}: {e}", file=sys.stderr)
        print(traceback.format_exc(), file=sys.stderr)

# --- API Endpoints ---

@app.post("/upload-ktp/")
async def upload_ktp(file: UploadFile = File(...)):
    try:
        temp_file_path = f"temp_{file.filename}"
        with open(temp_file_path, "wb") as temp_file:
            temp_file.write(await file.read())

        ktp_info_str = extract_info_from_image(temp_file_path)
        os.remove(temp_file_path)

        ktp_info_dict = {}
        try:
            ktp_info_dict = json.loads(ktp_info_str)
        except json.JSONDecodeError:
            print(f"Warning: Failed to parse KTP info JSON string from OCR: {ktp_info_str}", file=sys.stderr)

        users_ref = db.reference('users')
        user_data = {
            "ktp_info_raw": ktp_info_str,
            "status": "pending",
            "created_at": datetime.now().isoformat(),
            "nik": ktp_info_dict.get("nik", "N/A"),
            "nama": ktp_info_dict.get("nama", "N/A"),
            "tempat_lahir": ktp_info_dict.get("tempat_lahir", "N/A"),
            "tanggal_lahir": ktp_info_dict.get("tanggal_lahir", "N/A"),
            "jenis_kelamin": ktp_info_dict.get("jenis_kelamin", "N/A"),
            "golongan_darah": ktp_info_dict.get("golongan_darah", "N/A"),
            "alamat": ktp_info_dict.get("alamat", "N/A"),
            "rt": ktp_info_dict.get("rt", "N/A"),
            "rw": ktp_info_dict.get("rw", "N/A"),
            "kelurahan_atau_desa": ktp_info_dict.get("kelurahan_atau_desa", "N/A"),
            "kecamatan": ktp_info_dict.get("kecamatan", "N/A"),
            "agama": ktp_info_dict.get("agama", "N/A"),
            "status_perkawinan": ktp_info_dict.get("status_perkawinan", "N/A"),
            "pekerjaan": ktp_info_dict.get("pekerjaan", "N/A"),
            "kewarganegaraan": ktp_info_dict.get("kewarganegaraan", "N/A")
        }
        new_user_ref = users_ref.push(user_data)
        user_id = new_user_ref.key

        return JSONResponse(content={"message": "Image uploaded successfully", "user_id": user_id})
    except Exception as e:
        print(f"Error in /upload-ktp/: {e}", file=sys.stderr)
        print(traceback.format_exc(), file=sys.stderr)
        raise HTTPException(status_code=500, detail=f"An error occurred during KTP upload: {str(e)}")

@app.post("/submit-email/")
async def submit_email(user_id: str = Form(...), email: str = Form(...), nama_perusahaan: str = Form(...)):
    try:
        user_ref = db.reference(f'users/{user_id}')
        if user_ref.get() is None:
            raise HTTPException(status_code=404, detail="User not found")

        user_ref.update({
            "email": email,
            "nama_perusahaan": nama_perusahaan
        })
        return JSONResponse(content={"message": "Email and company name submitted successfully"})
    except Exception as e:
        print(f"Error in /submit-email/: {e}", file=sys.stderr)
        print(traceback.format_exc(), file=sys.stderr)
        raise HTTPException(status_code=500, detail=str(e))

@app.post("/approve-reject/")
async def approve_reject_registration(request: ApprovalRequest):
    try:
        user_ref = db.reference(f'users/{request.user_id}')
        user = user_ref.get()
        if not user:
            raise HTTPException(status_code=404, detail="User not found")

        if request.action == "approve":
            if not (request.username and request.password):
                raise HTTPException(status_code=400, detail="Username and password are required for approval")

            hashed_password = pwd_context.hash(request.password)

            user_ref.update({
                "status": "approved",
                "username": request.username,
                "password": hashed_password
            })

            if user.get("email"):
                send_email(user.get("email"), request.username, request.password)
                print(f"Approval email triggered for {user.get('email')}")

        elif request.action == "reject":
            user_ref.delete()
            print(f"User {request.user_id} has been rejected and deleted from database.")
        else:
            raise HTTPException(status_code=400, detail="Invalid action. Use 'approve' or 'reject'.")

        return JSONResponse(content={"message": f"User registration has been {request.action}d."})
    except Exception as e:
        print(f"Error in /approve-reject/: {e}", file=sys.stderr)
        print(traceback.format_exc(), file=sys.stderr)
        raise HTTPException(status_code=500, detail=str(e))

@app.post("/login/")
async def login(request: LoginRequest):
    try:
        users_ref = db.reference('users')
        all_users = users_ref.order_by_child('username').equal_to(request.username).get()

        if not all_users:
            raise HTTPException(status_code=401, detail="Invalid username or password")

        user_id, user_data = list(all_users.items())[0]

        if user_data.get("status") != "approved":
            raise HTTPException(status_code=403, detail="User account is not approved")

        if not pwd_context.verify(request.password, user_data.get("password")):
            raise HTTPException(status_code=401, detail="Invalid username or password")

        user_display_name = user_data.get("nama", "N/A")
        user_email = user_data.get("email")

        return JSONResponse(content={
            "message": "Login successful",
            "user_id": user_id,
            "nama": user_display_name,
            "email": user_email
        })
    except HTTPException as http_exc:
        print(f"HTTP Exception in /login/: {http_exc.detail}", file=sys.stderr)
        print(traceback.format_exc(), file=sys.stderr)
        raise http_exc
    except Exception as e:
        print(f"Error in /login/: {e}", file=sys.stderr)
        print(traceback.format_exc(), file=sys.stderr)
        raise HTTPException(status_code=500, detail=f"An internal server error occurred: {str(e)}")

@app.get("/pending-registrations/")
async def get_pending_registrations():
    try:
        users_ref = db.reference('users')
        users = users_ref.get()
        if not users:
            return []
        pending_users = [
            {"id": user_id, **data}
            for user_id, data in users.items()
            if data.get("status") == "pending"
        ]
        return pending_users
    except Exception as e:
        print(f"Error in /pending-registrations/: {e}", file=sys.stderr)
        print(traceback.format_exc(), file=sys.stderr)
        raise HTTPException(status_code=500, detail=str(e))

@app.get("/approved-members/")
async def get_approved_members():
    try:
        users_ref = db.reference('users')
        users = users_ref.get()
        if not users:
            return []
        approved_users = [
            {"id": user_id, **data}
            for user_id, data in users.items()
            if data.get("status") == "approved"
        ]
        return approved_users
    except Exception as e:
        print(f"Error in /approved-members/: {e}", file=sys.stderr)
        print(traceback.format_exc(), file=sys.stderr)
        raise HTTPException(status_code=500, detail=str(e))

@app.post("/update-member/")
async def update_member(request: MemberUpdateRequest):
    try:
        user_ref = db.reference(f'users/{request.user_id}')
        if user_ref.get() is None:
            raise HTTPException(status_code=404, detail="Member not found")

        update_data = {
            "nama_perusahaan": request.nama_perusahaan,
            "username": request.username,
            "nik": request.ktp_info.get("nik", "N/A"),
            "nama": request.ktp_info.get("nama", "N/A"),
            "tempat_lahir": request.ktp_info.get("tempat_lahir", "N/A"),
            "tanggal_lahir": request.ktp_info.get("tanggal_lahir", "N/A"),
            "jenis_kelamin": request.ktp_info.get("jenis_kelamin", "N/A"),
            "golongan_darah": request.ktp_info.get("golongan_darah", "N/A"),
            "alamat": request.ktp_info.get("alamat", "N/A"),
            "rt": request.ktp_info.get("rt", "N/A"),
            "rw": request.ktp_info.get("rw", "N/A"),
            "kelurahan_atau_desa": request.ktp_info.get("kelurahan_atau_desa", "N/A"),
            "kecamatan": request.ktp_info.get("kecamatan", "N/A"),
            "agama": request.ktp_info.get("agama", "N/A"),
            "status_perkawinan": request.ktp_info.get("status_perkawinan", "N/A"),
            "pekerjaan": request.ktp_info.get("pekerjaan", "N/A"),
            "kewarganegaraan": request.ktp_info.get("kewarganegaraan", "N/A"),
            "ktp_info_raw": json.dumps(request.ktp_info)
        }

        user_ref.update(update_data)
        return JSONResponse(content={"message": "Member data updated successfully"})
    except Exception as e:
        print(f"Error in /update-member/: {e}", file=sys.stderr)
        print(traceback.format_exc(), file=sys.stderr)
        raise HTTPException(status_code=500, detail=str(e))

@app.post("/delete-member/")
async def delete_member(user_id: str = Form(...)):
    try:
        user_ref = db.reference(f'users/{user_id}')
        if user_ref.get() is None:
            raise HTTPException(status_code=404, detail="Member not found")
        user_ref.delete()
        return JSONResponse(content={"message": "Member deleted"})
    except Exception as e:
        print(f"Error in /delete-member/: {e}", file=sys.stderr)
        print(traceback.format_exc(), file=sys.stderr)
        raise HTTPException(status_code=500, detail=str(e))

# --- Endpoint Pengumuman ---

@app.post("/add-announcement/")
async def add_announcement(judul: str = Form(...), isi: str = Form(...), tanggal: str = Form(...), file: UploadFile = File(...)):
    if s3_client is None or S3_BUCKET is None:
        raise HTTPException(status_code=500, detail="AWS S3 client or bucket not configured on server.")

    try:
        # Validasi tipe file
        allowed_types = ["image/jpeg", "image/jpg", "image/png"]
        if file.content_type not in allowed_types:
            raise HTTPException(status_code=400, detail="File must be JPG, JPEG, or PNG")

        # Baca seluruh konten file ke memori
        file_content = await file.read()
        
        file_size = len(file_content)
        if file_size > 300 * 1024:
            # This indicates the client-side compression might have failed or sent a larger file
            raise HTTPException(status_code=400, detail=f"File size exceeds 300KB limit. Current size: {file_size / 1024:.2f}KB")
        
        # --- UPLOAD GAMBAR KE AWS S3 ---
        file_name = f"pengumuman/{datetime.now().timestamp()}_{file.filename}"
        s3_client.upload_fileobj(
            io.BytesIO(file_content), # Upload dari BytesIO
            S3_BUCKET,
            file_name,
            ExtraArgs={
                'ContentType': file.content_type,
                'ACL': 'public-read' # Pastikan role IAM Anda memiliki s3:PutObjectAcl
            }
        )
        image_url = f"https://{S3_BUCKET}.s3.{os.getenv('AWS_REGION')}.amazonaws.com/{file_name}"

        # Simpan metadata ke Firebase Realtime Database
        ref = db.reference('pengumuman')
        new_announcement_ref = ref.push()
        
        data_to_save = {
            'id': new_announcement_ref.key,
            'judul': judul,
            'isi': isi,
            'gambarUrl': image_url,
            'tanggal': tanggal
        }
        
        new_announcement_ref.set(data_to_save)

        return JSONResponse(content={"message": "Pengumuman berhasil ditambahkan", "data": data_to_save})
    except HTTPException as http_exc:
        print(f"HTTP Exception in /add-announcement/: {http_exc.detail}", file=sys.stderr)
        print(traceback.format_exc(), file=sys.stderr)
        raise http_exc
    except Exception as e:
        print(f"Error in /add-announcement/: {e}", file=sys.stderr)
        print(traceback.format_exc(), file=sys.stderr)
        raise HTTPException(status_code=500, detail=f"Gagal menambahkan pengumuman: {str(e)}")

@app.get("/get-announcements/")
async def get_announcements():
    try:
        ref = db.reference('pengumuman')
        announcements = ref.get()
        if not announcements:
            return []
  
        return [value for key, value in announcements.items()]
    except Exception as e:
        print(f"Error in /get-announcements/: {e}", file=sys.stderr)
        print(traceback.format_exc(), file=sys.stderr)
        raise HTTPException(status_code=500, detail=f"Gagal mengambil pengumuman: {str(e)}")