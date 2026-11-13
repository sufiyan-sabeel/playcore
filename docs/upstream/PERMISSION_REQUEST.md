# Permission and source request — ZKMNEXT GameSpace 1.2.9

**Status: prepared, not sent.** Nothing below has been transmitted. Send it yourself, or tell me to send
it and I will say plainly when it has gone.

## Where to send it

| Route | Address | Notes |
| --- | --- | --- |
| Email | `zuanvfx01github@gmail.com` | Public in the git history as the author of all 23 commits. Best route for a formal request with attachments. |
| Telegram (official) | `t.me/ZUANVFXPROJECT3` | Named in the README as the official channel. Use the short version, then offer to email the detail. |
| GitHub issue | `github.com/zuanvfx02/zkmnext-gamespace/issues` | Four open issues, zero maintainer replies. Use it only to create a dated public record. |


## The four permissions, kept separate

The request deliberately asks for four different things. A "yes" to one is not a "yes" to another, and
the answer needs to be read that way.

| # | Permission | Question in the letter | If granted | If refused |
| --- | --- | --- | --- | --- |
| 1 | Redistribute the **unchanged** APK | Q5 | The prepared mirror release can be published, labelled unofficial | No mirror; the APK stays a local verification copy only |
| 2 | **Access to** the corresponding source | Q1, Q3, Q7 | Source review, and a basis for real reuse once the licence is settled | No source review; PLAYCORE stays clean-room |
| 3 | **Modify, rebrand and distribute** a derivative | Q6 | PLAYCORE could be built on upstream code, under whatever licence the author requires | PLAYCORE stays an independent implementation |
| 4 | **Use of names, logos and trademarks** | Q8 | PLAYCORE could reference the original by name in certain ways | PLAYCORE refers to it only as a factual reference, with no marks |

Note on item 4: PLAYCORE does not need the marks — it has its own name, icon and artwork, and the request
says so. The question is asked so the answer is on record, not because PLAYCORE wants to use ZEA, the ZKM
or ZKMNEXT wordmarks, or the character art. A refusal here changes nothing about PLAYCORE's own identity.

## Subject

Permission, source and licence clarification — ZKMNEXT GameSpace v1.2.9

## Body

Hello,

I am writing about ZKMNEXT GameSpace v1.2.9, published at
https://github.com/zuanvfx02/zkmnext-gamespace/releases/tag/1.2.9 (asset `app-release.apk`,
18,881,081 bytes, SHA-256 `73861d7a…c30e1`).

I am building an independent Android gaming launcher called PLAYCORE (Kotlin, Jetpack Compose, Room,
DataStore, minSdk 29), and GameSpace is its reference. Before I go further I need your answer on
licensing, because the repository's history leaves the position unclear and I do not want to guess.

What I have observed, factually:

1. The initial commit `505219d` on 21 October 2026 included a `LICENSE` file of 674 lines — the GNU
   General Public License, version 3.
2. Commit `f9581ae` on 29 October 2026, titled "Delete LICENSE", removed that file.
3. The first APK, v1.0.0, was published on 31 October 2026 — two days after the deletion.
4. The README still states that the project "contains GPL-3.0-or-later notices and a GPLv3 license",
   and that "corresponding-source obligations remain in force".
5. No tag in the repository contains a `LICENSE` file, and no corresponding source has been
   published for any release.

I would be grateful for answers to these seven questions:

1. **Which licence governs the v1.2.9 binary** and the components inside it?
2. **Why was the GPLv3 LICENSE deleted on 29 October 2026**, and was that intended to change the
   licensing of code or binaries already or subsequently distributed?
3. **Where is the corresponding source for v1.2.9**, or if there is none, what is the intended route
   for someone who received the binary to obtain it?
4. **What licence covers the five native libraries** (`libgamespace_core`, `libgamespace_daemon`,
   `libgamespace_touch`, `libzkm_engine`, `libzkmintegrity`) and the third-party components bundled in
   the APK?
5. **May I redistribute the unchanged v1.2.9 APK** in a separate repository, with full attribution, a
   copy of the licence text, the SHA-256 and the signing certificate fingerprint, and a clear statement
   that it is not an official ZKMNEXT channel? If yes, on what conditions?
6. **May I modify, rebrand and distribute a derivative application** under the name PLAYCORE, with its
   own name, icon and artwork, no ZEA character, no ZKM or ZKMNEXT marks, and all upstream notices
   preserved? If yes, under which licence would you require the derivative to be released?
7. **Is the GPL-3.0 grant on your other repository**, `zuan-kernel-manager-next` — specifically its
   `feature/gamespace/` package, whose files carry "Copyright (c) 2026 ZKM / GPL-3.0-or-later" —
   intended to extend to reuse of that code in a rebranded application?
8. **Do you permit any use of the ZKMNEXT and ZKM names, the GameSpace name, or the ZEA character and
   artwork** in a third-party application — for example a factual "based on ZKMNEXT GameSpace" credit, or
   a compatibility statement? PLAYCORE does not need them and will not use them without written
   permission; I am asking so the position is recorded either way.

To be clear about what I am not asking for: PLAYCORE will not carry the ZEA character, the ZKM or ZKMNEXT
names and wordmarks, the character artwork, or the Oxanium-derived logo treatment. It has its own name,
icon and artwork and will keep them separate. Question 8 exists only to put your position on record.

If the answer is "no licence" or "no permission", I will keep PLAYCORE as a clean-room independent
implementation and will not reuse any of your code or assets. If the answer is "GPL-3.0", I will comply
in full: keep every notice, publish PLAYCORE's corresponding source, and document the origin of every
reused file.

A short reply to questions 1, 5 and 6 is enough for me to proceed. Thank you for the documentation and
release notes — they have been genuinely useful.

Kind regards,
[your name]
[PLAYCORE repository link]

## Short version for Telegram

Hello — I maintain an independent Android game launcher (PLAYCORE, Kotlin/Compose) and ZKMNEXT
GameSpace v1.2.9 is my reference. I have three licensing questions and I do not want to guess:

1. Which licence governs the v1.2.9 APK? The repo had a GPLv3 LICENSE from 21 Oct, it was deleted on
   29 Oct ("Delete LICENSE"), and the first APK shipped on 31 Oct. Where is the corresponding source?
2. May I redistribute the unchanged 1.2.9 APK in my own repository, with attribution, the licence text
   and the SHA-256, marked as an unofficial mirror?
3. May I rebrand and distribute a derivative as PLAYCORE (own name, icon, artwork; no ZEA, no ZKM
   marks; all notices kept)? If yes, under which licence?

If the answer is no licence, I will stay clean-room and use none of your code. Happy to send the full
written request by email if that is easier — zuanvfx01github@gmail.com is the address I found in the
commit history. Thank you.
