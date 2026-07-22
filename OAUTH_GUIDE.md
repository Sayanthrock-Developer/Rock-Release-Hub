# GitHub OAuth Guide

Rock Release Hub utilizes GitHub's Device Flow for authentication.

## Registering the App
1. Go to your GitHub account settings -> Developer settings -> OAuth Apps.
2. Click "New OAuth App".
3. Name it "Rock Release Hub" (or a test variation).
4. For Homepage URL, enter `https://github.com`.
5. For Authorization callback URL, enter `rockreleasehub://oauth2`.
6. Click "Register application".
7. Check "Enable Device Flow".

## Using in the App
The Client ID should be injected into the application securely to handle the device flow endpoints at `https://github.com/login/device/code`.
