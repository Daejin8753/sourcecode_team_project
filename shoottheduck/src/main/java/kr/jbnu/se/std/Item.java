package kr.jbnu.se.std;

import java.awt.*;
import java.awt.image.BufferedImage;

public class Item {
    public int x, y;
    public BufferedImage itemImg;
    public boolean isCollected;
    public long spawnTime;

    // 생성자
    public Item(int x, int y, BufferedImage itemImg) {
        this.x = x;
        this.y = y;
        this.itemImg = itemImg;
        this.isCollected = false; // 처음엔 수집되지 않음
    }

    // 아이템 그리기
    public void Draw(Graphics2D g2d) {
        if (!isCollected) {
            g2d.drawImage(itemImg, x, y, null);
        }
    }

    // 플레이어가 아이템을 획득했는지 체크
    public boolean checkCollision(Point mousePosition) {
        return new Rectangle(x, y, itemImg.getWidth(), itemImg.getHeight()).contains(mousePosition);
    }

    // 아이템을 수집했을 때 호출
    public void collect() {
        isCollected = true;
    }
}
