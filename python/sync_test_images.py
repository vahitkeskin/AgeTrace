#!/usr/bin/env python3
import os
import glob
import shutil

ARTIFACT_DIR = "/Users/vahitkeskin/.gemini/antigravity/brain/fbd40b66-61a9-4c7c-838f-f6f5d0ea3167"
DEST_DIR = "test_assets/evaluation_suite"
os.makedirs(DEST_DIR, exist_ok=True)

patterns = [
    "test_*.jpg",
    "person_*.jpg"
]

copied = 0
for pat in patterns:
    for src in glob.glob(os.path.join(ARTIFACT_DIR, pat)):
        fname = os.path.basename(src)
        # e.g. test_01_baby_1yo_1793261602268.jpg -> test_01_baby_1yo.jpg
        parts = fname.split("_")
        if len(parts) >= 3 and parts[-1].endswith(".jpg") and parts[-1][:-4].isdigit():
            clean_name = "_".join(parts[:-1]) + ".jpg"
        else:
            clean_name = fname
        dst = os.path.join(DEST_DIR, clean_name)
        if not os.path.exists(dst) or os.path.getsize(dst) != os.path.getsize(src):
            shutil.copy2(src, dst)
            copied += 1
            print(f"Copied: {clean_name}")

print(f"Total images in {DEST_DIR}: {len(os.listdir(DEST_DIR))}")
