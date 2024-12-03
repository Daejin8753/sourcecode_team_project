package kr.jbnu.se.std;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import java.awt.*;

/**
 * 프레임을 생성하고 속성을 설정함.
 *
 * @author www.gametutorial.net
 */

public class Window extends JFrame{

    private Window()
    {
        // 프레임의 제목을 설정함.
        this.setTitle("Shoot the duck");

        // 프레임의 크기를 설정함.
        if(false) // 전체 화면 모드
        {
            // 이 프레임의 장식을 비활성화함.
            this.setUndecorated(true);
            // 프레임을 전체 화면으로 설정함.
            this.setExtendedState(Frame.MAXIMIZED_BOTH);
        }
        else // 창 모드
        {
            // 프레임의 크기 설정.
            this.setSize(800, 600);
            // 화면 중앙에 프레임을 위치시킴.
            this.setLocationRelativeTo(null);
            // 프레임의 크기를 사용자가 조정할 수 없도록 설정함.
            this.setResizable(false);
        }

        // 사용자가 프레임을 닫을 때 애플리케이션을 종료함.
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        this.setContentPane(new Framework());

        this.setVisible(true);
    }

    public static void main(String[] args)
    {
        // 이벤트 디스패치 스레드를 사용하여 스레드 안전을 위해 UI를 빌드함.
        SwingUtilities.invokeLater(Window::new);
    }
}
