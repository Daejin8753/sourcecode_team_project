package kr.jbnu.se.std;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;

/**
 * 실제 게임 클래스.
 *
 * @author www.gametutorial.net
 */

public class Game {

    private BufferedImage badDuckImg;
    /**
     * 랜덤 숫자를 생성하기 위해 사용됨.
     */
    private Random random;

    /**
     * 화면에 통계를 표시하기 위해 사용할 폰트.
     */
    private Font font;

    /**
     * 오리들의 배열 리스트.
     */
    private ArrayList<Duck> ducks;

    /**
     * 화면을 떠나는 오리의 수.
     */
    private int runawayDucks;

    /**
     * 플레이어가 잡은 오리의 수.
     */
    private int killedDucks;

    /**
     * 플레이어가 오리를 잡을 때 얻는 점수.
     */
    private int score;

    /**
     * 플레이어가 쏜 횟수.
     */
    private int shoots;

    /**
     * 마지막으로 총을 쏜 시간.
     */
    private long lastTimeShoot;
    /**
     * 총을 쏘기 위해 경과해야 하는 시간.
     */
    private long timeBetweenShots;

    /**
     * kr.jbnu.se.std.Game 배경 이미지.
     */
    private BufferedImage backgroundImg;

    /**
     * 하단의 잔디 이미지.
     */
    private BufferedImage grassImg;

    /**
     * kr.jbnu.se.std.Duck 이미지.
     */
    private BufferedImage duckImg;

    /**
     * 샷건 조준경 이미지.
     */
    private BufferedImage sightImg;

    /**
     * 조준경 이미지의 중간 너비.
     */
    private int sightImgMiddleWidth;
    /**
     * 조준경 이미지의 중간 높이.
     */
    private int sightImgMiddleHeight;

    private int difficultyLevel; // 난이도 변수

    private final long[] duckSpawnTimes = {1000000000, 800000000, 600000000, 400000000, 200000000}; // 각 난이도별 오리 출현 시간 (나노초)

    private int killCount; // 잡은 오리 수

    private boolean isDoubleScoreActive; // 점수 2배 활성화 여부

    private int miss;

    private boolean isFeverTimeActive; // 피버타임 활성화 여부
    private long feverStartTime; // 피버타임 시작 시간
    private long feverDuration = 5000; // 피버타임 지속 시간 (밀리초 단위)

    public Game()
    {
        random = new Random();
        ducks = new ArrayList<>();
        LoadContent();

        Framework.gameState = Framework.GameState.GAME_CONTENT_LOADING;

        Thread threadForInitGame = new Thread() {
            @Override
            public void run(){
                // 게임을 위한 변수와 객체 설정.
                Initialize();
                // 게임 파일 로드 (이미지, 소리 등)
                LoadContent();

                Framework.gameState = Framework.GameState.PLAYING;
            }
        };
        threadForInitGame.start();
    }

    /**
     * 게임을 위한 변수와 객체 설정.
     */
    private void Initialize()
    {
        random = new Random();
        font = new Font("monospaced", Font.BOLD, 18);

        ducks = new ArrayList<Duck>();

        runawayDucks = 0;
        killedDucks = 0;
        score = 0;
        shoots = 0;
        miss = 0;
        killCount = 0; // 잡은 오리 수 초기화
        isDoubleScoreActive = false; // 점수 2배 비활성화
        lastTimeShoot = 0;
        timeBetweenShots = Framework.secInNanosec / 3;
        difficultyLevel = 0;
    }

    private void AdjustDuckSpawnTime() {
        Duck.timeBetweenDucks = duckSpawnTimes[difficultyLevel] * 2; // 현재 난이도에 따른 오리 출현 시간 설정
    }

    /**
     * 게임 파일 로드 - 이미지, 소리 등.
     */
    private void LoadContent()
    {
        try
        {
            URL backgroundImgUrl = this.getClass().getResource("/images/background.jpg");
            backgroundImg = ImageIO.read(backgroundImgUrl);

            URL grassImgUrl = this.getClass().getResource("/images/grass.png");
            grassImg = ImageIO.read(grassImgUrl);

            URL duckImgUrl = this.getClass().getResource("/images/duck.png");
            duckImg = ImageIO.read(duckImgUrl);

            URL sightImgUrl = this.getClass().getResource("/images/sight.png");

            duckImg = ImageIO.read(duckImgUrl);
            // BadDuck 이미지 로드
            URL badDuckImgUrl = this.getClass().getResource("/images/bad_duck.png");
            badDuckImg = ImageIO.read(badDuckImgUrl);

            sightImg = ImageIO.read(sightImgUrl);
            sightImgMiddleWidth = sightImg.getWidth() / 2;
            sightImgMiddleHeight = sightImg.getHeight() / 2;

        }
        catch (IOException ex) {
            Logger.getLogger(Game.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * 게임을 재시작함 - 몇 가지 변수를 재설정함.
     */
    public void RestartGame()
    {
        // 모든 오리를 리스트에서 제거함.
        ducks.clear();

        // 마지막 오리 생성 시간을 0으로 설정함.
        Duck.lastDuckTime = 0;

        runawayDucks = 0;
        killedDucks = 0;
        score = 0;
        shoots = 0;

        lastTimeShoot = 0;
    }

    private int getMaxDucksByDifficulty() {
        switch(difficultyLevel) {
            case 0: return 3;  // 쉬운 난이도에서 최대 3마리
            case 1: return 4;  // 중간 난이도에서 최대 4마리
            case 2: return 5;  // 어려운 난이도에서 최대 5마리
            case 3: return 6;  // 매우 어려운 난이도에서 최대 6마리
            default: return 5; // 기본적으로 5마리 제한
        }
    }

    /**
     * 게임 로직 업데이트.
     *
     * @param gameTime 게임의 현재 시간.
     * @param mousePosition 현재 마우스 위치.
     */
    public void UpdateGame(long gameTime, Point mousePosition)
    {
        AdjustDuckSpawnTime(); // 난이도에 따른 오리 출현 간격 조정

        if (killCount >= 5 && runawayDucks <= 0 && !isFeverTimeActive) {
            isFeverTimeActive = true;
            feverStartTime = System.nanoTime();
            timeBetweenShots = 0; // 사격 딜레이 없애기
        }

        // 피버타임 동안 오리 출현 빈도 조정
        if (isFeverTimeActive) {
            if (System.nanoTime() - Duck.lastDuckTime >= Duck.timeBetweenDucks / 2) {
                int direction = random.nextInt(2);
                int startX, speed;
                if (direction == 0) {
                    startX = Duck.duckLines[Duck.nextDuckLines][0] + random.nextInt(200);
                    speed = Duck.duckLines[Duck.nextDuckLines][2];
                } else {
                    startX = 0 - random.nextInt(200);
                    speed = -Duck.duckLines[Duck.nextDuckLines][2];
                }
                ducks.add(new Duck(startX, Duck.duckLines[Duck.nextDuckLines][1], speed, Duck.duckLines[Duck.nextDuckLines][3], duckImg));

                // 다음 라인으로 이동
                Duck.nextDuckLines++;
                if (Duck.nextDuckLines >= Duck.duckLines.length)
                    Duck.nextDuckLines = 0;

                Duck.lastDuckTime = System.nanoTime();
            }
        }
        else {
            if (ducks.size() < 5 && System.nanoTime() - Duck.lastDuckTime >= Duck.timeBetweenDucks) {
                int direction = random.nextInt(2);  // 0이면 오른쪽 -> 왼쪽, 1이면 왼쪽 -> 오른쪽
                int startX, speed;
                if (direction == 0) {  // 오른쪽에서 왼쪽으로 이동
                    startX = Duck.duckLines[Duck.nextDuckLines][0] + random.nextInt(200); // 기존과 동일하게 오른쪽에서 출발
                    speed = Duck.duckLines[Duck.nextDuckLines][2];  // 음수 속도 (왼쪽으로 이동)
                } else {  // 왼쪽에서 오른쪽으로 이동
                    startX = 0 - random.nextInt(200);  // 왼쪽에서 출발
                    speed = -Duck.duckLines[Duck.nextDuckLines][2];  // 양수 속도 (오른쪽으로 이동)
                }

                ducks.add(new Duck(startX, Duck.duckLines[Duck.nextDuckLines][1], speed, Duck.duckLines[Duck.nextDuckLines][3], duckImg));

                if (random.nextInt(10) < 8) {  // 80% 확률로 일반 오리 생성
                    ducks.add(new Duck(Duck.duckLines[Duck.nextDuckLines][0] + random.nextInt(200),
                            Duck.duckLines[Duck.nextDuckLines][1],
                            Duck.duckLines[Duck.nextDuckLines][2],
                            Duck.duckLines[Duck.nextDuckLines][3],
                            duckImg));
                } else {  // 20% 확률로 BadDuck 생성
                    ducks.add(new BadDuck(Duck.duckLines[Duck.nextDuckLines][0] + random.nextInt(200),
                            Duck.duckLines[Duck.nextDuckLines][1],
                            Duck.duckLines[Duck.nextDuckLines][2],
                            Duck.duckLines[Duck.nextDuckLines][3],
                            badDuckImg));
                }

                Duck.nextDuckLines++;
                if (Duck.nextDuckLines >= Duck.duckLines.length)
                    Duck.nextDuckLines = 0;

                Duck.lastDuckTime = System.nanoTime();
            }
        }
        // 피버타임 종료 처리
        if (isFeverTimeActive && System.nanoTime() - feverStartTime >= feverDuration * 1000000) {
            isFeverTimeActive = false; // 피버타임 종료
            timeBetweenShots = Framework.secInNanosec / 3; // 사격 딜레이 원상 복구
            killCount = 0; // 연속 사격 수 초기화
        }
        // 새로운 오리를 생성하고 배열 리스트에 추가함.
        if(System.nanoTime() - Duck.lastDuckTime >= Duck.timeBetweenDucks)
        {
            // 새로운 오리를 생성하고 배열 리스트에 추가함.
            ducks.add(new Duck(Duck.duckLines[Duck.nextDuckLines][0] + random.nextInt(200), Duck.duckLines[Duck.nextDuckLines][1], Duck.duckLines[Duck.nextDuckLines][2], Duck.duckLines[Duck.nextDuckLines][3], duckImg));

            // 다음 라인으로 이동하여 다음 오리가 생성되도록 함.
            Duck.nextDuckLines++;
            if(Duck.nextDuckLines >= Duck.duckLines.length)
                Duck.nextDuckLines = 0;

            Duck.lastDuckTime = System.nanoTime();
        }

        // 모든 오리들을 업데이트함.
        for(int i = 0; i < ducks.size(); i++)
        {
            // 오리를 이동시킴.
            ducks.get(i).Update();

            // 오리가 화면을 벗어나면 제거함.
            if(ducks.get(i).x < 0 - duckImg.getWidth())
            {
                ducks.remove(i);
                runawayDucks++;
            }
        }

        // 플레이어가 총을 쏘았는지 확인함.
        if(Canvas.mouseButtonState(MouseEvent.BUTTON1))
        {
            // 다시 총을 쏠 수 있는지 확인함.
            if(System.nanoTime() - lastTimeShoot >= timeBetweenShots)
            {
                boolean hit = false;
                shoots++;

                // 모든 오리들을 순회하며 총에 맞았는지 확인함.
                for(int i = 0; i < ducks.size(); i++)
                {
                    Duck currentDuck = ducks.get(i);
                    // 플레이어가 쏜 위치가 오리의 머리나 몸체에 있는지 확인함.
                    if(new Rectangle(ducks.get(i).x + 18, ducks.get(i).y     , 27, 30).contains(mousePosition) ||
                            new Rectangle(ducks.get(i).x + 30, ducks.get(i).y + 30, 88, 25).contains(mousePosition))
                    {
                        killedDucks++;
                        killCount++;
                        // 점수 계산
                        int scoreMultiplier = isDoubleScoreActive ? 2 : 1; // 점수 배수 결정
                        score += currentDuck.score * scoreMultiplier;

                        // 10마리 오리 잡으면 점수 2배 활성화
                        if (killCount >= 10) {
                            isDoubleScoreActive = true; // 점수 2배 활성화
                        }

                        // 도망간 오리 수 증가에 따른 점수 배수 되돌리기
                        if (runawayDucks > 0 && runawayDucks % 1 == 0 && isDoubleScoreActive) {
                            isDoubleScoreActive = false; // 점수 배수 비활성화
                        }
                        score += currentDuck.score;
                        score += ducks.get(i).score;

                        // 오리를 배열 리스트에서 제거함.
                        ducks.remove(i);
                        hit = true;

                        // 플레이어가 쏜 오리를 찾았으므로 for 루프를 나감.
                        break;
                    }
                }
                if (!hit){
                    miss++;
                    score -= 10;
                }

                lastTimeShoot = System.nanoTime();
            }
        }
        if (score >= 50 && difficultyLevel < 4) {
            difficultyLevel++; // 점수가 50 이상일 때 난이도 증가
        }

        // 200마리의 오리가 도망가면 게임이 종료됨.
        if(runawayDucks >= 200)
            Framework.gameState = Framework.GameState.GAMEOVER;
    }

    /**
     * 게임을 화면에 그림.
     *
     * @param g2d Graphics2D
     * @param mousePosition 현재 마우스 위치.
     */
    public void Draw(Graphics2D g2d, Point mousePosition)
    {

        g2d.drawImage(backgroundImg, 0, 0, Framework.frameWidth, Framework.frameHeight, null);

        // 피버타임이 활성화된 동안에는 화면 전체를 반투명한 색으로 덮는 효과를 추가
        if (isFeverTimeActive) {
            g2d.setColor(new Color(255, 0, 0, 100));  // 빨간색 반투명 레이어
            g2d.fillRect(0, 0, Framework.frameWidth, Framework.frameHeight);
        }

        // 모든 오리들을 그림.
        for(int i = 0; i < ducks.size(); i++)
        {
            ducks.get(i).Draw(g2d);
        }

        g2d.drawImage(grassImg, 0, Framework.frameHeight - grassImg.getHeight(), Framework.frameWidth, grassImg.getHeight(), null);

        g2d.drawImage(sightImg, mousePosition.x - sightImgMiddleWidth, mousePosition.y - sightImgMiddleHeight, null);

        g2d.setFont(font);
        g2d.setColor(Color.darkGray);

        g2d.drawString("RUNAWAY: " + runawayDucks, 10, 21);
        g2d.drawString("KILLS: " + killedDucks, 160, 21);
        g2d.drawString("MISS: " + miss, 299, 21);
        g2d.drawString("SCORE: " + score, 440, 21);
    }

    /**
     * 게임 오버 화면을 그림.
     *
     * @param g2d Graphics2D
     * @param mousePosition 현재 마우스 위치.
     */
    public void DrawGameOver(Graphics2D g2d, Point mousePosition)
    {
        Draw(g2d, mousePosition);

        // 첫 번째 텍스트는 그림자를 위해 사용됨.
        g2d.setColor(Color.black);
        g2d.drawString("kr.jbnu.se.std.Game Over", Framework.frameWidth / 2 - 39, (int)(Framework.frameHeight * 0.65) + 1);
        g2d.drawString("Press space or enter to restart.", Framework.frameWidth / 2 - 149, (int)(Framework.frameHeight * 0.70) + 1);
        g2d.setColor(Color.red);
        g2d.drawString("kr.jbnu.se.std.Game Over", Framework.frameWidth / 2 - 40, (int)(Framework.frameHeight * 0.65));
        g2d.drawString("Press space or enter to restart.", Framework.frameWidth / 2 - 150, (int)(Framework.frameHeight * 0.70));
    }
}
