#!/usr/bin/env python3
"""Publishes MorseKit to Google Play with the Play Developer API (Android Publisher v3).

Standard library only, plus the `openssl` command to sign the service-account login, so no
third-party code ever sees the Play key. Used by .github/workflows/android-release.yml and
play-promote.yml; see docs/13-ci-release.md.

    play.py notes < release-body.md
    play.py upload  --aab app.aab --mapping mapping.txt --notes-file body.md --release-name "1.0.0 (5)"
    play.py promote [--version-code 5] --rollout 20 --priority 0

`upload` and `promote` read the service-account key (JSON) from PLAY_SERVICE_ACCOUNT_JSON.
With --dry-run, the edit is validated by Play and then discarded: nothing is published.
"""

import argparse
import base64
import json
import os
import re
import subprocess
import sys
import tempfile
import time
import urllib.error
import urllib.parse
import urllib.request

PACKAGE = "in.geekofia.morsekit"
# The closed test, where MorseKit's testers are. Play Console's first closed track is "alpha".
DEFAULT_TRACK = "alpha"
SCOPE = "https://www.googleapis.com/auth/androidpublisher"
API_ROOT = "https://androidpublisher.googleapis.com"
NOTES_LANGUAGE = "en-US"
NOTES_LIMIT = 500  # Play's limit for "What's new", per language
DEFAULT_NOTES = "Bug fixes and improvements."


class PlayError(Exception):
    pass


# --- Release notes -------------------------------------------------------------------------

def to_play_notes(markdown: str, limit: int = NOTES_LIMIT) -> str:
    """Turns a GitHub release body (Markdown) into Play's plain "What's new" text."""
    text = re.sub(r"<!--.*?-->", "", markdown.replace("\r\n", "\n"), flags=re.S)
    lines = []
    for line in text.split("\n"):
        stripped = line.strip()
        # GitHub's generated notes: drop the changelog link and contributor boilerplate.
        if re.match(r"\**Full Changelog\**:", stripped) or re.match(r"#+\s*New Contributors", stripped):
            break
        line = re.sub(r"^\s*#{1,6}\s*", "", line)  # headings
        line = re.sub(r"^\s*[-*+]\s+", "• ", line)  # bullets
        line = re.sub(r"!\[[^\]]*\]\([^)]*\)", "", line)  # images
        line = re.sub(r"\[([^\]]+)\]\([^)]*\)", r"\1", line)  # links keep their text
        line = re.sub(r"\s+by @[\w-]+(\[bot\])? in https://\S+", "", line)  # "by @x in <PR url>"
        line = re.sub(r"(\*\*|__)(.+?)\1", r"\2", line)  # bold
        line = re.sub(r"(?<![\w*])\*(?!\s)(.+?)(?<!\s)\*(?![\w*])", r"\1", line)  # *italic*
        line = re.sub(r"`([^`]*)`", r"\1", line)  # code
        lines.append(line.rstrip())
    text = re.sub(r"\n{3,}", "\n\n", "\n".join(lines)).strip()
    if not text:
        return DEFAULT_NOTES
    if len(text) <= limit:
        return text
    cut = text[: limit - 1]
    # Prefer ending at a line, then a word, so the text isn't cut mid-word.
    for sep in ("\n", " "):
        at = cut.rfind(sep)
        if at >= limit // 2:
            cut = cut[:at]
            break
    return cut.rstrip() + "…"


# --- Authentication ------------------------------------------------------------------------

def _b64url(data: bytes) -> str:
    return base64.urlsafe_b64encode(data).rstrip(b"=").decode()


def _sign_rs256(message: bytes, private_key_pem: str) -> bytes:
    fd, key_path = tempfile.mkstemp(suffix=".pem")  # created 0600
    try:
        with os.fdopen(fd, "w") as key_file:
            key_file.write(private_key_pem)
        return subprocess.run(
            ["openssl", "dgst", "-sha256", "-sign", key_path],
            input=message, capture_output=True, check=True,
        ).stdout
    finally:
        os.remove(key_path)


def access_token(service_account_json: str) -> str:
    """OAuth 2.0 for service accounts: a signed JWT exchanged for an access token."""
    try:
        account = json.loads(service_account_json)
        email, key = account["client_email"], account["private_key"]
    except (ValueError, KeyError) as e:
        raise PlayError("PLAY_SERVICE_ACCOUNT_JSON is not a service-account key (JSON)") from e
    token_uri = account.get("token_uri", "https://oauth2.googleapis.com/token")
    now = int(time.time())
    header = _b64url(json.dumps({"alg": "RS256", "typ": "JWT"}).encode())
    claims = _b64url(json.dumps(
        {"iss": email, "scope": SCOPE, "aud": token_uri, "iat": now, "exp": now + 3600}
    ).encode())
    signing_input = f"{header}.{claims}".encode()
    jwt = f"{header}.{claims}.{_b64url(_sign_rs256(signing_input, key))}"
    body = urllib.parse.urlencode(
        {"grant_type": "urn:ietf:params:oauth:grant-type:jwt-bearer", "assertion": jwt}
    ).encode()
    return _request("POST", token_uri, body, "application/x-www-form-urlencoded")["access_token"]


# --- HTTP ----------------------------------------------------------------------------------

def _request(method, url, body=None, content_type="application/json", token=None, timeout=300):
    request = urllib.request.Request(url, data=body, method=method)
    if body is not None:
        request.add_header("Content-Type", content_type)
    if token:
        request.add_header("Authorization", f"Bearer {token}")
    try:
        with urllib.request.urlopen(request, timeout=timeout) as response:
            data = response.read()
    except urllib.error.HTTPError as e:
        with e:
            detail = e.read().decode(errors="replace")
        try:
            detail = json.loads(detail)["error"]["message"]
        except (ValueError, KeyError, TypeError):
            pass
        raise PlayError(f"{method} {urllib.parse.urlsplit(url).path}: HTTP {e.code}: {detail}") from None
    return json.loads(data) if data.strip() else {}


class Edit:
    """One Play "edit": changes are staged, then committed together or discarded."""

    def __init__(self, token: str, package: str = PACKAGE):
        self.token = token
        self.app = f"{API_ROOT}/androidpublisher/v3/applications/{package}"
        self.upload_app = f"{API_ROOT}/upload/androidpublisher/v3/applications/{package}"
        self.id = self._json("POST", f"{self.app}/edits", {})["id"]
        self.url = f"{self.app}/edits/{self.id}"

    def _json(self, method, url, payload=None):
        if payload is not None:
            body = json.dumps(payload).encode()
        else:
            # Google answers a POST without Content-Length with HTTP 411; b"" makes urllib send 0.
            body = b"" if method in ("POST", "PUT") else None
        return _request(method, url, body, token=self.token)

    def _upload(self, url, path):
        with open(path, "rb") as f:
            return _request("POST", f"{url}?uploadType=media", f.read(), "application/octet-stream",
                            token=self.token)

    def upload_bundle(self, path) -> int:
        return int(self._upload(f"{self.upload_app}/edits/{self.id}/bundles", path)["versionCode"])

    def upload_mapping(self, version_code, path):
        self._upload(f"{self.upload_app}/edits/{self.id}/deobfuscationFiles/{version_code}/proguard", path)

    def releases(self, track) -> list:
        return self._json("GET", f"{self.url}/tracks/{track}").get("releases", [])

    def set_release(self, track, release):
        self._json("PUT", f"{self.url}/tracks/{track}", {"track": track, "releases": [release]})

    def finish(self, dry_run: bool):
        if dry_run:
            self._json("POST", f"{self.url}:validate")
            self._json("DELETE", self.url)
        else:
            self._json("POST", f"{self.url}:commit")

    def discard(self):
        try:
            self._json("DELETE", self.url)
        except PlayError:
            pass  # Uncommitted edits expire on their own.


# --- Commands ------------------------------------------------------------------------------

def rollout_fields(rollout_percent: float) -> dict:
    if not 0 < rollout_percent <= 100:
        raise PlayError(f"--rollout must be above 0 and at most 100, got {rollout_percent:g}")
    if rollout_percent == 100:
        return {"status": "completed"}
    return {"status": "inProgress", "userFraction": rollout_percent / 100}


def choose_release(releases: list, version_code=None) -> dict:
    candidates = [r for r in releases if r.get("versionCodes")]
    if version_code is not None:
        candidates = [r for r in candidates if str(version_code) in map(str, r["versionCodes"])]
    if not candidates:
        wanted = f"version code {version_code}" if version_code is not None else "a release"
        raise PlayError(f"The source track has no {wanted}")
    return max(candidates, key=lambda r: max(int(v) for v in r["versionCodes"]))


def upload(edit: Edit, aab, mapping, notes, release_name, track):
    version_code = edit.upload_bundle(aab)
    if mapping:
        edit.upload_mapping(version_code, mapping)
    release = {
        "name": release_name,
        "versionCodes": [str(version_code)],
        "status": "completed",
        "releaseNotes": [{"language": NOTES_LANGUAGE, "text": notes}],
    }
    edit.set_release(track, release)
    return release


def promote(edit: Edit, source, target, version_code, rollout_percent, priority):
    if not 0 <= priority <= 5:
        raise PlayError(f"--priority must be 0-5, got {priority}")
    picked = choose_release(edit.releases(source), version_code)
    release = {
        "name": picked.get("name", ""),
        "versionCodes": picked["versionCodes"],
        **rollout_fields(rollout_percent),
        "inAppUpdatePriority": priority,
    }
    if picked.get("releaseNotes"):
        release["releaseNotes"] = picked["releaseNotes"]
    edit.set_release(target, release)
    return release


def _summary(title, release, track, dry_run):
    notes = next((n["text"] for n in release.get("releaseNotes", [])), "")
    lines = [
        f"### {title}{' (dry run: validated, not published)' if dry_run else ''}",
        f"- Track: `{track}`",
        f"- Release: {release.get('name') or '-'}, version code {', '.join(map(str, release['versionCodes']))}",
        f"- Status: {release['status']}"
        + (f" ({release['userFraction']:.0%} of users)" if "userFraction" in release else ""),
    ]
    if "inAppUpdatePriority" in release:
        lines.append(f"- In-app update priority: {release['inAppUpdatePriority']}")
    if notes:
        lines += ["", "What's new:", "", *(f"> {line}" for line in notes.split("\n"))]
    text = "\n".join(lines)
    print(text)
    if os.environ.get("GITHUB_STEP_SUMMARY"):
        with open(os.environ["GITHUB_STEP_SUMMARY"], "a") as f:
            f.write(text + "\n")


def main(argv=None):
    parser = argparse.ArgumentParser(description=__doc__.split("\n")[0])
    commands = parser.add_subparsers(dest="command", required=True)

    commands.add_parser("notes", help="Print Play release notes for a Markdown body read from stdin")

    up = commands.add_parser("upload", help="Upload an AAB (+ mapping) to a track")
    up.add_argument("--aab", required=True)
    up.add_argument("--mapping")
    up.add_argument("--notes-file", help="Markdown release notes (e.g. the GitHub release body)")
    up.add_argument("--release-name", required=True)
    up.add_argument("--track", default=DEFAULT_TRACK)
    up.add_argument("--dry-run", action="store_true")

    pr = commands.add_parser("promote", help="Copy a release from one track to another")
    pr.add_argument("--version-code", type=int, help="Default: the newest release on --from")
    pr.add_argument("--from", dest="source", default=DEFAULT_TRACK)
    pr.add_argument("--to", dest="target", default="production")
    pr.add_argument("--rollout", type=float, default=100, help="Percent of users; below 100 is staged")
    pr.add_argument("--priority", type=int, default=0, help="In-app update priority, 0-5")
    pr.add_argument("--dry-run", action="store_true")

    args = parser.parse_args(argv)
    if args.command == "notes":
        print(to_play_notes(sys.stdin.read()))
        return 0

    credentials = os.environ.get("PLAY_SERVICE_ACCOUNT_JSON", "")
    if not credentials.strip():
        raise PlayError("PLAY_SERVICE_ACCOUNT_JSON is not set")
    edit = Edit(access_token(credentials))
    try:
        if args.command == "upload":
            markdown = ""
            if args.notes_file:
                with open(args.notes_file) as f:
                    markdown = f.read()
            release = upload(edit, args.aab, args.mapping, to_play_notes(markdown),
                             args.release_name, args.track)
            title, track = "Uploaded to Google Play", args.track
        else:
            release = promote(edit, args.source, args.target, args.version_code,
                              args.rollout, args.priority)
            title, track = f"Promoted {args.source} → {args.target}", args.target
        edit.finish(args.dry_run)
    except BaseException:
        edit.discard()
        raise
    _summary(title, release, track, args.dry_run)
    return 0


if __name__ == "__main__":
    try:
        sys.exit(main())
    except PlayError as e:
        print(f"::error::{e}", file=sys.stderr)
        sys.exit(1)
