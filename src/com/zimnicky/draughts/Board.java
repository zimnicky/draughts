package com.zimnicky.draughts;

public class Board {

    public enum Cell {
        EMPTY, INVALID, BLACK, WHITE, BLACK_QUEEN, WHITE_QUEEN;

        boolean sameColor(Cell other) {
            return (isWhite() && other.isWhite()) || (isBlack() && other.isBlack());
        }

        boolean sameColor(Player.Color color){
            return (isWhite() && color == Player.Color.WHITE) || (isBlack() && color == Player.Color.BLACK);
        }

        boolean isQueen() {
            return this == BLACK_QUEEN || this == WHITE_QUEEN;
        }

        boolean isBlack() {
            return this == BLACK || this == BLACK_QUEEN;
        }

        boolean isWhite() {
            return this == WHITE || this == WHITE_QUEEN;
        }

        boolean isEmpty() {
            return this == EMPTY;
        }
    }

    private final static int defaultSize = 8;

    private int blackCells; // 0 -- empty or white piece, 1 -- black piece
    private int whiteCells; // 0 -- empty or black piece, 1 -- whitePiece
    private int queens;     // 0 -- empty or not queen, 1 -- queen

    private void startPosition() {

        whiteCells = 0b11111111111100000000000000000000;
        blackCells = 0b00000000000000000000111111111111;
        queens     = 0;

       // whiteCells = 0b00000000010000000000000000000000;
       // blackCells = 0b00000000000000001000000000000000;
       // queens     = 0b00000000000000001000000000000000;

//        for (int i = 0; i < 8; i++){
//            for (int j = 0; j < 8; j++){
//                System.out.print(getCell(i, j) + "  ");
//            }
//            System.out.println();
//        }


    }

    private int getCellMask(int row, int col){
        return 1 << (row*(defaultSize >> 1) + (col >> 1));
    }

    public Board() {
        startPosition();
    }

    public Board(Board board) {
        blackCells = board.blackCells;
        whiteCells = board.whiteCells;
        queens = board.queens;
    }

    public boolean isCorrectCell(int row, int col){
        if (row < 0 || col < 0 || row >= defaultSize || col >= defaultSize
                || ((row*(defaultSize-1) + col) & 1) == 0){
            return false;
        }
        return true;
    }

    public int getSize() {
        return defaultSize;
    }


    synchronized public Cell getCell(int row, int col) {
        if (!isCorrectCell(row,col)){
            return Cell.INVALID;
        }
        int mask = getCellMask(row, col);
        if ((blackCells & mask) != 0){
            if ((queens & mask) != 0){
                return Cell.BLACK_QUEEN;
            }
            return Cell.BLACK;
        } else if ((whiteCells & mask) != 0){
            if ((queens & mask) != 0){
                return Cell.WHITE_QUEEN;
            }
            return Cell.WHITE;
        }
        return Cell.EMPTY;
    }

    synchronized public void setCell(int row, int col, Cell cell) {
        if (isCorrectCell(row,col)){
            int mask = getCellMask(row, col);
            if (cell.isWhite()){
                whiteCells |= mask;
                blackCells &= ~mask;
            } else if (cell.isBlack()){
                blackCells |= mask;
                whiteCells &= ~mask;
            } else {
                blackCells &= ~mask;
                whiteCells &= ~mask;
            }

            if (cell.isQueen()){
                queens |= mask;
            } else {
                queens &= ~mask;
            }
        }
    }

    synchronized public int getCountWhite() {
        return Integer.bitCount(whiteCells);
    }

    synchronized public int getCountBlack() {
        return Integer.bitCount(blackCells);
    }

    public int segmentCount(int startR, int startC, int distR, int distC, Cell color) {
        if (startR == distR || startC == distC){
            return 0;
        }
        int count = 0;
        int dr = distR - startR;
        int dc = distC - startC;
        dr /= Math.abs(dr);
        dc /= Math.abs(dc);

        for (int i = startR, j = startC; i != distR && j != distC; i += dr, j += dc) {
            if (getCell(i, j).sameColor(color)) {
                count++;
            }
        }

        return count;
    }

    public int segmentCount(int startR, int startC, int distR, int distC, Player.Color color){
        if (color == Player.Color.BLACK){
            return segmentCount(startR, startC, distR, distC, Cell.BLACK);
        }
        return segmentCount(startR, startC, distR, distC, Cell.WHITE);
    }

    public int segmentCountOppositeColor(int startR, int startC, int distR, int distC, Cell color){
        if (color.isBlack()){
            return segmentCount(startR, startC, distR, distC, Cell.WHITE);
        }
        return segmentCount(startR, startC, distR, distC, Cell.BLACK);
    }

    public int segmentCountOppositeColor(int startR, int startC, int distR, int distC, Player.Color color){
        if (color == Player.Color.BLACK){
            return segmentCount(startR, startC, distR, distC, Cell.WHITE);
        }
        return segmentCount(startR, startC, distR, distC, Cell.BLACK);
    }

    public int segmentCountBlack(int startR, int startC,int lenR, int lenC) {
        return segmentCount(startR, startC, lenR, lenC, Cell.BLACK);
    }

    public int segmentCountWhite(int startR, int startC,int lenR, int lenC) {
        return segmentCount(startR, startC, lenR, lenC, Cell.WHITE);
    }

    public int sequenceLength(int startR, int startC, int dr, int dc) {
        if (Math.abs(dr) != 1 || Math.abs(dc) != 1
                || !isCorrectCell(startR, startC))
            return 0;
        int r = startR;
        int c = startC;
        Cell et = getCell(r, c);
        int count = 0;
        while (getCell(r, c) == et) {
            r += dr;
            c += dc;
            count++;
        }
        return count;
    }

}
