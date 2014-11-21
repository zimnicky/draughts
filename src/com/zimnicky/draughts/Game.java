package com.zimnicky.draughts;


import java.util.ArrayList;

public class Game implements Runnable{
    public enum MoveResult{UNKNOWN, MOVED, BEAT, WRONG}
    public static class Move{
        private MoveResult result = MoveResult.UNKNOWN;
        private byte startCol;
        private byte startRow;
        private byte distCol;
        private byte distRow;

        public Move(){}

        public Move(int rowi, int coli, int rowj, int colj) {
            startCol = (byte)coli;
            startRow = (byte)rowi;
            distCol = (byte)colj;
            distRow = (byte)rowj;
        }
        public Move(Move other){
            startCol = other.startCol;
            startRow = other.startRow;
            distCol = other.distCol;
            distRow = other.distRow;
        }

        public int getStartCol() {
            return startCol;
        }

        public void setStartCol(int startCol) {
            this.startCol = (byte)startCol;
        }

        public int getStartRow() {
            return startRow;
        }

        public void setStartRow(int startRow) {
            this.startRow = (byte)startRow;
        }

        public int getDistCol() {
            return distCol;
        }

        public void setDistCol(int distCol) {
            this.distCol = (byte)distCol;
        }

        public int getDistRow() {
            return distRow;
        }

        public void setDistRow(int distRow) {
            this.distRow = (byte)distRow;
        }

        public void set(Move other){
            if (other == null){
                return;
            }
            startRow = other.startRow;
            startCol = other.startCol;
            distRow = other.distRow;
            distCol = other.distCol;
            result = other.result;
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

    private boolean beatSequence;

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
        Thread th = new Thread(this);
        th.start();
    }


    private void makeMove(Move move) {
        makeMove(board, move);
    }

    public void makeMove(Board board, Move move){
        Board.Cell cell = board.getCell(move.getStartRow(), move.getStartCol());

        if (cell == Board.Cell.BLACK && move.getDistRow() == board.getSize() - 1){
            board.setCell(move.getDistRow(), move.getDistCol(), Board.Cell.BLACK_QUEEN);
        } else if (cell == Board.Cell.WHITE && move.getDistRow() == 0){
            board.setCell(move.getDistRow(), move.getDistCol(), Board.Cell.WHITE_QUEEN);
        } else{
            board.setCell(move.getDistRow(), move.getDistCol(), cell);
        }
        board.setCell(move.getStartRow(), move.getStartCol(), Board.Cell.EMPTY);

        if (move.getResult() == MoveResult.BEAT) {
            int dr = ((move.getDistRow() - move.getStartRow()) / Math.abs(move.getDistRow() - move.getStartRow()));
            int dc = ((move.getDistCol() - move.getStartCol()) / Math.abs(move.getDistCol() - move.getStartCol()));
            int j = (move.getStartCol() + dc);
            for (int i = (move.getStartRow() + dr); i != move.getDistRow(); i += dr, j += dc){
                board.setCell(i,j, Board.Cell.EMPTY);
            }
        }
    }



    public boolean canBeat(int i, int j, Board board){
        Board.Cell cell = board.getCell(i,j);
        for (int[] d: directions) {

            int ii = (i + d[0]);
            int jj = (j + d[1]);
            while (cell.isQueen() && board.getCell(ii, jj) == Board.Cell.EMPTY){
                ii += d[0];
                jj += d[1];
            }
            if (board.isCorrectCell(ii,jj) && !board.getCell(ii, jj).isEmpty()
                    && !board.getCell(ii, jj).sameColor(cell)) {
                int seqLen = board.sequenceLength(ii, jj, d[0], d[1]);
                if (seqLen == 1) {
                    ii += seqLen * d[0];
                    jj += seqLen * d[1];
                    if (board.getCell(ii, jj).isEmpty()) {
                        return true;
                    }
                }
            }

        }
        return false;
    }

    public boolean canBeat(int i, int j) {
        return canBeat(i, j, board);
    }



    public boolean canBeat(Player.Color playerColor, Board board) {                      // returns true if 1 or more beats are available for player
        for (int row = 0; row < board.getSize(); row++){
            for (int col = 0; col < board.getSize(); col++) {
                if (board.getCell(row,col).sameColor(playerColor) && canBeat(row, col, board)){
                    return true;
                }
            }
        }
        return false;
    }

    public boolean canBeat(){
        return canBeat(players[currentPlayer].getColor(), board);
    }


    public boolean canMove(Move move, Move lastMove, Board board){
        if (move == null) {
            return false;
        }

        int dr = (move.getDistRow() - move.getStartRow());
        int dc = (move.getDistCol() - move.getStartCol());
        int da = Math.abs(dr); // distance between cells

        // MUST: abs(dr) = abs(dc),  both cells on board
        if (da != Math.abs(dc) || !board.isCorrectCell(move.getStartRow(), move.getStartCol())
                || !board.isCorrectCell(move.getDistRow(), move.getDistCol())) {
            move.result = MoveResult.WRONG;
            return false;
        }

        //  start cell mustn't be empty & dist cell must be one
        Board.Cell dist = board.getCell(move.getDistRow(), move.getDistCol());
        Board.Cell start = board.getCell(move.getStartRow(), move.getStartCol());
        if (start.isEmpty() || start == Board.Cell.INVALID || !dist.isEmpty()) {
            move.result = MoveResult.WRONG;
            return false;
        }

        // MUST: only 1 opposite color cell on the path
        int c = board.segmentCountOppositeColor(move.getStartRow(), move.getStartCol(), (move.getStartRow() + dr), (move.getStartCol() + dc), start);
        if (c > 1 || board.segmentCount(move.getStartRow(), move.getStartCol(), (move.getStartRow() + dr), (move.getStartCol() + dc), start) > 1){
            move.result = MoveResult.WRONG;
            return false;
        }

        boolean isBeat = (c == 1);

        if (!start.isQueen()) {

            // MUST: only 1 or 2 (if beat) length of path
            if ((da > 2 || (da == 2 && !isBeat))) {
                move.result = MoveResult.WRONG;
                return false;
            }

            // MUST: right direction
            if (!isBeat && ((start.isBlack() && dr < 0) || (start.isWhite() && dr > 0))) {
                move.result = MoveResult.WRONG;
                return false;
            }
        }

        // MUST: use last piece if it beat & can beat more
        if (lastMove != null && lastMove.result == MoveResult.BEAT
                && canBeat(lastMove.getDistRow(), lastMove.getDistCol(), board)
                && (move.getStartCol() != lastMove.getDistCol() || move.getStartRow() != lastMove.getDistRow() || !isBeat)){
            move.result = MoveResult.WRONG;
            return false;
        }

        // MUST: beat if can
        if (!isBeat && canBeat(move.getStartRow(), move.getStartCol(), board)){
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

    public boolean canMove(Move move, Move lastMove) {
       return canMove(move, lastMove, board);
    }



    // returns null if there are no moves from this cell
    public ArrayList<Move> getAvailableMoves(int row, int col, Board board, Player.Color playerColor, Move lastMove){
        if (board.getCell(row, col) == Board.Cell.INVALID
                || (lastMove != null && lastMove.result == MoveResult.BEAT
                    && (row != lastMove.getDistRow() || col != lastMove.getDistCol())
                    && canBeat(lastMove.getDistRow(), lastMove.getDistCol(), board))){
            return null;
        }
        boolean beat = canBeat(playerColor, board);
        if (beat && !canBeat(row, col, board)){
            return null;
        }
        ArrayList<Move> moves = new ArrayList<>();
        for (int[] d: directions) {
            int l = 7;
            if (!board.getCell(row,col).isQueen()){
                board.sequenceLength(row + d[0], col + d[1], d[0], d[1]);
            }
            for (int i = 1; i <= l+1; i++){
                Move move = new Move(row, col, row + i*d[0], col + i*d[1]);
                if (canMove(move, lastMove, board) && (!beat || move.getResult() == MoveResult.BEAT)) {
                    moves.add(move);
                }
            }
        }
        if (moves.size() == 0){
            return null;
        }
        return moves;
    }

    public ArrayList<Move> getAvailableMoves(int row, int col){
        if (beatSequence) {
            return getAvailableMoves(row, col, board, players[currentPlayer].getColor(), lastMove[currentPlayer]);
        }
        return getAvailableMoves(row, col, board, players[currentPlayer].getColor(), null);
    }


    public ArrayList<Move> getAllAvailableMoves(Board board, Player.Color playerColor, Move lastMove){
        ArrayList<Move> moves = null;
        for (int row = 0; row < board.getSize(); row++){
            for (int col = 0; col < board.getSize(); col++) {
                if (board.getCell(row, col).sameColor(playerColor)){
                    ArrayList<Move> movesRC = getAvailableMoves(row, col, board, playerColor, lastMove);
                    if (movesRC != null) {
                        if (moves == null){
                            moves = new ArrayList<>();
                        }
                        moves.addAll(movesRC);
                    }
                }
            }
        }
        return moves;
    }

    public boolean canMove(Board board, Player.Color playerColor, Move lastMove) {
        for (int row = 0; row < board.getSize(); row++){
            for (int col = 0; col < board.getSize(); col++) {
                if ((board.getCell(row, col).sameColor(playerColor)) && (getAvailableMoves(row, col, board, playerColor, lastMove) != null)){
                        return true;
                }
            }
        }
        return false;
    }

    public boolean canMove() { // returns true if 1 or more moves are available for current player
        if (beatSequence){
            return canMove(board, players[currentPlayer].getColor(), getLastMove(currentPlayer));
        }
        return canMove(board, players[currentPlayer].getColor(), null);
    }



    public void run() {

        currentPlayer = 0;
        players[0].setColor(Player.Color.WHITE);
        players[1].setColor(Player.Color.BLACK);
        board = new Board();
        winner = -1;

        lastMove = new Move[2];
        beatSequence = false;

        while (board.getCountBlack() > 0 && board.getCountWhite() > 0 && winner == -1 && canMove()) {
            Move move;
            boolean correct;
            do {
                if (beatSequence) {
                    move = players[currentPlayer].makeMove(this, lastMove[currentPlayer]);
                    correct = canMove(move, lastMove[currentPlayer]);
                } else {
                    move = players[currentPlayer].makeMove(this, null);
                    correct = canMove(move, null);
                }
            }while (!correct && move != null);

            if (move != null) {
                makeMove(move);

                lastMove[currentPlayer] = move;
                if (move.getResult() != MoveResult.BEAT || !canBeat(move.getDistRow(), move.getDistCol())) {
                    currentPlayer ^= 1;
                    beatSequence = false;
                } else {
                    beatSequence = true;
                }
            }
            else {
                winner = currentPlayer ^ 1;
            }
        }

        if (board.getCountBlack() == 0){
            winner = 0;
        } else if (board.getCountWhite() == 0){
            winner = 1;
        } else if (winner == -1){
            winner = currentPlayer ^ 1;
        }
    }
}
