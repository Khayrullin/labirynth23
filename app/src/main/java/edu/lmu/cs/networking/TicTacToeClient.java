package edu.lmu.cs.networking;

import java.awt.*;
import java.awt.event.*;
import java.io.BufferedReader;
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
    private final int ONE_LINE = 9;
    private final int SIZE_SQUARE = ONE_LINE * ONE_LINE;
    private final int START_LOCATION = (int) Math.floor(SIZE_SQUARE / 2);

    private final Color BREAKED_COLOR = Color.pink;
    private final Color FREE_COLOR = Color.white;

    private JFrame frame = new JFrame("Bombermans");
    private JLabel messageLabel = new JLabel("");
    private ImageIcon icon;
    private ImageIcon opponentIcon;

    private Square[] board = new Square[SIZE_SQUARE];
    private Square[] boardForOpponent = new Square[SIZE_SQUARE];

    private int currentSquareLocation;
    public Square currentSquare;


    private static int PORT = 8901;
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;


    private int direction;

    private boolean ingame = true;

    private ImageIcon moveAfterBomb;
    private ImageIcon noMove;
    private ImageIcon green;
    private ImageIcon blue;


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

        initBoard();

        socket = new Socket(serverAddress, PORT);
        in = new BufferedReader(new InputStreamReader(
                socket.getInputStream()));
        out = new PrintWriter(socket.getOutputStream(), true);

    }

    private void initBoard() {
        JPanel boardPanel = new JPanel();
        boardPanel.setBackground(Color.black);//границы
        boardPanel.setLayout(new GridLayout(ONE_LINE, ONE_LINE * 2, 1, 1));
        for (int i = 0; i < board.length; i++) {
            board[i] = new Square();
            boardPanel.add(board[i]);
        }

        frame.getContentPane().add(boardPanel, "Center");
        messageLabel.setSize(350,100);
        frame.getContentPane().add(messageLabel, BorderLayout.NORTH);

    }

    private void loadImg() {

        ImageIcon bl = createImageIcon("plX.png", "Player O");
        blue = bl;


        ImageIcon gr = createImageIcon("head.png", "Player X");
        green = gr;

        ImageIcon noMove = createImageIcon("noMove.png", "No Move");
        noMove = noMove;

        ImageIcon kirp = createImageIcon("breakMove.png", "Move after bomb");
        moveAfterBomb = kirp;
    }


    private void initNewCurSquare() {
        currentSquare.removeIcon();
        currentSquareLocation += direction;
        currentSquare = board[currentSquareLocation];
        if (!currentSquare.getColor().equals(BREAKED_COLOR)) {
            currentSquare.setColor(FREE_COLOR);
        }
        currentSquare.setIcon(icon);
        currentSquare.repaint();
    }

    private void squareIsWall() {
        Square wallSquare = board[currentSquareLocation + direction];
        wallSquare.setColor(Color.black);
        wallSquare.repaint();
        messageLabel.setText("Здесь неизвестная стена");
    }

    private void squareIsGranit() {
        Square granitSquare = board[currentSquareLocation + direction];
        granitSquare.setColor(Color.red);
        granitSquare.repaint();
        messageLabel.setText("Эта стена - невзрываемый гранит.");
    }

    private void squareIsFreeToGO() {
        Square freeSquare = board[currentSquareLocation + direction];
        freeSquare.setColor(FREE_COLOR);
        freeSquare.repaint();
        messageLabel.setText("Тут ничего нет");
    }

    private void squareAfterBrick() {
        Square emptySquare = board[currentSquareLocation + direction];
        if (!emptySquare.getColor().equals(FREE_COLOR)){
            emptySquare.setColor(BREAKED_COLOR);
            emptySquare.repaint();
        }
        messageLabel.setText("Здесь был кирпич");

    }

    public void play() throws Exception {

        loadImg();

        String response;
        try {
            response = in.readLine();
            if (response.startsWith("WELCOME")) {
                char mark = response.charAt(8);
                if (mark == 'X') {
                    icon = blue;
                    opponentIcon = green;
                } else {
                    icon = green;
                    opponentIcon = blue;
                }
                currentSquareLocation = START_LOCATION;
                currentSquare = board[currentSquareLocation];
                currentSquare.setColor(Color.white);
                currentSquare.setIcon(icon);
                currentSquare.repaint();
                frame.setTitle("Player " + mark);

            }

            bindKeyListener();

            //проверка ответа

            while (true) {
                response = in.readLine();
                if (response != null) {
                    System.out.println(response);
                    if (response.endsWith("Your move") || (response.startsWith("OTHER") && (!response.startsWith("OTHER BLACK_KVAD")))) {
                        frame.setFocusable(true);
                    }
                    if (response.startsWith("CURRENT")) {
                        if (response.startsWith("CURRENT MOVED")) {
                            initNewCurSquare();
                        } else if (response.startsWith("CURRENT BLACK_KVAD")) {
                            squareIsWall();
                            frame.setFocusable(true);
                            frame.requestFocus();
                        } else if (response.startsWith("CURRENT EMPTY")) {
                            squareIsFreeToGO();
                        } else if (response.startsWith("CURRENT GRANIT")) {
                            squareIsGranit();
                        } else if (response.startsWith("CURRENT WON")) {
                            messageLabel.setText("You win!");
                            break;
                        } else if (response.startsWith("CURRENT LOSE")) {
                            messageLabel.setText("You lose! Ha-ha");
                            break;
                        } else if (response.startsWith("CURRENT VZORVAL")) {
                            squareAfterBrick();
                        } else if (response.startsWith("CURRENT OTHER WAS HERE")) {
                            squareAfterBrick();
                        }
                    } else if (response.startsWith("OTHER") && (!response.startsWith("OTHER BLACK_KVAD"))) {
                        System.out.println("Я запускаюсь");
                        frame.setFocusable(true);
                    }
                }else {
                    frame.setFocusable(true);
                }
            }



        } finally {
            socket.close();
        }
    }


    static class Square extends JPanel {

        JLabel label = new JLabel((Icon) null);

        public Square() {
            setBackground(Color.gray);// фон всего
            add(label);
        }

        public void setIcon(Icon icon) {
            label.setIcon(icon);
        }


        public void removeIcon() {
            this.setIcon(null);
        }

        public void setColor(Color color) {
            this.setBackground(color);
        }

        public Color getColor() {
            return this.getBackground();
        }
    }

    /**
     * Returns an ImageIcon, or null if the path was invalid.
     */
    protected ImageIcon createImageIcon(String path,
                                        String description) {

        java.net.URL imgURL = getClass().getClassLoader().getResource(path);//ClassLoader.getSystemResource(path);
        System.out.println(imgURL);
        if (imgURL != null) {
            //ImageIcon(this.getClass().getResource("/images/filename.png"));

            return new ImageIcon(imgURL, description);
        } else {
            System.err.println("Couldn't find file: " + path);
            return null;
        }
    }


    private void bindKeyListener() {
        frame.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                int press = e.getKeyCode();
                String event;

                switch (press) {
                    case KeyEvent.VK_LEFT:
                        event = "MOVE 1";
                        System.out.println("LEFT");
                        direction = -1;
                        break;
                    case KeyEvent.VK_RIGHT:
                        event = "MOVE 3";
                        System.out.println("RIGHT");
                        direction = 1;
                        break;
                    case KeyEvent.VK_UP:
                        event = "MOVE 2";
                        System.out.println("UP ");
                        direction = 0 - ONE_LINE;
                        break;
                    case KeyEvent.VK_DOWN:
                        event = "MOVE 4";
                        System.out.println("DOWN");
                        direction = ONE_LINE;
                        break;
                    case KeyEvent.VK_ENTER:
                        event = "PROP";
                        System.out.println("ENTER");
                        break;
                    case KeyEvent.VK_W:
                        event = "BOMB 2";
                        System.out.println("W");
                        direction = 0 - ONE_LINE;
                        break;
                    case KeyEvent.VK_A:
                        event = "BOMB 1";
                        System.out.println("A");
                        direction = -1;
                        break;
                    case KeyEvent.VK_S:
                        event = "BOMB 4";
                        System.out.println("S");
                        direction = ONE_LINE;
                        break;
                    case KeyEvent.VK_D:
                        event = "BOMB 3";
                        System.out.println("D");
                        direction = 1;
                        break;
                    default:
                        event = "WRONG KEY";
                }
                out.println(event);
                System.out.println(event);
                if (!event.equals("WRONG KEY")) {
                    frame.setFocusable(false);
                }else{
                    messageLabel.setText("Введена неверная клавиша!Попробуй ещё");
                }


            }
        });
    }


    public void actionPerformed() {
        frame.repaint();
    }


}