package com.zimnicky.draughts;

import java.util.ArrayList;
import java.util.Random;

public class SimpleAIPlayer extends Player {

    public Game.Move makeMove(Game game, Game.Move lastCorrectMove, Game.Move lastMove)
    {
        Game.Move move = null;
        Board board = game.getBoard();
        if (lastCorrectMove != null && lastCorrectMove.getResult() == Game.MoveResult.BEAT
                && game.canBeat(lastCorrectMove.distRow, lastCorrectMove.distCol)) {
            ArrayList<Game.Move> moves = game.getAvailableMoves(lastCorrectMove.distRow, lastCorrectMove.distCol);
            Random rand = new Random();
            move = moves.get(rand.nextInt(moves.size()));
            return move;
        }

        for (int i = 0; i < board.getSize(); i++){
            for (int j = 0; j < board.getSize(); j++){
                if ((color == Color.BLACK && board.getCell(i,j).isBlack())
                        || (color == Color.WHITE && board.getCell(i,j).isWhite())) {
                    ArrayList<Game.Move> moves = game.getAvailableMoves(i, j);
                    if (moves != null){
                        Random rand = new Random();
                        move = moves.get(rand.nextInt(moves.size()));
                        return move;
                    }
                }
            }
        }

        return move;
    }
}
