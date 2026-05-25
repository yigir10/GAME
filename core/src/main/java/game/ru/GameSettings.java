package game.ru;

public class GameSettings {
    public static final int SCREEN_WIDTH = 720;
    public static final int SCREEN_HEIGHT = 1280;
    public static final float STEP_TIME = 1f / 60;
    public static final int VELOCITY_ITERATIONS = 6;
    public static final int POSITION_ITERATIONS = 6;
    public static final float SCALE = 0.05f;

    // Параметры персонажа
    public static final int JETPACK_HEIGHT = 180;
    public static final int JETPACK_WIDTH = 120;
    public static final short JETPACK_BIT = 2;

    // Физика
    public static final float GRAVITY = -50f;
    public static final float JUMP_FORCE = 1.5f;

    public static final float GAME_SPEED = 300f;

    // Интервалы появления (в секундах)
    public static final float LASER_SPAWN_INTERVAL = 2.0f;
    public static final float COIN_SPAWN_INTERVAL = 1.5f;
    public static final float ROCKET_SPAWN_INTERVAL = 5.0f;

    public static final int LASER_HEIGHT = 250;
    public static final int LASER_WIDTH = 50;
    public static final short LASER_BIT = 8;

    public static final int COIN_HEIGHT = 60;
    public static final int COIN_WIDTH = 60;
    public static final short COIN_BIT = 16;

    public static final short BOUNDS_BIT = 1;
}
