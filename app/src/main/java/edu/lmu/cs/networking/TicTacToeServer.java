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
 * <p>
 * Client -> Server           Server -> Client
 * ----------------           ----------------
 * MOVE <n>  (1 <= n <= 4)    WELCOME <char>  (char in {X, O})
 * BOMB <n> (1 <= n <= 4)     CURRENT (empty / Granit /shodil / cherny kvadrat/ won/ lose)
 * QUIT                       OTHER (empty / Granit /shodil / cherny kvadrat/ won/ lose)
 * PROP                       MESSAGE <text>
 * <p>
 * A second change is that it allows an unlimited number of pairs of
 * players to play.
 */
public class TicTacToeServer {

    public static volatile List<Game> gamesArchive = new ArrayList<Game>() {
    };

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

                Game.Player playerX = game.new Player(listener.accept(), 'X', game.getXPlayerPlace());

                Game.Player playerO = game.new Player(listener.accept(), 'O', game.getOPlayerPlace());

                playerX.setOpponent(playerO);

                playerO.setOpponent(playerX);

                game.currentPlayer = playerX;

                playerX.start();
                playerO.start();
            }
        } finally {
            listener.close();
        }
    }
}

