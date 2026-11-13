# Source request message

Two versions: a full one for a GitHub issue or email, and a short one for the Telegram channel
the README names as official.

## Full version (GitHub issue or email)

**Subject: Request for ZKMNEXT GameSpace source code and licence clarification**

Hello,

I am building an independent Android gaming launcher called PLAYCORE — Kotlin, Jetpack Compose,
Room, DataStore, minSdk 29 — and ZKMNEXT GameSpace is the reference for its structure and behaviour.
I am writing to ask for the development source and for a written statement of your licensing terms,
rather than continuing to work only from the published APK and documentation.

I have read the repository README, which states that the current development source is not publicly
published, and that the project contains GPL-3.0-or-later notices and a GPLv3 licence with
corresponding-source obligations. I also note that the repository itself contains no LICENSE file.
Because GPL-3.0 section 6 attaches the corresponding-source obligation to distribution of the binary,
I would like to ask directly:

1. Is ZKMNEXT GameSpace distributed under GPL-3.0-or-later, and if so, where is the corresponding
   source published?
2. Are the native libraries (libgamespace_core, libgamespace_daemon, libgamespace_touch,
   libzkm_engine, libzkmintegrity) covered by the same licence, or by other terms?
3. Would you be willing to provide, or grant access to:
   - the Android Studio / Gradle project and Kotlin sources;
   - the Jetpack Compose UI implementation;
   - the Room schema and database layer;
   - the service implementations (overlay, game detection, session, recording);
   - the plugin implementations (Keymap, Gravity X, Shoulder Triggers, Macro, Scout, MTP);
   - native C/C++ sources, where they can be shared;
   - build instructions and the dependency list.
4. Would you authorise a rebranded redistribution under the name PLAYCORE, keeping all copyright and
   licence notices intact and clearly stating that it is not an official ZKMNEXT product?

I understand that the ZEA character, the ZKM and ZKMNEXT names and wordmarks, and the character
artwork are your identity assets. I am not asking for those; PLAYCORE would use its own name, icon
and artwork.

I have also read your other public project, ZUAN KERNEL MANAGER NEXT, which is published under
GPL-3.0 and contains a `feature/gamespace/` package (Energy Cube, Super Base, Handheld Mecha,
CPU mode engine, overlay services). If GameSpace itself stays private, may I treat that GPL-3.0 code
as the licensed basis for the gaming layer, with attribution and with PLAYCORE's own source
published in turn?

If the answer to the licence question is "yes, GPL-3.0-or-later", I will comply with the full
obligations: keep every notice, publish PLAYCORE's corresponding source, and document the origin of
every reused file. If the answer is "no" or there is no licence, I will keep PLAYCORE as a clean-room
independent implementation and will not reuse any of your code or assets.

A short answer to questions 1 and 4 is all I need to decide the direction. Thank you for your time
and for publishing the documentation and release notes, which have been genuinely useful.

Kind regards,
[your name]
[PLAYCORE repository link]

## Short version (Telegram)

Hello — I am building an independent Android game launcher (PLAYCORE, Kotlin/Compose) and ZKMNEXT
GameSpace is my reference. Two questions, both about licensing:

1. Is GameSpace released under GPL-3.0-or-later? The README says so, but there is no LICENSE file in
   the repo and no public source — where can the corresponding source be obtained?
2. May I reuse the GPL-3.0 code from ZUAN KERNEL MANAGER NEXT (the `feature/gamespace/` package:
   Energy Cube, Super Base, Handheld Mecha, CPU mode engine) as the licensed basis for PLAYCORE's
   gaming layer, with attribution and with PLAYCORE's source published under GPL-3.0 as well?

I am not asking for the ZEA character or the ZKM/ZKMNEXT branding — PLAYCORE has its own name and
artwork. If the answer is no licence, I will keep it clean-room and use none of your code.

Thank you.
