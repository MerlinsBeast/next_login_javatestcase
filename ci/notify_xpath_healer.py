#!/usr/bin/env python3
"""Post a build result to xpath_healer. Run from the Jenkins workspace after Maven.

Reads Jenkins' own environment for build metadata and the surefire reports for
failures, then POSTs one normalized-ish payload. It is deliberately forgiving:
a notifier that crashes must never turn a green build red, so every failure
here is reported to stdout and swallowed.

Env:
  XPATH_HEALER_URL     full endpoint URL
  XPATH_HEALER_SECRET  shared secret, sent as X-Webhook-Secret
  BUILD_RESULT         SUCCESS | FAILURE | UNSTABLE
"""

import glob
import json
import os
import sys
import urllib.error
import urllib.request
import xml.etree.ElementTree as ET

CONSOLE_TAIL_CHARS = 4000
MAX_STACK_CHARS = 6000


def collect_failures(workspace):
    """Every <testcase> carrying a <failure> or <error>, from the surefire XML."""
    failures = []
    totals = {"total": 0, "failed": 0, "skipped": 0}

    for path in glob.glob(os.path.join(workspace, "target/surefire-reports/TEST-*.xml")):
        try:
            root = ET.parse(path).getroot()
        except ET.ParseError as e:
            print(f"[notify] could not parse {path}: {e}")
            continue

        for case in root.iter("testcase"):
            totals["total"] += 1
            if case.find("skipped") is not None:
                totals["skipped"] += 1
                continue

            problem = case.find("failure")
            if problem is None:
                problem = case.find("error")
            if problem is None:
                continue

            totals["failed"] += 1
            failures.append(
                {
                    "className": case.get("classname", "unknown"),
                    "testName": case.get("name", "unknown"),
                    "message": problem.get("message", "") or "",
                    "stackTrace": (problem.text or "")[:MAX_STACK_CHARS],
                }
            )

    totals["passed"] = totals["total"] - totals["failed"] - totals["skipped"]
    return failures, totals


def build_payload(workspace, result):
    failures, totals = collect_failures(workspace)
    job = os.environ.get("JOB_NAME", "unknown")
    number = os.environ.get("BUILD_NUMBER", "")

    return {
        "job": job,
        "build": number,
        "url": os.environ.get("BUILD_URL"),
        "result": result,
        "branch": os.environ.get("GIT_BRANCH"),
        "commit": os.environ.get("GIT_COMMIT"),
        "repoUrl": os.environ.get("GIT_URL"),
        "testsTotal": totals["total"],
        "testsPassed": totals["passed"],
        "testsFailed": totals["failed"],
        "testsSkipped": totals["skipped"],
        "failures": failures,
        "consoleTail": None,
    }, f"{job}#{number}"


def main():
    url = os.environ.get("XPATH_HEALER_URL")
    secret = os.environ.get("XPATH_HEALER_SECRET")
    result = os.environ.get("BUILD_RESULT", "FAILURE")

    if not url or not secret:
        print("[notify] XPATH_HEALER_URL or XPATH_HEALER_SECRET unset, skipping.")
        return 0

    payload, delivery_id = build_payload(os.environ.get("WORKSPACE", "."), result)
    print(f"[notify] {result}: {payload['testsFailed']} failed of {payload['testsTotal']} -> {url}")

    request = urllib.request.Request(
        url,
        data=json.dumps(payload).encode(),
        headers={
            "Content-Type": "application/json",
            "X-Webhook-Secret": secret,
            "X-Delivery-Id": delivery_id,
        },
        method="POST",
    )

    try:
        with urllib.request.urlopen(request, timeout=15) as response:
            print(f"[notify] {response.status} {response.read().decode()[:600]}")
    except urllib.error.HTTPError as e:
        print(f"[notify] {e.code} {e.read().decode()[:600]}")
    except Exception as e:
        # Never fail the build because the notifier could not reach the healer.
        print(f"[notify] could not deliver: {e}")

    return 0


if __name__ == "__main__":
    sys.exit(main())
