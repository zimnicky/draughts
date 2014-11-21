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

    private int size;
    private Cell[][] data;
    private int countWhite;
    private int countBlack;

    private void startPosition() {

        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (((i*(size-1) + j) & 1) == 1) {
                    if (i < size/2 - 1) {
                        data[i][j] = Cell.BLACK;
                    } else if (i > size/2) {
                        data[i][j] = Cell.WHITE;
                    } else {
                        data[i][j] = Cell.EMPTY;
                    }
                } else {
                    data[i][j] = Cell.INVALID;
                }
            }
        }

        countWhite = countBlack = (size-1)*(size-1)/4;
    }

    public Board() {
        this(defaultSize);
    }

    public Board(int size) {
        this.size = size;
        data = new Cell[size][size];
        startPosition();
    }

    public Board(Board board) {
        this.size = board.size;
        data = new Cell[size][size];
        for (int i = 0; i < size; i++){
            for (int j = 0; j < size; j++){
                data[i][j] = board.getCell(i,j);
            }
        }
        countBlack = board.countBlack;
        countWhite = board.countWhite;
    }

    public boolean isCorrectCell(int row, int col){
        if (row < 0 || col < 0 || row >= size || col >= size){
            return false;
        }
        return true;
    }

    public int getSize() {
        return size;
    }

    synchronized public Cell getCell(int row, int col) {
        if (!isCorrectCell(row,col)){
            return Cell.INVALID;
        }
        return data[row][col];
    }

    synchronized public void setCell(int row, int col, Cell cell) {
        if (isCorrectCell(row,col)){
            data[row][col] = cell;
        }
    }

    synchronized public int getCountWhite() {
        return countWhite;
    }

    synchronized public int getCountBlack() {
        return countBlack;
    }

    synchronized public void setCountWhite(int countWhite) {
        this.countWhite = countWhite;
    }

    synchronized public void setCountBlack(int countBlack) {
        this.countBlack = countBlack;
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
        if (color == Cell.BLACK){
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
