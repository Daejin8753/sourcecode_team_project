package kr.jbnu.se.std;

import java.awt.*;
import java.awt.image.BufferedImage;

public class FastDuck extends Duck {
    public FastDuck(int x, int y, int speed, int line, BufferedImage duckImg) {
        super(x, y, speed * 2, line, duckImg); // 오리의 속도를 2배로 설정
    }

    @Override
    public void Draw(Graphics2D g2d) {
        g2d.drawImage(duckImg, x, y, null); // 일반 오리와 동일한 그리기 방식
    }
}
