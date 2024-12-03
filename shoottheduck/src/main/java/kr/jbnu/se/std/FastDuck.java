package kr.jbnu.se.std;

import java.awt.image.BufferedImage;

public class FastDuck extends Duck {
    public FastDuck(int x, int y, double speed, int line, BufferedImage fastDuckImg) {
        super(x, y, speed * 2, line, fastDuckImg); // 오리의 속도를 2배로 설정
    }
}
