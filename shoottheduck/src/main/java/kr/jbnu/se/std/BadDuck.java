package kr.jbnu.se.std;

import java.awt.image.BufferedImage;

public class BadDuck extends Duck {
    public BadDuck(int x, int y, double speedX, int speedY, BufferedImage badDuckImg) {
        super(x, y, speedX, speedY, badDuckImg);
        this.score = -50; // 플레이어가 쏘면 -50점을 잃음
    }
}