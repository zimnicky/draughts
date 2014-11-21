package com.zimnicky.draughts;


public class HumanPlayer extends Player {

    private boolean waitingMoveStatus;
    private Game.Move move;
    private Game.Move previousMove;
    private Game game;

    synchronized private void waitMove() {
        waitingMoveStatus = true;
        try {
            wait();
        }catch (InterruptedException e){
            assert false;
        }
    }

    public Game.Move getPreviousMove() {
        return previousMove;
    }

    public HumanPlayer() {
        waitingMoveStatus = false;
    }

    public Game.Move makeMove(Game game, Game.Move lastMove)
    {
        this.game = game;
        move = null;
        previousMove = lastMove;
        while (!game.canMove(move, previousMove)) {
            waitMove();
        }
        return move;
    }

    public boolean canMove(Game.Move move){
        return game.canMove(move, previousMove);
    }

    synchronized public boolean isWaitMove() {
        return waitingMoveStatus;
    }

    synchronized public void setMove(Game.Move move) {
        if (!waitingMoveStatus){
            return;
        }
        this.move = new Game.Move(move);
        waitingMoveStatus = false;
        notify();
    }
}
