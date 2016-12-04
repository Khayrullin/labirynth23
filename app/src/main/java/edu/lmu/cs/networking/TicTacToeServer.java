package edu.lmu.cs.networking;


import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.List;

/**
 * A server for a network multi-player tic tac toe game.  Modified and
 * extended from the class presented in Deitel and Deitel "Java How to
 * Program" book.  I made a bunch of enhancements and rewrote large sections
 * of the code.  The main change is instead of passing *data* between the
 * client and server, I made a TTTP (tic tac toe protocol) which is totally
 * plain text, so you can test the game with Telnet (always a good idea.)
 * The strings that are sent in TTTP are:
 *
 *  Client -> Server           Server -> Client
 *  ----------------           ----------------
 *  MOVE <n>  (0 <= n <= 8)    WELCOME <char>  (char in {X, O})
 *  BOMBTHIS <n>               BOMBED_OR (empty / Granit / Vzorval Sopernika)
 *  QUIT                       MOVED_OR (shodil / cherny kvadrat)
 *
 *                             OTHER_PLAYER_MOVED <n> (result)
 *                             OTHER_PLAYER_BOMBED
 *                             VICTORY
 *
 *                             LOSE
 *                             MESSAGE <text>
 *
 * A second change is that it allows an unlimited number of pairs of
 * players to play.
 */
public class TicTacToeServer {

    public static volatile List<Game> gamesArchive = new ArrayList<Game>(){};

    /**
     * Runs the application. Pairs up clients that connect.
     */
    public static void main(String[] args) throws Exception {
        ServerSocket listener = new ServerSocket(8901);
        System.out.println("Bomber Server is Running");
        try {
            while (true) {
                Game game = new Game();
                gamesArchive.add(game);
                System.out.println("1");
                Game.Player playerX = game.new Player(listener.accept(), 'X', game.getXPlayerPlace());
                System.out.println("2");
                Game.Player playerO = game.new Player(listener.accept(), 'O', game.getOPlayerPlace());
                System.out.println("3");
                playerX.setOpponent(playerO);
                System.out.println("4");
                playerO.setOpponent(playerX);
                System.out.println("5");
                game.currentPlayer = playerX;

                playerX.start();
                playerO.start();
            }
        } finally {
            listener.close();
        }
    }
}

