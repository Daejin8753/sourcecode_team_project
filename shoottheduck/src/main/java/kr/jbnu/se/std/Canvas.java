package kr.jbnu.se.std;


import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import javax.swing.JPanel;

/**
 * JPanel을 생성하고, 그 위에 그리기를 수행하며 키보드와 마우스 이벤트를 수신함.
 *
 * @author www.gametutorial.net
 */

public abstract class Canvas extends JPanel implements KeyListener, MouseListener {

    // 키보드 상태 - 키보드 키 상태를 저장함 - 누른 상태인지 아닌지.
    private static boolean[] keyboardState = new boolean[525];

    // 마우스 상태 - 마우스 버튼 상태를 저장함 - 누른 상태인지 아닌지.
    private static boolean[] mouseState = new boolean[3];


    public Canvas()
    {
        // 더블 버퍼를 사용하여 화면에 그림을 그림.
        this.setDoubleBuffered(true);
        this.setFocusable(true);
        this.setBackground(Color.black);

        // 직접 마우스 커서를 그리거나 마우스 커서를 숨기고 싶다면,
        // "true"를 넣으면 마우스 커서가 사라짐.
        if(true)
        {
            BufferedImage blankCursorImg = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
            Cursor blankCursor = Toolkit.getDefaultToolkit().createCustomCursor(blankCursorImg, new Point(0, 0), null);
            this.setCursor(blankCursor);
        }

        // JPanel에 키보드 리스너를 추가하여 이 컴포넌트로부터 키 이벤트를 수신함.
        this.addKeyListener(this);
        // JPanel에 마우스 리스너를 추가하여 이 컴포넌트로부터 마우스 이벤트를 수신함.
        this.addMouseListener(this);
    }


    // 이 메서드는 kr.jbnu.se.std.Framework.java에서 오버라이딩 되며, 화면에 그리기 위해 사용됨.
    public abstract void Draw(Graphics2D g2d);

    @Override
    public void paintComponent(Graphics g)
    {
        Graphics2D g2d = (Graphics2D)g;
        super.paintComponent(g2d);
        Draw(g2d);
    }


    // 키보드
    /**
     * 키보드 키 "key"가 눌려 있는지 확인함.
     *
     * @param key 상태를 확인하고자 하는 키의 번호.
     * @return 키가 눌려 있으면 true, 그렇지 않으면 false.
     */
    public static boolean keyboardKeyState(int key)
    {
        return keyboardState[key];
    }

    // 키보드 리스너의 메서드들.
    @Override
    public void keyPressed(KeyEvent e)
    {
        keyboardState[e.getKeyCode()] = true;
    }

    @Override
    public void keyReleased(KeyEvent e)
    {
        keyboardState[e.getKeyCode()] = false;
        keyReleasedFramework(e);
    }

    @Override
    public void keyTyped(KeyEvent e) { }

    public abstract void keyReleasedFramework(KeyEvent e);


    // 마우스
    /**
     * 마우스 버튼 "button"이 눌려 있는지 확인함.
     * 파라미터 "button"은 "MouseEvent.BUTTON1" - 마우스 버튼 #1을 나타내거나,
     * "MouseEvent.BUTTON2" - 마우스 버튼 #2 등을 나타냄.
     *
     * @param button 상태를 확인하고자 하는 마우스 버튼의 번호.
     * @return 버튼이 눌려 있으면 true, 그렇지 않으면 false.
     */
    public static boolean mouseButtonState(int button)
    {
        return mouseState[button - 1];
    }

    // 마우스 키 상태 설정.
    private void mouseKeyStatus(MouseEvent e, boolean status)
    {
        if(e.getButton() == MouseEvent.BUTTON1)
            mouseState[0] = status;
        else if(e.getButton() == MouseEvent.BUTTON2)
            mouseState[1] = status;
        else if(e.getButton() == MouseEvent.BUTTON3)
            mouseState[2] = status;
    }

    // 마우스 리스너의 메서드들.
    @Override
    public void mousePressed(MouseEvent e)
    {
        mouseKeyStatus(e, true);
    }

    @Override
    public void mouseReleased(MouseEvent e)
    {
        mouseKeyStatus(e, false);
    }

    @Override
    public void mouseClicked(MouseEvent e) { }

    @Override
    public void mouseEntered(MouseEvent e) { }

    @Override
    public void mouseExited(MouseEvent e) { }

}
