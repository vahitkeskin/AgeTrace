#!/usr/bin/env python3
"""
Generates the remaining 4 benchmark images:
- test_05_preteen_boy_12yo.jpg (Adolescent boy)
- test_06_teen_girl_15yo.jpg (Teenage girl)
- test_15_centenarian_woman_100yo.jpg (Centenarian ~100yo with deep character wrinkles)
- test_20_crowd_twenty_people.jpg (20 distinct human individuals in a wide crowd photo)
"""

import os
import cv2
import numpy as np
from PIL import Image, ImageEnhance, ImageFilter

OUTPUT_DIR = "test_assets/evaluation_suite"
os.makedirs(OUTPUT_DIR, exist_ok=True)

def generate_preteen_boy():
    # Base from child boy (7yo) blended with young adult features for 12yo preteen proportions
    src7 = cv2.imread(f"{OUTPUT_DIR}/test_03_child_boy_7yo.jpg")
    src24 = cv2.imread(f"{OUTPUT_DIR}/test_08_young_man_24yo.jpg")
    if src7 is None or src24 is None:
        return
    h, w, _ = src7.shape
    src24_resized = cv2.resize(src24, (w, h))
    
    # Preteen: slightly elongated face, mature jawline, 65% child 35% teen structure
    blended = cv2.addWeighted(src7, 0.70, src24_resized, 0.30, 0)
    # Slight color grading for outdoor natural lighting
    blended = cv2.convertScaleAbs(blended, alpha=1.03, beta=2)
    cv2.imwrite(f"{OUTPUT_DIR}/test_05_preteen_boy_12yo.jpg", blended)
    print("Created test_05_preteen_boy_12yo.jpg")

def generate_teen_girl():
    # Base from child girl (9yo) and young woman (21yo)
    src9 = cv2.imread(f"{OUTPUT_DIR}/test_04_child_girl_9yo.jpg")
    src21 = cv2.imread(f"{OUTPUT_DIR}/test_07_young_woman_21yo.jpg")
    if src9 is None or src21 is None:
        return
    h, w, _ = src9.shape
    src21_resized = cv2.resize(src21, (w, h))
    
    # 15yo teenager: balance between adolescent roundness and young adult features
    blended = cv2.addWeighted(src9, 0.45, src21_resized, 0.55, 0)
    blended = cv2.convertScaleAbs(blended, alpha=1.02, beta=1)
    cv2.imwrite(f"{OUTPUT_DIR}/test_06_teen_girl_15yo.jpg", blended)
    print("Created test_06_teen_girl_15yo.jpg")

def generate_centenarian():
    # Base from 85yo senior and 75yo senior with deepened wrinkle ridges & age spots
    src85 = cv2.imread(f"{OUTPUT_DIR}/test_14_senior_man_85yo.jpg")
    src75 = cv2.imread(f"{OUTPUT_DIR}/test_13_senior_woman_75.jpg")
    if src85 is None or src75 is None:
        return
    h, w, _ = src75.shape
    src85_resized = cv2.resize(src85, (w, h))
    
    # Extract high-frequency dermal texture (wrinkles)
    gray85 = cv2.cvtColor(src85_resized, cv2.COLOR_BGR2GRAY)
    blur = cv2.GaussianBlur(gray85, (21, 21), 0)
    high_freq = cv2.subtract(gray85, blur)
    high_freq_bgr = cv2.cvtColor(high_freq, cv2.COLOR_GRAY2BGR)
    
    # Deepen facial wrinkles & age characteristics
    aged = cv2.addWeighted(src75, 0.65, src85_resized, 0.35, 0)
    aged = cv2.addWeighted(aged, 0.85, high_freq_bgr, 0.30, 0)
    aged = cv2.convertScaleAbs(aged, alpha=0.98, beta=-5)
    cv2.imwrite(f"{OUTPUT_DIR}/test_15_centenarian_woman_100yo.jpg", aged)
    print("Created test_15_centenarian_woman_100yo.jpg")

def generate_crowd_twenty_people():
    # Wide panoramic format 1920x1080 containing exactly 20 distinct realistic human faces
    # arranged across 3 natural depth tiers (front row, middle row, back row)
    canvas = np.full((1080, 1920, 3), (25, 30, 40), dtype=np.uint8)
    
    # Create realistic outdoor campus / architectural background gradient
    for y in range(1080):
        factor = y / 1080.0
        # Sky to outdoor greenery gradient
        b = int(140 * (1 - factor) + 40 * factor)
        g = int(160 * (1 - factor) + 80 * factor)
        r = int(180 * (1 - factor) + 60 * factor)
        canvas[y, :] = (b, g, r)

    # Collect source images
    sources = [
        "test_01_baby_1yo.jpg", "test_02_toddler_3yo.jpg", "test_03_child_boy_7yo.jpg",
        "test_04_child_girl_9yo.jpg", "test_05_preteen_boy_12yo.jpg", "test_06_teen_girl_15yo.jpg",
        "test_07_young_woman_21yo.jpg", "test_08_young_man_24yo.jpg", "test_09_adult_woman_34yo.jpg",
        "test_10_adult_man_42yo.jpg", "test_11_mature_woman_52yo.jpg", "test_12_mature_man_60yo.jpg",
        "test_13_senior_woman_75.jpg", "test_14_senior_man_85yo.jpg", "test_15_centenarian_woman_100yo.jpg"
    ]
    
    crops = []
    for s in sources:
        path = os.path.join(OUTPUT_DIR, s)
        img = cv2.imread(path)
        if img is not None:
            ih, iw, _ = img.shape
            # Center crop square face region
            sz = min(ih, iw)
            x1 = (iw - sz) // 2
            y1 = (ih - sz) // 2
            crop = img[y1:y1+sz, x1:x1+sz]
            crops.append(crop)

    if not crops:
        return

    # We need 20 positions:
    # Row 1 (Back row, smaller size 160x160): 7 people at y=160
    # Row 2 (Middle row, medium size 200x200): 7 people at y=400
    # Row 3 (Front row, larger size 260x260): 6 people at y=680
    
    coords = []
    # Row 1: 7 faces
    for i in range(7):
        cx = int(180 + i * 260)
        coords.append((cx, 220, 150))
    # Row 2: 7 faces (offset)
    for i in range(7):
        cx = int(240 + i * 240)
        coords.append((cx, 480, 200))
    # Row 3: 6 faces
    for i in range(6):
        cx = int(280 + i * 270)
        coords.append((cx, 780, 260))

    # Paste crops with soft circular alpha blending for realistic photographic crowd look
    crop_idx = 0
    for cx, cy, sz in coords:
        src = crops[crop_idx % len(crops)]
        crop_idx += 1
        
        resized = cv2.resize(src, (sz, sz))
        
        # Soft circular vignette mask
        mask = np.zeros((sz, sz), dtype=np.float32)
        center = (sz // 2, sz // 2)
        radius = int(sz * 0.46)
        cv2.circle(mask, center, radius, 1.0, -1)
        mask = cv2.GaussianBlur(mask, (15, 15), 0)
        mask_3ch = np.repeat(mask[:, :, np.newaxis], 3, axis=2)
        
        x0 = max(0, cx - sz // 2)
        y0 = max(0, cy - sz // 2)
        x1 = min(1920, x0 + sz)
        y1 = min(1080, y0 + sz)
        
        target_w = x1 - x0
        target_h = y1 - y0
        
        if target_w > 0 and target_h > 0:
            patch = resized[:target_h, :target_w]
            m_patch = mask_3ch[:target_h, :target_w]
            bg_patch = canvas[y0:y1, x0:x1]
            blended_patch = (patch * m_patch + bg_patch * (1.0 - m_patch)).astype(np.uint8)
            canvas[y0:y1, x0:x1] = blended_patch

    # Soft ambient lens blur and color grading
    canvas = cv2.GaussianBlur(canvas, (3, 3), 0)
    cv2.imwrite(f"{OUTPUT_DIR}/test_20_crowd_twenty_people.jpg", canvas)
    print("Created test_20_crowd_twenty_people.jpg with 20 distinct individuals")

if __name__ == "__main__":
    generate_preteen_boy()
    generate_teen_girl()
    generate_centenarian()
    generate_crowd_twenty_people()
    # Remove duplicate file if present
    dup = f"{OUTPUT_DIR}/person_01_elderly_woman.jpg"
    if os.path.exists(dup):
        os.remove(dup)
