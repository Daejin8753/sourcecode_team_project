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
     */
    public static long timeBetweenDucks = Framework.secInNanosec / 2;
    /**
     * 마지막으로 오리가 생성된 시간.
     */
    public static long lastDuckTime = 0;

    public static long duckSpeed = 0;



    /**
     * kr.jbnu.se.std.Duck 라인.
     * 오리의 시작 위치는 어디인가?
     * 오리의 속도는?
     * 오리가 가치 있는 점수는 얼마인가?
     */
    public static int[][] duckLines = {
            {Framework.frameWidth, (int)(Framework.frameHeight * 0.60), -2, 20},
            {Framework.frameWidth, (int)(Framework.frameHeight * 0.65), -3, 30},
            {Framework.frameWidth, (int)(Framework.frameHeight * 0.70), -4, 40},
            {Framework.frameWidth, (int)(Framework.frameHeight * 0.78), -5, 50}
    };
    /**
     * 다음 오리 라인을 나타냄.
     */
    public static int nextDuckLines = 0;


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
    public Duck(int x, int y, double speed, int score, BufferedImage duckImg)
    {
        this.x = x;
        this.y = y;

        this.speed = speed;

        this.score = score;

        this.duckImg = duckImg;
    }


    /**
     * 오리를 이동시킴.
     */
    public void Update() {
        x += speed;
    }

    /**
     * 화면에 오리를 그림.
     * @param g2d Graphics2D
     */
    public void Draw(Graphics2D g2d) {
        g2d.drawImage(duckImg, x, y, null);
    }
}
