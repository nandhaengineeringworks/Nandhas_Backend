# Backend CI/CD: ECR → EC2

The workflow in `.github/workflows/backend-cicd.yml` performs this sequence on every push to `main`:

1. Runs the Maven tests.
2. Builds the backend Docker image.
3. Pushes the image to Amazon ECR in `ap-south-2`.
4. Uses AWS Systems Manager to deploy the image to the selected EC2 instance.
5. Writes the RDS, S3, JWT, and Firebase runtime configuration on EC2.
6. Verifies the API health through `/api/products`.

## AWS resources required

Create these resources in `ap-south-2`:

- ECR private repository: `nandhas-backend`
- RDS MySQL database
- S3 bucket for media uploads
- EC2 instance running Docker
- Systems Manager-managed EC2 instance

Attach an EC2 instance role containing:

- `AmazonSSMManagedInstanceCore`
- Read-only pull access to the ECR repository
- S3 read/write access limited to the media bucket

The GitHub deployment IAM user needs:

- ECR push permissions for `nandhas-backend`
- `ssm:SendCommand`, `ssm:GetCommandInvocation`, and `ssm:ListCommandInvocations` for the target instance
- `iam:PassRole` only if your deployment design requires passing a role

ECR requires authorization plus layer-upload and image-write permissions; use a repository-scoped least-privilege policy. See the [AWS ECR push permissions](https://docs.aws.amazon.com/AmazonECR/latest/userguide/image-push-iam.html) guide.

## GitHub repository secrets

Open the backend repository on GitHub:

`Settings → Secrets and variables → Actions → New repository secret`

Create these secrets:

| Secret | Value |
|---|---|
| `AWS_ACCESS_KEY_ID` | IAM deployment user access key |
| `AWS_SECRET_ACCESS_KEY` | IAM deployment user secret key |
| `AWS_REGION` | `ap-south-2` |
| `ECR_REPOSITORY` | `nandhas-backend` |
| `EC2_INSTANCE_ID` | EC2 instance ID, for example `i-xxxxxxxx` |
| `DB_HOST` | RDS endpoint, without `https://` |
| `DB_PORT` | `3306` |
| `DB_NAME` | `nandhas_ecommerce` |
| `DB_USERNAME` | RDS application username |
| `DB_PASSWORD` | RDS application password |
| `AWS_S3_BUCKET` | S3 bucket name |
| `AWS_CLOUDFRONT_DOMAIN` | CloudFront hostname, or blank |
| `JWT_SECRET` | Long random application signing secret |
| `FIREBASE_SERVICE_ACCOUNT_JSON_B64` | Base64-encoded Firebase service-account JSON |

Create the Firebase secret on PowerShell without putting the JSON in Git:

```powershell
$json = [IO.File]::ReadAllBytes('C:\secure\firebase-service-account.json')
[Convert]::ToBase64String($json)
```

Copy the printed single-line value into `FIREBASE_SERVICE_ACCOUNT_JSON_B64`.

GitHub recommends repository secrets for sensitive Action values. Never put these values in workflow files or source code. See the [GitHub Actions secrets reference](https://docs.github.com/en/actions/reference/security/secrets).

## EC2 prerequisites

Install Docker and ensure the SSM Agent is connected. The instance must be able to reach:

- ECR endpoints
- SSM endpoints
- RDS on TCP port `3306`
- S3

In the RDS security group, allow TCP `3306` from the EC2 security group, not from the entire internet.

After adding all secrets, push to the backend `main` branch. The workflow will deploy automatically. You can also run it manually from GitHub Actions using **Run workflow**.
