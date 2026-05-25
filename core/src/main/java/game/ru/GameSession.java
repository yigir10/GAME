package game.ru;

public class GameSession {
    public int distance = 0;
    public int coins = 0;
    public boolean isGameOver = false;
    public boolean usedRevive = false;

    // Комбо-система
    public int combo = 0;
    public float comboTimer = 0;
    public final float COMBO_DURATION = 2.0f;

    public void addDistance(int value) {
        distance += value;
    }

    public void addCoin(int value) {
        coins += value;
        combo++;
        comboTimer = COMBO_DURATION;
    }

    public void update(float delta) {
        if (comboTimer > 0) {
            comboTimer -= delta;
            if (comboTimer <= 0) {
                combo = 0;
            }
        }
    }
}
