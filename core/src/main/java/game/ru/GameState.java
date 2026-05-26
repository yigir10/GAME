package game.ru;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;

public class GameState {
    private static final String PREFS_NAME = "game_prefs";
    private static final String KEY_COINS = "total_coins";
    private static final String KEY_JUMP_LEVEL = "jump_level";
    private static final String KEY_COIN_LEVEL = "coin_level";
    private static final String KEY_MAGNET_LEVEL = "magnet_level";
    private static final String KEY_SHIELD_LEVEL = "shield_level";
    private static final String KEY_HIGH_SCORE = "high_score";
    private static final String KEY_SOUND_ON = "sound_on";
    private static final String KEY_MUSIC_ON = "music_on";
    private static final String KEY_MISSION_DISTANCE_TARGET = "m_dist_target";
    private static final String KEY_MISSION_COINS_TARGET = "m_coin_target";
    private static Preferences prefs;

    private static Preferences getPrefs() {
        if (prefs == null) {
            prefs = Gdx.app.getPreferences(PREFS_NAME);
        }
        return prefs;
    }

    public static void resetProgress() {
        getPrefs().clear();
        getPrefs().flush();
    }

    public static boolean isSoundOn() { return getPrefs().getBoolean(KEY_SOUND_ON, true); }
    public static void setSoundOn(boolean on) { getPrefs().putBoolean(KEY_SOUND_ON, on); getPrefs().flush(); }

    public static boolean isMusicOn() { return getPrefs().getBoolean(KEY_MUSIC_ON, true); }
    public static void setMusicOn(boolean on) { getPrefs().putBoolean(KEY_MUSIC_ON, on); getPrefs().flush(); }

    public static int getTotalCoins() { return getPrefs().getInteger(KEY_COINS, 0); }
    public static void addCoins(int amount) { getPrefs().putInteger(KEY_COINS, getTotalCoins() + amount); getPrefs().flush(); }

    public static boolean spendCoins(int amount) {
        int current = getTotalCoins();
        if (current >= amount) {
            getPrefs().putInteger(KEY_COINS, current - amount);
            getPrefs().flush();
            return true;
        }
        return false;
    }

    public static int getHighScore() { return getPrefs().getInteger(KEY_HIGH_SCORE, 0); }
    public static void updateHighScore(int score) {
        if (score > getHighScore()) {
            getPrefs().putInteger(KEY_HIGH_SCORE, score);
            getPrefs().flush();
        }
    }

    public static int getDistanceTarget() { return getPrefs().getInteger(KEY_MISSION_DISTANCE_TARGET, 500); }
    public static int getCoinsTarget() { return getPrefs().getInteger(KEY_MISSION_COINS_TARGET, 50); }

    public static void completeDistanceMission() {
        getPrefs().putInteger(KEY_MISSION_DISTANCE_TARGET, getDistanceTarget() + 500);
        addCoins(100);
        getPrefs().flush();
    }

    public static void completeCoinsMission() {
        getPrefs().putInteger(KEY_MISSION_COINS_TARGET, getCoinsTarget() + 50);
        addCoins(100);
        getPrefs().flush();
    }

    public static int getUpgradeCost(int level, int baseCost) {
        return baseCost + (level * (baseCost / 2));
    }

    public static int getJumpLevel() { return getPrefs().getInteger(KEY_JUMP_LEVEL, 0); }
    public static void upgradeJump() { getPrefs().putInteger(KEY_JUMP_LEVEL, getJumpLevel() + 1); getPrefs().flush(); }

    public static int getCoinLevel() { return getPrefs().getInteger(KEY_COIN_LEVEL, 0); }
    public static void upgradeCoin() { getPrefs().putInteger(KEY_COIN_LEVEL, getCoinLevel() + 1); getPrefs().flush(); }

    public static int getMagnetLevel() { return getPrefs().getInteger(KEY_MAGNET_LEVEL, 0); }
    public static void upgradeMagnet() { getPrefs().putInteger(KEY_MAGNET_LEVEL, getMagnetLevel() + 1); getPrefs().flush(); }

    public static int getShieldLevel() { return getPrefs().getInteger(KEY_SHIELD_LEVEL, 0); }
    public static void upgradeShield() { getPrefs().putInteger(KEY_SHIELD_LEVEL, getShieldLevel() + 1); getPrefs().flush(); }
}
