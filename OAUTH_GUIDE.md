# GitHub OAuth Device Flow Setup

Rock Release Hub uses GitHub OAuth Device Flow for native Android authentication. It does not use a browser callback to return the authorization result to the app.

## Register the OAuth App

Open **GitHub → Settings → Developer settings → OAuth Apps → New OAuth App** and enter:

- **Application name:** `Rock Release Hub`
- **Homepage URL:** `https://github.com/Sayanthrock-Developer/Rock-Release-Hub`
- **Application description:** `A native Android control centre for GitHub releases, workflow builds, artifacts, APK inspection, downloads, and application updates.`
- **Authorization callback URL:** `https://github.com/Sayanthrock-Developer/Rock-Release-Hub`
- **Enable Device Flow:** checked

The callback URL is required by GitHub's registration form, but Rock Release Hub does not use it during Device Flow authentication.

Select **Register application**, then copy the generated **Client ID**.

## Project configuration

The registered OAuth Client ID is configured in the `feature-auth` module. A different OAuth App can be used without editing source code by passing a Gradle property:

```bash
./gradlew assembleDebug -PGITHUB_CLIENT_ID=YOUR_CLIENT_ID
```

A GitHub OAuth Client ID is a public application identifier and is included in the APK. The Client Secret must never be included.

## Security rules

- Use only the OAuth App **Client ID** in the Android application.
- Never bundle the OAuth App **Client Secret** in the APK, repository, Gradle files, logs, or GitHub Actions artifacts.
- Store the returned access token with Android Keystore-backed encrypted storage.
- Never print access tokens in logs.

## Device Flow endpoints

The Android client uses:

- Device code request: `POST https://github.com/login/device/code`
- Access-token polling: `POST https://github.com/login/oauth/access_token`
- Verification page: `https://github.com/login/device`

Send `Accept: application/json` for both OAuth requests.

## Authentication flow

1. Request a device code using the Client ID and required scopes.
2. Display the returned `user_code` and `verification_uri`.
3. Open the verification page in the user's browser.
4. Poll the access-token endpoint using the server-provided interval.
5. Handle `authorization_pending`, `slow_down`, `expired_token`, and `access_denied` without exposing secrets.
6. Encrypt the access token with an Android Keystore AES-GCM key before saving it locally.
7. Attach the token to GitHub API requests through the `Authorization: Bearer` header.

## Local development

Keep local OAuth configuration out of version control. Do not commit `local.properties` or any generated secrets.
