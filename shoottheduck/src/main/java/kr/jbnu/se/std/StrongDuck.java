package kr.jbnu.se.std;

import java.awt.image.BufferedImage;

public class StrongDuck extends Duck {
    private boolean dodged; // 사격 회피 여부

    public StrongDuck(int x, int y, int speed, int points, BufferedImage image) {
        super(x, y, speed, points, image);
        this.dodged = false; // 회피 상태 초기화
    }

    @Override
    public void Update() {
        super.Update(); // 기본 오리 업데이트 호출

        // 10% 확률로 회피
        if (!dodged && Math.random() < 0.1) {
            dodged = true; // 회피 상태로 변경
        }
    }

    public boolean hasDodged() {
        return dodged; // 회피 여부 반환
    }
}
