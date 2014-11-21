package com.zimnicky.draughts;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.*;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;


public class Main extends Activity {

    private TextView gameStatusText;

    private Game game;
    private HumanPlayer player;
    private Game.Move currentMove;
    private Game.Move opponentsLastMove;
    private ArrayList<Game.Move> availableMoves;
    private boolean doingMove;

    BoardCellView[][] cellViews;

    private void repaintBoard(){
        Board board = game.getBoard();
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                Board.Cell cell = board.getCell(i,j);
                if (cell != Board.Cell.INVALID) {
                    cellViews[i][j].setCell(cell);
                    if (cellViews[i][j].getCell().isWhite()) {
                        if (currentMove.getStartRow() == -1) {
                                ArrayList<Game.Move> moves = game.getAvailableMoves(i, j);
                            if (moves != null) {
                                cellViews[i][j].setCanSelect(true);
                            }
                        }
                        else {
                            cellViews[i][j].setCanSelect(false);
                        }
                    }
                }
            }
        }
    }

    private ArrayList<Game.Move> getMoveStartAvailableMoves(){
        if (availableMoves == null && currentMove.getStartRow() >= 0){
            availableMoves = game.getAvailableMoves(currentMove.getStartRow(), currentMove.getStartCol());
        }
        if (currentMove.getStartRow() < 0){
            return null;
        }
        return availableMoves;
    }

    private void unselectMoveStartCell(){
        if (currentMove.getStartRow() < 0) {
            return;
        }
        cellViews[currentMove.getStartRow()][currentMove.getStartCol()].setHighlighted(false);
        ArrayList<Game.Move> moves = getMoveStartAvailableMoves();
        if (moves != null) {
            for (Game.Move move : moves) {
                cellViews[move.getDistRow()][move.getDistCol()].setCanSelect(false);
            }
        }
    }

    private void cellOnClick(BoardCellView view) {

        if (!doingMove) {
            return;
        }

        if (view.isHighlighted()) {
            unselectMoveStartCell();
            currentMove.setStartRow(-1);
            currentMove.setStartCol(-1);
            return;
        }

        if (currentMove.getStartRow() < 0 || view.getCell().isWhite()) {
            ArrayList<Game.Move> moves = game.getAvailableMoves(view.getRow(), view.getCol());
            if (moves != null) {
                unselectMoveStartCell();
                for (Game.Move move : moves) {
                    cellViews[move.getDistRow()][move.getDistCol()].setCanSelect(true);
                }
                currentMove.setStartRow(view.getRow());
                currentMove.setStartCol(view.getCol());
                view.setHighlighted(true);
                availableMoves = moves;
            }
        } else {
            currentMove.setDistRow(view.getRow());
            currentMove.setDistCol(view.getCol());
            if (player.canMove(currentMove)) {
                unselectMoveStartCell();
                doingMove = false;
                availableMoves = null;
                if (opponentsLastMove != null) {
                    cellViews[opponentsLastMove.getStartRow()][opponentsLastMove.getStartCol()].setOpponentsPath(false);
                    cellViews[opponentsLastMove.getDistRow()][opponentsLastMove.getDistCol()].setOpponentsPath(false);
                }
                player.setMove(currentMove);
            }
        }
    }

    private void checkCurrentPlayer(){
        if (doingMove){
            return;
        }

        switch (game.getWinner()){
            case 0: gameStatusText.setText("You win!");
                break;
            case 1: gameStatusText.setText("You lose!");
                break;
            default:
                if (!player.isWaitMove()){
                    doingMove = false;
                    gameStatusText.setText("Opponent's move!");
                }
                else{
                    currentMove.setStartRow(-1);
                    currentMove.setStartCol(-1);
                    doingMove = true;
                    gameStatusText.setText("Your move!");
                    opponentsLastMove = game.getLastMove(1);
                    if (opponentsLastMove != null) {
                        cellViews[opponentsLastMove.getStartRow()][opponentsLastMove.getStartCol()].setOpponentsPath(true);
                        cellViews[opponentsLastMove.getDistRow()][opponentsLastMove.getDistCol()].setOpponentsPath(true);
                    }
                }
        }

    }

    private void createBoard() {

        setContentView(R.layout.main);

        TableLayout layout = (TableLayout) findViewById(R.id.BoardLayout);
        layout.setStretchAllColumns(true);

        cellViews = new BoardCellView[8][8];
        TableRow[] rows = new TableRow[8];
        for (int i = 0; i < 8; i++){
            rows[i] = new TableRow(this);

            for (int j = 0; j < 8; j++){
                cellViews[i][j] = new BoardCellView(this, i, j);
                if (((i*7 + j) & 1) == 1) {
                    cellViews[i][j].setBackgroundColor(Color.BLACK);
                    cellViews[i][j].setOnClickListener(new View.OnClickListener() {
                        public void onClick(View view) {
                            cellOnClick((BoardCellView)view);
                        }
                    });
                } else {
                    cellViews[i][j].setBackgroundColor(Color.WHITE);
                }
                rows[i].addView(cellViews[i][j]);
            }
            layout.addView(rows[i]);
        }

    }



    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        createBoard();

        player = new HumanPlayer();
        Player player2 = new ABAIPlayer((byte)5);

        currentMove = new Game.Move();
        game = new Game(player, player2);

        gameStatusText = (TextView) findViewById(R.id.gameStatusText);


        Timer timer = new Timer();

        timer.schedule(new TimerTask() {
            public void run() {
                runOnUiThread(new Runnable() {
                    public void run() {
                        repaintBoard();
                        checkCurrentPlayer();
                    }
                });
            }

        }, 0, 100);

    }
}
