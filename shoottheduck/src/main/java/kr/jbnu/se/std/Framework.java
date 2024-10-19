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
import javax.swing.*;

/**
 * kr.jbnu.se.std.Framework는 게임(kr.jbnu.se.std.Game.java)을 제어하고, 업데이트하며 화면에 그리기를 담당함.
 *
 * @author www.gametutorial.net
 */


public class Framework extends Canvas{

    /**
     * 프레임의 너비.
     */

    public static int frameWidth;
    /**
     * 프레임의 높이.
     */
    public static int frameHeight;

    /**
     * 1초를 나노초로 나타낸 시간.
     * 1초 = 1,000,000,000 나노초
     */
    public static final long secInNanosec = 1000000000L;

    /**
     * 1밀리초를 나노초로 나타낸 시간.
     * 1밀리초 = 1,000,000 나노초
     */
    public static final long milisecInNanosec = 1000000L;

    /**
     * FPS - 초당 프레임 수
     * 게임이 매 초 몇 번 업데이트되어야 하는지.
     */
    private final int GAME_FPS = 60;
    /**
     * 업데이트 간의 대기 시간. 나노초 단위로 나타냄.
     */
    private final long GAME_UPDATE_PERIOD = secInNanosec / GAME_FPS;

    public static void changePanel(Game game) {
    }


    /**
     * 게임의 가능한 상태들
     */
    public static enum GameState{STARTING, VISUALIZING, GAME_CONTENT_LOADING, MAIN_MENU, OPTIONS, PLAYING, GAMEOVER, DESTROYED, PAUSED}
    /**
     * 현재 게임 상태
     */
    public static GameState gameState;

    /**
     * 게임에서 경과된 시간 (나노초 단위).
     */
    private long gameTime;
    // 경과 시간을 계산하기 위해 사용됨.
    private long lastTime;

    // 실제 게임 객체
    private Game game;

    /**
     * 메뉴 이미지.
     */
    private BufferedImage shootTheDuckMenuImg;


    public Framework ()
    {
        super();

        gameState = GameState.VISUALIZING;

        // 새로운 스레드에서 게임을 시작함.
        Thread gameThread = new Thread() {
            @Override
            public void run(){
                GameLoop();
            }
        };
        gameThread.start();
    }

    /**
     * 변수와 객체를 설정함.
     * 이 메서드는 이 클래스의 변수와 객체를 설정하는 데 사용되며, 실제 게임의 변수와 객체는 kr.jbnu.se.std.Game.java에서 설정될 수 있음.
     */
    private void Initialize()
    {

    }

    /**
     * 파일을 로드함 - 이미지, 소리 등.
     * 이 메서드는 이 클래스의 파일을 로드하는 데 사용되며, 실제 게임의 파일은 kr.jbnu.se.std.Game.java에서 로드될 수 있음.
     */
    private void LoadContent()
    {
        try
        {
            URL shootTheDuckMenuImgUrl = this.getClass().getResource("/images/menu.jpg");
            shootTheDuckMenuImg = ImageIO.read(shootTheDuckMenuImgUrl);
        }
        catch (IOException ex) {
            Logger.getLogger(Framework.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * 특정 시간 간격(GAME_UPDATE_PERIOD)마다 게임 로직을 업데이트하고 게임을 화면에 그림.
     */
    private void GameLoop()
    {
        // 이 두 변수는 게임의 VISUALIZING 상태에서 사용됨. 프레임/창 해상도를 올바르게 가져오기 위해 시간을 기다리기 위해 사용함.
        long visualizingTime = 0, lastVisualizingTime = System.nanoTime();

        // 이 변수들은 스레드를 대기 상태로 설정하여 GAME_FPS를 맞추기 위해 사용됨.
        long beginTime, timeTaken, timeLeft;

        while(true)
        {
            beginTime = System.nanoTime();

            switch (gameState)
            {
                case PLAYING:
                    gameTime += System.nanoTime() - lastTime;

                    game.UpdateGame(gameTime, mousePosition());

                    lastTime = System.nanoTime();
                    break;
                case PAUSED:
                    // 일시정지 상태에서는 아무것도 하지 않음
                    break;
                case GAMEOVER:
                    //...
                    break;
                case MAIN_MENU:
                    //...
                    break;
                case OPTIONS:
                    //...
                    break;
                case GAME_CONTENT_LOADING:
                    //...
                    break;
                case STARTING:
                    // 변수와 객체를 설정함.
                    Initialize();
                    // 파일을 로드함 - 이미지, 소리 등.
                    LoadContent();

                    // 위에서 호출된 모든 작업이 완료되면 게임 상태를 메인 메뉴로 변경함.
                    gameState = GameState.MAIN_MENU;
                    break;
                case VISUALIZING:
                    // Ubuntu OS에서 (구형 컴퓨터에서 테스트한 경우) this.getWidth() 메서드는 즉시 올바른 값을 반환하지 않음 (예: 프레임이 800px 너비여야 할 때 0을 반환하고 그 후 790, 마지막으로 798px).
                    // 그래서 우리는 창/프레임이 올바른 크기로 설정되도록 1초를 기다림. 혹시 몰라서 'this.getWidth() > 1' 조건을 추가함.
                    if(this.getWidth() > 1 && visualizingTime > secInNanosec)
                    {
                        frameWidth = this.getWidth();
                        frameHeight = this.getHeight();

                        // 프레임의 크기를 가져온 후 상태를 변경함.
                        gameState = GameState.STARTING;
                    }
                    else
                    {
                        visualizingTime += System.nanoTime() - lastVisualizingTime;
                        lastVisualizingTime = System.nanoTime();
                    }
                    break;
            }

            // 화면을 다시 그림.
            repaint();

            // 스레드를 대기 상태로 설정하여 GAME_FPS를 맞추기 위한 시간을 계산함.
            timeTaken = System.nanoTime() - beginTime;
            timeLeft = (GAME_UPDATE_PERIOD - timeTaken) / milisecInNanosec; // 밀리초 단위로 계산
            // 시간이 10밀리초보다 적으면, 다른 스레드가 작업을 할 수 있도록 스레드를 10밀리초 동안 대기 상태로 설정함.
            if (timeLeft < 10)
                timeLeft = 10; // 최소 대기 시간 설정
            try {
                // 필요한 지연 시간을 제공하고 다른 스레드가 작업을 할 수 있도록 제어를 양보함.
                Thread.sleep(timeLeft);
            } catch (InterruptedException ex) { }
        }
    }

    /**
     * 게임을 화면에 그림. GameLoop() 메서드에서 repaint() 메서드를 통해 호출됨.
     */
    @Override
    public void Draw(Graphics2D g2d)
    {
        switch (gameState)
        {
            case PLAYING:
                game.Draw(g2d, mousePosition());
                break;
            case GAMEOVER:
                game.DrawGameOver(g2d, mousePosition());
                break;
            case MAIN_MENU:
                g2d.drawImage(shootTheDuckMenuImg, 0, 0, frameWidth, frameHeight, null);
                g2d.drawString("왼쪽 마우스 버튼을 사용해 오리를 쏘세요.", frameWidth / 2 - 83, (int)(frameHeight * 0.65));
                g2d.drawString("게임을 시작하려면 왼쪽 마우스 버튼을 클릭하세요.", frameWidth / 2 - 100, (int)(frameHeight * 0.67));
                g2d.drawString("게임을 종료하려면 언제든지 ESC를 누르세요.", frameWidth / 2 - 75, (int)(frameHeight * 0.70));
                g2d.setColor(Color.white);
                g2d.drawString("WWW.GAMETUTORIAL.NET", 7, frameHeight - 5);
                break;
            case PAUSED:  // 일시정지 화면 출력
                g2d.setColor(Color.white);
                g2d.drawString("게임이 일시정지됨", frameWidth / 2 - 50, frameHeight / 2 - 20);
                g2d.drawString("게임을 다시 시작하려면 'P'를 누르세요", frameWidth / 2 - 60, frameHeight / 2);
                break;
            case OPTIONS:
                //...
                break;
            case GAME_CONTENT_LOADING:
                g2d.setColor(Color.white);
                g2d.drawString("게임 로딩 중", frameWidth / 2 - 50, frameHeight / 2);
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
        gameState = GameState.PLAYING;
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
    public void keyReleasedFramework(KeyEvent e)
    {
        switch (gameState)
        {
            case GAMEOVER:
                if(e.getKeyCode() == KeyEvent.VK_ESCAPE)
                    System.exit(0);
                else if(e.getKeyCode() == KeyEvent.VK_SPACE || e.getKeyCode() == KeyEvent.VK_ENTER)
                    restartGame();
                break;
            case PLAYING:
                if(e.getKeyCode() == KeyEvent.VK_ESCAPE)
                    System.exit(0);
                else if(e.getKeyCode() == KeyEvent.VK_P)  // P 키로 일시정지
                    gameState = GameState.PAUSED;
                break;
            case PAUSED:
                if(e.getKeyCode() == KeyEvent.VK_P)  // P 키로 일시정지 해제
                    gameState = GameState.PLAYING;
                break;
            case MAIN_MENU:
                if(e.getKeyCode() == KeyEvent.VK_ESCAPE)
                    System.exit(0);
                break;
        }
    }

    /**
     * 마우스 버튼이 클릭되었을 때 호출되는 메서드.
     *
     * @param e MouseEvent
     */
    @Override
    public void mouseClicked(MouseEvent e)
    {
        switch (gameState)
        {
            case MAIN_MENU:
                if(e.getButton() == MouseEvent.BUTTON1)
                    newGame();
                break;
        }
    }
}
