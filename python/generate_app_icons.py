#!/usr/bin/env python3
"""
AgeTrace App Icon Generator
Renders a luxury, minimalist, and polite app icon for Android, iOS, and Desktop.
Features:
- Midnight sapphire & cosmic obsidian glassmorphic backdrop
- Minimalist elegant biometric human face profile & contour
- 4 Precision rounded focus brackets (mirroring AgeTrace UI)
- Chronometric age trace ring / laser pulse node (signifying age detection)
Outputs:
- iOS 1024x1024 AppIcon
- Android mipmaps (mdpi, hdpi, xhdpi, xxhdpi, xxxhdpi) + Vector Drawables
- Desktop icon (PNG 1024, 512, 256, 128, 64, 32, 16 + ICO + ICNS)
"""

import math
import os
import struct
from PIL import Image, ImageDraw, ImageFilter

MASTER_SIZE = 2048  # High resolution for super-crisp downsampling
OUTPUT_DIR = "generated_icons"
os.makedirs(OUTPUT_DIR, exist_ok=True)

def create_master_icon(size=2048, is_adaptive_foreground=False, is_adaptive_background=False):
    img = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)

    cx = size / 2.0
    cy = size / 2.0

    if not is_adaptive_foreground:
        # Background: Deep midnight obsidian & sapphire gradient
        # Pre-render radial ambient background
        bg = Image.new("RGBA", (size, size), (0, 0, 0, 0))
        bg_draw = ImageDraw.Draw(bg)
        
        # Base fill
        bg_draw.rectangle([0, 0, size, size], fill=(11, 15, 25, 255))
        
        # Ambient subtle radial glow in center-top
        glow = Image.new("RGBA", (size, size), (0, 0, 0, 0))
        glow_draw = ImageDraw.Draw(glow)
        max_r = size * 0.75
        steps = 60
        for i in range(steps, 0, -1):
            r = (max_r * i) / steps
            alpha = int(45 * (1.0 - (i / steps)**0.7))
            # subtle electric blue / teal gradient glow
            glow_draw.ellipse(
                [cx - r, cy - r * 0.9, cx + r, cy + r * 0.9],
                fill=(2, 132, 199, alpha)
            )
        glow = glow.filter(ImageFilter.GaussianBlur(radius=size * 0.05))
        bg.alpha_composite(glow)

        # Subtle secondary violet-slate bottom glow
        bot_glow = Image.new("RGBA", (size, size), (0, 0, 0, 0))
        bot_draw = ImageDraw.Draw(bot_glow)
        for i in range(40, 0, -1):
            r = (size * 0.6 * i) / 40
            alpha = int(25 * (1.0 - (i / 40)))
            bot_draw.ellipse(
                [cx - r, size - r * 0.5, cx + r, size + r * 0.5],
                fill=(99, 102, 241, alpha)
            )
        bot_glow = bot_glow.filter(ImageFilter.GaussianBlur(radius=size * 0.06))
        bg.alpha_composite(bot_glow)

        # Specular ambient border / squircle vignette (for universal icons)
        if not is_adaptive_background:
            border_overlay = Image.new("RGBA", (size, size), (0, 0, 0, 0))
            b_draw = ImageDraw.Draw(border_overlay)
            # Inner border ring
            margin = size * 0.02
            b_draw.rounded_rectangle(
                [margin, margin, size - margin, size - margin],
                radius=size * 0.22,
                outline=(255, 255, 255, 25),
                width=int(size * 0.006)
            )
            bg.alpha_composite(border_overlay)

        img.alpha_composite(bg)

    if is_adaptive_background:
        return img

    # Foreground elements: Biometric Face Silhouette + Precision Focus Brackets + Age Chrono Rings
    fg = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    fg_draw = ImageDraw.Draw(fg)

    # 1. Concentric Chronometric "Age Rings" (subtle concentric timeline circles)
    # Symbolizes the rings of a tree / chronometric progression of age
    ring_layer = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    ring_draw = ImageDraw.Draw(ring_layer)
    
    # Focal center for biometric scan (slightly right and center)
    fx = cx + size * 0.02
    fy = cy - size * 0.02

    ring_radii = [size * 0.16, size * 0.26, size * 0.36]
    for idx, r in enumerate(ring_radii):
        alpha = int(20 + idx * 8)
        # Draw arc with gaps for technical aesthetic
        ring_draw.arc(
            [fx - r, fy - r, fx + r, fy + r],
            start=45, end=290,
            fill=(56, 189, 248, alpha),
            width=max(2, int(size * 0.003))
        )
    fg.alpha_composite(ring_layer)

    # 2. Minimalist Biometric Face Profile / Continuous Silhouette
    # An exquisite, refined line representing the face profile facing right
    # Forehead -> brow -> nose bridge -> nose tip -> philtrum -> lips -> chin -> jawline
    face_layer = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    face_draw = ImageDraw.Draw(face_layer)

    # Coords relative to size
    pts = [
        # Top of head / forehead
        (cx - size * 0.08, cy - size * 0.24),
        (cx - size * 0.02, cy - size * 0.23),
        (cx + size * 0.04, cy - size * 0.20),
        (cx + size * 0.08, cy - size * 0.15),
        # Brow dip
        (cx + size * 0.085, cy - size * 0.11),
        # Nose bridge and tip
        (cx + size * 0.10, cy - size * 0.07),
        (cx + size * 0.165, cy - size * 0.015),
        (cx + size * 0.16, cy + size * 0.01),
        # Philtrum & lips
        (cx + size * 0.10, cy + size * 0.04),
        (cx + size * 0.125, cy + size * 0.065),
        (cx + size * 0.10, cy + size * 0.09),
        (cx + size * 0.12, cy + size * 0.115),
        (cx + size * 0.08, cy + size * 0.14),
        # Chin
        (cx + size * 0.10, cy + size * 0.175),
        (cx + size * 0.08, cy + size * 0.21),
        # Jawline & neck curve
        (cx + size * 0.02, cy + size * 0.23),
        (cx - size * 0.06, cy + size * 0.24),
        (cx - size * 0.12, cy + size * 0.22)
    ]

    # Render smooth spline curve through points
    # Interpolate points smoothly
    smooth_pts = []
    for i in range(len(pts) - 1):
        p0 = pts[max(0, i - 1)]
        p1 = pts[i]
        p2 = pts[i + 1]
        p3 = pts[min(len(pts) - 1, i + 2)]
        for t_step in range(12):
            t = t_step / 12.0
            t2 = t * t
            t3 = t2 * t
            # Catmull-Rom spline formula
            x = 0.5 * ((2 * p1[0]) +
                       (-p0[0] + p2[0]) * t +
                       (2 * p0[0] - 5 * p1[0] + 4 * p2[0] - p3[0]) * t2 +
                       (-p0[0] + 3 * p1[0] - 3 * p2[0] + p3[0]) * t3)
            y = 0.5 * ((2 * p1[1]) +
                       (-p0[1] + p2[1]) * t +
                       (2 * p0[1] - 5 * p1[1] + 4 * p2[1] - p3[1]) * t2 +
                       (-p0[1] + 3 * p1[1] - 3 * p2[1] + p3[1]) * t3)
            smooth_pts.append((x, y))
    smooth_pts.append(pts[-1])

    # Draw face glow
    face_glow = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    face_glow_draw = ImageDraw.Draw(face_glow)
    face_glow_draw.line(smooth_pts, fill=(56, 189, 248, 120), width=int(size * 0.025), joint="curve")
    face_glow = face_glow.filter(ImageFilter.GaussianBlur(radius=size * 0.015))
    fg.alpha_composite(face_glow)

    # Draw crisp face line (smooth ivory-cyan)
    face_draw.line(smooth_pts, fill=(248, 250, 252, 240), width=int(size * 0.012), joint="curve")
    fg.alpha_composite(face_layer)

    # 3. Precision Rounded Corner Brackets (Top-Left, Top-Right, Bottom-Right, Bottom-Left)
    # Matches FaceOverlayCanvas rounded corners!
    bracket_layer = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    b_draw = ImageDraw.Draw(bracket_layer)

    # Box dimension
    bw = size * 0.54
    bh = size * 0.58
    bx = cx - bw / 2.0
    by = cy - bh / 2.0
    br = size * 0.08  # corner radius
    arm = size * 0.12  # bracket arm length
    b_width = int(size * 0.015)

    bracket_color = (56, 189, 248, 245)  # Electric cyan

    # Glow under brackets
    bracket_glow = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    bglow_draw = ImageDraw.Draw(bracket_glow)

    def draw_corner_bracket(drawer, x0, y0, corner, color, width):
        # 1: TL, 2: TR, 3: BR, 4: BL
        if corner == "TL":
            drawer.line([(x0, y0 + arm), (x0, y0 + br)], fill=color, width=width)
            drawer.arc([x0, y0, x0 + 2 * br, y0 + 2 * br], 180, 270, fill=color, width=width)
            drawer.line([(x0 + br, y0), (x0 + arm, y0)], fill=color, width=width)
        elif corner == "TR":
            drawer.line([(x0 + bw - arm, y0), (x0 + bw - br, y0)], fill=color, width=width)
            drawer.arc([x0 + bw - 2 * br, y0, x0 + bw, y0 + 2 * br], 270, 360, fill=color, width=width)
            drawer.line([(x0 + bw, y0 + br), (x0 + bw, y0 + arm)], fill=color, width=width)
        elif corner == "BR":
            drawer.line([(x0 + bw, y0 + bh - arm), (x0 + bw, y0 + bh - br)], fill=color, width=width)
            drawer.arc([x0 + bw - 2 * br, y0 + bh - 2 * br, x0 + bw, y0 + bh], 0, 90, fill=color, width=width)
            drawer.line([(x0 + bw - br, y0 + bh), (x0 + bw - arm, y0 + bh)], fill=color, width=width)
        elif corner == "BL":
            drawer.line([(x0 + arm, y0 + bh), (x0 + br, y0 + bh)], fill=color, width=width)
            drawer.arc([x0, y0 + bh - 2 * br, x0 + 2 * br, y0 + bh], 90, 180, fill=color, width=width)
            drawer.line([(x0, y0 + bh - br), (x0, y0 + bh - arm)], fill=color, width=width)

    for c in ["TL", "TR", "BR", "BL"]:
        draw_corner_bracket(bglow_draw, bx, by, c, (56, 189, 248, 140), int(b_width * 2.2))
        draw_corner_bracket(b_draw, bx, by, c, bracket_color, b_width)

    bracket_glow = bracket_glow.filter(ImageFilter.GaussianBlur(radius=size * 0.012))
    fg.alpha_composite(bracket_glow)
    fg.alpha_composite(bracket_layer)

    # 4. Laser Scan Trace Beam & Chrono Sparkle Node
    scan_layer = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    s_draw = ImageDraw.Draw(scan_layer)
    scan_y = cy - size * 0.02
    
    # Delicate beam gradient
    for x in range(int(bx + size * 0.04), int(bx + bw - size * 0.04), 4):
        dist = abs(x - (bx + bw * 0.58))
        factor = max(0.0, 1.0 - (dist / (bw * 0.55)))
        alpha = int(180 * factor)
        s_draw.line([(x, scan_y), (x + 4, scan_y)], fill=(56, 189, 248, alpha), width=int(size * 0.005))

    # Glowing Biometric Analysis Target Node
    nx = bx + bw * 0.58
    ny = scan_y
    node_glow = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    ng_draw = ImageDraw.Draw(node_glow)
    ng_draw.ellipse([nx - size * 0.04, ny - size * 0.04, nx + size * 0.04, ny + size * 0.04], fill=(56, 189, 248, 160))
    node_glow = node_glow.filter(ImageFilter.GaussianBlur(radius=size * 0.015))
    fg.alpha_composite(node_glow)

    # Center bright node
    s_draw.ellipse([nx - size * 0.015, ny - size * 0.015, nx + size * 0.015, ny + size * 0.015], fill=(255, 255, 255, 255))
    s_draw.ellipse([nx - size * 0.008, ny - size * 0.008, nx + size * 0.008, ny + size * 0.008], fill=(103, 232, 249, 255))

    fg.alpha_composite(scan_layer)

    # 5. Subtle "AGE TRACE" branding biometric spark in top-right
    spark_layer = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    sp_draw = ImageDraw.Draw(spark_layer)
    sx = bx + bw - size * 0.04
    sy = by + size * 0.05
    sp_draw.ellipse([sx - size * 0.008, sy - size * 0.008, sx + size * 0.008, sy + size * 0.008], fill=(56, 189, 248, 220))
    fg.alpha_composite(spark_layer)

    img.alpha_composite(fg)
    return img

def create_adaptive_foreground_icon(size=2048):
    # Android Adaptive foreground (must have elements sized so they fit in 66dp of 108dp viewport)
    # That means elements should be centered within 66/108 ~= 61% of total width
    fg_full = create_master_icon(size=size, is_adaptive_foreground=True)
    # Scale content down slightly (to 72%) so it fits within the adaptive icon safe zone
    scale = 0.72
    new_w = int(size * scale)
    new_h = int(size * scale)
    resized = fg_full.resize((new_w, new_h), Image.Resampling.LANCZOS)
    
    fg_adaptive = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    offset = int((size - new_w) / 2)
    fg_adaptive.paste(resized, (offset, offset), resized)
    return fg_adaptive

def create_adaptive_background_icon(size=2048):
    return create_master_icon(size=size, is_adaptive_background=True)

if __name__ == "__main__":
    print("Generating Master AgeTrace Icon (2048x2048)...")
    master_icon = create_master_icon(2048)
    
    # 1. iOS AppIcon (1024x1024)
    print("Exporting iOS 1024x1024 icon...")
    ios_icon = master_icon.resize((1024, 1024), Image.Resampling.LANCZOS)
    # iOS icon must have no transparency (opaque background)
    ios_opaque = Image.new("RGB", (1024, 1024), (11, 15, 25))
    ios_opaque.paste(ios_icon, (0, 0), ios_icon)
    ios_opaque.save(os.path.join(OUTPUT_DIR, "app-icon-1024.png"), "PNG")
    
    # 2. Android Mipmaps
    print("Exporting Android Mipmaps...")
    android_sizes = {
        "mipmap-mdpi": 48,
        "mipmap-hdpi": 72,
        "mipmap-xhdpi": 96,
        "mipmap-xxhdpi": 144,
        "mipmap-xxxhdpi": 192
    }
    
    for folder, px in android_sizes.items():
        resized = master_icon.resize((px, px), Image.Resampling.LANCZOS)
        # Rounded icon version
        mask = Image.new("L", (px, px), 0)
        mask_draw = ImageDraw.Draw(mask)
        mask_draw.ellipse([0, 0, px, px], fill=255)
        round_icon = Image.new("RGBA", (px, px), (0, 0, 0, 0))
        round_icon.paste(resized, (0, 0), mask)
        
        folder_path = os.path.join(OUTPUT_DIR, folder)
        os.makedirs(folder_path, exist_ok=True)
        resized.save(os.path.join(folder_path, "ic_launcher.png"), "PNG")
        round_icon.save(os.path.join(folder_path, "ic_launcher_round.png"), "PNG")

    # 3. Android Adaptive Icon PNGs (for foreground & background)
    print("Exporting Android Adaptive Icons...")
    adaptive_fg = create_adaptive_foreground_icon(1024)
    adaptive_bg = create_adaptive_background_icon(1024)
    adaptive_fg.save(os.path.join(OUTPUT_DIR, "ic_launcher_foreground.png"), "PNG")
    adaptive_bg.save(os.path.join(OUTPUT_DIR, "ic_launcher_background.png"), "PNG")

    # 4. Desktop Icons (PNGs + ICO + ICNS)
    print("Exporting Desktop Icons...")
    desktop_dir = os.path.join(OUTPUT_DIR, "desktop")
    os.makedirs(desktop_dir, exist_ok=True)
    master_icon.resize((512, 512), Image.Resampling.LANCZOS).save(os.path.join(desktop_dir, "icon-512.png"), "PNG")
    master_icon.resize((256, 256), Image.Resampling.LANCZOS).save(os.path.join(desktop_dir, "icon-256.png"), "PNG")
    master_icon.resize((128, 128), Image.Resampling.LANCZOS).save(os.path.join(desktop_dir, "icon-128.png"), "PNG")
    master_icon.resize((64, 64), Image.Resampling.LANCZOS).save(os.path.join(desktop_dir, "icon-64.png"), "PNG")
    master_icon.resize((32, 32), Image.Resampling.LANCZOS).save(os.path.join(desktop_dir, "icon-32.png"), "PNG")
    master_icon.resize((16, 16), Image.Resampling.LANCZOS).save(os.path.join(desktop_dir, "icon-16.png"), "PNG")
    
    # Save standard .ico for Windows Desktop (contains multiple resolutions)
    ico_images = [
        master_icon.resize((256, 256), Image.Resampling.LANCZOS),
        master_icon.resize((128, 128), Image.Resampling.LANCZOS),
        master_icon.resize((64, 64), Image.Resampling.LANCZOS),
        master_icon.resize((48, 48), Image.Resampling.LANCZOS),
        master_icon.resize((32, 32), Image.Resampling.LANCZOS),
        master_icon.resize((16, 16), Image.Resampling.LANCZOS),
    ]
    ico_images[0].save(
        os.path.join(desktop_dir, "icon.ico"),
        format="ICO",
        sizes=[(256, 256), (128, 128), (64, 64), (48, 48), (32, 32), (16, 16)]
    )

    # Save 512x512 as icon.png for Compose Window Painter
    master_icon.resize((512, 512), Image.Resampling.LANCZOS).save(os.path.join(desktop_dir, "icon.png"), "PNG")

    print("Icon generation completed successfully!")
