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
 *
 *  Client -> Server           Server -> Client
 *  ----------------           ----------------
 *  MOVE <n>  (0 <= n <= 8)    WELCOME <char>  (char in {X, O})
 *  QUIT                       VALID_MOVE
 *                             OTHER_PLAYER_MOVED <n>
 *                             VICTORY
 *                             DEFEAT
 *                             TIE
 *                             MESSAGE <text>
 *
 */
public class TicTacToeClient{
    private final int ONE_LINE = 9;
    private final int SIZE_SQUARE = ONE_LINE*ONE_LINE;
    private final int START_LOCATION = (int)Math.floor(SIZE_SQUARE/2);

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
            client.frame.setSize(350, 350);
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
private void initBoard(){
    JPanel boardPanel = new JPanel();
    boardPanel.setBackground(Color.black);//границы
    boardPanel.setLayout(new GridLayout(ONE_LINE, ONE_LINE*2, 1, 1));
    for (int i = 0; i < board.length; i++) {
        board[i] = new Square();
        boardPanel.add(board[i]);
    }

    frame.getContentPane().add(boardPanel, "Center");


}

    private void loadImg() {

        ImageIcon bl = createImageIcon("plX.png","Player O");
        blue = bl;


        ImageIcon gr = createImageIcon("head.png","Player X");
        green = gr;

        ImageIcon noMove = createImageIcon("noMove.png","No Move");
        noMove = noMove;

        ImageIcon kirp = createImageIcon("breakMove.png", "Move after bomb");
        moveAfterBomb = kirp;
    }

    private void initCurSquare(){
        currentSquare = board[currentSquareLocation];
        currentSquare.setColor(Color.white);
        currentSquare.setIcon(icon);
        currentSquare.repaint();
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
                } else
                {
                    icon = green;
                    opponentIcon = blue;
                }
                currentSquareLocation = START_LOCATION;
                initCurSquare();
                frame.setTitle("Player " + mark);
            }

            move();

          /*  while (true){
                response = in.readLine();
                if (response.startsWith("CURRENT_MOVED")) {
                    int direction = Integer.parseInt(response.substring(15));
                    int newDir = interpetateDirection(direction);
                    currentSquareLocation += newDir + currentSquareLocation;
                    initCurSquare();
                }
            }*/
            //TODO : ПОСМОТРЕТЬ ЭТО ГОВНИЩЕ, КАК НАХУЙ БЛЯТЬ УДАЛЯТЬ ЕБАНУЮ ИКОНКУ!!!!
            currentSquareLocation += 1;
            System.out.println(currentSquareLocation);
            initCurSquare();

            /*while (true) {
                response = in.readLine();
                if (response.startsWith("CURRENT MOVED")) {
                    int direction = Integer.parseInt(response.substring(15));
                    messageLabel.setText("Valid move, please wait");
                    currentSquare.setIcon(icon);
                    currentSquare.repaint();
                } else if (response.startsWith("OPPONENT_MOVED")) {
                    int loc = Integer.parseInt(response.substring(15));
                    board[loc].setIcon(opponentIcon);
                    board[loc].repaint();
                    messageLabel.setText("Opponent moved, your turn");
                } else if (response.startsWith("VICTORY")) {
                    messageLabel.setText("You win");
                    break;
                } else if (response.startsWith("DEFEAT")) {
                    messageLabel.setText("You lose");
                    break;
                } else if (response.startsWith("TIE")) {
                    messageLabel.setText("You tied");
                    break;
                } else if (response.startsWith("MESSAGE")) {
                    messageLabel.setText(response.substring(8));
                }
            }
            out.println("QUIT");
            */
        } finally {
            socket.close();
        }
    }
private int interpetateDirection(int direction){
    int newDir;
    switch (direction){
        case 1:
            newDir = 1;
            break;
        case 2:
            newDir = -ONE_LINE;
            break;
        case 3:
            newDir = -1;
            break;
        case 4:
            newDir = ONE_LINE;
            break;
        default:
            newDir = 0;
    }
    return newDir;
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
        //TODO: NAHUI REMOVE!
        public void removeIcon(Icon icon){
            this.remove((Component) icon);
        }
        public void setColor(Color color){
            this.setBackground(color);
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


    private void move(){
        frame.addKeyListener(new KeyAdapter(){
            @Override
            public void keyPressed(KeyEvent e) {
                int press = e.getKeyCode();
                String event = null;
                switch (press){
                    case KeyEvent.VK_LEFT :
                        event = "MOVE 1";
                        System.out.println("LEFT");
                        break;
                    case KeyEvent.VK_RIGHT :
                        event = "MOVE 3";
                        System.out.println("RIGHT");
                        break;
                    case KeyEvent.VK_UP :
                        event = "MOVE 2";
                        System.out.println("UP ");
                        break;
                    case KeyEvent.VK_DOWN :
                        event = "MOVE 4";
                        System.out.println("DOWN");
                        break;
                    case KeyEvent.VK_ENTER :
                        event = "PROP";
                        System.out.println("ENTER");
                        break;
                    case KeyEvent.VK_W :
                        event = "BOMB 2";
                        System.out.println("W");
                        break;
                    case KeyEvent.VK_A :
                        event = "BOMB 1";
                        System.out.println("A");
                        break;
                    case KeyEvent.VK_S :
                        event  = "BOMB 4";
                        System.out.println("S");
                        break;
                    case KeyEvent.VK_D :
                        event = "BOMB 3";
                        System.out.println("D");
                        break;
                    default:
                        event = "NONE";
                }
                out.println(event);
                frame.setFocusable(false);
            }
        });
    }


        public void actionPerformed() {
            frame.repaint();
        }



}