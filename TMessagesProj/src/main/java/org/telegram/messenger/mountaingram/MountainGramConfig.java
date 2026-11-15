package org.telegram.messenger.mountaingram;

import android.content.Context;
import android.content.SharedPreferences;
import org.telegram.messenger.ApplicationLoader;

public class MountainGramConfig {

    private static volatile MountainGramConfig instance;
    private final SharedPreferences prefs;

    // Ghost Mode
    public static final String KEY_GHOST_NO_READ      = "ghost_no_read";
    public static final String KEY_GHOST_NO_TYPING    = "ghost_no_typing";
    public static final String KEY_GHOST_NO_ONLINE    = "ghost_no_online";

    // История
    public static final String KEY_SAVE_DELETED       = "save_deleted_messages";
    public static final String KEY_SAVE_EDITS         = "save_edit_history";

    private MountainGramConfig() {
        prefs = ApplicationLoader.applicationContext
                .getSharedPreferences("mountaingram_config", Context.MODE_PRIVATE);
    }

    public static MountainGramConfig getInstance() {
        if (instance == null) {
            synchronized (MountainGramConfig.class) {
                if (instance == null) instance = new MountainGramConfig();
            }
        }
        return instance;
    }

    public boolean isGhostNoRead()   { return prefs.getBoolean(KEY_GHOST_NO_READ,   false); }
    public boolean isGhostNoTyping() { return prefs.getBoolean(KEY_GHOST_NO_TYPING, false); }
    public boolean isGhostNoOnline() { return prefs.getBoolean(KEY_GHOST_NO_ONLINE, false); }
    public boolean isSaveDeleted()   { return prefs.getBoolean(KEY_SAVE_DELETED,    false); }
    public boolean isSaveEdits()     { return prefs.getBoolean(KEY_SAVE_EDITS,      false); }

    public void set(String key, boolean value) {
        prefs.edit().putBoolean(key, value).apply();
    }
}
