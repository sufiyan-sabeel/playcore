# APK preservation record — ZKMNEXT GameSpace 1.2.9

The verified APK is kept byte-for-byte unchanged, read-only, under
`/scratch/work/preserved/ZKMNEXT-GameSpace-1.2.9-app-release.apk` (18,881,081 bytes, mode 444).
It was never re-signed, repackaged, renamed inside the archive, or edited.

| Field | Value |
| --- | --- |
| Original release URL | https://github.com/zuanvfx02/zkmnext-gamespace/releases/download/1.2.9/app-release.apk |
| Release page | https://github.com/zuanvfx02/zkmnext-gamespace/releases/tag/1.2.9 |
| Original asset filename | `app-release.apk` |
| Local preserved filename | `ZKMNEXT-GameSpace-1.2.9-app-release.apk` (rename only, bytes untouched) |
| Size | 18,881,081 bytes |
| SHA-256 | `73861d7a58f8dc0e014f57fa380641027a98b895b3fad737e394fa03949c30e1` |
| GitHub stored asset digest | `sha256:73861d7a58f8dc0e014f57fa380641027a98b895b3fad737e394fa03949c30e1` (matches) |
| Package name | `com.zkmnext.gamespace` |
| Application label | GameSpace |
| versionName / versionCode | 1.2.9 / 14 |
| minSdk / targetSdk | 29 / 35 |
| ABIs | arm64-v8a, armeabi-v7a |
| Signature schemes | v1, v2, v3 |
| Signing certificate SHA-1 | `4D:D3:E4:4D:7F:32:19:28:8C:A0:6A:A7:90:DB:CF:67:24:F9:F7:A8` |
| Signing certificate SHA-256 | `B601FB0F67D7D67A190E51E34893CCEE0B0C491F76C281CF6F21E1095DAD9EBA` |
| Certificate validity | 2026-11-04 → 2051-10-29, CN=release, self-signed |
| Archive integrity | `zipfile.testzip()` clean; 771 entries |

## Verification procedure

Run `bash docs/upstream/verify_apk.sh <path-to-apk>`. It checks the size, the SHA-256, the archive
integrity, the package, version, SDK levels, ABIs and the signing certificate fingerprints, and exits
non-zero on any mismatch. The same fields can be checked by hand:

```bash
sha256sum app-release.apk
unzip -t app-release.apk | tail -1
```

On a device or with Android SDK build-tools available:

```bash
apksigner verify --print-certs app-release.apk
aapt2 dump badging app-release.apk | head -5
```

Any copy whose SHA-256 or certificate fingerprint differs from the values above is not the release this
record describes.
