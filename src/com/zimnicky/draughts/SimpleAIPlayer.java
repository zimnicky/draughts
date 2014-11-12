package com.zimnicky.draughts;

import java.util.ArrayList;
import java.util.Random;

public class SimpleAIPlayer extends Player {

    public Game.Move makeMove(Game game, Game.Move lastMove, boolean beatSequence)
    {
        Game.Move move = null;
        Board board = game.getBoard();
        if (lastMove != null && beatSequence
                && game.canBeat(lastMove.distRow, lastMove.distCol)) {
            ArrayList<Game.Move> moves = game.getAvailableMoves(lastMove.distRow, lastMove.distCol);
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
