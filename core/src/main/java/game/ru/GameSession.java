package game.ru;

public class GameSession {
    public int score = 0;
    public boolean isGameOver = false;

    public void addScore(int value) {
        score += value;
    }
}
