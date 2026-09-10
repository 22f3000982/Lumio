import urllib.request
import mimetypes
import uuid

def upload_file(filename, upload_url):
    boundary = uuid.uuid4().hex
    headers = {'Content-Type': f'multipart/form-data; boundary={boundary}'}
    with open(filename, 'rb') as f:
        file_bytes = f.read()
    body = (
        f'--{boundary}\r\n'
        f'Content-Disposition: form-data; name="file"; filename="{filename}"\r\n'
        f'Content-Type: application/vnd.android.package-archive\r\n\r\n'
    ).encode('utf-8') + file_bytes + f'\r\n--{boundary}--\r\n'.encode('utf-8')
    req = urllib.request.Request(upload_url, data=body, headers=headers, method='POST')
    with urllib.request.urlopen(req, timeout=60) as resp:
        print(resp.read().decode('utf-8'))

upload_file('Lumio-Class10.apk', 'https://temp.sh/upload')
