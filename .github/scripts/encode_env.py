#!/usr/bin/env python3
"""
Encode runtime environment variables as a base64 string and write to
GitHub Actions output.

Reads from environment variables set in the workflow step.
Prints:  env_b64=<base64string>
to stdout, which GitHub Actions appends to $GITHUB_OUTPUT.

NO secret values are ever printed beyond the base64-encoded blob.
"""

import os
import base64
import sys


def get(name, default=""):
    return os.environ.get(name, default)


lines = [
    "SPRING_PROFILES_ACTIVE=prod",
    f"DB_HOST={get('V_DB_HOST')}",
    f"DB_PORT={get('V_DB_PORT', '3306')}",
    f"DB_NAME={get('V_DB_NAME', 'nandhas_ecommerce')}",
    f"DB_USERNAME={get('V_DB_USER')}",
    f"DB_PASSWORD={get('V_DB_PASS')}",
    "STORAGE_TYPE=s3",
    f"AWS_REGION={get('V_REGION')}",
    f"AWS_S3_BUCKET={get('V_S3_BUCKET')}",
    f"JWT_SECRET={get('V_JWT')}",
    "FIREBASE_ENABLED=true",
    "GOOGLE_APPLICATION_CREDENTIALS=/opt/nandhas/secrets/firebase-service-account.json",
]

# Validate required fields are not empty
required_envs = {
    "V_DB_HOST":   get("V_DB_HOST"),
    "V_DB_USER":   get("V_DB_USER"),
    "V_DB_PASS":   get("V_DB_PASS"),
    "V_JWT":       get("V_JWT"),
    "V_S3_BUCKET": get("V_S3_BUCKET"),
    "V_REGION":    get("V_REGION"),
}
missing = [k for k, v in required_envs.items() if not v]
if missing:
    print(f"ERROR: Missing env vars for encode_env.py: {missing}", file=sys.stderr)
    sys.exit(1)

content = "\n".join(lines) + "\n"
encoded = base64.b64encode(content.encode("utf-8")).decode("ascii")
print(f"env_b64={encoded}")
