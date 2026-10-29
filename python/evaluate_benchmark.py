#!/usr/bin/env python3
"""
AgeTrace Comprehensive 20-Image Benchmark & Discrepancy Evaluator
Evaluates face detection recall, precision, and biometric age calibration accuracy across:
- Infant/Toddler (1 - 3)
- Children (5 - 9)
- Adolescents (10 - 15)
- Young Adults (18 - 25)
- Adults (30 - 45)
- Mature Adults (50 - 65)
- Seniors & Centenarians (75 - 100)
- Group photos (2, 3, 5, 10, and 20 individuals)
"""

import sys
import os
import json
import time
from pathlib import Path

# Add python directory to path
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))

from age_trace_ai import analyze_image_bytes

EVAL_DIR = Path("test_assets/evaluation_suite")

EXPECTED_METADATA = {
    "test_01_baby_1yo": {"min_age": 0, "max_age": 4, "expected_faces": 1, "is_adult": False},
    "test_02_toddler_3yo": {"min_age": 1, "max_age": 6, "expected_faces": 1, "is_adult": False},
    "test_03_child_boy_7yo": {"min_age": 5, "max_age": 10, "expected_faces": 1, "is_adult": False},
    "test_04_child_girl_9yo": {"min_age": 6, "max_age": 12, "expected_faces": 1, "is_adult": False},
    "test_05_preteen_boy_12yo": {"min_age": 10, "max_age": 15, "expected_faces": 1, "is_adult": False},
    "test_06_teen_girl_15yo": {"min_age": 13, "max_age": 18, "expected_faces": 1, "is_adult": False},
    "test_07_young_woman_21yo": {"min_age": 18, "max_age": 28, "expected_faces": 1, "is_adult": True},
    "test_08_young_man_24yo": {"min_age": 19, "max_age": 30, "expected_faces": 1, "is_adult": True},
    "test_09_adult_woman_34yo": {"min_age": 28, "max_age": 42, "expected_faces": 1, "is_adult": True},
    "test_10_adult_man_42yo": {"min_age": 35, "max_age": 50, "expected_faces": 1, "is_adult": True},
    "test_11_mature_woman_52yo": {"min_age": 45, "max_age": 60, "expected_faces": 1, "is_adult": True},
    "test_12_mature_man_60yo": {"min_age": 52, "max_age": 70, "expected_faces": 1, "is_adult": True},
    "test_13_senior_woman_75": {"min_age": 68, "max_age": 90, "expected_faces": 1, "is_adult": True},
    "test_14_senior_man_85yo": {"min_age": 75, "max_age": 95, "expected_faces": 1, "is_adult": True},
    "test_15_centenarian_woman_100yo": {"min_age": 85, "max_age": 105, "expected_faces": 1, "is_adult": True},
    "test_16_duo_mother_toddler": {"expected_faces": 2, "multi_person": True},
    "test_17_trio_three_generations": {"expected_faces": 3, "multi_person": True},
    "test_18_family_five_people": {"expected_faces": 5, "multi_person": True},
    "test_19_classroom_ten_kids": {"expected_faces": 10, "multi_person": True},
    "test_20_crowd_twenty_people": {"expected_faces": 20, "multi_person": True},
}

def evaluate_suite():
    results = []
    issues_found = []

    image_files = sorted(list(EVAL_DIR.glob("*.jpg")) + list(EVAL_DIR.glob("*.png")))
    print(f"Found {len(image_files)} test images in {EVAL_DIR}...")

    for img_path in image_files:
        stem = img_path.stem
        # find matching meta key
        meta_key = None
        for k in EXPECTED_METADATA:
            if k in stem:
                meta_key = k
                break

        print(f"\n--- Testing: {img_path.name} ---")
        t0 = time.time()
        with open(img_path, "rb") as f:
            data = f.read()
        res = analyze_image_bytes(data)
        elapsed = (time.time() - t0) * 1000

        faces = res.get("faces", [])
        faces_count = len(faces)
        print(f"Detected {faces_count} faces in {elapsed:.1f}ms")

        meta = EXPECTED_METADATA.get(meta_key, {})
        expected_faces = meta.get("expected_faces")

        test_record = {
            "file": img_path.name,
            "faces_detected": faces_count,
            "expected_faces": expected_faces,
            "inference_time_ms": elapsed,
            "faces_detail": []
        }

        # Face count check
        if expected_faces is not None:
            if meta.get("multi_person"):
                # For crowds, tolerate within reasonable threshold (e.g. at least 70% of faces detected)
                min_acceptable = int(expected_faces * 0.7)
                if faces_count < min_acceptable:
                    issues_found.append(f"{img_path.name}: Expected ~{expected_faces} faces, but only detected {faces_count}")
            else:
                if faces_count != expected_faces:
                    issues_found.append(f"{img_path.name}: Expected {expected_faces} face, detected {faces_count}")

        # Age and classification check
        for idx, f in enumerate(faces):
            age = f.get("age")
            is_adult = f.get("is_adult")
            classification = f.get("classification")
            badge = f.get("badge")
            bbox = f.get("bbox", {})

            # Check bbox validity
            if bbox.get("w", 0) <= 0 or bbox.get("h", 0) <= 0:
                issues_found.append(f"{img_path.name} face {idx}: Invalid bbox dimensions {bbox}")
            if bbox.get("x", 0) < -0.1 or bbox.get("y", 0) < -0.1 or bbox.get("x", 0) > 1.1 or bbox.get("y", 0) > 1.1:
                issues_found.append(f"{img_path.name} face {idx}: Bbox out of image bounds {bbox}")

            # Check age boundary
            min_age = meta.get("min_age")
            max_age = meta.get("max_age")
            exp_adult = meta.get("is_adult")

            if exp_adult is not None and is_adult != exp_adult:
                issues_found.append(
                    f"{img_path.name} face {idx}: Adult classification mismatch! Expected is_adult={exp_adult}, got {is_adult} (age: {age})"
                )

            if min_age is not None and max_age is not None:
                if not (min_age - 3 <= age <= max_age + 8):
                    issues_found.append(
                        f"{img_path.name} face {idx}: Age {age} deviated significantly from expected range [{min_age}-{max_age}]"
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
    print(f"\nBenchmark completed. Report saved to {report_path}")
    print(f"Summary: {len(results)} images tested, {len(issues_found)} discrepancies detected.")
    return report

if __name__ == "__main__":
    evaluate_suite()
