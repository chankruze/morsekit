"""Tests for play.py against a fake Play API (no network). Run: python3 -m unittest discover scripts/play"""

import contextlib
import io
import json
import os
import re
import subprocess
import sys
import tempfile
import unittest
import urllib.error
from unittest import mock
from urllib.parse import parse_qs, urlsplit

sys.path.insert(0, os.path.dirname(__file__))


class FakePlay:
    """Just enough of the Android Publisher API, answering in place of urllib's urlopen.

    `state` is reset per test.
    """

    state = {}

    @classmethod
    def urlopen(cls, request, timeout=None):
        if request.get_method() in ("POST", "PUT") and request.data is None:
            # What Google's front end does: urllib sends no Content-Length without a body.
            raise urllib.error.HTTPError(request.full_url, 411, "Length Required", {}, io.BytesIO(b""))
        code, payload = cls._handle(request.get_method(), request.full_url, request.data or b"",
                                    request.get_header("Authorization"))
        body = io.BytesIO(json.dumps(payload).encode())
        if code >= 400:
            raise urllib.error.HTTPError(request.full_url, code, "error", {}, body)
        return contextlib.closing(body)

    @classmethod
    def _handle(cls, method, url, body, authorization):
        s = cls.state
        path = urlsplit(url).path
        s["calls"].append((method, path.replace("/edits/e1", "/edits/{id}")))

        if path == "/token":
            assertion = parse_qs(body.decode())["assertion"][0]
            header, claims, signature = assertion.split(".")
            s["claims"] = json.loads(_unb64(claims))
            ok = subprocess.run(
                ["openssl", "dgst", "-sha256", "-verify", s["public_key"], "-signature", "/dev/stdin",
                 s["signed_file"](f"{header}.{claims}")],
                input=_unb64(signature), capture_output=True,
            ).returncode == 0
            return (200, {"access_token": "tok"}) if ok else (401, {})

        if authorization != "Bearer tok":
            return 401, {"error": {"message": "no token"}}
        if s.get("fail_on") and s["fail_on"] in path:
            return 403, {"error": {"message": "The caller does not have permission"}}

        app = "/androidpublisher/v3/applications/in.geekofia.morsekit"
        upload = "/upload" + app
        if method == "POST" and path == f"{app}/edits":
            return 200, {"id": "e1"}
        if method == "POST" and path == f"{upload}/edits/e1/bundles":
            s["bundle"] = body
            return 200, {"versionCode": 5}
        if method == "POST" and path == f"{upload}/edits/e1/deobfuscationFiles/5/proguard":
            s["mapping"] = body
            return 200, {}
        m = re.fullmatch(rf"{app}/edits/e1/tracks/(\w+)", path)
        if m and method == "GET":
            return 200, {"track": m[1], "releases": s["tracks"].get(m[1], [])}
        if m and method == "PUT":
            s["put"][m[1]] = json.loads(body)
            return 200, json.loads(body)
        if path in (f"{app}/edits/e1:commit", f"{app}/edits/e1:validate") or method == "DELETE":
            return 200, {}
        return 404, {"error": {"message": f"unexpected {method} {path}"}}


def _unb64(text):
    import base64
    return base64.urlsafe_b64decode(text + "=" * (-len(text) % 4))


class PlayNotesTest(unittest.TestCase):
    def setUp(self):
        import play
        self.notes = play.to_play_notes

    def test_markdown_becomes_plain_text(self):
        body = (
            "## What's new\r\n\r\n"
            "- **Check for updates** in Settings\n"
            "* Faster `Morse` decoding, see [the docs](https://x.y/docs)\n"
            "<!-- internal note -->\n"
            "![shot](https://x.y/a.png)\n"
            "Works *offline*; snake_case_names stay.\n"
        )
        self.assertEqual(
            self.notes(body),
            "What's new\n\n• Check for updates in Settings\n• Faster Morse decoding, see the docs\n\n"
            "Works offline; snake_case_names stay.",
        )

    def test_github_generated_boilerplate_is_dropped(self):
        body = (
            "## What's Changed\n"
            "* feat: in-app updates by @chankruze in https://github.com/chankruze/morsekit/pull/3\n\n"
            "## New Contributors\n* @someone made their first contribution\n\n"
            "**Full Changelog**: https://github.com/chankruze/morsekit/compare/v1.0.0...v1.0.1\n"
        )
        self.assertEqual(self.notes(body), "What's Changed\n• feat: in-app updates")

    def test_empty_body_uses_the_default(self):
        self.assertEqual(self.notes("  \n<!-- nothing -->\n"), "Bug fixes and improvements.")

    def test_long_notes_are_cut_at_a_word_within_the_limit(self):
        text = self.notes("word " * 200)
        self.assertLessEqual(len(text), 500)
        self.assertTrue(text.endswith("word…"), text[-10:])

    def test_long_notes_prefer_a_line_break(self):
        body = "\n".join(f"- change number {i}" for i in range(60))
        text = self.notes(body)
        self.assertLessEqual(len(text), 500)
        self.assertRegex(text, r"• change number \d+…$")


class PlayApiTest(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.dir = tempfile.TemporaryDirectory()
        d = cls.dir.name
        key, pub = os.path.join(d, "key.pem"), os.path.join(d, "pub.pem")
        subprocess.run(["openssl", "genpkey", "-algorithm", "RSA", "-pkeyopt", "rsa_keygen_bits:2048",
                        "-out", key], check=True, capture_output=True)
        subprocess.run(["openssl", "pkey", "-in", key, "-pubout", "-out", pub], check=True, capture_output=True)
        with open(key) as f:
            cls.private_key, cls.public_key = f.read(), pub
        cls.root = "https://play.test"

    @classmethod
    def tearDownClass(cls):
        cls.dir.cleanup()

    def setUp(self):
        def signed_file(text):
            path = os.path.join(self.dir.name, "signed.txt")
            with open(path, "w") as f:
                f.write(text)
            return path

        FakePlay.state = {"calls": [], "put": {}, "tracks": {}, "public_key": self.public_key,
                          "signed_file": signed_file}
        os.environ["PLAY_SERVICE_ACCOUNT_JSON"] = json.dumps({
            "client_email": "ci@morsekit.iam.gserviceaccount.com",
            "private_key": self.private_key,
            "token_uri": f"{self.root}/token",
        })
        import play
        self.play = play
        for patch in (mock.patch.object(play, "API_ROOT", self.root),
                      mock.patch("urllib.request.urlopen", FakePlay.urlopen)):
            patch.start()
            self.addCleanup(patch.stop)

    def run_play(self, *args):
        with open(os.devnull, "w") as out:
            stdout, sys.stdout = sys.stdout, out
            try:
                return self.play.main(list(args))
            finally:
                sys.stdout = stdout

    def files(self):
        d = self.dir.name
        for name, content in (("app.aab", b"AAB"), ("mapping.txt", b"MAP"),
                              ("notes.md", b"- **New:** check for updates\n")):
            with open(os.path.join(d, name), "wb") as f:
                f.write(content)
        return [os.path.join(d, n) for n in ("app.aab", "mapping.txt", "notes.md")]

    def calls(self):
        return [c for c in FakePlay.state["calls"] if c[1] != "/token"]

    def test_login_is_a_jwt_signed_with_the_service_account_key(self):
        self.assertEqual(self.play.access_token(os.environ["PLAY_SERVICE_ACCOUNT_JSON"]), "tok")
        claims = FakePlay.state["claims"]
        self.assertEqual(claims["iss"], "ci@morsekit.iam.gserviceaccount.com")
        self.assertEqual(claims["scope"], "https://www.googleapis.com/auth/androidpublisher")
        self.assertEqual(claims["exp"] - claims["iat"], 3600)

    def test_a_key_that_does_not_match_is_refused(self):
        other = os.path.join(self.dir.name, "other.pem")
        subprocess.run(["openssl", "genpkey", "-algorithm", "RSA", "-out", other], check=True, capture_output=True)
        with open(other) as f:
            account = {**json.loads(os.environ["PLAY_SERVICE_ACCOUNT_JSON"]), "private_key": f.read()}
        with self.assertRaisesRegex(self.play.PlayError, "/token: HTTP 401"):
            self.play.access_token(json.dumps(account))

    def test_upload_sends_bundle_mapping_and_notes_to_the_closed_test_then_commits(self):
        aab, mapping, notes = self.files()
        self.run_play("upload", "--aab", aab, "--mapping", mapping, "--notes-file", notes,
                      "--release-name", "1.0.0 (5)")
        s = FakePlay.state
        self.assertEqual((s["bundle"], s["mapping"]), (b"AAB", b"MAP"))
        self.assertEqual(s["put"]["alpha"], {"track": "alpha", "releases": [{
            "name": "1.0.0 (5)", "versionCodes": ["5"], "status": "completed",
            "releaseNotes": [{"language": "en-US", "text": "• New: check for updates"}],
        }]})
        self.assertEqual(self.calls()[-1], ("POST", "/androidpublisher/v3/applications/in.geekofia.morsekit/edits/{id}:commit"))

    def test_dry_run_validates_and_discards_instead_of_committing(self):
        aab, _, _ = self.files()
        self.run_play("upload", "--aab", aab, "--release-name", "x", "--dry-run")
        tail = [c[1].rsplit("/", 1)[-1] for c in self.calls()[-2:]]
        self.assertEqual(tail, ["{id}:validate", "{id}"])
        self.assertEqual(self.calls()[-1][0], "DELETE")
        self.assertNotIn("commit", str(self.calls()))

    def test_promote_copies_the_newest_closed_test_release_to_production(self):
        notes = [{"language": "en-US", "text": "New stuff"}]
        FakePlay.state["tracks"]["alpha"] = [
            {"name": "1.0.0 (4)", "versionCodes": ["4"], "status": "completed"},
            {"name": "1.0.1 (6)", "versionCodes": ["6"], "status": "completed", "releaseNotes": notes},
        ]
        self.run_play("promote")
        self.assertEqual(FakePlay.state["put"]["production"]["releases"], [{
            "name": "1.0.1 (6)", "versionCodes": ["6"], "status": "completed",
            "inAppUpdatePriority": 0, "releaseNotes": notes,
        }])
        self.assertIn("commit", self.calls()[-1][1])

    def test_promote_a_chosen_version_as_a_staged_urgent_rollout(self):
        FakePlay.state["tracks"]["alpha"] = [
            {"name": "a", "versionCodes": ["4"], "status": "completed"},
            {"name": "b", "versionCodes": ["6"], "status": "completed"},
        ]
        self.run_play("promote", "--version-code", "4", "--rollout", "20", "--priority", "4")
        self.assertEqual(FakePlay.state["put"]["production"]["releases"], [{
            "name": "a", "versionCodes": ["4"], "status": "inProgress", "userFraction": 0.2,
            "inAppUpdatePriority": 4,
        }])

    def test_promote_fails_clearly_and_discards_the_edit(self):
        FakePlay.state["tracks"]["alpha"] = [{"name": "a", "versionCodes": ["4"], "status": "completed"}]
        for args, message in (
            (["--version-code", "9"], "no version code 9"),
            (["--rollout", "0"], "--rollout must be above 0"),
            (["--priority", "6"], "--priority must be 0-5"),
        ):
            FakePlay.state["calls"].clear()
            with self.assertRaisesRegex(self.play.PlayError, message):
                self.run_play("promote", *args)
            self.assertEqual(self.calls()[-1][0], "DELETE", args)
            self.assertNotIn("production", FakePlay.state["put"])

    def test_api_errors_carry_googles_message(self):
        aab, _, _ = self.files()
        FakePlay.state["fail_on"] = "/bundles"
        with self.assertRaisesRegex(self.play.PlayError, r"HTTP 403: The caller does not have permission"):
            self.run_play("upload", "--aab", aab, "--release-name", "x")
        self.assertEqual(self.calls()[-1][0], "DELETE")

    def test_missing_or_invalid_credentials(self):
        os.environ["PLAY_SERVICE_ACCOUNT_JSON"] = ""
        with self.assertRaisesRegex(self.play.PlayError, "is not set"):
            self.run_play("promote")
        os.environ["PLAY_SERVICE_ACCOUNT_JSON"] = "{not json"
        with self.assertRaisesRegex(self.play.PlayError, "not a service-account key"):
            self.run_play("promote")


if __name__ == "__main__":
    unittest.main()
