package com.zimnicky.draughts;


import java.util.ArrayList;

public class Game implements Runnable{
    public enum MoveResult{UNKNOWN, MOVED, BEAT, WRONG};
    public static class Move{
        private MoveResult result = MoveResult.UNKNOWN;
        public int startCol;
        public int startRow;
        public int distCol;
        public int distRow;

        public Move(){}
        public Move(int rowi, int coli, int rowj, int colj) {
            startCol = coli;
            startRow = rowi;
            distCol = colj;
            distRow = rowj;
        }

       public MoveResult getResult() {
           return result;
       }
       public String toString(){
           return "(" + startRow + ", " + startCol + ") - (" + distRow + ", " + distCol + ")";
       }
    }

    private final int[][] directions = {{1,1},{1, -1}, {-1, -1}, {-1, 1}};

    private Player[] players;
    private int currentPlayer;
    private int winner;
    private Move[] lastMove;  // last move of each player
    private Board board;


    public Player getPlayer(int i) {
        return players[i];
    }

    public int getWinner() {
        return winner;
    }

    public int getCurrentPlayer() {
        return currentPlayer;
    }

    public Board getBoard() {
        return new Board(board);
    }

    public Move getLastMove(int player) {
        return lastMove[player];
    }

    public Game(Player first, Player second) {
        players = new Player[2];
        players[0] = first;
        players[1] = second;
        first.setColor(Player.Color.WHITE);
        first.setColor(Player.Color.BLACK);
    }


    private void makeMove(Move move) {
        Board.Cell cell = board.getCell(move.startRow, move.startCol);
        board.setCell(move.distRow, move.distCol, cell);
        board.setCell(move.startRow, move.startCol, Board.Cell.EMPTY);

        if (move.getResult() == MoveResult.BEAT) {
            int d = (move.distRow - move.startRow)/Math.abs(move.distRow - move.startRow);
            int j = move.startCol+d;
            for (int i = move.startRow+d; i < move.distRow; i+=d){
                board.setCell(i,j, Board.Cell.EMPTY);
                if (cell.isWhite()){
                    board.setCountBlack(board.getCountBlack() - 1);
                }
                j += d;
            }
        }
    }

    public boolean canBeat(int i, int j) {
        Board.Cell cell = board.getCell(i,j);
        for (int[] d: directions) {

            int ii = i + d[0];
            int jj = j + d[1];
            if ( ii >=0 && jj >= 0 && ii < board.getSize() && jj < board.getSize()
                    && board.getCell(ii, jj) != Board.Cell.EMPTY) {
                int seqLen = board.sequenceLength(ii,jj, d[0], d[1]);
                if (cell.isQueen() || seqLen == 1) {
                    ii += seqLen * d[0];
                    jj += seqLen * d[1];
                    if (ii >= 0 && jj >= 0 && ii < board.getSize() && jj < board.getSize()
                            && board.getCell(ii, jj) == Board.Cell.EMPTY) {
                        return true;
                    }
                }
            }

        }
        return false;
    }

    public boolean canMove(Move move, Move lastMove) {
        if (move == null) {
            return false;
        }

        int dr = move.distRow - move.startRow;
        int dc = move.distCol - move.startCol;
        int da = Math.abs(dr); // distance between cells
        if (da != Math.abs(dc) || move.startRow >= board.getSize() || move.startCol >= board.getSize()
                || move.distRow >= board.getSize() || move.distCol >= board.getSize()
                || move.startRow < 0 || move.startCol < 0
                || move.distRow < 0 || move.distCol < 0) {
            move.result = MoveResult.WRONG;
            return false;
        }

        Board.Cell dist = board.getCell(move.distRow, move.distCol);
        Board.Cell start = board.getCell(move.startRow, move.startCol);
        if (start == Board.Cell.EMPTY || start == Board.Cell.INVALID
                || dist != Board.Cell.EMPTY) {
            move.result = MoveResult.WRONG;
            return false;
        }


        boolean isBeat = false;

        if (start.isQueen()) {
            /* ! not absolutely correct check
            * can allow player to make sequence of beats by one move
            * but it's not important*/
            if (start.isBlack()) {
                if (board.segmentCountBlack(move.startRow, move.startCol, dr) > 1) {
                    move.result = MoveResult.WRONG;
                    return false;
                }
                isBeat = board.segmentCountWhite(move.startRow, move.startCol, dr) > 0;

            } else { // isWhite()
                if (board.segmentCountWhite(move.startRow, move.startCol, dr) > 1) {
                    move.result = MoveResult.WRONG;
                    return false;
                }
                isBeat = board.segmentCountBlack(move.startRow, move.startCol, dr) > 0;
            }

        }
        else {

            if (da > 2) {
                move.result = MoveResult.WRONG;
                return false;
            }

            if (da == 2) {
                if ((start.isBlack() && board.segmentCountWhite(move.startRow, move.startCol, dr) != 1)
                        || (start.isWhite() && board.segmentCountBlack(move.startRow, move.startCol, dr) != 1)) {
                    move.result = MoveResult.WRONG;
                    return false;
                } else {
                    isBeat = true;
                }
            }

            if (!isBeat && (start.isBlack() && dr > 0)
                    || (start.isWhite() && dr < 0)){
                move.result = MoveResult.WRONG;
                return false;
            }

        }

        // must beat with the lastMove checker
        if (lastMove != null && lastMove.result == MoveResult.BEAT
                && (move.startCol != lastMove.distCol || move.startRow != lastMove.distRow
                || !isBeat)){
            move.result = MoveResult.WRONG;
            return false;
        }

        if (!isBeat && canBeat(move.startRow, move.startCol)){
            move.result = MoveResult.WRONG;
            return false;
        }

        if (isBeat) {
            move.result = MoveResult.BEAT;
        } else {
            move.result = MoveResult.MOVED;
        }
        return true;
    }

    public ArrayList<Move> getAvailableMoves(int row, int col){     // returns null if there are no moves from this cell
        if (board.getCell(row, col) == Board.Cell.INVALID){
            return null;
        }
        ArrayList<Move> moves = new ArrayList();
        for (int[] d: directions) {
            int l = board.sequenceLength(row + d[0], col + d[1], d[0], d[1]);
            for (int i = 1; i <= l; i++){
                Move move = new Move(row, col, row + i*d[0], col + i*d[1]);
                if (canMove(move, lastMove[currentPlayer])) {
                    moves.add(move);
                }
            }
        }
        if (moves.size() == 0){
            return null;
        }
        return moves;
    }

    public boolean canMove() {                      // returns true if 1 or more moves are available for current player
        for (int row = 0; row < board.getSize(); row++){
            for (int col = 0; col < board.getSize(); col++) {
                if (((currentPlayer == 0 && board.getCell(row, col).isWhite())
                        || (currentPlayer == 1 && board.getCell(row,col).isBlack()))
                        && (getAvailableMoves(row, col) != null)
                        ){
                        return true;
                }
            }
        }
        return false;
    }

    public void run() {

        currentPlayer = 0;
        players[0].setColor(Player.Color.WHITE);
        players[1].setColor(Player.Color.BLACK);
        board = new Board();
        winner = -1;

        lastMove = new Move[2];

        while (board.getCountBlack() > 0 && board.getCountWhite() > 0 && canMove()) {
            Move move = null;
            do {
                move = players[currentPlayer].makeMove(this, lastMove[currentPlayer], move);
                if (move == null)
                    System.out.println("p" + currentPlayer + " wrong move");
            }while (!canMove(move, lastMove[currentPlayer]));

            System.out.println("p" + currentPlayer + ": " + move.toString());
            makeMove(move);

            lastMove[currentPlayer] = move;
            if (move.getResult() != MoveResult.BEAT || !canBeat(move.distRow, move.distCol)) {
                currentPlayer ^= 1;
            }
        }

        if (board.getCountBlack() == 0){
            winner = 0;
        } else if (board.getCountWhite() == 0){
            winner = 1;
        } else {
            winner = currentPlayer ^ 1;
        }
    }
}
