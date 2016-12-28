package edu.lmu.cs.networking;

import edu.lmu.cs.utils.SquareUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

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
    private JFrame frameOpponent = new JFrame("Opponent");

    private JLabel messageLabel = new JLabel("");

    private boolean isCurrentMessageLabel;


    private SquareUtil squareUtil;
    private SquareUtil opponentSquareUtil;
    private SquareUtil currentSquareUtil;

    private static int PORT = 8901;
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;


    private int direction;

    private boolean moved = false;

    /**
     * Runs the client as an application.
     */
    public static void main(String[] args) {
        //SMSSender.smsSend("Game started","79047640086");
        String serverAddress = (args.length == 0) ? "localhost" : args[1];
        TicTacToeClient client = new TicTacToeClient(serverAddress);
        client.play();

    }


    /**
     * Constructs the client by connecting to a server, laying out the
     * GUI and registering GUI listeners.
     */
    public TicTacToeClient(String serverAddress) {

        try {
            socket = new Socket(serverAddress, PORT);
            in = new BufferedReader(new InputStreamReader(
                    socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(frame, "Не удалось подключиться к серверу. Сервер недоступен");
            System.exit(-1);
        }

        initFrame();
    }


    private void initFrame() {
        squareUtil = new SquareUtil(ONE_LINE_SQUARES, BOARD_SIZE, START_LOCATION, false);
        opponentSquareUtil = new SquareUtil(ONE_LINE_SQUARES, BOARD_SIZE, START_LOCATION, true);

        JPanel boardPanel = squareUtil.initBoard();
        JPanel boardPanelOpponent = opponentSquareUtil.initBoard();

        frame.getContentPane().add(boardPanel, "Center");

        messageLabel.setSize(350, 100);
        messageLabel.setText("");
        frame.getContentPane().add(messageLabel, BorderLayout.NORTH);

        frameOpponent.getContentPane().add(boardPanelOpponent, "Center");


        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(350, 450);
        frame.setVisible(true);
        frame.setResizable(false);

        frameOpponent.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frameOpponent.setSize(350, 450);
        frameOpponent.setVisible(true);
        frameOpponent.setResizable(false);
    }

    private void clearFrame() {
        frame.getContentPane().removeAll();
    }

    public void play() {

        String response;
        try {
            response = in.readLine();

            if (response.startsWith("WELCOME")) {
                char mark = response.charAt(8);
                squareUtil.setStartIcons(mark);
                squareUtil.initNewCurSquare(0);
                opponentSquareUtil.setStartIcons(mark);
                opponentSquareUtil.initNewCurSquare(0);
                frame.setTitle("Player " + mark);
            }

            bindKeyListener();
            frame.setFocusable(false);

            response = in.readLine();
            System.out.println(response);

            while (response.startsWith("MESSAGE")) {
                response = in.readLine();
                if (response.endsWith("All players connected")) {
                    System.out.println(response);
                    response = in.readLine();
                    System.out.println(response);
                    if (response.endsWith("Your move") || response.endsWith("Other move")) {
                        if (response.endsWith("Your move")) {
                            switchOnKeyListener();
                            messageLabel.setText("Ваш ход");
                        } else {
                            messageLabel.setText("Ход противника");
                        }
                        break;
                    }

                }
            }
            //проверка ответа
            while (true) {
                response = in.readLine();
                if (response != null) {
                    System.out.println(response);
                    if (response.startsWith("CURRENT")) {
                        currentSquareUtil = squareUtil;
                        isCurrentMessageLabel = true;
                        if (response.endsWith("END")) {
                            messageLabel.setText("Ход противника");
                        }
                    } else if (response.startsWith("OTHER")) {

                        char opponentDirection = response.charAt(6);

                        direction = setOpponentDirection(opponentDirection);

                        currentSquareUtil = opponentSquareUtil;
                        isCurrentMessageLabel = false;

                        if (response.endsWith("END")) {
                            currentSquareUtil = squareUtil;
                            isCurrentMessageLabel = true;
                            messageLabel.setText("Ваш ход");
                            switchOnKeyListener();
                        }

                    } else if (response.startsWith("MESSAGE ?")) {
                        if (isCurrentMessageLabel) {
                            messageLabel.setText("Дождитесь вашего хода.");
                        }
                    }

                    if (response.endsWith("MOVED")) {
                        move();
                    } else if (response.endsWith("BLACK_KVAD")) {
                        stuckWithWall();
                    } else if (response.endsWith("EMPTY")) {
                        freeWay();
                    } else if (response.endsWith("GRANIT")) {
                        wallIsUnbreakable();
                    } else if (response.endsWith("WON")) {
                        messageLabel.setText("You win!");
                        endOfGame();
                    } else if (response.endsWith("LOSE")) {
                        messageLabel.setText("You lose! Ha-ha");
                        endOfGame();
                    } else if (response.endsWith("VZORVAL")) {
                        bombedWoodenWall();
                    } else if (response.endsWith("VZORVAL KLADKU")) {
                        bombedWoodenWall();
                    } else if ((response.endsWith("OTHER WAS HERE")) || (response.endsWith("CURRENT WAS HERE"))) {
                        bombedWoodenWall();
                        move();
                    }
                } else {
                    switchOnKeyListener();
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    private void stuckWithWall() {
        boolean wasNotBombed = currentSquareUtil.squareIsWall(direction);
        if (isCurrentMessageLabel) {
            if (wasNotBombed) {
                messageLabel.setText("Здесь неизвестная стена. Вы можете её взорвать.");
            } else {
                messageLabel.setText("Эта стена - невзрываемый гранит.Повторите ход");
                moved = false;
            }
        }
    }

    private void move() {
        currentSquareUtil.initNewCurSquare(direction);
        if (isCurrentMessageLabel) {
            messageLabel.setText("Можете взорвать клетку или пропустить ход.");
        }

    }

    private void freeWay() {
        currentSquareUtil.squareIsFreeToGO(direction);
        if (isCurrentMessageLabel) {
            messageLabel.setText("Тут ничего нет");
        }
    }

    private void wallIsUnbreakable() {
        currentSquareUtil.squareIsGranit(direction);
        if (isCurrentMessageLabel) {
            messageLabel.setText("Эта стена - невзрываемый гранит.");
        }
    }

    private void bombedWoodenWall() {
        currentSquareUtil.squareAfterBrick(direction);
        if (isCurrentMessageLabel) {
            messageLabel.setText("Здесь был кирпич");
        }
    }

    private void switchOnKeyListener() {
        frame.setFocusable(true);
        frame.requestFocus();
    }

    private boolean wantsToPlayAgain() {
        int response = JOptionPane.showConfirmDialog(frame,
                "Want to play again?",
                "Bombing is Fun Fun Fun",
                JOptionPane.YES_NO_OPTION);
        return response == JOptionPane.YES_OPTION;
    }

    private void endOfGame() {
        if (wantsToPlayAgain()) {
            out.println("RESTART");
            clearFrame();
            initFrame();
            play();
        } else {
            out.print("QUIT");
            frame.dispose();
            frameOpponent.dispose();
        }
    }

    private int setOpponentDirection(char direction) {
        switch (direction) {
            case '1':
                return -1;
            case '2':
                return (0 - ONE_LINE_SQUARES);
            case '3':
                return 1;
            case '4':
                return ONE_LINE_SQUARES;
        }
        return 0;
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
                    case KeyEvent.VK_Q:
                        event = "QUIT";
                        System.out.println("Q");
                        endOfGame();
                        break;
                    default:
                        event = "WRONG KEY";
                }
                System.out.println(event);
                if (event != null && !event.equals("WRONG KEY") && !(currentSquareUtil.outOfBorder(direction))) {
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
                    direction = 0;
                }


            }
        });
    }


}