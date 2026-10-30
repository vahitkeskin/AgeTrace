#!/usr/bin/env python3
"""
Render AgeTrace 100-Image Annotated Benchmark Screenshots & Contact Sheets
Generates full UI HUD-annotated visual screenshots for all 100 test images
and compiles visual montage review boards across demographic age groups.
"""

import os
import sys
import json
import time
from pathlib import Path
import cv2
import numpy as np
from PIL import Image, ImageDraw, ImageFont

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from age_trace_ai import analyze_image_bytes

SUITE_DIR = Path("test_assets/evaluation_suite")
OUTPUT_DIR = Path("test_assets/annotated_screenshots")
OUTPUT_DIR.mkdir(parents=True, exist_ok=True)
MONTAGE_DIR = Path("test_assets/montages")
MONTAGE_DIR.mkdir(parents=True, exist_ok=True)

def draw_hud_box(draw: ImageDraw.ImageDraw, x0, y0, x1, y1, color=(56, 189, 248), corner_len=18, thickness=3):
    """Draws sleek sci-fi HUD corner brackets matching AgeTrace UI."""
    w = x1 - x0
    h = y1 - y0
    clen = min(corner_len, int(w * 0.25), int(h * 0.25))
    
    # Subtle full rectangle border
    draw.rectangle([x0, y0, x1, y1], outline=(56, 189, 248, 80), width=1)
    
    # Top-Left corner
    draw.line([(x0, y0), (x0 + clen, y0)], fill=color, width=thickness)
    draw.line([(x0, y0), (x0, y0 + clen)], fill=color, width=thickness)
    
    # Top-Right corner
    draw.line([(x1 - clen, y0), (x1, y0)], fill=color, width=thickness)
    draw.line([(x1, y0), (x1, y0 + clen)], fill=color, width=thickness)
    
    # Bottom-Left corner
    draw.line([(x0, y1), (x0 + clen, y1)], fill=color, width=thickness)
    draw.line([(x0, y1 - clen), (x0, y1)], fill=color, width=thickness)
    
    # Bottom-Right corner
    draw.line([(x1 - clen, y1), (x1, y1)], fill=color, width=thickness)
    draw.line([(x1, y1 - clen), (x1, y1)], fill=color, width=thickness)

def render_annotated_image(img_bgr: np.ndarray, faces: list, title: str) -> np.ndarray:
    """Renders AgeTrace UI HUD overlays directly on image."""
    h, w = img_bgr.shape[:2]
    img_rgb = cv2.cvtColor(img_bgr, cv2.COLOR_BGR2RGB)
    pil_img = Image.fromarray(img_rgb)
    draw = ImageDraw.Draw(pil_img, "RGBA")
    
    # Try system fonts
    try:
        font_large = ImageFont.truetype("/System/Library/Fonts/SFPro-Bold.otf", max(14, int(w * 0.024)))
        font_small = ImageFont.truetype("/System/Library/Fonts/SFPro-Medium.otf", max(11, int(w * 0.018)))
    except:
        try:
            font_large = ImageFont.truetype("/System/Library/Fonts/Helvetica.ttc", max(14, int(w * 0.024)))
            font_small = ImageFont.truetype("/System/Library/Fonts/Helvetica.ttc", max(11, int(w * 0.018)))
        except:
            font_large = ImageFont.load_default()
            font_small = ImageFont.load_default()

    for idx, face in enumerate(faces):
        bbox = face.get("bbox", {})
        fx0 = int(bbox.get("x", 0) * w)
        fy0 = int(bbox.get("y", 0) * h)
        fw = int(bbox.get("w", 0) * w)
        fh = int(bbox.get("h", 0) * h)
        fx1 = min(w - 1, fx0 + fw)
        fy1 = min(h - 1, fy0 + fh)
        
        # 1. Corner brackets
        draw_hud_box(draw, fx0, fy0, fx1, fy1, color=(56, 189, 248), corner_len=max(12, int(fw * 0.2)), thickness=3)
        
        # 2. Age Balloon above head
        age = face.get("age", 25)
        conf = int(face.get("confidence", 0.90) * 100)
        is_adult = face.get("is_adult", True)
        
        age_text = f"{age} Age"
        tag_text = f"%{conf} • {age} Yaş"
        
        balloon_w = max(70, int(len(age_text) * 11) + 24)
        balloon_h = 28
        bx0 = int((fx0 + fx1) / 2 - balloon_w / 2)
        by0 = max(10, fy0 - balloon_h - 10)
        bx1 = bx0 + balloon_w
        by1 = by0 + balloon_h
        
        # Dark HUD balloon with blue glow border
        draw.rounded_rectangle([bx0, by0, bx1, by1], radius=14, fill=(15, 23, 42, 230), outline=(56, 189, 248, 255), width=2)
        draw.text(((bx0 + bx1) / 2, (by0 + by1) / 2), age_text, fill=(255, 255, 255), font=font_large, anchor="mm")
        
        # 3. Status Tag pill below face
        cls_text = "18- Reşit Değil" if not is_adult else ("18+ Genç Yetişkin" if age < 30 else ("18+ Yetişkin" if age < 50 else ("18+ Olgun" if age < 65 else "18+ Kıdemli")))
        tag_bg = (19, 78, 74, 230) if not is_adult else (136, 19, 55, 230)
        tag_outline = (45, 212, 191, 255) if not is_adult else (244, 63, 94, 255)
        tag_fg = (94, 234, 212) if not is_adult else (253, 164, 175)
        
        tw = max(80, int(len(cls_text) * 8.5) + 20)
        th = 22
        tx0 = int((fx0 + fx1) / 2 - tw / 2)
        ty0 = min(h - th - 10, fy1 + 8)
        tx1 = tx0 + tw
        ty1 = ty0 + th
        
        draw.rounded_rectangle([tx0, ty0, tx1, ty1], radius=11, fill=tag_bg, outline=tag_outline, width=1)
        draw.text(((tx0 + tx1) / 2, (ty0 + ty1) / 2), cls_text, fill=tag_fg, font=font_small, anchor="mm")

    # Header watermark
    head_bg = (15, 23, 42, 210)
    draw.rounded_rectangle([12, 12, min(w - 12, 380), 44], radius=10, fill=head_bg, outline=(56, 189, 248, 120), width=1)
    summary_text = f"AgeTrace • {len(faces)} Kişi Tespit Edildi"
    draw.text((22, 28), summary_text, fill=(56, 189, 248), font=font_large, anchor="lm")

    return cv2.cvtColor(np.array(pil_img), cv2.COLOR_RGB2BGR)

def create_montage(image_list: list, grid_cols=4, tile_size=(360, 480)) -> np.ndarray:
    """Assembles a list of image file paths into a clean labeled grid contact sheet."""
    tw, th = tile_size
    n = len(image_list)
    grid_rows = (n + grid_cols - 1) // grid_cols
    
    canvas = np.zeros((grid_rows * th, grid_cols * tw, 3), dtype=np.uint8)
    canvas[:] = (15, 23, 42) # Slate background
    
    for idx, p in enumerate(image_list):
        r = idx // grid_cols
        c = idx % grid_cols
        y0 = r * th
        x0 = c * tw
        
        img = cv2.imread(str(p))
        if img is None: continue
        
        ih, iw = img.shape[:2]
        scale = min((tw - 10) / iw, (th - 35) / ih)
        nw, nh = int(iw * scale), int(ih * scale)
        res = cv2.resize(img, (nw, nh), interpolation=cv2.INTER_AREA)
        
        ox = x0 + (tw - nw) // 2
        oy = y0 + 5 + (th - 35 - nh) // 2
        canvas[oy:oy+nh, ox:ox+nw] = res
        
        # Border
        cv2.rectangle(canvas, (x0 + 2, y0 + 2), (x0 + tw - 2, y0 + th - 2), (30, 41, 59), 1)
        
        # Label at bottom
        fname = Path(p).stem.replace("_annotated", "")
        # Shorten label
        fname_short = fname.replace("test_", "")
        cv2.putText(canvas, fname_short[:30], (x0 + 8, y0 + th - 10), cv2.FONT_HERSHEY_SIMPLEX, 0.38, (148, 163, 184), 1, cv2.LINE_AA)
        
    return canvas

def main():
    image_files = sorted(list(SUITE_DIR.glob("*.jpg")) + list(SUITE_DIR.glob("*.png")))
    print(f"Starting screenshot rendering for {len(image_files)} test images...")
    
    processed_records = []
    annotated_paths = []
    
    for idx, p in enumerate(image_files):
        t0 = time.time()
        with open(p, "rb") as f:
            data = f.read()
        res = analyze_image_bytes(data)
        faces = res.get("faces", [])
        
        img_bgr = cv2.imread(str(p))
        annotated = render_annotated_image(img_bgr, faces, p.stem)
        
        out_path = OUTPUT_DIR / f"{p.stem}_annotated.jpg"
        cv2.imwrite(str(out_path), annotated, [int(cv2.IMWRITE_JPEG_QUALITY), 92])
        annotated_paths.append(out_path)
        
        rec = {
            "index": idx + 1,
            "filename": p.name,
            "faces_count": len(faces),
            "faces": [{"age": f.get("age"), "is_adult": f.get("is_adult"), "conf": f.get("confidence")} for f in faces]
        }
        processed_records.append(rec)
        print(f"[{idx+1}/100] Rendered: {out_path.name} ({len(faces)} faces)")

    print(f"\nAll 100 screenshots generated in {OUTPUT_DIR}")
    
    # ----------------------------------------------------
    # Assemble Demographic Review Montages (Contact Sheets)
    # ----------------------------------------------------
    # 1. Infants & Toddlers (Ages 1 - 3)
    infants = [p for p in annotated_paths if any(k in p.stem for k in ["baby", "infant", "toddler", "1yo", "3yo"])]
    if infants:
        mont_infants = create_montage(infants[:12], grid_cols=4, tile_size=(360, 480))
        cv2.imwrite(str(MONTAGE_DIR / "montage_01_infants_toddlers.jpg"), mont_infants)
        print("Montage 1 (Infants & Toddlers) created.")
        
    # 2. Children & Preteens (Ages 7 - 12)
    children = [p for p in annotated_paths if any(k in p.stem for k in ["child", "preteen", "7yo", "8yo", "9yo", "12yo"])]
    if children:
        mont_children = create_montage(children[:12], grid_cols=4, tile_size=(360, 480))
        cv2.imwrite(str(MONTAGE_DIR / "montage_02_children_preteens.jpg"), mont_children)
        print("Montage 2 (Children & Preteens) created.")

    # 3. Teenagers & Young Adults (Ages 15 - 25)
    youth = [p for p in annotated_paths if any(k in p.stem for k in ["teen", "young", "15yo", "16yo", "21yo", "22yo", "24yo", "25yo"])]
    if youth:
        mont_youth = create_montage(youth[:12], grid_cols=4, tile_size=(360, 480))
        cv2.imwrite(str(MONTAGE_DIR / "montage_03_youth_young_adults.jpg"), mont_youth)
        print("Montage 3 (Youth & Young Adults) created.")

    # 4. Adults & Mature (Ages 34 - 60)
    adults = [p for p in annotated_paths if any(k in p.stem for k in ["adult", "mature", "34yo", "35yo", "42yo", "44yo", "52yo", "54yo", "56yo", "60yo"])]
    if adults:
        mont_adults = create_montage(adults[:12], grid_cols=4, tile_size=(360, 480))
        cv2.imwrite(str(MONTAGE_DIR / "montage_04_adults_mature.jpg"), mont_adults)
        print("Montage 4 (Adults & Mature) created.")

    # 5. Seniors & Centenarians (Ages 70 - 100)
    seniors = [p for p in annotated_paths if any(k in p.stem for k in ["senior", "centenarian", "75", "78", "80", "85", "100yo"])]
    if seniors:
        mont_seniors = create_montage(seniors[:12], grid_cols=4, tile_size=(360, 480))
        cv2.imwrite(str(MONTAGE_DIR / "montage_05_seniors_centenarians.jpg"), mont_seniors)
        print("Montage 5 (Seniors & Centenarians) created.")

    # 6. Duos, Trios, Families & Crowds (Multi-person)
    groups = [p for p in annotated_paths if any(k in p.stem for k in ["duo", "trio", "quartet", "family", "group", "team", "classroom", "crowd"])]
    if groups:
        mont_groups = create_montage(groups[:12], grid_cols=4, tile_size=(360, 480))
        cv2.imwrite(str(MONTAGE_DIR / "montage_06_groups_families_crowds.jpg"), mont_groups)
        print("Montage 6 (Groups & Crowds) created.")

    # Copy montages to conversation artifact dir for inspection
    art_dir = Path("/Users/vahitkeskin/.gemini/antigravity/brain/fbd40b66-61a9-4c7c-838f-f6f5d0ea3167")
    for mf in MONTAGE_DIR.glob("*.jpg"):
        dest = art_dir / mf.name
        cv2.imwrite(str(dest), cv2.imread(str(mf)))
        print(f"Copied {mf.name} to artifact directory")

if __name__ == "__main__":
    main()

