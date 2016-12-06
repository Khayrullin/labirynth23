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
        return false;

    }

    public Game() {
        fillTheBoard();
    }

    /**
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

    /**
     * Tut vse prosto:
     *
     */
    public boolean hasWinner(int direction, Player player) {
        int wantedIndex = getWantedIndex(direction, player);
        return wantedIndex == player.opponent.getLocation();
    }
    /**
     * delaet vzryv esli mojno:
     *
     */
    public boolean bombThatShit(int direction, Player player) {
        int wantedIndex = getWantedIndex(direction, player);
        int loc = player.getLocation();

        if ((wantedIndex > board.length - 1 || wantedIndex < 0) || (direction == 1 && ((loc % 5) == 0))
                || (direction == 3 && (((loc + 1) % 5) == 0))) {
            return false;
        } else {
            if (player == currentPlayer &&
                    board[wantedIndex] != Block.IMMORTAL && wantedIndex != player.opponent.getLocation()) {
                board[wantedIndex] = null;
                return true;
            }
        }
        return false;
    }
    /**
     * vozvrashaem index po nomeru klienta ot 1 do 4
     *
     */
    public int getWantedIndex(int direction, Player player) {
        int wantedIndex = -1;
        int loc = player.getLocation();
        switch (direction) {
            case 1:
                wantedIndex = loc - 1;
                break;
            case 2:
                wantedIndex = loc - 5;
                break;
            case 3:
                wantedIndex = loc + 1;
                break;
            case 4:
                wantedIndex = loc + 5;
                break;
        }
        return wantedIndex;
    }
    /**
     * delaet hod esli mojno:
     *
     */
    public synchronized boolean canIMoveIfCan_Move(int direction, Player player) {
        int wantedIndex = getWantedIndex(direction, player);
        int loc = player.getLocation();

        if ((wantedIndex > board.length - 1 || wantedIndex < 0) || (direction == 1 && ((loc % 5) == 0))
                || (direction == 3 && (((loc + 1) % 5) == 0))) {

            return false;
        } else {
            if (player == currentPlayer && board[wantedIndex] != Block.BRICK &&
                    board[wantedIndex] != Block.IMMORTAL) {
                currentPlayer.setLocation(wantedIndex);

                return true;
            }
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
         * PEredacha deystviy klientu
         */

        public void otherPlayerAction(int field, int location) {
            switch (field) {
                case 1:
                    output.println("OTHER MOVED" + location);
                case 2:
                    output.println("OTHER BLACK_KVAD" + location);
                case 3:
                    output.println("OTHER EMPTY" + location);
                case 4:
                    output.println("OTHER GRANIT" + location);
                case 5:
                    output.println("OTHER WON" + location);
                case 6:
                    output.println("OTHER LOSE" + location);

            }
        }
        /**
         * peredacha deystviy klientu
         *
         */
        public void currentPlayerAction(int field, int location) {
            switch (field) {
                case 1:
                    output.println("CURRENT MOVED" + location);
                case 2:
                    output.println("CURRENT BLACK_KVAD" + location);
                case 3:
                    output.println("CURRENT EMPTY" + location);
                case 4:
                    output.println("CURRENT GRANIT" + location);
                case 5:
                    output.println("CURRENT WON" + location);
                case 6:
                    output.println("CURRENT LOSE" + location);
            }
        }

        /**
         * The run method of this thread.
         */
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
                        int direction = Integer.parseInt(command.substring(5));
                        if (canIMoveIfCan_Move(direction, this)) {
                            currentPlayerAction(1, this.getLocation());
                            currentPlayer.opponent.otherPlayerAction(1, this.getLocation());
                        } else {
                            currentPlayerAction(2, this.getLocation());
                            currentPlayer.opponent.otherPlayerAction(2, this.getLocation());
                        }
                    } else if (command.startsWith("BOMB")) {
                        int direction = Integer.parseInt(command.substring(5));
                        if (!hasWinner()) {
                            if (bombThatShit(direction, this)) {
                                currentPlayerAction(3, this.getLocation());
                                currentPlayer.opponent.otherPlayerAction(3, this.getLocation());
                            } else {
                                currentPlayerAction(4, this.getLocation());
                                currentPlayer.opponent.otherPlayerAction(4, this.getLocation());
                            }
                        } else {
                            currentPlayerAction(5, this.getLocation());
                            currentPlayer.opponent.otherPlayerAction(6, this.getLocation());
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