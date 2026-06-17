package com.ourbreak.ecs.systems;

import java.util.prefs.Preferences;

/**
 * The player's best survival result (highest round reached), persisted across runs via the platform
 * {@link Preferences} store. Used to reframe a game-over as an achievement — a new record is celebrated
 * rather than just a loss (the peak-end rule: end the session on a high note).
 */
public final class HighScore {

    private static final Preferences PREFS = Preferences.userRoot().node("com/ourbreak");
    private static final String KEY = "bestRound";

    private HighScore() {
    }

    /** The best round reached so far (0 if none yet). */
    public static int best() {
        return PREFS.getInt(KEY, 0);
    }

    /** Records a finished run; stores it and returns true when it beats the previous best. */
    public static boolean submit(int round) {
        if (round > best()) {
            PREFS.putInt(KEY, round);
            return true;
        }
        return false;
    }
}
