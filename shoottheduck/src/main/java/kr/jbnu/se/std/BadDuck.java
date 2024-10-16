package kr.jbnu.se.std;

import java.awt.image.BufferedImage;

public class BadDuck extends Duck {
    public BadDuck(int x, int y, int speedX, int speedY, BufferedImage badDuckImg) {
        super(x, y, speedX, speedY, badDuckImg);
        this.score = -50; // 플레이어가 쏘면 -50점을 잃음 } @Override public void Draw(Graphics2D g2d) { super.Draw(g2d); // 기본 오리와 같은 방식으로 그리기 } @Override public void Update() { super.Update(); // 기본 오리와 같은 방식으로 업데이트 }
    }
}