# My Vault

An offline-first Android document vault with automatic ML Kit scanning, encrypted storage, biometric plus PIN unlock, PDF/Word export, printing, sharing, encrypted cloud backup, and a two-step GitHub Pages PC portal.

## Security design

- Every stored page and the vault index use AES-256-GCM.
- The encryption key is derived locally from the six-digit vault PIN with PBKDF2-HMAC-SHA256 (310,000 iterations) and a unique random salt.
- Android requires strong biometrics before accepting the PIN.
- The PC portal requires a Supabase email OTP before accepting the same PIN.
- The PIN and derived encryption key are never uploaded.
- Supabase Storage is private and protected by per-user row-level policies.
- Android backups and screenshots are disabled.

The Android scanner is provided by Google Play services ML Kit and performs automatic capture, corner detection, crop, rotation, filters, shadow removal, and stain cleanup on device.
