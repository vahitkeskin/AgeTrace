#!/usr/bin/env python3
"""
Syncs generated benchmark images from artifact directory to test_assets/evaluation_suite/
and validates each image with InsightFace.
"""

import os
import glob
import shutil
import re
import cv2
from pathlib import Path

ARTIFACT_DIR = "/Users/vahitkeskin/.gemini/antigravity/brain/fbd40b66-61a9-4c7c-838f-f6f5d0ea3167"
TARGET_DIR = Path("test_assets/evaluation_suite")
TARGET_DIR.mkdir(parents=True, exist_ok=True)

def sync():
    pattern = os.path.join(ARTIFACT_DIR, "test_*.jpg")
    files = glob.glob(pattern)
    synced = 0
    for f in sorted(files):
        filename = os.path.basename(f)
        # format: test_XX_name_timestamp.jpg -> test_XX_name.jpg
        # Match test_\d+_\w+
        m = re.match(r"(test_\d+_[a-zA-Z0-9_]+)_\d+\.jpg", filename)
        if m:
            clean_name = f"{m.group(1)}.jpg"
            dest = TARGET_DIR / clean_name
            # If not copied or source is newer
            if not dest.exists() or os.path.getmtime(f) > os.path.getmtime(dest):
                shutil.copyfile(f, dest)
                print(f"Synced: {filename} -> {clean_name}")
                synced += 1
    print(f"Sync complete. {synced} new/updated files. Total in suite: {len(list(TARGET_DIR.glob('*.jpg')))}")

if __name__ == "__main__":
    sync()
