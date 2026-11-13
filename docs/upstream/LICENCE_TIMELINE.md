# Licence timeline and status — ZKMNEXT GameSpace

Factual record from the cloned repository (`8d183e8`), the GitHub API and the published APK. Where the
evidence does not settle a question, it is listed as unresolved rather than decided.

## 1. Timeline

| Date (2026, +0700) | Event | Evidence |
| --- | --- | --- |
| 21 Oct, 18:36 | Initial commit `505219d` — `.gitignore`, `README.md` (2 lines), **`LICENSE` 674 lines, GNU GPL v3** | `git show 505219d --stat` |
| 21 Oct | `32c331d` revises README; a GPL-3.0 badge appears | `git show 32c331d:README.md` |
| 29 Oct, 19:36 | `f9581ae` "Delete LICENSE" — 674 deletions | `git show f9581ae --stat` |
| 31 Oct | `911e6ff` revises README and **adds the "Source availability and licensing" section** asserting GPL-3.0-or-later notices and corresponding-source obligations; tag `1.0.0` points here | `git show 911e6ff:README.md` |
| 31 Oct | **v1.0.0 APK published** — first binary, two days after the licence was deleted | GitHub releases API |
| 3 Nov | v1.1.0 APK published (same tag commit `911e6ff`) | releases API |
| 7 Nov | `8bb5848` README update; tags `1.2.0`, `1.2.1`, `1.2.9` all point here | `git rev-parse` |
| 8 Nov | v1.2.1 APK published | releases API |
| 9 Nov | **v1.2.9 APK published** (the verified release) | releases API |
| 10 Nov | `f9f3c46` README revision | git log |
| 12 Nov | `e46b784`, then `8d183e8` "Add files via upload" — current HEAD | git log |

No tag contains a `LICENSE` file. The only licence-bearing paths ever tracked are `LICENSE` (deleted),
`docs/licenses/Oxanium-OFL.txt` (SIL OFL 1.1, the typeface) and a placeholder.

## 2. Which terms apply where

| Scope | Position on the evidence |
| --- | --- |
| Commits 21–29 Oct (while LICENSE existed) | A GPLv3 licence file was present in the repository root. Anyone who received that code during that window received it under GPLv3. |
| Commits from 29 Oct onward | No licence file in the tree. The README continued to assert GPL-3.0-or-later, and from 31 Oct asserted it in stronger terms, including the corresponding-source obligation. |
| The v1.2.9 release | Shipped from a tree with no LICENSE file, under a README that claims the project "contains GPL-3.0-or-later notices and a GPLv3 license" — which is not true of the tree that was published. The APK itself carries no GPL notice: only AndroidX Apache-2.0 files under `META-INF`. |
| Copyright ownership | No explicit copyright statement exists in the repository. The only named party is the commit author, `ZUANVFX02 <zuanvfx01github@gmail.com>`. Ownership is therefore presumptively the author's, but nothing in the repository states it. |
| Third-party components | AndroidX, Kotlin, Compose, Room, DataStore, WorkManager, Coil and Gson are Apache-2.0 with their notices in `META-INF`; the Oxanium typeface is SIL OFL 1.1. The five native libraries (`libgamespace_core`, `libgamespace_daemon`, `libgamespace_touch`, `libzkm_engine`, `libzkmintegrity`) carry no licence statement and no source. |

## 3. GPL corresponding-source obligation, stated precisely

GPL-3.0 section 6 permits conveying object code only if it is accompanied by one of: the corresponding
source; a written offer, valid for at least three years, to supply it; or an offer relayed from a party
entitled to give one. Section 6 also requires the licence text to travel with the work, and section 5
requires notices to be kept.

Applied here: if the project is GPL-3.0-or-later as the README states, then a third party who conveys
the APK must satisfy one of those three conditions. No corresponding source is published, so the only
realistic route is the copyright holder publishing it or granting written permission. If instead the
project is unlicensed, there is no permission to convey at all.

Deleting the licence file does not revoke rights already granted to recipients of the earlier code, but
those rights still carry the source obligation and do not create a right to ignore it.

## 4. Unresolved questions

1. Which licence governs the v1.2.9 binary and its native libraries.
2. Whether the deletion of `LICENSE` was intended to withdraw GPL terms, and whether that is effective
   for binaries distributed afterwards.
3. Where the corresponding source is, or will be, published.
4. Whether the ZEA character and the ZKM/ZKMNEXT marks are protected in a way that constrains a rebrand.
5. Whether the GPL header on `zuan-kernel-manager-next`'s `feature/gamespace/` package extends to reuse
   of that code in a different application.
6. Who owns copyright in contributions, if any were made outside the maintainer's account.

None of these can be settled from the repository. They are questions for the copyright holder, and
questions 2, 4 and 5 would benefit from a lawyer's view before money or a store listing is involved.

## 5. PLAYCORE licence compatibility

| Direction | Permitted? | Basis |
| --- | --- | --- |
| Apache-2.0 code inside a GPL-3.0 project | Yes | Apache-2.0 is compatible with GPLv3; the combined work is distributed under GPLv3 with Apache notices kept |
| GPL-3.0 code inside an Apache-2.0 project | No | GPLv3 adds conditions Apache-2.0 does not permit; the combined work would have to be GPLv3 |
| Relicensing PLAYCORE's own code from Apache-2.0 to GPL-3.0 | Yes, by the copyright holder | PLAYCORE's code is authored here; its dependencies are all Apache-2.0 or OFL, which are compatible |

Consequence: PLAYCORE can stay Apache-2.0 and reuse nothing from upstream, or become GPL-3.0-or-later
and then be able to combine upstream GPL code with its existing Apache dependencies. There is no
configuration in which PLAYCORE imports upstream GPL code and keeps Apache-2.0.

**No source has been imported. PLAYCORE remains Apache-2.0 and contains no upstream code or assets.**
