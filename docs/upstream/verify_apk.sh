#!/usr/bin/env bash
# Verify a copy of ZKMNEXT GameSpace 1.2.9 against the recorded values.
# Usage: bash verify_apk.sh <path-to-apk>
set -u
APK="${1:-}"
EXPECTED_SHA="73861d7a58f8dc0e014f57fa380641027a98b895b3fad737e394fa03949c30e1"
EXPECTED_SIZE="18881081"
EXPECTED_CERT_SHA256="B601FB0F67D7D67A190E51E34893CCEE0B0C491F76C281CF6F21E1095DAD9EBA"
fail=0

if [ -z "$APK" ] || [ ! -f "$APK" ]; then
  echo "usage: bash verify_apk.sh <path-to-apk>"; exit 2
fi

size=$(wc -c < "$APK" | tr -d ' ')
sha=$(sha256sum "$APK" | cut -d' ' -f1)

echo "file:   $APK"
echo "size:   $size (expected $EXPECTED_SIZE)"
[ "$size" = "$EXPECTED_SIZE" ] || { echo "  MISMATCH"; fail=1; }
echo "sha256: $sha"
if [ "$sha" = "$EXPECTED_SHA" ]; then echo "  OK"; else echo "  MISMATCH — this is not the verified release"; fail=1; fi

echo -n "archive: "
if unzip -t "$APK" >/dev/null 2>&1; then echo "OK"; else echo "CORRUPT"; fail=1; fi

python3 - "$APK" "$EXPECTED_CERT_SHA256" <<'PY' || fail=1
import sys
path, expected_cert = sys.argv[1], sys.argv[2].upper()
try:
    from loguru import logger as _lg
    _lg.remove()
except Exception:
    pass
try:
    from androguard.core.apk import APK
except ImportError:
    print("androguard not installed — skipping manifest and certificate checks")
    sys.exit(0)
import logging; logging.disable(logging.CRITICAL)
a = APK(path)
checks = [
    ("package", a.get_package(), "com.zkmnext.gamespace"),
    ("versionName", a.get_androidversion_name(), "1.2.9"),
    ("versionCode", str(a.get_androidversion_code()), "14"),
    ("minSdk", str(a.get_min_sdk_version()), "29"),
    ("targetSdk", str(a.get_target_sdk_version()), "35"),
]
bad = False
for name, got, want in checks:
    ok = str(got) == want
    print(f"{name}: {got} (expected {want}) {'OK' if ok else 'MISMATCH'}")
    bad = bad or not ok
certs = a.get_certificates()
if certs:
    got = (certs[0].sha256_fingerprint or "").replace(" ", "").upper()
    ok = got == expected_cert
    print(f"cert sha256: {got} {'OK' if ok else 'MISMATCH'}")
    bad = bad or not ok
else:
    print("cert: none found"); bad = True
sys.exit(1 if bad else 0)
PY

if [ "$fail" -eq 0 ]; then
  echo "RESULT: verified — this file is the ZKMNEXT GameSpace 1.2.9 release asset"
else
  echo "RESULT: FAILED verification"
fi
exit "$fail"
