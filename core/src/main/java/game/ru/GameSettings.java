package game.ru;

public class GameSettings {
    public static final int SCREEN_WIDTH = 720;
    public static final int SCREEN_HEIGHT = 1280;
    public static final float STEP_TIME = 1f / 60;
    public static final int VELOCITY_ITERATIONS = 6;
    public static final int POSITION_ITERATIONS = 6;
    public static final float SCALE = 0.05f;

    // Параметры персонажа
    public static final int JETPACK_HEIGHT = 180; // Сделал чуть меньше
    public static final int JETPACK_WIDTH = 120;
    public static final short JETPACK_BIT = 2;

    public static final int BULLET_HEIGHT = 40;
    public static final int BULLET_WIDTH = 40;
    public static final short BULLET_BIT = 4;
    public static final float BULLET_VELOCITY = 20f;

    // Физика
    public static final float GRAVITY = -50f;     // Усилил гравитацию
    public static final float JUMP_FORCE = 1.5f;  // Увеличил силу прыжка для компенсации

    public static final float GAME_SPEED = 300f;  // Немного ускорил темп игры

    public static final int LASER_HEIGHT = 250;
    public static final int LASER_WIDTH = 50;
    public static final short LASER_BIT = 8;

    public static final short BOUNDS_BIT = 1;
}
