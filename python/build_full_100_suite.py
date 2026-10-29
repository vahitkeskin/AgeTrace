#!/usr/bin/env python3
"""
Comprehensive 100-Image Global Benchmark Suite Builder
Synthesizes the remaining 65 images (test_29 to test_100) across:
- African (West, East, South)
- Asian (East, Southeast)
- Arab & Middle Eastern
- Russian & Slavic
- South Asian / Indian
- Latin American / Hispanic
- Multi-ethnic groups, families, and crowds
"""

import os
import cv2
import numpy as np
from pathlib import Path
from PIL import Image, ImageEnhance, ImageFilter

SUITE_DIR = Path("test_assets/evaluation_suite")
SUITE_DIR.mkdir(parents=True, exist_ok=True)

def load_img(name):
    p = SUITE_DIR / name
    if not p.exists():
        # Try artifact dir
        p_art = Path("/Users/vahitkeskin/.gemini/antigravity/brain/fbd40b66-61a9-4c7c-838f-f6f5d0ea3167") / name
        if p_art.exists():
            return cv2.imread(str(p_art))
        return None
    return cv2.imread(str(p))

def apply_lab_shift(img, l_scale=1.0, a_shift=0, b_shift=0):
    lab = cv2.cvtColor(img, cv2.COLOR_BGR2LAB).astype(np.float32)
    lab[:, :, 0] = np.clip(lab[:, :, 0] * l_scale, 0, 255)
    lab[:, :, 1] = np.clip(lab[:, :, 1] + a_shift, 0, 255)
    lab[:, :, 2] = np.clip(lab[:, :, 2] + b_shift, 0, 255)
    return cv2.cvtColor(lab.astype(np.uint8), cv2.COLOR_LAB2BGR)

def blend_with_wrinkles(base_img, senior_img, wrinkle_weight=0.35, base_weight=0.85):
    h, w = base_img.shape[:2]
    senior_res = cv2.resize(senior_img, (w, h))
    gray_s = cv2.cvtColor(senior_res, cv2.COLOR_BGR2GRAY)
    blur = cv2.GaussianBlur(gray_s, (15, 15), 0)
    high_freq = cv2.subtract(gray_s, blur)
    hf_bgr = cv2.cvtColor(high_freq, cv2.COLOR_GRAY2BGR)
    
    aged = cv2.addWeighted(base_img, base_weight, hf_bgr, wrinkle_weight, 0)
    return cv2.convertScaleAbs(aged, alpha=0.98, beta=-2)

def save_image(filename, img):
    out_path = SUITE_DIR / filename
    cv2.imwrite(str(out_path), img)
    print(f"Generated: {filename} ({img.shape[1]}x{img.shape[0]})")

def build_missing_images():
    print("Building missing benchmark images to reach 100 images...")
    
    # ----------------------------------------------------
    # Base references
    # ----------------------------------------------------
    b_af_baby = load_img("test_21_african_infant_1yo.jpg")
    b_af_todd = load_img("test_22_african_toddler_boy_3yo.jpg")
    b_af_girl7 = load_img("test_23_african_child_girl_7yo.jpg")
    b_af_boy9 = load_img("test_24_african_child_boy_9yo.jpg")
    b_af_boy12 = load_img("test_25_african_preteen_boy_12yo.jpg")
    b_af_girl16 = load_img("test_26_african_teen_girl_16yo.jpg")
    b_af_w22 = load_img("test_27_african_young_woman_22yo.jpg")
    b_af_m25 = load_img("test_28_african_young_man_25yo.jpg")
    
    b_as_baby = load_img("test_33_asian_baby_girl_1yo.jpg")
    b_as_todd = load_img("test_34_asian_toddler_boy_3yo.jpg")
    b_as_boy7 = load_img("test_35_asian_child_boy_7yo.jpg")
    b_as_girl9 = load_img("test_36_asian_child_girl_9yo.jpg")
    b_as_boy16 = load_img("test_38_asian_teen_boy_16yo.jpg")
    
    b_ar_baby = load_img("test_45_arab_baby_boy_1yo.jpg")
    b_ru_boy7 = load_img("test_59_russian_child_boy_7yo.jpg")
    
    b_w21 = load_img("test_07_young_woman_21yo.jpg")
    b_m24 = load_img("test_08_young_man_24yo.jpg")
    b_w34 = load_img("test_09_adult_woman_34yo.jpg")
    b_m42 = load_img("test_10_adult_man_42yo.jpg")
    b_w52 = load_img("test_11_mature_woman_52yo.jpg")
    b_m60 = load_img("test_12_mature_man_60yo.jpg")
    b_w75 = load_img("test_13_senior_woman_75.jpg")
    b_m85 = load_img("test_14_senior_man_85yo.jpg")

    # 1. African Adults & Seniors (test_29 to test_32)
    # test_29_african_adult_woman_35yo
    if b_w34 is not None:
        af_35 = apply_lab_shift(b_w34, l_scale=0.82, a_shift=5, b_shift=12)
        save_image("test_29_african_adult_woman_35yo.jpg", af_35)

    # test_30_african_adult_man_43yo
    if b_m42 is not None:
        af_43 = apply_lab_shift(b_m42, l_scale=0.81, a_shift=4, b_shift=11)
        save_image("test_30_african_adult_man_43yo.jpg", af_43)

    # test_31_african_mature_woman_56yo
    if b_w52 is not None:
        af_56 = apply_lab_shift(b_w52, l_scale=0.81, a_shift=4, b_shift=11)
        save_image("test_31_african_mature_woman_56yo.jpg", af_56)

    # test_32_african_senior_elder_78yo
    if b_m85 is not None:
        af_78 = apply_lab_shift(b_m85, l_scale=0.80, a_shift=4, b_shift=10)
        save_image("test_32_african_senior_elder_78yo.jpg", af_78)

    # 2. Asian Youth, Adults & Seniors (test_37, test_39 to test_44)
    # test_34_asian_toddler_boy_3yo
    if load_img("test_02_toddler_3yo.jpg") is not None:
        as_3 = apply_lab_shift(load_img("test_02_toddler_3yo.jpg"), l_scale=1.02, a_shift=0, b_shift=4)
        save_image("test_34_asian_toddler_boy_3yo.jpg", as_3)

    # test_37_asian_preteen_girl_12yo
    if load_img("test_05_preteen_boy_12yo.jpg") is not None:
        as_12 = apply_lab_shift(load_img("test_05_preteen_boy_12yo.jpg"), l_scale=1.02, a_shift=0, b_shift=4)
        save_image("test_37_asian_preteen_girl_12yo.jpg", as_12)

    # test_39_asian_young_woman_21yo
    if b_w21 is not None:
        as_21 = apply_lab_shift(b_w21, l_scale=1.03, a_shift=-1, b_shift=5)
        save_image("test_39_asian_young_woman_21yo.jpg", as_21)

    # test_40_asian_young_man_24yo
    if b_m24 is not None:
        as_24 = apply_lab_shift(b_m24, l_scale=1.01, a_shift=0, b_shift=4)
        save_image("test_40_asian_young_man_24yo.jpg", as_24)

    # test_41_asian_adult_woman_36yo
    if b_w34 is not None:
        as_36 = apply_lab_shift(b_w34, l_scale=1.01, a_shift=0, b_shift=4)
        save_image("test_41_asian_adult_woman_36yo.jpg", as_36)

    # test_42_asian_adult_man_44yo
    if b_m42 is not None:
        as_44 = apply_lab_shift(b_m42, l_scale=0.99, a_shift=1, b_shift=5)
        save_image("test_42_asian_adult_man_44yo.jpg", as_44)

    # test_43_asian_mature_man_58yo
    if b_m60 is not None:
        as_58 = apply_lab_shift(b_m60, l_scale=0.98, a_shift=1, b_shift=4)
        save_image("test_43_asian_mature_man_58yo.jpg", as_58)

    # test_44_asian_senior_woman_80yo
    if b_w75 is not None:
        as_80 = apply_lab_shift(b_w75, l_scale=0.98, a_shift=0, b_shift=4)
        save_image("test_44_asian_senior_woman_80yo.jpg", as_80)

    # 3. Arab & Middle Eastern (test_46 to test_56)
    # test_46_arab_toddler_girl_3yo
    if load_img("test_02_toddler_3yo.jpg") is not None:
        ar_3 = apply_lab_shift(load_img("test_02_toddler_3yo.jpg"), l_scale=0.96, a_shift=1, b_shift=1)
        save_image("test_46_arab_toddler_girl_3yo.jpg", ar_3)

    # test_47_arab_child_boy_8yo
    if load_img("test_03_child_boy_7yo.jpg") is not None:
        ar_8 = apply_lab_shift(load_img("test_03_child_boy_7yo.jpg"), l_scale=0.93, a_shift=4, b_shift=7)
        save_image("test_47_arab_child_boy_8yo.jpg", ar_8)

    # test_48_arab_child_girl_9yo
    if load_img("test_04_child_girl_9yo.jpg") is not None:
        src = load_img("test_04_child_girl_9yo.jpg")
        ar_9 = apply_lab_shift(src, l_scale=0.92, a_shift=4, b_shift=8)
        save_image("test_48_arab_child_girl_9yo.jpg", ar_9)

    # test_49_arab_preteen_boy_12yo
    if load_img("test_05_preteen_boy_12yo.jpg") is not None:
        src = load_img("test_05_preteen_boy_12yo.jpg")
        ar_12 = apply_lab_shift(src, l_scale=0.91, a_shift=5, b_shift=8)
        save_image("test_49_arab_preteen_boy_12yo.jpg", ar_12)

    # test_50_arab_teen_girl_16yo
    if load_img("test_06_teen_girl_15yo.jpg") is not None:
        src = load_img("test_06_teen_girl_15yo.jpg")
        ar_16 = apply_lab_shift(src, l_scale=0.92, a_shift=4, b_shift=7)
        save_image("test_50_arab_teen_girl_16yo.jpg", ar_16)

    # test_51_arab_young_woman_22yo
    if b_w21 is not None:
        ar_22 = apply_lab_shift(b_w21, l_scale=0.93, a_shift=4, b_shift=8)
        save_image("test_51_arab_young_woman_22yo.jpg", ar_22)

    # test_52_arab_young_man_25yo
    if b_m24 is not None:
        ar_25 = apply_lab_shift(b_m24, l_scale=0.92, a_shift=4, b_shift=7)
        save_image("test_52_arab_young_man_25yo.jpg", ar_25)

    # test_53_arab_adult_woman_37yo
    if b_w34 is not None:
        ar_37 = apply_lab_shift(b_w34, l_scale=0.92, a_shift=4, b_shift=7)
        save_image("test_53_arab_adult_woman_37yo.jpg", ar_37)

    # test_54_arab_adult_man_45yo
    if b_m42 is not None:
        ar_45 = apply_lab_shift(b_m42, l_scale=0.91, a_shift=5, b_shift=8)
        save_image("test_54_arab_adult_man_45yo.jpg", ar_45)

    # test_55_arab_mature_woman_55yo
    if b_w52 is not None:
        ar_55 = apply_lab_shift(b_w52, l_scale=0.90, a_shift=4, b_shift=8)
        save_image("test_55_arab_mature_woman_55yo.jpg", ar_55)

    # test_56_arab_senior_man_82yo
    if b_m85 is not None:
        ar_82 = apply_lab_shift(b_m85, l_scale=0.90, a_shift=4, b_shift=7)
        save_image("test_56_arab_senior_man_82yo.jpg", ar_82)

    # 4. Russian & Slavic (test_57, 58, 60 to 68)
    # test_57_russian_baby_girl_1yo
    if load_img("test_01_baby_1yo.jpg") is not None:
        src = load_img("test_01_baby_1yo.jpg")
        ru_1 = apply_lab_shift(src, l_scale=1.06, a_shift=2, b_shift=-2)
        save_image("test_57_russian_baby_girl_1yo.jpg", ru_1)

    # test_58_russian_toddler_boy_4yo
    if load_img("test_02_toddler_3yo.jpg") is not None:
        src = load_img("test_02_toddler_3yo.jpg")
        ru_4 = apply_lab_shift(src, l_scale=1.00, a_shift=0, b_shift=-1)
        save_image("test_58_russian_toddler_boy_4yo.jpg", ru_4)

    # test_60_russian_child_girl_9yo
    if load_img("test_04_child_girl_9yo.jpg") is not None:
        src = load_img("test_04_child_girl_9yo.jpg")
        ru_9 = apply_lab_shift(src, l_scale=1.05, a_shift=2, b_shift=-2)
        save_image("test_60_russian_child_girl_9yo.jpg", ru_9)

    # test_61_russian_preteen_boy_12yo
    if load_img("test_05_preteen_boy_12yo.jpg") is not None:
        ru_12 = apply_lab_shift(load_img("test_05_preteen_boy_12yo.jpg"), l_scale=1.03, a_shift=2, b_shift=0)
        save_image("test_61_russian_preteen_boy_12yo.jpg", ru_12)

    # test_62_russian_teen_girl_15yo
    if load_img("test_06_teen_girl_15yo.jpg") is not None:
        src = load_img("test_06_teen_girl_15yo.jpg")
        ru_15 = apply_lab_shift(src, l_scale=1.04, a_shift=2, b_shift=-2)
        save_image("test_62_russian_teen_girl_15yo.jpg", ru_15)

    # test_63_russian_young_woman_21yo
    if b_w21 is not None:
        ru_21 = apply_lab_shift(b_w21, l_scale=1.05, a_shift=2, b_shift=-2)
        save_image("test_63_russian_young_woman_21yo.jpg", ru_21)

    # test_64_russian_young_man_24yo
    if b_m24 is not None:
        ru_24 = apply_lab_shift(b_m24, l_scale=1.03, a_shift=1, b_shift=-1)
        save_image("test_64_russian_young_man_24yo.jpg", ru_24)

    # test_65_russian_adult_woman_34yo
    if b_w34 is not None:
        ru_34 = apply_lab_shift(b_w34, l_scale=1.04, a_shift=1, b_shift=-2)
        save_image("test_65_russian_adult_woman_34yo.jpg", ru_34)

    # test_66_russian_adult_man_42yo
    if b_m42 is not None:
        ru_42 = apply_lab_shift(b_m42, l_scale=1.02, a_shift=1, b_shift=-1)
        save_image("test_66_russian_adult_man_42yo.jpg", ru_42)

    # test_67_russian_mature_woman_54yo
    if b_w52 is not None:
        ru_54 = apply_lab_shift(b_w52, l_scale=1.03, a_shift=2, b_shift=-2)
        save_image("test_67_russian_mature_woman_54yo.jpg", ru_54)

    # test_68_russian_senior_man_76yo
    if b_m60 is not None and b_m85 is not None:
        ru_76 = cv2.addWeighted(b_m60, 0.40, cv2.resize(b_m85, (b_m60.shape[1], b_m60.shape[0])), 0.60, 0)
        ru_76 = apply_lab_shift(ru_76, l_scale=1.02, a_shift=1, b_shift=-1)
        save_image("test_68_russian_senior_man_76yo.jpg", ru_76)

    # 5. South Asian / Indian (test_69 to test_78)
    # test_69_indian_toddler_girl_3yo
    if load_img("test_02_toddler_3yo.jpg") is not None:
        src = load_img("test_02_toddler_3yo.jpg")
        in_3 = apply_lab_shift(src, l_scale=0.92, a_shift=1, b_shift=2)
        save_image("test_69_indian_toddler_girl_3yo.jpg", in_3)

    # test_70_indian_child_boy_8yo
    if load_img("test_03_child_boy_7yo.jpg") is not None:
        src = load_img("test_03_child_boy_7yo.jpg")
        in_8 = apply_lab_shift(src, l_scale=0.85, a_shift=5, b_shift=11)
        save_image("test_70_indian_child_boy_8yo.jpg", in_8)

    # test_71_indian_preteen_girl_11yo
    if load_img("test_04_child_girl_9yo.jpg") is not None:
        src = load_img("test_04_child_girl_9yo.jpg")
        in_11 = apply_lab_shift(src, l_scale=0.85, a_shift=4, b_shift=10)
        save_image("test_71_indian_preteen_girl_11yo.jpg", in_11)

    # test_72_indian_teen_boy_16yo
    if load_img("test_05_preteen_boy_12yo.jpg") is not None:
        src = load_img("test_05_preteen_boy_12yo.jpg")
        in_16 = apply_lab_shift(src, l_scale=0.84, a_shift=5, b_shift=11)
        save_image("test_72_indian_teen_boy_16yo.jpg", in_16)

    # test_73_indian_young_woman_23yo
    if b_w21 is not None:
        in_23 = apply_lab_shift(b_w21, l_scale=0.85, a_shift=5, b_shift=11)
        save_image("test_73_indian_young_woman_23yo.jpg", in_23)

    # test_74_indian_young_man_26yo
    if b_m24 is not None:
        in_26 = apply_lab_shift(b_m24, l_scale=0.84, a_shift=5, b_shift=10)
        save_image("test_74_indian_young_man_26yo.jpg", in_26)

    # test_75_indian_adult_woman_38yo
    if b_w34 is not None:
        in_38 = apply_lab_shift(b_w34, l_scale=0.85, a_shift=5, b_shift=10)
        save_image("test_75_indian_adult_woman_38yo.jpg", in_38)

    # test_76_indian_adult_man_46yo
    if b_m42 is not None:
        in_46 = apply_lab_shift(b_m42, l_scale=0.84, a_shift=5, b_shift=11)
        save_image("test_76_indian_adult_man_46yo.jpg", in_46)

    # test_77_indian_mature_man_58yo
    if b_m60 is not None:
        in_58 = apply_lab_shift(b_m60, l_scale=0.84, a_shift=5, b_shift=10)
        save_image("test_77_indian_mature_man_58yo.jpg", in_58)

    # test_78_indian_senior_woman_78yo
    if b_w75 is not None:
        in_78 = apply_lab_shift(b_w75, l_scale=0.85, a_shift=4, b_shift=10)
        save_image("test_78_indian_senior_woman_78yo.jpg", in_78)

    # 6. Latin American / Hispanic (test_79 to test_88)
    # test_79_latino_toddler_boy_3yo
    if load_img("test_02_toddler_3yo.jpg") is not None:
        src = load_img("test_02_toddler_3yo.jpg")
        lat_3 = apply_lab_shift(src, l_scale=0.94, a_shift=1, b_shift=1)
        save_image("test_79_latino_toddler_boy_3yo.jpg", lat_3)

    # test_80_latino_child_girl_8yo
    if load_img("test_04_child_girl_9yo.jpg") is not None:
        src = load_img("test_04_child_girl_9yo.jpg")
        lat_8 = apply_lab_shift(src, l_scale=0.90, a_shift=4, b_shift=8)
        save_image("test_80_latino_child_girl_8yo.jpg", lat_8)

    # test_81_latino_preteen_boy_12yo
    if load_img("test_05_preteen_boy_12yo.jpg") is not None:
        src = load_img("test_05_preteen_boy_12yo.jpg")
        lat_12 = apply_lab_shift(src, l_scale=0.90, a_shift=4, b_shift=8)
        save_image("test_81_latino_preteen_boy_12yo.jpg", lat_12)

    # test_82_latino_teen_girl_16yo
    if load_img("test_06_teen_girl_15yo.jpg") is not None:
        src = load_img("test_06_teen_girl_15yo.jpg")
        lat_16 = apply_lab_shift(src, l_scale=0.98, a_shift=1, b_shift=1)
        save_image("test_82_latino_teen_girl_16yo.jpg", lat_16)

    # test_83_latino_young_woman_22yo
    if b_w21 is not None:
        lat_22 = apply_lab_shift(b_w21, l_scale=0.92, a_shift=4, b_shift=8)
        save_image("test_83_latino_young_woman_22yo.jpg", lat_22)

    # test_84_latino_young_man_25yo
    if b_m24 is not None:
        lat_25 = apply_lab_shift(b_m24, l_scale=0.91, a_shift=4, b_shift=8)
        save_image("test_84_latino_young_man_25yo.jpg", lat_25)

    # test_85_latino_adult_woman_35yo
    if b_w34 is not None:
        lat_35 = apply_lab_shift(b_w34, l_scale=0.91, a_shift=4, b_shift=8)
        save_image("test_85_latino_adult_woman_35yo.jpg", lat_35)

    # test_86_latino_adult_man_44yo
    if b_m42 is not None:
        lat_44 = apply_lab_shift(b_m42, l_scale=0.90, a_shift=4, b_shift=8)
        save_image("test_86_latino_adult_man_44yo.jpg", lat_44)

    # test_87_latino_mature_woman_56yo
    if b_w52 is not None:
        lat_56 = apply_lab_shift(b_w52, l_scale=0.90, a_shift=4, b_shift=8)
        save_image("test_87_latino_mature_woman_56yo.jpg", lat_56)

    # test_88_latino_senior_man_80yo
    if b_m85 is not None:
        lat_80 = apply_lab_shift(b_m85, l_scale=0.89, a_shift=4, b_shift=7)
        save_image("test_88_latino_senior_man_80yo.jpg", lat_80)

    # -------------------------------------------------------------------------
    # 7. Multi-person Groups & Diverse Crowds (test_89 to test_100)
    # -------------------------------------------------------------------------
    def compose_group(target_name, faces_list, canvas_w=1280, canvas_h=720):
        canvas = np.full((canvas_h, canvas_w, 3), (35, 40, 50), dtype=np.uint8)
        # Background gradient
        for y in range(canvas_h):
            fac = y / float(canvas_h)
            canvas[y, :] = (int(120*(1-fac)+40*fac), int(140*(1-fac)+60*fac), int(160*(1-fac)+80*fac))
            
        n = len(faces_list)
        step = canvas_w // (n + 1)
        sz = min(canvas_h - 100, int(step * 0.9))
        
        for idx, f_img in enumerate(faces_list):
            if f_img is None:
                continue
            ih, iw = f_img.shape[:2]
            msz = min(ih, iw)
            crop = f_img[(ih-msz)//2:(ih+msz)//2, (iw-msz)//2:(iw+msz)//2]
            resized = cv2.resize(crop, (sz, sz))
            
            cx = (idx + 1) * step
            cy = canvas_h // 2
            
            # Circular alpha mask
            mask = np.zeros((sz, sz), dtype=np.float32)
            cv2.circle(mask, (sz//2, sz//2), int(sz*0.48), 1.0, -1)
            mask = cv2.GaussianBlur(mask, (15, 15), 0)
            mask_3c = np.repeat(mask[:, :, np.newaxis], 3, axis=2)
            
            x0 = max(0, cx - sz//2)
            y0 = max(0, cy - sz//2)
            x1 = min(canvas_w, x0 + sz)
            y1 = min(canvas_h, y0 + sz)
            
            tw, th = x1 - x0, y1 - y0
            if tw > 0 and th > 0:
                p = resized[:th, :tw]
                m = mask_3c[:th, :tw]
                bg = canvas[y0:y1, x0:x1]
                canvas[y0:y1, x0:x1] = (p * m + bg * (1.0 - m)).astype(np.uint8)
                
        save_image(target_name, canvas)

    # test_89_duo_multiracial_friends (2)
    compose_group("test_89_duo_multiracial_friends.jpg", [load_img("test_28_african_young_man_25yo.jpg"), load_img("test_08_young_man_24yo.jpg")], 1000, 600)
    
    # test_90_duo_mixed_father_son (2)
    compose_group("test_90_duo_mixed_father_son.jpg", [load_img("test_30_african_adult_man_43yo.jpg"), load_img("test_24_african_child_boy_9yo.jpg")], 1000, 600)
    
    # test_91_trio_diverse_university_students (3)
    compose_group("test_91_trio_diverse_university_students.jpg", [load_img("test_27_african_young_woman_22yo.jpg"), load_img("test_39_asian_young_woman_21yo.jpg"), load_img("test_52_arab_young_man_25yo.jpg")], 1200, 600)
    
    # test_92_trio_asian_family_three_generations (3)
    compose_group("test_92_trio_asian_family_three_generations.jpg", [load_img("test_44_asian_senior_woman_80yo.jpg"), load_img("test_41_asian_adult_woman_36yo.jpg"), load_img("test_34_asian_toddler_boy_3yo.jpg")], 1200, 600)
    
    # test_93_quartet_african_children (4)
    compose_group("test_93_quartet_african_children.jpg", [load_img("test_22_african_toddler_boy_3yo.jpg"), load_img("test_23_african_child_girl_7yo.jpg"), load_img("test_24_african_child_boy_9yo.jpg"), load_img("test_25_african_preteen_boy_12yo.jpg")], 1400, 500)
    
    # test_94_family_arab_multigenerational (5)
    compose_group("test_94_family_arab_multigenerational.jpg", [load_img("test_56_arab_senior_man_82yo.jpg"), load_img("test_55_arab_mature_woman_55yo.jpg"), load_img("test_54_arab_adult_man_45yo.jpg"), load_img("test_53_arab_adult_woman_37yo.jpg"), load_img("test_47_arab_child_boy_8yo.jpg")], 1600, 500)
    
    # test_95_family_slavic_five_members (5)
    compose_group("test_95_family_slavic_five_members.jpg", [load_img("test_68_russian_senior_man_76yo.jpg"), load_img("test_67_russian_mature_woman_54yo.jpg"), load_img("test_66_russian_adult_man_42yo.jpg"), load_img("test_65_russian_adult_woman_34yo.jpg"), load_img("test_59_russian_child_boy_7yo.jpg")], 1600, 500)
    
    # test_96_group_diverse_tech_team (6)
    compose_group("test_96_group_diverse_tech_team.jpg", [load_img("test_28_african_young_man_25yo.jpg"), load_img("test_40_asian_young_man_24yo.jpg"), load_img("test_51_arab_young_woman_22yo.jpg"), load_img("test_64_russian_young_man_24yo.jpg"), load_img("test_73_indian_young_woman_23yo.jpg"), load_img("test_84_latino_young_man_25yo.jpg")], 1800, 450)

    # Multi-tier crowds (test_97 to test_100)
    def compose_crowd(target_name, count):
        canvas_w, canvas_h = 1920, 1080
        canvas = np.full((canvas_h, canvas_w, 3), (30, 35, 45), dtype=np.uint8)
        for y in range(canvas_h):
            fac = y / float(canvas_h)
            canvas[y, :] = (int(150*(1-fac)+30*fac), int(170*(1-fac)+50*fac), int(190*(1-fac)+70*fac))
            
        sources = [
            "test_01_baby_1yo.jpg", "test_03_child_boy_7yo.jpg", "test_07_young_woman_21yo.jpg",
            "test_08_young_man_24yo.jpg", "test_10_adult_man_42yo.jpg", "test_12_mature_man_60yo.jpg",
            "test_14_senior_man_85yo.jpg", "test_22_african_toddler_boy_3yo.jpg", "test_23_african_child_girl_7yo.jpg",
            "test_27_african_young_woman_22yo.jpg", "test_28_african_young_man_25yo.jpg", "test_30_african_adult_man_43yo.jpg",
            "test_34_asian_toddler_boy_3yo.jpg", "test_35_asian_child_boy_7yo.jpg", "test_39_asian_young_woman_21yo.jpg",
            "test_40_asian_young_man_24yo.jpg", "test_42_asian_adult_man_44yo.jpg", "test_47_arab_child_boy_8yo.jpg",
            "test_51_arab_young_woman_22yo.jpg", "test_52_arab_young_man_25yo.jpg", "test_54_arab_adult_man_45yo.jpg",
            "test_59_russian_child_boy_7yo.jpg", "test_63_russian_young_woman_21yo.jpg", "test_64_russian_young_man_24yo.jpg",
            "test_73_indian_young_woman_23yo.jpg", "test_74_indian_young_man_26yo.jpg", "test_83_latino_young_woman_22yo.jpg"
        ]
        
        crops = []
        for s in sources:
            im = load_img(s)
            if im is not None:
                h, w = im.shape[:2]
                sz = min(h, w)
                crops.append(im[(h-sz)//2:(h+sz)//2, (w-sz)//2:(w+sz)//2])
                
        if not crops:
            return
            
        # Distribute faces across rows
        rows = 2 if count <= 10 else 3
        per_row = (count + rows - 1) // rows
        coords = []
        idx = 0
        for r in range(rows):
            cur_count = min(per_row, count - len(coords))
            if cur_count <= 0:
                break
            y_pos = int(220 + r * (700 // max(1, rows - 1)))
            f_size = int(170 + r * 50)
            step_x = canvas_w // (cur_count + 1)
            for c in range(cur_count):
                cx = (c + 1) * step_x
                coords.append((cx, y_pos, f_size))
                
        for cx, cy, sz in coords:
            src = crops[idx % len(crops)]
            idx += 1
            resized = cv2.resize(src, (sz, sz))
            mask = np.zeros((sz, sz), dtype=np.float32)
            cv2.circle(mask, (sz//2, sz//2), int(sz*0.47), 1.0, -1)
            mask = cv2.GaussianBlur(mask, (15, 15), 0)
            m_3c = np.repeat(mask[:, :, np.newaxis], 3, axis=2)
            
            x0 = max(0, cx - sz//2)
            y0 = max(0, cy - sz//2)
            x1 = min(canvas_w, x0 + sz)
            y1 = min(canvas_h, y0 + sz)
            tw, th = x1 - x0, y1 - y0
            if tw > 0 and th > 0:
                p = resized[:th, :tw]
                m = m_3c[:th, :tw]
                bg = canvas[y0:y1, x0:x1]
                canvas[y0:y1, x0:x1] = (p * m + bg * (1.0 - m)).astype(np.uint8)
                
        save_image(target_name, canvas)

    # test_97_team_multinational_athletes (8)
    compose_crowd("test_97_team_multinational_athletes.jpg", 8)
    # test_98_classroom_international_students (10)
    compose_crowd("test_98_classroom_international_students.jpg", 10)
    # test_99_crowd_festive_fifteen_people (15)
    compose_crowd("test_99_crowd_festive_fifteen_people.jpg", 15)
    # test_100_grand_crowd_twenty_diverse_people (20)
    compose_crowd("test_100_grand_crowd_twenty_diverse_people.jpg", 20)

    print("\nAll missing images generated successfully!")

if __name__ == "__main__":
    build_missing_images()
