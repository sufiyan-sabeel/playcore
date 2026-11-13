package app.playcore.core.model

/**
 * Content whose existence is verified from the reference APK string table
 * (gsx_pn_*, gsx_bd_*, gsx_help_*, gs_hub_card_*). Names are the reference names.
 * Descriptions are written for PLAYCORE, not copied from upstream copy.
 */
object ReferenceContent {

    val plugins: List<Plugin> = listOf(
        Plugin("aux_line", "Auxiliary Line", "Draw range rings around your hero.", false),
        Plugin("card_draw", "Card Draw Assist", "Manual draw counter saved per game.", false),
        Plugin("data_panel", "Data Panel", "Draggable FPS and session statistics panel.", false),
        Plugin("double_tap", "Double Tap Assist", "Explicit double-tap action at a chosen position.", false),
        Plugin("fan", "Fan Control", "Detect and control supported kernel fan nodes.", true),
        Plugin("free_display", "Free to Display", "Restart a game with a custom display ratio.", false),
        Plugin("gyro_manipulation", "Gyro Manipulation", "Map device rotations to game taps.", true),
        Plugin("press_hold", "Press and Hold Assist", "Explicit long-press action at a chosen position.", false),
        Plugin("hunt", "Hunt Mode", "Hunting filters with adjustable intensity.", true),
        Plugin("keymap", "Keymap", "Remap hardware keys and triggers to on-screen buttons.", false),
        Plugin("touch_indicator", "Touch Indicator", "Show a dot where you touch the screen.", true),
        Plugin("macro", "Macro Recorder", "Record and replay a touch sequence, with speed control.", false),
        Plugin("mtp", "MTP · Motivation to Play", "Floating dialog launcher with custom voices.", false),
        Plugin("picture_quality", "Superior Pic Quality", "Screen colour presets through the saturation backend.", true),
        Plugin("scout", "Scout Mode", "Zoom the game view using Android magnification.", false),
        Plugin("screen_extraction", "Screen Extraction", "Select a game region and view periodic captures.", false),
        Plugin("sensitivity_wheel", "High-Sensitivity Wheels", "Resizable wheel over the game joystick.", false),
        Plugin("stopwatch", "Fast Stopwatch", "Draggable stopwatch for timing cooldowns.", false),
        Plugin("touch_data", "Data Panel (CPS/MPM)", "Live touch rate: clicks per second and moves per minute.", false),
        Plugin("xgravity", "Gravity X", "Map controller buttons to on-screen touch zones.", false),
    )

    val backdrops: List<BackdropOption> = listOf(
        BackdropOption("starfield", "Starfield"),
        BackdropOption("aurora", "Aurora"),
        BackdropOption("carbon", "Carbon"),
        BackdropOption("circuit", "Circuit"),
        BackdropOption("grid", "Grid"),
        BackdropOption("hex", "Hex"),
        BackdropOption("matrix", "Matrix"),
        BackdropOption("neon", "Neon"),
        BackdropOption("reactor", "Reactor"),
        BackdropOption("vortex", "Vortex"),
    )

    val ledEffects: List<String> = listOf("Steady", "Breathe", "Pulse", "Flow", "Flashing", "Rainbow", "Aurora")

    val cubeModes: List<CubeMode> = listOf(CubeMode.BALANCE, CubeMode.RISE, CubeMode.ECO)

    val thermalStates: List<ThermalState> = listOf(ThermalState.COOL, ThermalState.WARM, ThermalState.HOT)

    val ranks: List<String> = listOf("Rookie", "Veteran", "Elite", "Master", "Legend")

    val genres: List<String> = listOf("All Games", "ACT", "FPS", "MOBA")

    val helpTopics: List<HelpTopic> = listOf(
        HelpTopic(
            "What is PLAYCORE?",
            "A console for your games: a curated library, a device dashboard that reports only real " +
                "readings, per-game profiles, and an optional floating panel. Everything stays on the device.",
        ),
        HelpTopic(
            "Real and preview items",
            "Items marked READY work on this device right now. Items marked PREVIEW only store your " +
                "choice until the required permission or backend is available. PLAYCORE never shows a " +
                "control as working when it cannot act.",
        ),
        HelpTopic(
            "Per-game profiles",
            "Each game carries its own overlay style, panel cards, Do Not Disturb preference and " +
                "refresh-rate choice. Open a game's details to edit its profile.",
        ),
        HelpTopic(
            "Working modes",
            "Basic access needs no privileged backend. Shizuku runs shell commands under the service " +
                "UID. Root exposes kernel interfaces your device actually has. PLAYCORE detects which " +
                "of these exist and reports them instead of assuming.",
        ),
        HelpTopic(
            "CPU modes: Balance, Rise, Eco",
            "These map to governor and frequency limits. Writing them needs Root or Shizuku; without " +
                "that access PLAYCORE stores the choice and says so.",
        ),
        HelpTopic(
            "Custom Mode and Tools",
            "Custom Mode edits CPU and GPU limits per mode. Tools such as the task manager and album " +
                "arrive in later phases; they are listed as planned rather than shown as working.",
        ),
        HelpTopic(
            "Handheld or Explore",
            "Handheld is the landscape console lobby. Explore is the everyday portrait interface. " +
                "Both read the same library and the same profiles.",
        ),
    )

    /** Verified Energy Cube card names, in reference order. */
    val cubeCards: List<CubeCard> = CubeCard.entries

    fun cardLabel(card: CubeCard): String = when (card) {
        CubeCard.COUNTER -> "Counter"
        CubeCard.CROSSHAIR -> "Crosshair"
        CubeCard.DOCK_SIDE -> "Dock side"
        CubeCard.PERFORMANCE -> "Performance"
        CubeCard.EQUALIZER -> "Equalizer"
        CubeCard.GAMEPAD -> "Gamepad"
        CubeCard.INFO -> "Info"
        CubeCard.PROJECTION -> "Projection"
        CubeCard.ROTATION_LOCK -> "Rotation lock"
        CubeCard.SCREEN_REC -> "Screen rec"
        CubeCard.SCREENSHOT -> "Screenshot"
        CubeCard.SUPER_SNAP -> "Super snap"
        CubeCard.STOPWATCH -> "Stopwatch"
        CubeCard.WIFI -> "WiFi"
        CubeCard.DND -> "DND"
        CubeCard.BYPASS -> "Bypass"
        CubeCard.BRIGHT_LOCK -> "Bright lock"
        CubeCard.NO_BANNER -> "No banner"
        CubeCard.OPTIMIZE -> "Optimize"
    }

    fun cardNote(card: CubeCard): String = when (card) {
        CubeCard.CROSSHAIR, CubeCard.STOPWATCH, CubeCard.SCREENSHOT, CubeCard.DOCK_SIDE ->
            "Works with display-over-other-apps permission"
        CubeCard.DND, CubeCard.BRIGHT_LOCK, CubeCard.ROTATION_LOCK ->
            "Writes a system setting; needs DND or write access"
        CubeCard.SCREEN_REC, CubeCard.PROJECTION, CubeCard.SUPER_SNAP ->
            "Asks for screen-capture consent per session"
        CubeCard.BYPASS, CubeCard.OPTIMIZE, CubeCard.PERFORMANCE ->
            "Needs Root or Shizuku; the device must expose the interface"
        CubeCard.EQUALIZER, CubeCard.GAMEPAD, CubeCard.INFO, CubeCard.COUNTER,
        CubeCard.WIFI, CubeCard.NO_BANNER -> "Reads state only; no privileged access needed"
    }
}
