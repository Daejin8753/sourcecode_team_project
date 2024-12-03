package kr.jbnu.se.std;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import java.awt.Font;

/**
 * kr.jbnu.se.std.Framework는 게임(kr.jbnu.se.std.Game.java)을 제어하고, 업데이트하며 화면에 그리기를 담당함.
 *
 * @author www.gametutorial.net
 */


public class Framework extends Canvas{

    /**
     * 프레임의 너비.
     */

    public static final int FRAME_WIDTH = 800;
    /**
     * 프레임의 높이.
     */
    public static final int FRAME_HEIGHT = 600;

    /**
     * 1초를 나노초로 나타낸 시간.
     * 1초 = 1,000,000,000 나노초
     */
    public static final long SEC_IN_NANOSEC = 1000000000L;

    /**
     * 1밀리초를 나노초로 나타낸 시간.
     * 1밀리초 = 1,000,000 나노초
     */
    public static final long MILI_SEC_IN_NANOSEC = 1000000L;

    /**
     * FPS - 초당 프레임 수
     * 게임이 매 초 몇 번 업데이트되어야 하는지.
     */
    private static final int GAME_FPS = 60;
    /**
     * 업데이트 간의 대기 시간. 나노초 단위로 나타냄.
     */
    private static final long GAME_UPDATE_PERIOD = SEC_IN_NANOSEC / GAME_FPS;

    private transient BufferedImage levelbackgroundImg;


    /**
     * 게임의 가능한 상태들
     */
    public enum GameState {
        STARTING,
//        VISUALIZING,
        GAME_CONTENT_LOADING,
        MAIN_MENU,
        PLAYING,
        GAMEOVER,
        PAUSED,
        LEVEL_SETTINGS;

        // 현재 게임 상태를 저장하는 변수 (초기값 설정)
        private static GameState currentState = MAIN_MENU;

        // 현재 상태를 반환
        public static GameState getCurrentState() {
            return currentState;
        }

        // 상태를 변경
        public static void changeState(GameState newState) {
            if (newState != null) {
                currentState = newState;
            }
        }
    }
    /**
     * 현재 게임 상태
     */


    /**
     * 게임에서 경과된 시간 (나노초 단위).
     */
    private long gameTime;

    public long getGameTime() {
        return gameTime;
    }

    // 경과 시간을 계산하기 위해 사용됨.
    private long lastTime;

    // 실제 게임 객체
    private transient Game game;

    /**
     * 메뉴 이미지.
     */
    private transient BufferedImage shootTheDuckMenuImg;


    public Framework () {
        super();

        // 새로운 스레드에서 게임을 시작함.
        Thread gameThread = new Thread() {
            @Override
            public void run(){
                gameLoop();
            }
        };
        gameThread.start();
    }


    /**
     * 파일을 로드함 - 이미지, 소리 등.
     * 이 메서드는 이 클래스의 파일을 로드하는 데 사용되며, 실제 게임의 파일은 kr.jbnu.se.std.Game.java에서 로드될 수 있음.
     */
    private void loadContent()
    {
        try
        {
            URL shootTheDuckMenuImgUrl = this.getClass().getResource("/images/menu.jpg");
            shootTheDuckMenuImg = ImageIO.read(shootTheDuckMenuImgUrl);

            URL levelSettingBgImgUrl = this.getClass().getResource("/images/background.jpg");
            levelbackgroundImg = ImageIO.read(levelSettingBgImgUrl);
        }
        catch (IOException ex) {
            Logger.getLogger(Framework.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private boolean isRunning = true;
    /**
     * 특정 시간 간격(GAME_UPDATE_PERIOD)마다 게임 로직을 업데이트하고 게임을 화면에 그림.
     */
    private void gameLoop() {

        while(isRunning) {

            long beginTime = System.nanoTime();

            switch (GameState.getCurrentState()) {

                case PLAYING:
                    gameTime += System.nanoTime() - lastTime;
                    game.UpdateGame(gameTime, mousePosition());
                    lastTime = System.nanoTime();
                    break;

                case PAUSED:
                    break;          // 일시정지 상태에서는 아무것도 하지 않음

                case GAMEOVER:
                    //...
                    break;
                case MAIN_MENU:
                    loadContent();
                    break;
                case LEVEL_SETTINGS:
                    //...
                    break;
                case GAME_CONTENT_LOADING:
                    //...
                    break;
                case STARTING:
                    // 위에서 호출된 모든 작업이 완료되면 게임 상태를 메인 메뉴로 변경함.
                    GameState.changeState(GameState.MAIN_MENU);
                    break;

            }

            // 화면을 다시 그림.
            repaint();

            // 스레드를 대기 상태로 설정하여 GAME_FPS를 맞추기 위한 시간을 계산함.
            long timeTaken = System.nanoTime() - beginTime;
            long timeLeft = (GAME_UPDATE_PERIOD - timeTaken) / MILI_SEC_IN_NANOSEC; // 밀리초 단위로 계산

            if (timeLeft < 10)                              // 시간이 10밀리초보다 적으면, 다른 스레드가 작업을 할 수 있도록 스레드를 10밀리초 동안 대기 상태로 설정함.
                timeLeft = 10;                              // 최소 대기 시간 설정

            try {
                Thread.sleep(timeLeft);                     // 필요한 지연 시간을 제공하고 다른 스레드가 작업을 할 수 있도록 제어를 양보함.
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt(); // 인터럽트 상태 복원
                Logger.getLogger(Framework.class.getName()).log(Level.WARNING, "Game loop interrupted", ex);
                isRunning = false; // 루프 종료
            }
        }
    }

    /**
     * 게임을 화면에 그림. gameLoop() 메서드에서 repaint() 메서드를 통해 호출됨.
     */
    @Override
    public void Draw(Graphics2D g2d) {
        switch (GameState.getCurrentState()) {
            case PLAYING:
                game.Draw(g2d, mousePosition());
                break;
            case GAMEOVER:
                game.DrawGameOver(g2d, mousePosition());
                break;
            case MAIN_MENU:
                g2d.drawImage(shootTheDuckMenuImg, 0, 0, FRAME_WIDTH, FRAME_HEIGHT, null);
                g2d.drawString("게임을 시작하려면 왼쪽 마우스 버튼을 클릭.", FRAME_WIDTH / 2 - 83, (int)(FRAME_HEIGHT * 0.65));
                g2d.drawString("L 키로 난이도 설정.", FRAME_WIDTH / 2 - 100, (int)(FRAME_HEIGHT * 0.67));
                g2d.drawString("게임을 종료하려면 언제든지 ESC.", FRAME_WIDTH / 2 - 75, (int)(FRAME_HEIGHT * 0.70));
                g2d.setColor(Color.white);
                g2d.drawString("WWW.GAMETUTORIAL.NET", 7, FRAME_HEIGHT - 5);
                break;
            case PAUSED:  // 일시정지 화면 출력
                g2d.setColor(Color.white);
                g2d.drawString("게임이 일시정지됨", FRAME_WIDTH / 2 - 50, FRAME_HEIGHT / 2 - 20);
                g2d.drawString("게임을 다시 시작하려면 'P'를 누르세요", FRAME_WIDTH / 2 - 60, FRAME_HEIGHT / 2);
                break;
            case GAME_CONTENT_LOADING:
                g2d.setColor(Color.white);
                g2d.drawString("게임 로딩 중", FRAME_WIDTH / 2 - 50, FRAME_HEIGHT / 2);
                break;
            case LEVEL_SETTINGS:
                g2d.drawImage(levelbackgroundImg, 0, 0, FRAME_WIDTH, FRAME_HEIGHT, null);
                g2d.setColor(Color.white);
                g2d.drawString("WWW.GAMETUTORIAL.NET", 7, FRAME_HEIGHT - 5);
                g2d.setColor(Color.black);
                g2d.setFont(new Font("Malgun Gothic", Font.BOLD, 13));
                g2d.drawString("ESC 또는 L키로 메인 메뉴로 돌아갈 수 있습니다", 10, 20);
                g2d.setFont(new Font("Malgun Gothic", Font.BOLD, 18));
                g2d.drawString("방향키 ↑ ↓로 난이도를 선택하세요.", 100, 150);
                g2d.drawString("게임을 시작하려면 왼쪽 마우스 버튼을 클릭하세요.", 100, 200);
                g2d.setFont(new Font("Arial", Font.BOLD, 24));
                int currentLevel = Game.getDifficultyLevel();
                g2d.drawString("LEVEL    :    " + currentLevel, 300, 410); //현재 난이도 표시
                g2d.drawString("▲", 435, 370);
                g2d.drawString("▼", 435, 450);
                break;
            case STARTING:
                g2d.drawString("로딩중...", FRAME_WIDTH / 2 - 50, FRAME_HEIGHT / 2 - 20);
                break;
            default:

                break;
        }
    }

    /**
     * 새로운 게임을 시작함.
     */
    private void newGame()
    {
        // 게임 시간을 0으로 설정하고 현재 시간으로 lastTime을 설정하여 이후 계산에 사용함.
        gameTime = 0;
        lastTime = System.nanoTime();

        game = new Game();
        GameState.changeState(GameState.PLAYING);
    }


    /**
     * 게임을 재시작함 - 게임 시간을 재설정하고 게임 객체의 RestartGame() 메서드를 호출하여 몇 가지 변수를 재설정함.
     */
    private void restartGame()
    {
        // 게임 시간을 0으로 설정하고 현재 시간으로 lastTime을 설정하여 이후 계산에 사용함.
        gameTime = 0;
        lastTime = System.nanoTime();

        game.RestartGame();

        // 게임 상태를 변경하여 게임을 시작할 수 있도록 함.
        GameState.changeState(GameState.PLAYING);
    }

    /**
     * 게임 프레임/창에서 마우스 포인터의 위치를 반환함.
     * 마우스 위치가 null이면 이 메서드는 0,0 좌표를 반환함.
     *
     * @return 마우스 좌표의 Point 객체.
     */
    private Point mousePosition()
    {
        try
        {
            Point mp = this.getMousePosition();

            if(mp != null)
                return this.getMousePosition();
            else
                return new Point(0, 0);
        }
        catch (Exception e)
        {
            return new Point(0, 0);
        }
    }

    /**
     * 키보드 키가 눌렸을 때 호출되는 메서드.
     *
     * @param e KeyEvent
     */
    @Override
    public void keyReleasedFramework(KeyEvent e) {
        switch (GameState.getCurrentState()) {
            case GAMEOVER:
                handleGameOverState(e);
                break;
            case PLAYING:
                handlePlayingState(e);
                break;
            case PAUSED:
                handlePausedState(e);
                break;
            case MAIN_MENU:
                handleMainMenuState(e);
                break;
            case LEVEL_SETTINGS:
                handleLevelSettingsState(e);
                break;
            default:
                break;
        }
    }

    // 상태별 핸들러 메서드
    private void handleGameOverState(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ESCAPE)
            System.exit(0);
        else if (e.getKeyCode() == KeyEvent.VK_SPACE || e.getKeyCode() == KeyEvent.VK_ENTER)
            restartGame();
    }

    private void handlePlayingState(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ESCAPE)
            System.exit(0);
        else if (e.getKeyCode() == KeyEvent.VK_P)
            GameState.changeState(GameState.PAUSED);
    }

    private void handlePausedState(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_P)
            GameState.changeState(GameState.PLAYING);
    }

    private void handleMainMenuState(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ESCAPE)
            System.exit(0);
        else if (e.getKeyCode() == KeyEvent.VK_L)
            GameState.changeState(GameState.LEVEL_SETTINGS);
    }

    private void handleLevelSettingsState(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ESCAPE || e.getKeyCode() == KeyEvent.VK_L) {
            GameState.changeState(GameState.MAIN_MENU);
            Game.resetLevel();
        } else if (e.getKeyCode() == KeyEvent.VK_UP) {
            Game.increaseDifficulty();
        } else if (e.getKeyCode() == KeyEvent.VK_DOWN) {
            Game.decreaseDifficulty();
        }
    }

    /**
     * 마우스 버튼이 클릭되었을 때 호출되는 메서드.
     *
     * @param e MouseEvent
     */
    @Override
    public void mouseClicked(MouseEvent e) {
        switch (GameState.getCurrentState()) {

            case MAIN_MENU:
            case LEVEL_SETTINGS:
                if(e.getButton() == MouseEvent.BUTTON1)
                    newGame();
                break;
            default:

                break;
        }
    }
}
