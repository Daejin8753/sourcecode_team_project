package kr.jbnu.se.std;

import java.awt.*;
import java.awt.image.BufferedImage;

public class Item {
    private final int x, y;
    private final BufferedImage itemImg;
    private boolean isCollected;


    // 생성자
    public Item(int x, int y, BufferedImage itemImg) {
        this.x = x;
        this.y = y;
        this.itemImg = deepCopy(itemImg);
        this.isCollected = false; // 처음엔 수집되지 않음
    }

    // 아이템 그리기


    // 플레이어가 아이템을 획득했는지 체크
    public boolean checkCollision(Point mousePosition) {
        return new Rectangle(x, y, itemImg.getWidth(), itemImg.getHeight()).contains(mousePosition);
    }

    // 아이템을 수집했을 때 호출
    public void collect() {
        isCollected = true;
    }

    public boolean isCollected() {
        return isCollected;
    }

    private BufferedImage deepCopy(BufferedImage original) {
        BufferedImage copy = new BufferedImage(original.getWidth(), original.getHeight(), original.getType());
        Graphics g = copy.getGraphics();
        g.drawImage(original, 0, 0, null);
        g.dispose();
        return copy;
    }
}
