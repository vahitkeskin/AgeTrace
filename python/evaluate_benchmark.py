#!/usr/bin/env python3
"""
AgeTrace Comprehensive 100-Image Global Benchmark & Discrepancy Evaluator
Evaluates face detection recall, precision, and biometric age calibration accuracy across:
- 100 photorealistic portraits spanning 6 major global ethnic groups:
  African, Asian (East & Southeast), Arab & Middle Eastern, Russian & Slavic,
  South Asian / Indian, Latin American / Hispanic, and Multi-ethnic Crowds.
- Ages 1 to 100 (infant, toddler, child, preteen, teen, young adult, adult, mature, senior, centenarian).
- Solo portraits (1 person) and multi-person compositions (2, 3, 4, 5, 6, 8, 10, 15, and 20 individuals).
"""

import sys
import os
import json
import re
import time
from pathlib import Path

# Add python directory to path
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))

from age_trace_ai import analyze_image_bytes

EVAL_DIR = Path("test_assets/evaluation_suite")

def get_image_metadata(stem: str):
    """Derives expected benchmark metadata from filename structure."""
    meta = {}
    
    # 1. Multi-person checks
    if any(k in stem for k in ["duo", "father_son", "multiracial_friends", "mother_toddler"]):
        return {"expected_faces": 2, "multi_person": True}
    if any(k in stem for k in ["trio", "three_generations"]):
        return {"expected_faces": 3, "multi_person": True}
    if "quartet" in stem:
        return {"expected_faces": 4, "multi_person": True}
    if any(k in stem for k in ["family", "five_members", "five_people"]):
        return {"expected_faces": 5, "multi_person": True}
    if "tech_team" in stem or "six" in stem:
        return {"expected_faces": 6, "multi_person": True}
    if "athletes" in stem:
        return {"expected_faces": 8, "multi_person": True}
    if "classroom" in stem or "ten_kids" in stem or "ten_students" in stem:
        return {"expected_faces": 10, "multi_person": True}
    if "fifteen" in stem:
        return {"expected_faces": 15, "multi_person": True}
    if "twenty" in stem or "crowd" in stem:
        return {"expected_faces": 20, "multi_person": True}

    # 2. Single-person age checks
    meta["expected_faces"] = 1
    meta["multi_person"] = False
    
    # Extract age pattern e.g. "1yo", "12yo", "35yo"
    m = re.search(r"(\d+)yo", stem)
    if not m:
        # Check for senior woman 75
        m2 = re.search(r"_(\d+)$", stem)
        target_age = int(m2.group(1)) if m2 else 30
    else:
        target_age = int(m.group(1))

    meta["target_age"] = target_age
    meta["is_adult"] = (target_age >= 18)

    if target_age <= 2:
        meta["min_age"] = 0
        meta["max_age"] = 4
    elif target_age <= 5:
        meta["min_age"] = 1
        meta["max_age"] = 6
    elif target_age <= 10:
        meta["min_age"] = 5
        meta["max_age"] = 12
    elif target_age <= 13:
        meta["min_age"] = 9
        meta["max_age"] = 15
    elif target_age <= 17:
        meta["min_age"] = 13
        meta["max_age"] = 18
    elif target_age <= 29:
        meta["min_age"] = 18
        meta["max_age"] = 30
    elif target_age <= 49:
        meta["min_age"] = 28
        meta["max_age"] = 52
    elif target_age <= 65:
        meta["min_age"] = 45
        meta["max_age"] = 72
    else:
        meta["min_age"] = 65
        meta["max_age"] = 105

    return meta

def evaluate_suite():
    results = []
    issues_found = []

    image_files = sorted(list(EVAL_DIR.glob("*.jpg")) + list(EVAL_DIR.glob("*.png")))
    print(f"Found {len(image_files)} test images in {EVAL_DIR}...")

    for img_path in image_files:
        stem = img_path.stem
        meta = get_image_metadata(stem)

        print(f"\n--- Testing [{stem}] ---")
        t0 = time.time()
        with open(img_path, "rb") as f:
            data = f.read()
        res = analyze_image_bytes(data)
        elapsed = (time.time() - t0) * 1000

        faces = res.get("faces", [])
        faces_count = len(faces)
        print(f"Detected {faces_count} faces in {elapsed:.1f}ms")

        expected_faces = meta.get("expected_faces")

        test_record = {
            "file": img_path.name,
            "faces_detected": faces_count,
            "expected_faces": expected_faces,
            "inference_time_ms": elapsed,
            "faces_detail": []
        }

        # Face count validation
        if expected_faces is not None:
            if meta.get("multi_person"):
                # For crowds / large groups, tolerate reasonable recall (>= 70%)
                min_acceptable = int(expected_faces * 0.7)
                if faces_count < min_acceptable:
                    issues_found.append(f"{img_path.name}: Expected ~{expected_faces} faces, but only detected {faces_count}")
            else:
                if faces_count != expected_faces:
                    issues_found.append(f"{img_path.name}: Expected {expected_faces} face, detected {faces_count}")

        # Age and classification validation
        for idx, f in enumerate(faces):
            age = f.get("age")
            is_adult = f.get("is_adult")
            classification = f.get("classification")
            badge = f.get("badge")
            bbox = f.get("bbox", {})

            # Check bbox validity
            if bbox.get("w", 0) <= 0 or bbox.get("h", 0) <= 0:
                issues_found.append(f"{img_path.name} face {idx}: Invalid bbox dimensions {bbox}")

            # Check adult classification
            exp_adult = meta.get("is_adult")
            min_age = meta.get("min_age")
            max_age = meta.get("max_age")

            if not meta.get("multi_person") and exp_adult is not None:
                if is_adult != exp_adult:
                    issues_found.append(
                        f"{img_path.name} face {idx}: Adult mismatch! Expected is_adult={exp_adult}, got {is_adult} (age: {age})"
                    )

            if not meta.get("multi_person") and min_age is not None and max_age is not None:
                if not (min_age - 3 <= age <= max_age + 8):
                    issues_found.append(
                        f"{img_path.name} face {idx}: Age {age} deviated significantly from [{min_age}-{max_age}]"
                    )

            test_record["faces_detail"].append({
                "face_idx": idx + 1,
                "age": age,
                "classification": classification,
                "is_adult": is_adult,
                "badge": badge,
                "confidence": f.get("confidence")
            })
            print(f"  Face {idx+1}: {badge} ({classification})")

        results.append(test_record)

    report = {
        "timestamp": time.strftime("%Y-%m-%d %H:%M:%S"),
        "total_images_tested": len(results),
        "total_issues_found": len(issues_found),
        "issues": issues_found,
        "results": results
    }

    report_path = "test_assets/benchmark_report.json"
    with open(report_path, "w") as f:
        json.dump(report, f, indent=2)
    print(f"\n=======================================================")
    print(f"Benchmark completed. Report saved to {report_path}")
    print(f"Summary: {len(results)} images tested, {len(issues_found)} discrepancies detected.")
    print(f"=======================================================")
    return report

if __name__ == "__main__":
    evaluate_suite()
