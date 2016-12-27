package edu.lmu.cs.utils;


import javax.swing.*;
import java.awt.*;

import static java.awt.Color.green;

/**
 * Created by Лия on 22.12.2016.
 */
public class SquareUtil {

    private Square[] board;
    private Square[] boardForOpponent;

    private int currentSquareLocation;
    private Square currentSquare;

    private int currentSquareLocationOpponent;
    private Square currentSquareOpponent;

    private final Color BREAKED_COLOR = Color.pink;
    private final Color FREE_COLOR = Color.white;
    private int ONE_LINE_SQUARES;
    private ImageIcon icon;
    private ImageIcon opponentIcon;

    private ImageIcon blue;
    private ImageIcon green;

   /* private  ImageIcon noMove;
    private ImageIcon moveAfterBomb;
*/

    public void setStartIcons(char mark) {
        loadImg();
        if (mark == 'X') {
            icon = blue;
            opponentIcon = green;
        } else {
            icon = green;
            opponentIcon = blue;
        }
    }
    /*
    Достает сущность класса для того, чтобы добиться эффекта - сигнлтона
     */
    private static class SquareUtilHolder {
        static final SquareUtil HOLDER_INSTANCE = new SquareUtil();
    }

    public static SquareUtil getInstance() {
        return SquareUtilHolder.HOLDER_INSTANCE;
    }


    private SquareUtil(){

    }


    public JPanel initBoard(int oneLineSquares, int boardSize, int startLocation, boolean current) {
        ONE_LINE_SQUARES = oneLineSquares;
        Square[] board = new Square[boardSize];
        JPanel boardPanel = new JPanel();
        boardPanel.setBackground(Color.black);//границы
        boardPanel.setLayout(new GridLayout(oneLineSquares, oneLineSquares * 2, 1, 1));
        for (int i = 0; i < board.length; i++) {
            board[i] = new Square();
            boardPanel.add(board[i]);
        }
        currentSquareLocation = startLocation;
        currentSquareLocationOpponent = startLocation;

        if (current) {
            this.board = board;
        } else {
            this.boardForOpponent = board;
        }
        return boardPanel;
    }

    public void initNewCurSquare(int direction, boolean current) {

        Square square;
        int squareLocation;
        Square[] board;
        ImageIcon icon;

        if (current) {
            square = currentSquare;
            squareLocation = currentSquareLocation;
            board = this.board;
            icon = this.icon;
        } else {
            square = currentSquareOpponent;
            squareLocation = currentSquareLocationOpponent;
            board = this.boardForOpponent;
            icon = this.opponentIcon;
        }

        if (square != null) {
            square.removeIcon();
        }

        squareLocation += direction;
        square = board[squareLocation];
        if (!square.getColor().equals(BREAKED_COLOR)) {
            square.setColor(FREE_COLOR);
        }
        square.setIcon(icon);
        square.repaint();
    }

    public  void squareIsWall(int direction) {
        Square wallSquare = board[currentSquareLocation + direction];
        wallSquare.setColor(Color.black);
        wallSquare.repaint();
    }

    public  void squareIsGranit(int direction) {
        Square granitSquare = board[currentSquareLocation + direction];
        granitSquare.setColor(Color.red);
        granitSquare.repaint();
    }

    public  void squareIsFreeToGO(int direction) {
        Square freeSquare = board[currentSquareLocation + direction];
        freeSquare.setColor(FREE_COLOR);
        freeSquare.repaint();
    }

    public  void squareAfterBrick(int direction) {
        Square emptySquare = board[currentSquareLocation + direction];
        if (!emptySquare.getColor().equals(FREE_COLOR)){
            emptySquare.setColor(BREAKED_COLOR);
            emptySquare.repaint();
        }

    }

    public boolean outOfBorder(int direction) {
        System.out.println(currentSquareLocation);
        for (int i = 1; i < ONE_LINE_SQUARES; i++) {
            if (((currentSquareLocation == (ONE_LINE_SQUARES * i - 1)) && (direction == 1))
                    || ((currentSquareLocation == ONE_LINE_SQUARES * i) && (direction == -1))) {
                return true;
            }
        }
        return ((currentSquareLocation + direction) >= ONE_LINE_SQUARES * ONE_LINE_SQUARES || (currentSquareLocation + direction) < 0);
    }

    private static class Square extends JPanel {

        JLabel label = new JLabel((Icon) null);

        Square() {
            setBackground(Color.gray);// фон всего
            add(label);
        }

        void setIcon(Icon icon) {
            label.setIcon(icon);
        }


        void removeIcon() {
            this.setIcon(null);
        }

        void setColor(Color color) {
            this.setBackground(color);
        }

        Color getColor() {
            return this.getBackground();
        }
    }

    private void loadImg() {
        blue = createImageIcon("plX.png", "Player O");
        green = createImageIcon("head.png", "Player X");
        /*noMove = createImageIcon("noMove.png", "No Move");
        moveAfterBomb = createImageIcon("breakMove.png", "Move after bomb");*/

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

}
