package edu.lmu.cs.networking;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;


public class Game {


    private int XPlayerPlace;

    public int getXPlayerPlace() {
        return XPlayerPlace;
    }

    public int getOPlayerPlace() {
        return OPlayerPlace;
    }

    private int OPlayerPlace;
    private final int PROCENT_OF_BLOCKS = 60;

    Player currentPlayer;
    private Object[] board = {
            null, null, null, null, null,
            null, null, null, null, null,
            null, null, null, null, null,
            null, null, null, null, null,
            null, null, null, null, null,
    };


    public boolean hasWinner() {
        return
                (board[0] != null && board[0] == board[1] && board[0] == board[2])
                        || (board[3] != null && board[3] == board[4] && board[3] == board[5])
                        || (board[6] != null && board[6] == board[7] && board[6] == board[8])
                        || (board[0] != null && board[0] == board[3] && board[0] == board[6])
                        || (board[1] != null && board[1] == board[4] && board[1] == board[7])
                        || (board[2] != null && board[2] == board[5] && board[2] == board[8])
                        || (board[0] != null && board[0] == board[4] && board[0] == board[8])
                        || (board[2] != null && board[2] == board[4] && board[2] == board[6]);
    }

    public Game() {
        fillTheBoard();
    }

    /**
     * TODO:
     * Zapolnyaem dosku igrokami i prepyatstviyami:
     * P.S sorry for govnokod :)
     */
    private void fillTheBoard() {
        XPlayerPlace = (int) (Math.random() * 25);
        OPlayerPlace = (int) (Math.random() * 25);
        while (XPlayerPlace == OPlayerPlace) {
            OPlayerPlace = (int) (Math.random() * 25);
        }

        int a, b;
        if (XPlayerPlace < OPlayerPlace) {
            a = XPlayerPlace;
            b = OPlayerPlace;
        } else {
            a = OPlayerPlace;
            b = XPlayerPlace;
        }

        while (b - a >= 5) {
            board[b] = "Path";
            b = b - 5;
        }
        if (b > a) {
            while (b - a > 0) {
                board[b] = "Path";
                b = b - 1;
            }
        } else {
            while (a - b > 0) {
                board[b] = "Path";
                b = b + 1;
            }
        }



        for (int i = 0; i < board.length; i++) {
            if (board[i] == null) {
                if (PROCENT_OF_BLOCKS > (int) (Math.random() * 100)) {
                    if (1 == (int) (Math.random() * 4)) {
                        board[i] = Block.IMMORTAL;
                    } else {
                        board[i] = Block.BRICK;
                    }
                }
            }
        }

        for (int i = 0; i < board.length; i++) {
            if (board[i] == "Path") {
                board[i] = null;
            }
        }

        //Vizualizaciya sgenerirovannoy doski:

//        int j = 0;
//     board[XPlayerPlace] = "X";
//     board[OPlayerPlace] = "O";
//        for (int k = 0; k < board.length; k++) {
//            System.out.print(board[k] + ",      ");
//            j = 1 + j;
//            if (j == 5) {
//                System.out.println();
//                j = 0;
//            }
//        }
    }


    public boolean bombThatShit(int location, Player player) {
       //TODO create method and add "win" Checking
        return true;
    }

    public synchronized boolean canIMove(int location, Player player) {
        if (player == currentPlayer && board[location] != Block.BRICK &&
                board[location] != Block.IMMORTAL) {
            currentPlayer.setLocation(location);
            currentPlayer.opponent.otherPlayerMoved(location);
            return true;
        }
        return false;
    }

    /**
     * The class for the helper threads in this multithreaded server
     * application.  A Player is identified by a character mark
     * which is either 'X' or 'O'.  For communication with the
     * client the player has a socket with its input and output
     * streams.  Since only text is being communicated we use a
     * reader and a writer.
     */
    class Player extends Thread {
        char mark;
        Player opponent;
        Socket socket;
        BufferedReader input;
        PrintWriter output;
        int location;

        public int getLocation() {
            return location;
        }

        public void setLocation(int location) {
            this.location = location;
        }



        /**
         * Constructs a handler thread for a given socket and mark
         * initializes the stream fields, displays the first two
         * welcoming messages.
         */
        public Player(Socket socket, char mark, int location) {
            this.location = location;
            this.socket = socket;
            this.mark = mark;
            try {
                input = new BufferedReader(
                        new InputStreamReader(socket.getInputStream()));
                output = new PrintWriter(socket.getOutputStream(), true);
                output.println("WELCOME " + mark);
                output.println("MESSAGE Waiting for opponent to connect");
            } catch (IOException e) {
                System.out.println("Player died: " + e);
            }
        }

        /**
         * Accepts notification of who the opponent is.
         */
        public void setOpponent(Player opponent) {
            this.opponent = opponent;
        }

        /**
         * Handles the otherPlayerMoved message.
         */
        public void otherPlayerMoved(int location) {
            output.println("OPPONENT_MOVED " + location);
        }

        /**
         * The run method of this thread.
         */
       //TODO change run method
        public void run() {
            try {
                // The thread is only started after everyone connects.
                output.println("MESSAGE All players connected");

                // Tell the first player that it is her turn.
                if (mark == 'X') {
                    output.println("MESSAGE Your move");
                }

                // Repeatedly get commands from the client and process them.
                while (true) {
                    String command = input.readLine();
                    if (command.startsWith("MOVE")) {
                        int location = Integer.parseInt(command.substring(5));
                        if (canIMove(location, this)) {
                            output.println("VALID_MOVE");

                        } else {
                            output.println("MESSAGE ?");
                        }
                    } else if (command.startsWith("QUIT")) {
                        return;
                    }
                }
            } catch (IOException e) {
                System.out.println("Player died: " + e);
            } finally {
                try {
                    socket.close();
                } catch (IOException e) {
                }
            }
        }
    }

    @Override
    public String toString() {
        if (currentPlayer != null) {
            return currentPlayer.mark + " has won";
        } else {
            return "game in progress";
        }

    }
}