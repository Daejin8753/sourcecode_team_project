package kr.jbnu.se.std;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

/**
 * 오리 클래스.
 *
 * @author www.gametutorial.net
 */

public class Duck {

    /**
     * 새로운 오리를 생성하기 위해 지나야 하는 시간.
     * 게임 난이도에 따라 동적으로 변경되기에, SonarLint의 경고를 무시함.
     */
    public static long timeBetweenDucks = Framework.SEC_IN_NANOSEC / 2;



    /**
     * kr.jbnu.se.std.Duck 라인.
     * 오리의 시작 위치는 어디인가?
     * 오리의 속도는?
     * 오리가 가치 있는 점수는 얼마인가?
     */
    protected static int[][] duckLines = {
            {Framework.FRAME_WIDTH, (int)(Framework.FRAME_HEIGHT * 0.60), -2, 20},
            {Framework.FRAME_WIDTH, (int)(Framework.FRAME_HEIGHT * 0.65), -3, 30},
            {Framework.FRAME_WIDTH, (int)(Framework.FRAME_HEIGHT * 0.70), -4, 40},
            {Framework.FRAME_WIDTH, (int)(Framework.FRAME_HEIGHT * 0.78), -5, 50}
    };
    /**
     * 다음 오리 라인을 나타냄.
     * 오리가 생성될 때마다 동적으로 업데이트되므로 SonarLint의 경고를 무시함.
     */
    static int nextDuckLines = 0;

    public static int getNextDuckLines() {
        return nextDuckLines;
    }

    public static void resetNextDuckLines() {
        nextDuckLines = 0;
    }

    public static void increseNextDuckLines(int lineCount) {
        nextDuckLines = (nextDuckLines + 1) % lineCount;
    }


    /**
     * 마지막으로 오리가 생성된 시간.
     */
    static long lastDuckTime;

    public static void resetLastDuckTime() {
        lastDuckTime = 0;
    }

    public static long getLastDuckTime() {
        return lastDuckTime;
    }

    public static void updateLastDuckTime(long newTime) {
        lastDuckTime = newTime;
    }

    /**
     * 오리의 X 좌표.
     */
    public int x;
    /**
     * 오리의 Y 좌표.
     */
    public int y;

    /**
     * 오리가 어느 방향으로 얼마나 빨리 이동해야 하는가?
     */
    private double speed;

    /**
     * 이 오리가 가치 있는 점수는 얼마인가?
     */
    public int score;

    /**
     * kr.jbnu.se.std.Duck 이미지.
     */
    protected BufferedImage duckImg;


    /**
     * 새로운 오리를 생성함.
     *
     * @param x 시작 x 좌표.
     * @param y 시작 y 좌표.
     * @param speed 이 오리의 속도.
     * @param score 이 오리가 가치 있는 점수.
     * @param duckImg 오리 이미지.
     */
    public Duck(int x, int y, double speed, int score, BufferedImage duckImg) {
        this.x = x;
        this.y = y;

        this.speed = speed;

        this.duckImg = (duckImg != null) ? deepCopyBufferedImage(duckImg) : null;
    }

    private BufferedImage deepCopyBufferedImage(BufferedImage image) {
        BufferedImage copy = new BufferedImage(image.getWidth(), image.getHeight(), image.getType());
        Graphics2D g2d = copy.createGraphics();
        g2d.drawImage(image, 0, 0, null);
        g2d.dispose();
        return copy;
    }

    /**
     * 오리를 이동시킴.
     */
    public void update() {
        x += speed;
    }

    /**
     * 화면에 오리를 그림.
     * @param g2d Graphics2D
     */
    public void draw(Graphics2D g2d) {
        g2d.drawImage(duckImg, x, y, null);
    }
}
