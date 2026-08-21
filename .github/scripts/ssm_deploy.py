#!/usr/bin/env python3
"""
Generate and execute the Nandhas backend SSM deployment.

This script is called by GitHub Actions and:
1. Reads deployment parameters from environment variables.
2. Generates the remote EC2 deployment bash script safely (no heredocs).
3. Base64-encodes it and sends it via AWS SSM.
4. Polls until completion.
5. Streams stdout/stderr back to the Actions log.
6. Exits non-zero if the deployment failed.

NO secret values are ever printed to stdout/stderr.
"""

import os
import sys
import json
import base64
import subprocess
import time
import string

# ── Helpers ───────────────────────────────────────────────────────────────────

def env(name, required=True, default=""):
    val = os.environ.get(name, default)
    if required and not val:
        print(f"ERROR: Required env var {name} is not set", file=sys.stderr)
        sys.exit(1)
    return val


def aws(*args, capture=True, check=True):
    cmd = ["aws"] + list(args)
    result = subprocess.run(cmd, capture_output=capture, text=True)
    if check and result.returncode != 0:
        print(result.stderr, file=sys.stderr)
        sys.exit(result.returncode)
    return result.stdout.strip() if capture else None


# ── Read parameters ────────────────────────────────────────────────────────────

IMAGE_URI       = env("IMAGE_URI")
ECR_REGISTRY    = env("ECR_REGISTRY")
AWS_REGION      = env("AWS_REGION_VAL")
EC2_INSTANCE_ID = env("EC2_INSTANCE_ID")
RUNTIME_ENV_B64 = env("RUNTIME_ENV_B64")
FIREBASE_B64    = env("FIREBASE_B64", required=False, default="")
GH_SHA          = env("GH_SHA", required=False, default="unknown")

# ── Build the remote bash script ──────────────────────────────────────────────
# We use string.Template ($var syntax) so that bash ${VAR} and {.Names}
# docker format strings are NOT touched by Python's substitution.
# Only $image, $registry, $region, $env_b64, $firebase_b64 are substituted.

SCRIPT_TEMPLATE = r"""#!/usr/bin/env bash
set -euo pipefail

IMAGE='$image'
REGISTRY='$registry'
REGION='$region'
ENV_B64='$env_b64'
FIREBASE_B64='$firebase_b64'

echo "=== Nandhas Backend Deployment ==="
echo "Image  : $$IMAGE"
echo "Region : $$REGION"

# ── Create required directories ───────────────────────────────────────────────
sudo mkdir -p /opt/nandhas/secrets /opt/nandhas/uploads

# ── Write env file (secrets decoded on EC2, never printed) ───────────────────
printf '%s' "$$ENV_B64" | base64 -d | sudo tee /opt/nandhas/backend.env > /dev/null
echo "backend.env written"

# ── Write Firebase credentials ────────────────────────────────────────────────
if [ -n "$$FIREBASE_B64" ]; then
  printf '%s' "$$FIREBASE_B64" | base64 -d | \
    sudo tee /opt/nandhas/secrets/firebase-service-account.json > /dev/null
  echo "Firebase credentials written"
else
  echo '{}' | sudo tee /opt/nandhas/secrets/firebase-service-account.json > /dev/null
  echo "WARNING: No Firebase credentials provided, wrote empty JSON"
fi

# ── Permissions ───────────────────────────────────────────────────────────────
sudo chmod 755 /opt/nandhas /opt/nandhas/secrets /opt/nandhas/uploads
sudo chmod 644 /opt/nandhas/backend.env \
               /opt/nandhas/secrets/firebase-service-account.json
sudo chmod -R 777 /opt/nandhas/uploads

# ── ECR login via EC2 IAM role (no static AWS keys needed) ───────────────────
echo "Logging Docker into ECR..."
aws ecr get-login-password --region "$$REGION" | \
  sudo docker login --username AWS --password-stdin "$$REGISTRY"
echo "ECR login OK"

# ── Pull the exact image built by CI ─────────────────────────────────────────
echo "Pulling: $$IMAGE"
sudo docker pull "$$IMAGE"
echo "Pull OK"

# ── Stop and remove previous container ────────────────────────────────────────
if sudo docker ps -a --format '{{.Names}}' | grep -q '^nandhas-backend$'; then
  echo "Removing old container..."
  sudo docker stop nandhas-backend 2>/dev/null || true
  sudo docker rm   nandhas-backend 2>/dev/null || true
  echo "Old container removed"
fi

# ── Start new container ───────────────────────────────────────────────────────
CONTAINER_ID=$(sudo docker run -d \
  --name nandhas-backend \
  --restart unless-stopped \
  --env-file /opt/nandhas/backend.env \
  -p 8080:8080 \
  -v /opt/nandhas/secrets:/opt/nandhas/secrets:ro \
  -v /opt/nandhas/uploads:/app/uploads \
  "$$IMAGE")
echo "Container started: $$CONTAINER_ID"

# ── Wait for JVM to initialize ────────────────────────────────────────────────
echo "Waiting 15s for Spring Boot to initialize..."
sleep 15

# ── Verify the container did not crash on startup ─────────────────────────────
if ! sudo docker ps --format '{{.Names}}' | grep -q '^nandhas-backend$'; then
  echo "ERROR: Container exited immediately after start."
  echo "--- container state ---"
  sudo docker inspect nandhas-backend \
    --format='Status={{.State.Status}} ExitCode={{.State.ExitCode}}' \
    2>/dev/null || true
  echo "--- all containers ---"
  sudo docker ps -a
  echo "--- container logs (300 lines, NO secrets printed) ---"
  sudo docker logs --tail 300 "$$CONTAINER_ID" 2>&1 || true
  exit 1
fi

# ── HTTP health-check loop (25 x 5s = 125s max) ──────────────────────────────
echo "Container running. Health-checking HTTP on port 8080..."
HEALTHY=0
for i in $(seq 1 25); do
  CODE=$(curl -s -o /dev/null -w '%{http_code}' \
    'http://127.0.0.1:8080/api/products?page=0&size=1' || echo '000')
  if [ "$$CODE" = "200" ] || [ "$$CODE" = "401" ] || [ "$$CODE" = "403" ]; then
    echo "Backend responsive (HTTP $$CODE) on attempt $$i"
    HEALTHY=1
    break
  fi
  echo "Attempt $$i/25: HTTP $$CODE - retrying in 5s..."
  sleep 5
done

if [ "$$HEALTHY" -ne 1 ]; then
  echo "ERROR: Health check failed after 25 attempts."
  echo "--- container state ---"
  sudo docker inspect nandhas-backend \
    --format='Status={{.State.Status}} ExitCode={{.State.ExitCode}}' \
    2>/dev/null || true
  echo "--- container logs (300 lines, NO secrets) ---"
  sudo docker logs --tail 300 nandhas-backend 2>&1 || true
  exit 1
fi

# ── Prune old images ──────────────────────────────────────────────────────────
sudo docker image prune -af --filter "until=168h" 2>/dev/null || true
echo "Deployment completed successfully!"
"""

# ── Substitute only our placeholders (not bash variables) ─────────────────────
# string.Template uses $var syntax; $$ in the template becomes $ in output.
tmpl = string.Template(SCRIPT_TEMPLATE)
deploy_script = tmpl.substitute(
    image=IMAGE_URI,
    registry=ECR_REGISTRY,
    region=AWS_REGION,
    env_b64=RUNTIME_ENV_B64,
    firebase_b64=FIREBASE_B64,
)

# ── Write to disk ─────────────────────────────────────────────────────────────
script_path = "/tmp/ssm-deploy.sh"
with open(script_path, "w") as f:
    f.write(deploy_script)
print(f"Remote deploy script written ({len(deploy_script)} bytes)")

# ── Base64-encode for safe SSM transport ──────────────────────────────────────
with open(script_path, "rb") as f:
    script_b64 = base64.b64encode(f.read()).decode("ascii")

# ── Build SSM command list ────────────────────────────────────────────────────
commands = [
    f'printf "%s" {script_b64} | base64 -d > /tmp/nandhas-deploy.sh',
    "chmod 700 /tmp/nandhas-deploy.sh",
    "bash /tmp/nandhas-deploy.sh",
]

# ── Send SSM command ──────────────────────────────────────────────────────────
print(f"Sending SSM command to EC2: {EC2_INSTANCE_ID}")
result = subprocess.run(
    [
        "aws", "ssm", "send-command",
        "--instance-ids", EC2_INSTANCE_ID,
        "--document-name", "AWS-RunShellScript",
        "--comment", f"nandhas-deploy-{GH_SHA[:8]}",
        "--parameters", json.dumps({"commands": commands}),
        "--timeout-seconds", "600",
        "--query", "Command.CommandId",
        "--output", "text",
    ],
    capture_output=True, text=True, check=True,
)
command_id = result.stdout.strip()
print(f"SSM CommandId: {command_id}")

# ── Poll until terminal state ─────────────────────────────────────────────────
max_wait = 660
interval  = 15
elapsed   = 0
status    = "Pending"

print("Polling SSM command status...")
while elapsed < max_wait:
    poll = subprocess.run(
        [
            "aws", "ssm", "get-command-invocation",
            "--command-id", command_id,
            "--instance-id", EC2_INSTANCE_ID,
            "--query", "Status",
            "--output", "text",
        ],
        capture_output=True, text=True,
    )
    status = poll.stdout.strip() if poll.returncode == 0 else "Pending"
    print(f"  [{elapsed}s] Status: {status}")
    if status in ("Success", "Failed", "Cancelled", "TimedOut"):
        break
    time.sleep(interval)
    elapsed += interval

# ── Stream EC2 stdout and stderr back to Actions console ──────────────────────
print("=" * 64)
print("REMOTE STDOUT (EC2 deployment log):")
print("=" * 64)
out = subprocess.run(
    [
        "aws", "ssm", "get-command-invocation",
        "--command-id", command_id,
        "--instance-id", EC2_INSTANCE_ID,
        "--query", "StandardOutputContent",
        "--output", "text",
    ],
    capture_output=True, text=True,
)
print(out.stdout or "(no stdout)")

print("=" * 64)
print("REMOTE STDERR (EC2 deployment errors):")
print("=" * 64)
err = subprocess.run(
    [
        "aws", "ssm", "get-command-invocation",
        "--command-id", command_id,
        "--instance-id", EC2_INSTANCE_ID,
        "--query", "StandardErrorContent",
        "--output", "text",
    ],
    capture_output=True, text=True,
)
print(err.stdout or "(no stderr)")

print("=" * 64)
print(f"FINAL SSM STATUS: {status}")
print("=" * 64)

# ── Fail the job if the deployment was not successful ─────────────────────────
if status != "Success":
    print(f"ERROR: Deployment failed with SSM status: {status}", file=sys.stderr)
    sys.exit(1)

print("Pipeline completed successfully!")
