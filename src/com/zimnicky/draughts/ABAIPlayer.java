package com.zimnicky.draughts;

import java.util.ArrayList;

public class ABAIPlayer extends Player{


    protected class SearchResult{
        Game.Move move;
        int value;
        SearchResult(int val, Game.Move move){
            value = val;
            this.move = move;
        }
    }

    protected byte depth = 5;
    protected Game game;

    protected Color opponentsColor(Color color){
        if (color == Color.BLACK){
            return Color.WHITE;
        }
        return Color.BLACK;
    }

    protected int calcPositionRating(Board board, Color color){
        int res = 0;
        int t;
        for (int i = 0; i < board.getSize(); i++){
            for (int j = 0 ; j < board.getSize(); j ++) {
                Board.Cell cell = board.getCell(i, j);
                if (!cell.isEmpty() && cell != Board.Cell.INVALID) {
                    if (cell.sameColor(color)) {
                        if (cell.isQueen()) {
                            t = 5;
                        } else {
                            t = 1;
                        }
                    } else if (cell.isQueen()) {
                            t = -5;
                    } else {
                            t = -1;
                    }
                    res += t;
                }
            }
        }
        return res;
    }

    protected SearchResult search(Board board, Color playerColor, Game.Move lastMove, byte depth, int lowerBound, int upperBound){

        if (depth <= 0 && (lastMove == null)) {
            return new SearchResult(calcPositionRating(board, playerColor), null);
        }

        ArrayList<Game.Move> moves;
        moves = game.getAllAvailableMoves(board, playerColor, lastMove);


        SearchResult best = new SearchResult(Integer.MIN_VALUE, null);

        if (moves != null) {
            for (Game.Move move : moves) {
                if (depth > 0 || game.canBeat(move.getStartRow(), move.getStartCol())) {
                    Board nextPos = new Board(board);
                    game.makeMove(nextPos, move);
                    int val;
                    if (move.getResult() == Game.MoveResult.BEAT && game.canBeat(move.getDistRow(), move.getDistCol(), nextPos)) {
                        val = search(nextPos, playerColor, move, (byte) (depth - 1), lowerBound, upperBound).value;
                    } else {
                        val = -search(nextPos, opponentsColor(playerColor), null, (byte) (depth - 1), -upperBound, -lowerBound).value;
                    }

                    if (best.value < val) {
                        best.value = val;
                        best.move = move;
                    }

                    if (lowerBound < val) {
                        lowerBound = val;
                    }

                    if (lowerBound >= upperBound) {
                        return best;
                    }
                }
            }
        } else if (color != playerColor){
            return new SearchResult(Integer.MAX_VALUE, null);
        }
        return best;
    }


    public ABAIPlayer(){}

    public ABAIPlayer(byte depth){
        this.depth = depth;
    }

    public Game.Move makeMove(Game game, Game.Move lastMove) {
        this.game = game;
        Board board = game.getBoard();

        SearchResult best = search(board, color, lastMove, depth, Integer.MIN_VALUE, Integer.MAX_VALUE);

        return best.move;
    }
}

