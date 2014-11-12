package com.zimnicky.draughts;

public abstract class Player {
    public enum Color {BLACK, WHITE};
    protected Color color;

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    abstract public Game.Move makeMove(Game game, Game.Move lastMove, boolean beatSequence);
}
