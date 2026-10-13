# My Vault

An offline-first Android document vault with automatic ML Kit scanning, encrypted storage, biometric plus PIN unlock, PDF/Word export, printing, sharing, encrypted cloud backup, and a two-step GitHub Pages PC portal.

## Security design

- Every stored page and the vault index use AES-256-GCM.
- A random 256-bit vault key encrypts the data and is wrapped locally by Android Keystore. Existing PIN-encrypted vaults migrate in place after a successful unlock.
- Android requires strong biometrics before accepting the PIN.
- Cloud backup requires a separate recovery code. The code is strengthened with PBKDF2-HMAC-SHA256 and wraps the vault key; the code itself is never stored or uploaded.
- The PC portal requires a Supabase email OTP followed by the private recovery code.
- Supabase Storage is private and protected by per-user row-level policies.
- Android backups and screenshots are disabled.

The Android scanner is provided by Google Play services ML Kit and performs automatic capture, corner detection, crop, rotation, filters, shadow removal, and stain cleanup on device.
