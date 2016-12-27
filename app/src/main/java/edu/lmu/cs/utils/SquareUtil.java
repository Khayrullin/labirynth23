package edu.lmu.cs.utils;


import javax.swing.*;
import java.awt.*;

import static java.awt.Color.green;

/**
 * Created by Лия on 22.12.2016.
 */
public class SquareUtil {

    private Square[] board;

    private int currentSquareLocation;
    private Square currentSquare;

    private final Color BREAKED_COLOR = Color.pink;
    private final Color FREE_COLOR = Color.white;
    private int ONE_LINE_SQUARES;

    private boolean opponent;

    private ImageIcon icon;

    private ImageIcon blue;
    private ImageIcon green;


    public SquareUtil(int oneLineSquares, int boardSize, int startLocation, boolean ifOpponent) {
        ONE_LINE_SQUARES = oneLineSquares;
        board = new Square[boardSize];
        this.opponent = ifOpponent;
        currentSquareLocation = startLocation;
    }

    public void setStartIcons(char mark) {
        ImageIcon opponentIcon;

        loadImg();
        if (mark == 'X') {
            icon = blue;
            opponentIcon = green;
        } else {
            icon = green;
            opponentIcon = blue;
        }

        if (opponent) {
            icon = opponentIcon;
        }
    }

    public JPanel initBoard() {

        JPanel boardPanel = new JPanel();
        boardPanel.setBackground(Color.black);//границы
        boardPanel.setLayout(new GridLayout(ONE_LINE_SQUARES, ONE_LINE_SQUARES * 2, 1, 1));
        for (int i = 0; i < board.length; i++) {
            board[i] = new Square();
            boardPanel.add(board[i]);
        }
        boardPanel.repaint();
        return boardPanel;
    }

    public void initNewCurSquare(int direction) {

        if (currentSquare != null) {
            currentSquare.removeIcon();

        }

        currentSquareLocation += direction;
        currentSquare = board[currentSquareLocation];
        if (!currentSquare.getColor().equals(BREAKED_COLOR)) {
            currentSquare.setColor(FREE_COLOR);
        }
        currentSquare.setIcon(icon);
        currentSquare.repaint();
    }

    public boolean squareIsWall(int direction) {
        Square wallSquare = board[currentSquareLocation + direction];
        if (!wallSquare.getColor().equals(Color.red)) {
            wallSquare.setColor(Color.black);
            wallSquare.repaint();
        } else {
            //square was bombed yet.
            return false;
        }
        return true;
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
