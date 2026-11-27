# Mihrab Android App Bundle handoff

`Mihrab-v1.5.5.aab` is the current bundle (renamed from the old `Mihrab-v2.0.aab`), version **1.5.5**.

- Source: the commit that adds this file (Hijri 1x2 widget fix, Day card widget, version 1.5.5)
- Application ID: `com.aistudio.mihrab.kqzmxp` (no `.test` suffix)
- App name: Mihrab / محراب
- Version code: `7`
- Version name: `1.5.5`
- Build type: release, not debuggable
- SHA-256: `b78e5b46ba015e7ac33ed6082afe6665570d0ad0fe1abfda5dba7ecd4fada805`
- Size: 17,595,881 bytes

## Signing still required

**This AAB is unsigned. It is not ready to upload to Google Play.** The original bundle's private signing key was not available, and no replacement signing key was used. Sign it with the appropriate upload key in the original signing environment before continuing distribution. No Play publishing was performed.

The previous AAB is retained in Git history. Its certificate fingerprint was SHA-256 `64:AF:B1:C9:DB:EA:6F:CB:13:4F:02:3A:E9:8E:F6:06:D4:94:BB:A1:2C:F0:27:EE:D8:64:3A:04:48:A0:1C:53` (Android Debug). That certificate alone does not establish the current Play upload-key requirements.

Validation: release build succeeded; manifest identity/version checked; absence of JAR signing verified. Release lint was skipped for this build.
