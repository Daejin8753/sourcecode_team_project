package kr.jbnu.se.std;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;


public class Game {

    /**
     * kr.jbnu.se.std.Duck image.
     */
    private BufferedImage duckImg;
    private BufferedImage strongDuckImg;
    private BufferedImage fastDuckImg;
    private BufferedImage badDuckImg;


    /**
     * We use this to generate a random number.
     */
    private Random random;

    /**
     * Font that we will use to write statistic to the screen.
     */
    private Font font;

    /**
     * Array list of the ducks.
     */
    private ArrayList<Duck> ducks;

    /**
     * How many ducks leave the screen alive?
     */
    private int runawayDucks;

    /**
     * How many ducks the player killed?
     */
    private int killedDucks;

    /**
     * For each killed duck, the player gets points.
     */
    private int score;

    /**
     * How many times a player is shot?
     */
    private int shoots;

    /**
     * Last time of the shoot.
     */
    private long lastTimeShoot;
    /**
     * The time which must elapse between shots.
     */
    private long timeBetweenShots;

    /**
     * kr.jbnu.se.std.Game background image.
     */
    private BufferedImage backgroundImg;

    /**
     * Bottom grass.
     */
    private BufferedImage grassImg;


    /**
     * Shotgun sight image.
     */
    private BufferedImage sightImg;

    /**
     * Middle width of the sight image.
     */
    private int sightImgMiddleWidth;
    /**
     * Middle height of the sight image.
     */
    private int sightImgMiddleHeight;

    private static int difficultyLevel;// 난이도 변수
    private static int initialDifficultyLevel;// 설정한 초기 난이도 저장하기 위한 변수

    private final long[] duckSpawnTimes = {1000000000, 800000000, 600000000, 400000000, 200000000}; // 각 난이도별 오리 출현 시간 (나노초)

    private int killCount; // 잡은 오리 수

    private boolean isDoubleScoreActive; // 점수 2배 활성화 여부

    private int miss;

    private boolean isFeverTimeActive; // 피버타임 활성화 여부
    private long feverStartTime; // 피버타임 시작 시간
    private long feverDuration = 5000; // 피버타임 지속 시간 (밀리초 단위)

    private boolean isZoomed = false;

    private ArrayList<Item> items = new ArrayList<>(); // 화면에 표시된 아이템 목록
    private BufferedImage itemImg; // 아이템 이미지



    public Game() {

        random = new Random();
        ducks = new ArrayList<>();
        LoadContent();

        Framework.GameState.changeState(Framework.GameState.GAME_CONTENT_LOADING);

        Thread threadForInitGame = new Thread() {
            @Override
            public void run(){
                // Sets variables and objects for the game.
                Initialize();

                // Load game files (images, sounds, ...)
                LoadContent();

                Framework.GameState.changeState(Framework.GameState.PLAYING);
            }
        };
        threadForInitGame.start();
    }


    protected static void increaseDifficulty() {
        if (difficultyLevel < 5) difficultyLevel++;
    }

    protected static void decreaseDifficulty() {
        if (difficultyLevel > 1) difficultyLevel--;
    }

    protected static int getDifficultyLevel(){
        if (difficultyLevel == 0) difficultyLevel = 1;              // 초기 값이 설정되지 않은 경우, 초기 난이도를 1로 설정
        return difficultyLevel;
    }

    // 초기 난이도를 설정하는 메서드

    public static int resetLevel() {
        initialDifficultyLevel = 1;                                 // 난이도를 1로 초기화.
        difficultyLevel = initialDifficultyLevel;                   // 그걸 난이도 값 에 넣음.
        return difficultyLevel;
    }


    /**
     * Set variables and objects for the game.
     */
    private void Initialize()
    {
        random = new Random();
        font = new Font("monospaced", Font.BOLD, 18);

        ducks = new ArrayList<>();

        runawayDucks = 0;
        killedDucks = 0;
        score = 0;
        shoots = 0;
        miss = 0;
        killCount = 0; // 잡은 오리 수 초기화
        isDoubleScoreActive = false; // 점수 2배 비활성화
        lastTimeShoot = 0;
        timeBetweenShots = Framework.SEC_IN_NANOSEC / 3;
        difficultyLevel = getDifficultyLevel();
        initialDifficultyLevel = difficultyLevel;

    }
    private void AdjustDuckSpawnTime() {
        Duck.timeBetweenDucks = duckSpawnTimes[difficultyLevel-1]*2; // 현재 난이도에 따른 오리 출현 시간 설정
    }


    /**
     * Load game files - images, sounds, ...
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
            sightImg = ImageIO.read(sightImgUrl);

            URL strongDuckImgUrl = this.getClass().getResource("/images/strong_duck.png");
            strongDuckImg = ImageIO.read(strongDuckImgUrl);

            // BadDuck 이미지 로드
            URL badDuckImgUrl = this.getClass().getResource("/images/bad_duck.png");
            badDuckImg = ImageIO.read(badDuckImgUrl);

            URL itemImgUrl = this.getClass().getResource("/images/item.png"); // 아이템 이미지 경로
            itemImg = ImageIO.read(itemImgUrl);

            sightImgMiddleWidth = sightImg.getWidth() / 2;
            sightImgMiddleHeight = sightImg.getHeight() / 2;

        }
        catch (IOException ex) {
            Logger.getLogger(Game.class.getName()).log(Level.SEVERE, null, ex);
        }
    }


    /**
     * Restart game - reset some variables.
     */
    public void RestartGame()
    {
        // Removes all of the ducks from this list.
        ducks.clear();

        // We set last duckt time to zero.
        Duck.resetLastDuckTime();

        runawayDucks = 0;
        killedDucks = 0;
        score = 0;
        shoots = 0;
        resetLevel();               // 초기 난이도를 1로 설정.
        lastTimeShoot = 0;
    }


    private void setDifficultyLevel() {
        if (score >= 3000 - adjustScore(initialDifficultyLevel) && difficultyLevel == 1) {
            difficultyLevel = 2;
            runawayDucks = 0;
        } else if (score >= 6000 - adjustScore(initialDifficultyLevel) && difficultyLevel == 2) {
            difficultyLevel = 3;
            runawayDucks = 0;
        } else if (score >= 9000 - adjustScore(initialDifficultyLevel) && difficultyLevel == 3) {
            difficultyLevel = 4;
            runawayDucks = 0;
        } else if (score >= 12000 - adjustScore(initialDifficultyLevel) && difficultyLevel == 4) {
            difficultyLevel = 5;
            runawayDucks = 0;
        }
    }

    private int adjustScore(int x){
        return 3000*(x - 1);
    }

    // 난이도에 따른 오리의 속도 조정
    private double adjustSpeedForDifficulty(double baseSpeed) {
        return baseSpeed * Math.pow(1.2, (double) difficultyLevel - 1);
    }


    /**
     * update game logic.
     *
     * @param gameTime gameTime of the game.
     * @param mousePosition current mouse position.
     */
    public void UpdateGame(long gameTime, Point mousePosition) {

        // 난이도에 따른 조정
        adjustDifficulty();

        // 피버타임 조정
        adjustFeverTime();

        // 줌 기능.
        adjustZoom();

        // 피버타임 동안, 또는 평상시 오리 출현 빈도 조정
        adjustSpawnFrequency();

        // 피버타임 종료 처리
        endFeverTime();

        // 새 오리 생성.
        if(System.nanoTime() - Duck.getLastDuckTime() >= Duck.timeBetweenDucks) {
            // Here we create new duck and add it to the array list.
            ducks.add(new Duck(Duck.duckLines[Duck.nextDuckLines][0] + random.nextInt(200), Duck.duckLines[Duck.nextDuckLines][1], adjustSpeedForDifficulty(Duck.duckLines[Duck.nextDuckLines][2]), Duck.duckLines[Duck.nextDuckLines][3], duckImg));

            // Here we increase nextDuckLines so that next duck will be created in next line.
            Duck.increseNextDuckLines(Duck.duckLines.length);
            if(Duck.nextDuckLines >= Duck.duckLines.length)
                Duck.resetNextDuckLines();

            Duck.updateLastDuckTime(System.nanoTime());
        }

        // 모든 오리 업데이트.
        for(int i = 0; i < ducks.size(); i++) {
            // Move the duck.
            ducks.get(i).update();

            // Checks if the duck leaves the screen and remove it if it does.
            if(ducks.get(i).x < 0 - duckImg.getWidth())
            {
                ducks.remove(i);
                runawayDucks++;
            }
        }


        // 플레이어가 사격했는지 확인.
        adjustShooting(mousePosition);

        // 아이템 업데이트
        updateItem(mousePosition);

        // 200마리 놓치면 게임오버.
        checkGameOver();
    }

    private void updateItem(Point mousePosition) {


        Iterator<Item> iterator = items.iterator();
        while (iterator.hasNext()) {
            Item item = iterator.next();

            // 플레이어가 아이템을 획득했는지 체크
            if (item.checkCollision(mousePosition) && !item.isCollected()) {
                item.collect(); // 아이템 수집
                ActivateItemEffect(); // 아이템 효과 적용
            }
        }
    }

    private void adjustDifficulty() {
        AdjustDuckSpawnTime(); // 난이도에 따른 오리 출현 간격 조정
        setDifficultyLevel(); // 난이도에 따른 오리 속도 조정
    }

    private void adjustFeverTime() {
        if (killCount >= 5 && runawayDucks <= 0 && !isFeverTimeActive) {
            isFeverTimeActive = true;
            feverStartTime = System.nanoTime();
            timeBetweenShots = 0; // 사격 딜레이 없애기
        }
    }

    private void adjustZoom() {
        if (Canvas.mouseButtonState(MouseEvent.BUTTON3)) {
            isZoomed = true;
        } else {
            isZoomed = false;
        }
    }

    private void adjustSpawnFrequency(){
        if (isFeverTimeActive) {
            spawnFeverTime();
        }
        else {
            spawnNormally();
        }
    }

    private void spawnFeverTime(){
        if (System.nanoTime() - Duck.lastDuckTime >= Duck.timeBetweenDucks / 2) {
            createDuck();
            Duck.lastDuckTime = System.nanoTime();
        }
    }

    private void spawnNormally(){
        if (ducks.size()<5&&System.nanoTime() - Duck.lastDuckTime >= Duck.timeBetweenDucks) {
            createDuck();

            if (random.nextInt(10) < 6) {  // 60% 확률로 일반 오리 생성
                ducks.add(new Duck(Duck.duckLines[Duck.nextDuckLines][0] + random.nextInt(200),
                        Duck.duckLines[Duck.nextDuckLines][1],
                        adjustSpeedForDifficulty(Duck.duckLines[Duck.nextDuckLines][2]),
                        Duck.duckLines[Duck.nextDuckLines][3],
                        duckImg));
            } else if (random.nextInt(10) < 3){  // 30% 확률로 BadDuck 생성
                ducks.add(new BadDuck(Duck.duckLines[Duck.nextDuckLines][0] + random.nextInt(200),
                        Duck.duckLines[Duck.nextDuckLines][1],
                        adjustSpeedForDifficulty(Duck.duckLines[Duck.nextDuckLines][2]),
                        Duck.duckLines[Duck.nextDuckLines][3],
                        badDuckImg));
            } else if (random.nextInt(10) < 2.5){  // 25% 확률로 strongDuck 생성
                ducks.add(new StrongDuck(Duck.duckLines[Duck.nextDuckLines][0] + random.nextInt(200),
                        Duck.duckLines[Duck.nextDuckLines][1],
                        adjustSpeedForDifficulty(Duck.duckLines[Duck.nextDuckLines][2]),
                        Duck.duckLines[Duck.nextDuckLines][3],
                        strongDuckImg));
            } else {
                if (random.nextInt(10) < 3) { // 30% 확률로 FastDuck 출현
                    ducks.add(new FastDuck(Duck.duckLines[Duck.nextDuckLines][0] + random.nextInt(200),
                            Duck.duckLines[Duck.nextDuckLines][1],
                            adjustSpeedForDifficulty(Duck.duckLines[Duck.nextDuckLines][2]),
                            Duck.duckLines[Duck.nextDuckLines][3],
                            fastDuckImg));
                }

            }
            Duck.lastDuckTime = System.nanoTime();
        }
    }

    private void createDuck(){
        int direction = random.nextInt(2);  // 0이면 오른쪽 -> 왼쪽, 1이면 왼쪽 -> 오른쪽
        int startX;
        double speed;
        if (direction == 0) {  // 오른쪽에서 왼쪽으로 이동
            startX = Duck.duckLines[Duck.nextDuckLines][0] + random.nextInt(200); // 기존과 동일하게 오른쪽에서 출발
            speed = adjustSpeedForDifficulty(Duck.duckLines[Duck.nextDuckLines][2]);  // 음수 속도 (왼쪽으로 이동)
        } else {  // 왼쪽에서 오른쪽으로 이동
            startX = 0 - random.nextInt(200);  // 왼쪽에서 출발
            speed = adjustSpeedForDifficulty(-Duck.duckLines[Duck.nextDuckLines][2]);  // 양수 속도 (오른쪽으로 이동)
        }
        ducks.add(new Duck(startX, Duck.duckLines[Duck.nextDuckLines][1], speed, Duck.duckLines[Duck.nextDuckLines][3], duckImg));

        // 다음 라인으로 이동
        Duck.nextDuckLines++;
        if (Duck.nextDuckLines >= Duck.duckLines.length)
            Duck.nextDuckLines = 0;
    }

    private void endFeverTime() {
        if (isFeverTimeActive && System.nanoTime() - feverStartTime >= feverDuration * 1000000) {
            isFeverTimeActive = false;
            timeBetweenShots = Framework.SEC_IN_NANOSEC / 3; // 사격 딜레이 원상 복구
            killCount = 0; // 연속 사격 수 초기화
        }
    }

    private void adjustShooting(Point mousePosition) {
        if (Canvas.mouseButtonState(MouseEvent.BUTTON1) && System.nanoTime() - lastTimeShoot >= timeBetweenShots) {
            boolean hit = false;
            shoots++;

            // 모든 Duck에 대해 처리
            for (int i = 0; i < ducks.size() && !hit; i++) { // hit이 true면 루프 종료
                Duck currentDuck = ducks.get(i);

                if (currentDuck instanceof StrongDuck) {
                    hit = CheckStrongDuckHit((StrongDuck) currentDuck, mousePosition);
                } else if (isDuckHit(currentDuck, mousePosition)) {
                    handleDuckHit(currentDuck, i);
                    hit = true;
                }
            }

            // FastDuck을 맞췄는지 확인(피버타임에 돌입할지 조정)
            CheckFastDuckHit(mousePosition);

            if (!hit) {
                miss++;
                score -= 10;
            }

            lastTimeShoot = System.nanoTime();
        }
    }

    private boolean isDuckHit(Duck currentDuck, Point mousePosition) {
        if (currentDuck instanceof StrongDuck) {
            return new Rectangle(currentDuck.x + 10, currentDuck.y, 40, 50).contains(mousePosition);
        } else {
            return new Rectangle(currentDuck.x + 18, currentDuck.y, 27, 30).contains(mousePosition) ||
                    new Rectangle(currentDuck.x + 30, currentDuck.y + 30, 88, 25).contains(mousePosition);
        }
    }

    private void handleDuckHit(Duck currentDuck, int duckIndex) {
        killedDucks++;
        killCount++;
        int scoreMultiplier = isDoubleScoreActive ? 2 : 1;
        score += currentDuck.score * scoreMultiplier;

        if (killCount >= 10) {
            isDoubleScoreActive = true;
        }

        if (runawayDucks > 0 && isDoubleScoreActive) {
            isDoubleScoreActive = false;
        }

        score += currentDuck.score;
        ducks.remove(duckIndex);
    }

    private void checkGameOver() {
        if (runawayDucks >= 200)
            Framework.GameState.changeState(Framework.GameState.GAMEOVER);
    }

    private void ActivateItemEffect() {
        // 30% 확률로 피버타임 활성화3
        if (random.nextInt(100) < 30) {
            isFeverTimeActive = true;
            feverStartTime = System.nanoTime();
        }
    }
    private void CheckFastDuckHit(Point mousePosition) {
        for (int i = 0; i < ducks.size(); i++) {
            Duck currentDuck = ducks.get(i);

            // FastDuck인 경우 추가 처리
            if (currentDuck instanceof FastDuck && isDuckHit(currentDuck, mousePosition)) {
                // 30% 확률로 피버 타임 돌입
                if (random.nextInt(100) < 30) { // 0~99 범위에서 30 미만이면 성공
                    ActivateFeverTime();
                }

                score += currentDuck.score * 2; // 점수 추가 (빠른 오리는 2배 점수)
                DropItem(currentDuck.x, currentDuck.y); // 아이템 드롭
                ducks.remove(i); // 빠른 오리 제거
                return;
            }
        }

    }
    private boolean CheckStrongDuckHit(StrongDuck strongDuck, Point mousePosition) {
        // StrongDuck이 맞은 경우 처리
        if (isDuckHit(strongDuck, mousePosition) && random.nextInt(100) < 50) { // StrongDuck 맞았고, 50% 확률로 사망
            ducks.remove(strongDuck); // 오리 제거
            killedDucks++;
            killCount++;

            // 점수 계산
            int scoreMultiplier = isDoubleScoreActive ? 2 : 1;
            score += strongDuck.score * scoreMultiplier;

            return true;
        }
        return false;
    }

    // 아이템 드롭 기능 추가
    private void DropItem(int x, int y) {
        // 30% 확률로 아이템 드롭
        if (random.nextInt(100) < 30) {
            items.add(new Item(x, y, itemImg)); // 아이템 추가
        }
    }

    // 피버타임 활성화
    private void ActivateFeverTime() {
        isFeverTimeActive = true;
        feverStartTime = System.nanoTime();
        timeBetweenShots = 0; // 피버타임 동안 사격 딜레이 없음
    }
    /**
     * Draw the game to the screen.
     *
     * @param g2d Graphics2D
     * @param mousePosition current mouse position.
     */
    public void Draw(Graphics2D g2d, Point mousePosition)
    {

        if (isZoomed) {
            g2d.scale(1.2, 1.2); // 화면을 1.2배 확대
        }
        g2d.drawImage(backgroundImg, 0, 0, Framework.FRAME_WIDTH, Framework.FRAME_HEIGHT, null);

        // 피버타임이 활성화된 동안에는 화면 전체를 반투명한 색으로 덮는 효과를 추가
        if (isFeverTimeActive) {
            g2d.setColor(new Color(255, 0, 0, 100));  // 빨간색 반투명 레이어
            g2d.fillRect(0, 0, Framework.FRAME_WIDTH, Framework.FRAME_HEIGHT);
        }

        // Here we draw all the ducks.
        for(int i = 0; i < ducks.size(); i++)
        {
            ducks.get(i).draw(g2d);
        }

        g2d.drawImage(grassImg, 0, Framework.FRAME_HEIGHT - grassImg.getHeight(), Framework.FRAME_WIDTH, grassImg.getHeight(), null);

        g2d.drawImage(sightImg, mousePosition.x - sightImgMiddleWidth, mousePosition.y - sightImgMiddleHeight, null);

        g2d.setFont(font);
        g2d.setColor(Color.darkGray);

        g2d.drawString("RUNAWAY: " + runawayDucks, 10, 21);
        g2d.drawString("KILLS: " + killedDucks, 160, 21);
        g2d.drawString("MISS: " + miss, 299, 21);
        g2d.drawString("SCORE: " + score, 440, 21);
        g2d.drawString("LEVEL: " + (difficultyLevel), 600, 21);

        g2d.setTransform(new AffineTransform());
    }


    /**
     * Draw the game over screen.
     *
     * @param g2d Graphics2D
     * @param mousePosition Current mouse position.
     */
    public void DrawGameOver(Graphics2D g2d, Point mousePosition)
    {
        Draw(g2d, mousePosition);

        // The first text is used for shade.
        g2d.setColor(Color.black);
        g2d.drawString("Game Over", Framework.FRAME_WIDTH / 2 - 39, (int)(Framework.FRAME_HEIGHT * 0.65) + 1);
        g2d.drawString("Press space or enter to restart.", Framework.FRAME_WIDTH / 2 - 149, (int)(Framework.FRAME_HEIGHT * 0.70) + 1);
        g2d.setColor(Color.red);
        g2d.drawString("Game Over", Framework.FRAME_WIDTH / 2 - 40, (int)(Framework.FRAME_HEIGHT * 0.65));
        g2d.drawString("Press space or enter to restart.", Framework.FRAME_WIDTH / 2 - 150, (int)(Framework.FRAME_HEIGHT * 0.70));
    }
}
