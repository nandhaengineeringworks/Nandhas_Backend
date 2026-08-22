#!/usr/bin/env python3
"""
Generate and execute the Nandhas backend SSM deployment.

Design: the shell script template uses ##MARKER## tokens (not $variables)
for Python-side substitution.  Every bare $ in the template is a real shell
$ that will be evaluated on the EC2 instance — Python never touches them.

This avoids the string.Template 'Invalid placeholder' error that occurs when
shell constructs like  $();  ${VAR};  grep '^pattern$'  appear in the script.

NO secret values are ever printed to stdout/stderr.
"""

import os
import sys
import json
import base64
import subprocess
import time


# ── Helpers ───────────────────────────────────────────────────────────────────

def env(name, required=True, default=""):
    val = os.environ.get(name, default)
    if required and not val:
        print(f"ERROR: Required env var {name} is not set", file=sys.stderr)
        sys.exit(1)
    return val


def aws_cli(*args):
    """Run an aws CLI command and return stripped stdout. Exits on failure."""
    result = subprocess.run(["aws"] + list(args), capture_output=True, text=True)
    if result.returncode != 0:
        print(result.stderr, file=sys.stderr)
        sys.exit(result.returncode)
    return result.stdout.strip()


def aws_query(*args):
    """Run aws CLI and return stdout (non-fatal). Used for status polling."""
    result = subprocess.run(["aws"] + list(args), capture_output=True, text=True)
    return result.stdout.strip() if result.returncode == 0 else ""


# ── Read parameters from GitHub Actions environment ───────────────────────────

IMAGE_URI       = env("IMAGE_URI")
ECR_REGISTRY    = env("ECR_REGISTRY")
AWS_REGION      = env("AWS_REGION_VAL")
EC2_INSTANCE_ID = env("EC2_INSTANCE_ID")
RUNTIME_ENV_B64 = env("RUNTIME_ENV_B64")
FIREBASE_B64    = env("FIREBASE_B64", required=False, default="")
GH_SHA          = env("GH_SHA", required=False, default="unknown")


# ── Shell script template ─────────────────────────────────────────────────────
# Tokens ##IMAGE##, ##REGISTRY##, ##REGION##, ##ENV_B64##, ##FIREBASE_B64##
# are substituted by Python below using plain str.replace().
#
# Every $ in this string is a SHELL variable / command-substitution / regex
# anchor that must remain intact and be evaluated on EC2 — Python never sees
# them as template placeholders.

SCRIPT_TEMPLATE = r"""#!/usr/bin/env bash
set -euo pipefail

IMAGE='##IMAGE##'
REGISTRY='##REGISTRY##'
REGION='##REGION##'
ENV_B64='##ENV_B64##'
FIREBASE_B64='##FIREBASE_B64##'

echo "=== Nandhas Backend Deployment ==="
echo "Image  : $IMAGE"
echo "Region : $REGION"

# ── Create required directories ───────────────────────────────────────────────
sudo mkdir -p /opt/nandhas/secrets /opt/nandhas/uploads

# ── Write env file (secrets decoded on EC2, never printed) ───────────────────
printf '%s' "$ENV_B64" | base64 -d | sudo tee /opt/nandhas/backend.env > /dev/null
echo "backend.env written"

# ── Write Firebase credentials ────────────────────────────────────────────────
if [ -n "$FIREBASE_B64" ]; then
  printf '%s' "$FIREBASE_B64" | base64 -d | \
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

# ── ECR login using EC2 IAM role (no static AWS keys) ────────────────────────
echo "Logging Docker into ECR..."
aws ecr get-login-password --region "$REGION" | \
  sudo docker login --username AWS --password-stdin "$REGISTRY"
echo "ECR login OK"

# ── Free disk space before pulling new image ─────────────────────────────
echo "Freeing disk space before pull..."
sudo docker image prune -af --filter "until=24h" 2>/dev/null || true
sudo docker container prune -f 2>/dev/null || true
echo "Disk free: $(df -h / | awk 'NR==2{print $4}') available"

# ── Pull the exact image built by CI ─────────────────────────────────────
echo "Pulling: $IMAGE"
sudo docker pull "$IMAGE"
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
  "$IMAGE")
echo "Container started: $CONTAINER_ID"

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
  sudo docker logs --tail 300 "$CONTAINER_ID" 2>&1 || true
  exit 1
fi

# ── HTTP health-check loop (25 x 5 s = 125 s max) ────────────────────────────
echo "Container running. Health-checking HTTP on port 8080..."
HEALTHY=0
for i in $(seq 1 25); do
  CODE=$(curl -s -o /dev/null -w '%{http_code}' \
    'http://127.0.0.1:8080/api/products?page=0&size=1' || echo '000')
  if [ "$CODE" = "200" ] || [ "$CODE" = "401" ] || [ "$CODE" = "403" ]; then
    echo "Backend responsive (HTTP $CODE) on attempt $i"
    HEALTHY=1
    break
  fi
  echo "Attempt $i/25: HTTP $CODE - retrying in 5s..."
  sleep 5
done

if [ "$HEALTHY" -ne 1 ]; then
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

# ── Substitute ##MARKER## tokens with real values ─────────────────────────────
# str.replace() is used deliberately — it never inspects $ characters at all,
# so shell variables, command substitutions and regex anchors are untouched.

deploy_script = (
    SCRIPT_TEMPLATE
    .replace("##IMAGE##",       IMAGE_URI)
    .replace("##REGISTRY##",    ECR_REGISTRY)
    .replace("##REGION##",      AWS_REGION)
    .replace("##ENV_B64##",     RUNTIME_ENV_B64)
    .replace("##FIREBASE_B64##", FIREBASE_B64)
)

# Quick sanity-check: no unresolved markers should remain
for marker in ("##IMAGE##", "##REGISTRY##", "##REGION##",
               "##ENV_B64##", "##FIREBASE_B64##"):
    if marker in deploy_script:
        print(f"ERROR: Marker {marker} was not replaced", file=sys.stderr)
        sys.exit(1)

# ── Write to disk ─────────────────────────────────────────────────────────────
script_path = "/tmp/ssm-deploy.sh"
with open(script_path, "w") as f:
    f.write(deploy_script)
print(f"Remote deploy script written ({len(deploy_script)} bytes)")

# ── Base64-encode for safe SSM transport ──────────────────────────────────────
with open(script_path, "rb") as f:
    script_b64 = base64.b64encode(f.read()).decode("ascii")

# ── Build SSM command list ────────────────────────────────────────────────────
# Three commands sent as a JSON array:
#   1. Decode the base64 script to a temp file
#   2. Make it executable
#   3. Execute it
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
max_wait = 660   # seconds
interval  = 15
elapsed   = 0
status    = "Pending"

print("Polling SSM command status...")
while elapsed < max_wait:
    status = aws_query(
        "ssm", "get-command-invocation",
        "--command-id", command_id,
        "--instance-id", EC2_INSTANCE_ID,
        "--query", "Status",
        "--output", "text",
    ) or "Pending"
    print(f"  [{elapsed}s] Status: {status}")
    if status in ("Success", "Failed", "Cancelled", "TimedOut"):
        break
    time.sleep(interval)
    elapsed += interval

# ── Always stream EC2 stdout + stderr to Actions console ─────────────────────
print("=" * 64)
print("REMOTE STDOUT (EC2 deployment log):")
print("=" * 64)
stdout_content = aws_query(
    "ssm", "get-command-invocation",
    "--command-id", command_id,
    "--instance-id", EC2_INSTANCE_ID,
    "--query", "StandardOutputContent",
    "--output", "text",
)
print(stdout_content or "(no stdout)")

print("=" * 64)
print("REMOTE STDERR (EC2 deployment errors):")
print("=" * 64)
stderr_content = aws_query(
    "ssm", "get-command-invocation",
    "--command-id", command_id,
    "--instance-id", EC2_INSTANCE_ID,
    "--query", "StandardErrorContent",
    "--output", "text",
)
print(stderr_content or "(no stderr)")

print("=" * 64)
print(f"FINAL SSM STATUS: {status}")
print("=" * 64)

# ── Fail the workflow job if the deployment was not successful ────────────────
if status != "Success":
    print(f"ERROR: Deployment failed with SSM status: {status}", file=sys.stderr)
    sys.exit(1)

print("Pipeline completed successfully!")
