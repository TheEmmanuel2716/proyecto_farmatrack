#!/usr/bin/env python3
import base64
import hashlib
import hmac
import json
import sys
import time


def encode(value):
    raw = json.dumps(value, separators=(",", ":")).encode()
    return base64.urlsafe_b64encode(raw).rstrip(b"=").decode()


if len(sys.argv) != 2:
    raise SystemExit("Uso: generate-test-jwt.py JWT_SECRET")

now = int(time.time())
header = encode({"alg": "HS256", "typ": "JWT"})
payload = encode({"sub": "demo-user", "iat": now, "exp": now + 3600})
signing_input = f"{header}.{payload}".encode()
signature = hmac.new(sys.argv[1].encode(), signing_input, hashlib.sha256).digest()
print(f"{header}.{payload}.{base64.urlsafe_b64encode(signature).rstrip(b'=').decode()}")
