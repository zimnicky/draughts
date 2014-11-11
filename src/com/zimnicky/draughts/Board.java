package com.zimnicky.draughts;


public class Board {

    public enum Cell {
        EMPTY, INVALID, BLACK, WHITE, BLACK_QUEEN, WHITE_QUEEN;

        boolean isQueen() {
            return this == BLACK_QUEEN || this == WHITE_QUEEN;
        }

        boolean isBlack() {
            return this == BLACK || this == BLACK_QUEEN;
        }

        boolean isWhite() {
            return this == WHITE || this == WHITE_QUEEN;
        }
    }

    private final static int defaultSize = 8;

    private int size;
    private Cell[][] data;
    private int countWhite;
    private int countBlack;

    private void startPosition() {

        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j ++) {
                if (((i*(size-1) + j) & 1) == 1) {
                    if (i < size/2 - 1) {
                        data[i][j] = Cell.WHITE;
                    } else if (i > size/2) {
                        data[i][j] = Cell.BLACK;
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

    public int getSize() {
        return size;
    }

    synchronized public Cell getCell(int row, int col) {
        return data[row][col];
    }

    synchronized public void setCell(int row, int col, Cell cell) {
        data[row][col] = cell;
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

    public int segmentCountBlack(int startR, int startC, int dist) {
        int count = 0;
        int i = startR;
        int j = startC;
        if (dist < 0) {
            i += dist;
            j += dist;
            dist = -dist;
        }

        for (int t = 0; t < dist; t++, i++, j++) {
            if (i >= 0 && j >= 0 && i < size && j < size
                    && getCell(i,j).isBlack()) {
                count++;
            }
        }

        return count;
    }

    public int segmentCountWhite(int startR, int startC, int dist) {
        int count = 0;
        int i = startR;
        int j = startC;
        if (dist < 0) {
            i += dist;
            j += dist;
            dist = -dist;
        }

        for (int t = 0; t < dist; t++, i++, j++) {
            if (i >= 0 && j >= 0 && i < size && j < size
                && getCell(i,j).isWhite()) {
                count++;
            }
        }

        return count;
    }

    public int sequenceLength(int startR, int startC, int dr, int dc) {
        if (Math.abs(dr) != 1 || Math.abs(dc) != 1
                || startR < 0 || startC < 0
                || startR >= size || startC >= size)
            return 0;
        int r = startR;
        int c = startC;
        Cell et = getCell(r, c);
        int count = 0;
        while (r >= 0 && c >= 0 && r < size && c < size && getCell(r, c) == et) {
            r += dr;
            c += dc;
            count++;
        }
        return count;
    }

}
