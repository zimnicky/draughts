package com.zimnicky.draughts;


import java.util.ArrayList;

public class Game implements Runnable{
    public enum MoveResult{UNKNOWN, MOVED, BEAT, WRONG}
    public static class Move{
        private MoveResult result = MoveResult.UNKNOWN;
        private int startCol;
        private int startRow;
        private int distCol;
        private int distRow;

        public Move(){}

        public Move(int move){
            distCol = (move & 0xFF);
            distRow = (move >> 8) & 0xFF;
            startCol = (move >> 16) & 0xFF;
            startRow = (move >> 24) & 0xFF;
        }

        public Move(int rowi, int coli, int rowj, int colj) {
            startCol = coli;
            startRow = rowi;
            distCol = colj;
            distRow = rowj;
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
            this.startCol = startCol;
        }

        public int getStartRow() {
            return startRow;
        }

        public void setStartRow(int startRow) {
            this.startRow = startRow;
        }

        public int getDistCol() {
            return distCol;
        }

        public void setDistCol(int distCol) {
            this.distCol = distCol;
        }

        public int getDistRow() {
            return distRow;
        }

        public void setDistRow(int distRow) {
            this.distRow = distRow;
        }

        public MoveResult getResult() {
           return result;
       }
        public String toString(){
           return "(" + startRow + ", " + startCol + ") - (" + distRow + ", " + distCol + ")";
       }

        public int toInt(){
            return (((((startRow & 0xFF) << 8) + (startCol & 0xFF) << 8) + (distRow & 0xFF)) << 8) + (distCol & 0xFF);
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
        Board.Cell cell = board.getCell(move.startRow, move.startCol);

        if (cell == Board.Cell.BLACK && move.distRow == board.getSize() - 1){
            board.setCell(move.distRow, move.distCol, Board.Cell.BLACK_QUEEN);
        } else if (cell == Board.Cell.WHITE && move.distRow == 0){
            board.setCell(move.distRow, move.distCol, Board.Cell.WHITE_QUEEN);
        } else{
            board.setCell(move.distRow, move.distCol, cell);
        }
        board.setCell(move.startRow, move.startCol, Board.Cell.EMPTY);

        if (move.getResult() == MoveResult.BEAT) {
            int dr = ((move.distRow - move.startRow) / Math.abs(move.distRow - move.startRow));
            int dc = ((move.distCol - move.startCol) / Math.abs(move.distCol - move.startCol));
            int j = (move.startCol + dc);
            for (int i = (move.startRow + dr); i != move.distRow; i += dr, j += dc){
                if (cell.isWhite() && board.getCell(i,j).isBlack()){
                    board.setCountBlack(board.getCountBlack() - 1);
                } else if (cell.isBlack() && board.getCell(i,j).isWhite()){
                    board.setCountWhite(board.getCountWhite() - 1);
                }
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
                if (board.getCell(row,col).sameColor(playerColor) && canBeat(row, col)){
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

        int dr = (move.distRow - move.startRow);
        int dc = (move.distCol - move.startCol);
        int da = Math.abs(dr); // distance between cells

        // MUST: abs(dr) = abs(dc),  both cells on board
        if (da != Math.abs(dc) || !board.isCorrectCell(move.startRow, move.startCol)
                || !board.isCorrectCell(move.distRow, move.distCol)) {
            move.result = MoveResult.WRONG;
            return false;
        }

        //  start cell mustn't be empty & dist cell must be one
        Board.Cell dist = board.getCell(move.distRow, move.distCol);
        Board.Cell start = board.getCell(move.startRow, move.startCol);
        if (start.isEmpty() || start == Board.Cell.INVALID || !dist.isEmpty()) {
            move.result = MoveResult.WRONG;
            return false;
        }

        // MUST: only 1 opposite color cell on the path
        int c = board.segmentCountOppositeColor(move.startRow, move.startCol, (move.startRow + dr), (move.startCol + dc), start);
        if (c > 1 || board.segmentCount(move.startRow, move.startCol, (move.startRow + dr), (move.startCol + dc), start) > 1){
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
                && (move.startCol != lastMove.distCol || move.startRow != lastMove.distRow
                || !isBeat)){
            move.result = MoveResult.WRONG;
            return false;
        }

        // MUST: beat if can
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

    public boolean canMove(Move move, Move lastMove) {
       return canMove(move, lastMove, board);
    }



    // returns null if there are no moves from this cell
    public ArrayList<Move> getAvailableMoves(int row, int col, Board board, Player.Color playerColor, Move lastMove){
        if (board.getCell(row, col) == Board.Cell.INVALID
                || (lastMove != null && lastMove.getResult() == MoveResult.BEAT
                    && (row != lastMove.distRow || col != lastMove.distCol))){
            return null;
        }
        boolean beat = canBeat(playerColor, board);
        if (beat && !canBeat(row, col, board)){
            return null;
        }
        ArrayList<Move> moves = new ArrayList<Move>();
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
                            moves = new ArrayList<Move>();
                        }
                        moves.addAll(movesRC);
                    }
                }
            }
        }
        return moves;
    }

    public boolean canMove(Board board, Player.Color playerColor) {
        for (int row = 0; row < board.getSize(); row++){
            for (int col = 0; col < board.getSize(); col++) {
                if ((board.getCell(row, col).sameColor(playerColor)) && (getAvailableMoves(row, col) != null)){
                        return true;
                }
            }
        }
        return false;
    }

    public boolean canMove() { // returns true if 1 or more moves are available for current player
        return canMove(board, players[currentPlayer].getColor());
    }



    public void run() {

        currentPlayer = 0;
        players[0].setColor(Player.Color.WHITE);
        players[1].setColor(Player.Color.BLACK);
        board = new Board();
        winner = -1;
        beatSequence = false;

        lastMove = new Move[2];

        while (board.getCountBlack() > 0 && board.getCountWhite() > 0 && canMove()) {
            Move move;
            boolean correct = false;
            do {
                if (beatSequence) {
                    move = players[currentPlayer].makeMove(this, lastMove[currentPlayer]);
                }
                else {
                    move = players[currentPlayer].makeMove(this, null);
                }
                if (beatSequence) {
                    correct = canMove(move, lastMove[currentPlayer]);
                } else {
                    correct = canMove(move, null);
                }
            }while (!correct);

            makeMove(move);

            lastMove[currentPlayer] = move;
            if (move.getResult() != MoveResult.BEAT || !canBeat(move.distRow, move.distCol)) {
                currentPlayer ^= 1;
                beatSequence = false;
            } else {
                beatSequence = true;
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
