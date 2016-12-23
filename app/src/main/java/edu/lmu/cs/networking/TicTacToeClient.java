package edu.lmu.cs.networking;

import java.awt.*;
import java.awt.event.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import com.meo.SMSSender;
import edu.lmu.cs.utils.SquareUtil;

import static com.sun.java.accessibility.util.AWTEventMonitor.addKeyListener;

/**
 * A client for the TicTacToe game, modified and extended from the
 * class presented in Deitel and Deitel "Java How to Program" book.
 * I made a bunch of enhancements and rewrote large sections of the
 * code.  In particular I created the TTTP (Tic Tac Toe Protocol)
 * which is entirely text based.  Here are the strings that are sent:
 * <p>
 * Client -> Server           Server -> Client
 * ----------------           ----------------
 * MOVE <n>  (0 <= n <= 8)    WELCOME <char>  (char in {X, O})
 * QUIT                       VALID_MOVE
 * OTHER_PLAYER_MOVED <n>
 * VICTORY
 * DEFEAT
 * TIE
 * MESSAGE <text>
 */
public class TicTacToeClient {
    private final int ONE_LINE_SQUARES = 9;
    private final int BOARD_SIZE = ONE_LINE_SQUARES * ONE_LINE_SQUARES;
    private final int START_LOCATION = (int) Math.floor(BOARD_SIZE / 2);


    private JFrame frame = new JFrame("Bombermans");
    private JLabel messageLabel = new JLabel("");


    private SquareUtil squareUtil = SquareUtil.getInstance();


    private static int PORT = 8901;
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;


    private int direction;

    private boolean ingame = true;
    private boolean moved = false;

    /**
     * Runs the client as an application.
     */
    public static void main(String[] args) throws Exception {
        //SMSSender.smsSend("Game started","79047640086");
        String serverAddress = (args.length == 0) ? "localhost" : args[1];
        TicTacToeClient client = new TicTacToeClient(serverAddress);
        client.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        client.frame.setSize(350, 450);
        client.frame.setVisible(true);
        client.frame.setResizable(false);
        client.play();
    }


    /**
     * Constructs the client by connecting to a server, laying out the
     * GUI and registering GUI listeners.
     */
    public TicTacToeClient(String serverAddress) throws Exception {

        initFrame();

        socket = new Socket(serverAddress, PORT);
        in = new BufferedReader(new InputStreamReader(
                socket.getInputStream()));
        out = new PrintWriter(socket.getOutputStream(), true);

    }

    private void initFrame() {

        JPanel boardPanel = squareUtil.initBoard(ONE_LINE_SQUARES, BOARD_SIZE, START_LOCATION);
        frame.getContentPane().add(boardPanel, "Center");
        messageLabel.setSize(350, 100);
        messageLabel.setText("");
        frame.getContentPane().add(messageLabel, BorderLayout.NORTH);

    }


    public void play() throws Exception {


        String response;
        try {
            response = in.readLine();
            if (response.startsWith("WELCOME")) {
                char mark = response.charAt(8);
                squareUtil.setStartIcons(mark);
                squareUtil.initNewCurSquare(0);
                frame.setTitle("Player " + mark);
            }

            bindKeyListener();

            //проверка ответа
            while (true) {
                response = in.readLine();
                if (response != null) {
                    System.out.println(response);
                    if (response.endsWith("Your move") || (response.startsWith("OTHER") && (!response.startsWith("OTHER BLACK_KVAD")))) {
                        switchOnKeyListener();
                    }
                    if (response.startsWith("CURRENT")) {
                        if (response.startsWith("CURRENT MOVED")) {
                            move();
                        } else if (response.startsWith("CURRENT BLACK_KVAD")) {
                            stuckWithWall();
                        } else if (response.startsWith("CURRENT EMPTY")) {
                            freeWay();
                        } else if (response.startsWith("CURRENT GRANIT")) {
                            wallIsUnbreakable();
                        } else if (response.startsWith("CURRENT WON")) {
                            messageLabel.setText("You win!");
                            break;
                        } else if (response.startsWith("CURRENT LOSE")) {
                            messageLabel.setText("You lose! Ha-ha");
                            break;
                        } else if (response.startsWith("CURRENT VZORVAL")) {
                            bombedWoodenWall();
                        } else if (response.startsWith("CURRENT OTHER WAS HERE")) {
                            bombedWoodenWall();
                        }
                    } else if (response.startsWith("OTHER") && !response.contains("MOVED") && !(response.contains("BLACK_KVAD"))) {
                        System.out.println("Запускаюсь");
                        switchOnKeyListener();
                    }
                } else {
                    switchOnKeyListener();
                }
            }

        } finally{
            try {
                socket.close();
            } catch (IOException e) {
            }
        }
    }

    private void stuckWithWall() {
        squareUtil.squareIsWall(direction);
        messageLabel.setText("Здесь неизвестная стена. Вы можете её взорвать.");
    }

    private void move() {
        squareUtil.initNewCurSquare(direction);
        messageLabel.setText("Можете взорвать клетку или пропустить ход.");
    }

    private void freeWay() {
        squareUtil.squareIsFreeToGO(direction);
        messageLabel.setText("Тут ничего нет");
    }

    private void wallIsUnbreakable() {
        squareUtil.squareIsGranit(direction);
        messageLabel.setText("Эта стена - невзрываемый гранит.");
    }

    private void bombedWoodenWall() {
        squareUtil.squareAfterBrick(direction);
        messageLabel.setText("Здесь был кирпич");
    }

    private void switchOnKeyListener() {
        frame.setFocusable(true);
        frame.requestFocus();
    }


    private void bindKeyListener() {

        frame.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                int press = e.getKeyCode();
                String event = null;
                switch (press) {
                    case KeyEvent.VK_LEFT:
                        if (moved) {
                            break;
                        } else {
                            event = "MOVE 1";
                            System.out.println("LEFT");
                            direction = -1;
                            break;
                        }
                    case KeyEvent.VK_RIGHT:
                        if (moved) {
                            break;
                        } else {
                            event = "MOVE 3";
                            System.out.println("RIGHT");
                            direction = 1;
                            break;
                        }
                    case KeyEvent.VK_UP:
                        if (moved) {
                            break;
                        } else {
                            event = "MOVE 2";
                            System.out.println("UP ");
                            direction = 0 - ONE_LINE_SQUARES;
                            break;
                        }
                    case KeyEvent.VK_DOWN:
                        if (moved) {
                            break;
                        } else {
                            event = "MOVE 4";
                            System.out.println("DOWN");
                            direction = ONE_LINE_SQUARES;
                            break;
                        }
                    case KeyEvent.VK_ENTER:
                        event = "PROP";
                        System.out.println("ENTER");
                        break;
                    case KeyEvent.VK_W:
                        event = "BOMB 2";
                        System.out.println("W");
                        direction = 0 - ONE_LINE_SQUARES;
                        break;
                    case KeyEvent.VK_A:
                        event = "BOMB 1";
                        System.out.println("A");
                        direction = -1;
                        break;
                    case KeyEvent.VK_S:
                        event = "BOMB 4";
                        System.out.println("S");
                        direction = ONE_LINE_SQUARES;
                        break;
                    case KeyEvent.VK_D:
                        event = "BOMB 3";
                        System.out.println("D");
                        direction = 1;
                        break;
                    default:
                        event = "WRONG KEY";
                }
                System.out.println(event);
                if (event != null && !event.equals("WRONG KEY")) {
                    out.println(event);
                    if (event.startsWith("MOVE")) {
                        moved = true;
                        switchOnKeyListener();
                    } else {
                        frame.setFocusable(false);
                        moved = false;
                    }
                } else {
                    messageLabel.setText("Введена неверная клавиша!Попробуй ещё");
                }


            }
        });
    }



}