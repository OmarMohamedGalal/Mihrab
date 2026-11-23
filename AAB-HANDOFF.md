# Mihrab Android App Bundle handoff

`Mihrab-v2.0.aab` has been replaced with the latest **6.1** bundle. The legacy filename is kept to replace the existing repository artifact at the same path.

- Source: commit `69172c719e094b0405870bd7c6cb880fdab69dbe`
- Application ID: `com.aistudio.mihrab.kqzmxp` (no `.test` suffix)
- App name: Mihrab / محراب
- Version code: `7`
- Version name: `6.1`
- Build type: release, not debuggable
- SHA-256: `e2f2bea6a234ce84262df5fbbef837f49ad3bb19290aea06630421e998d08fde`
- Size: 17,579,795 bytes

## Signing still required

**This AAB is unsigned. It is not ready to upload to Google Play.** The original bundle's private signing key was not available, and no replacement signing key was used. Sign it with the appropriate upload key in the original signing environment before continuing distribution. No Play publishing was performed.

The previous AAB is retained in Git history. Its certificate fingerprint was SHA-256 `64:AF:B1:C9:DB:EA:6F:CB:13:4F:02:3A:E9:8E:F6:06:D4:94:BB:A1:2C:F0:27:EE:D8:64:3A:04:48:A0:1C:53` (Android Debug). That certificate alone does not establish the current Play upload-key requirements.

Validation: release build and release vital lint passed; bundletool validation passed; manifest identity/version checked; absence of JAR signing verified. Source code is unchanged from the source commit above.
