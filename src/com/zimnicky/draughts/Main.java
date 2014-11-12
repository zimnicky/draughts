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

    private TextView currentPlayerText;

    private Game game;
    private HumanPlayer player;
    private Game.Move currentMove;
    private Game.Move opponentsLastMove;
    private ArrayList<Game.Move> availableMoves;
    private boolean doingMove;

    private Timer timer;

    BoardCellView[][] cellViews;

    private void repaintBoard(){
        Board board = game.getBoard();
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                Board.Cell cell = board.getCell(i,j);
                if (cell != Board.Cell.INVALID) {
                    cellViews[i][j].setCell(cell);
                    if (cellViews[i][j].getCell().isWhite()) {
                        if (currentMove.startRow == -1) {
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
        if (availableMoves == null && currentMove.startRow >= 0){
            availableMoves = game.getAvailableMoves(currentMove.startRow, currentMove.startCol);
        }
        if (currentMove.startRow < 0){
            return null;
        }
        return availableMoves;
    }

    private void unselectMoveStartCell(){
        if (currentMove.startRow < 0) {
            return;
        }
        cellViews[currentMove.startRow][currentMove.startCol].setHighlighted(false);
        ArrayList<Game.Move> moves = getMoveStartAvailableMoves();
        if (moves != null) {
            for (Game.Move move : moves) {
                cellViews[move.distRow][move.distCol].setCanSelect(false);
            }
        }
    }

    private void cellOnClick(BoardCellView view){

        if (!doingMove) {
            return;
        }

        if (view.isHighlighted()) {
            unselectMoveStartCell();
            currentMove.startRow = -1;
            currentMove.startCol = -1;
            return;
        }

        if (currentMove.startRow < 0 || view.getCell().isWhite()) {
            ArrayList<Game.Move> moves = game.getAvailableMoves(view.getRow(), view.getCol());
            if (moves != null) {
                unselectMoveStartCell();
                for (Game.Move move: moves){
                    cellViews[move.distRow][move.distCol].setCanSelect(true);
                }
                currentMove.startRow = view.getRow();
                currentMove.startCol = view.getCol();
                view.setHighlighted(true);
                availableMoves = moves;
            }
        }
        else {
            currentMove.distRow = view.getRow();
            currentMove.distCol = view.getCol();
            if (player.canMove(currentMove)){
                unselectMoveStartCell();
                doingMove = false;
                availableMoves = null;
                if (opponentsLastMove != null) {
                    cellViews[opponentsLastMove.startRow][opponentsLastMove.startCol].setOpponentsPath(false);
                    cellViews[opponentsLastMove.distRow][opponentsLastMove.distCol].setOpponentsPath(false);
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
            case 0: currentPlayerText.setText("You win!");
                break;
            case 1: currentPlayerText.setText("You lose!");
                break;
            default:
                if (!player.isWaitMove()){
                    doingMove = false;
                    currentPlayerText.setText("Opponent's move!");
                }
                else{
                    currentMove.startRow = -1;
                    currentMove.startCol = -1;
                    doingMove = true;
                    currentPlayerText.setText("Your move!");
                    opponentsLastMove = game.getLastMove(1);
                    if (opponentsLastMove != null) {
                        cellViews[opponentsLastMove.startRow][opponentsLastMove.startCol].setOpponentsPath(true);
                        cellViews[opponentsLastMove.distRow][opponentsLastMove.distCol].setOpponentsPath(true);
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
        SimpleAIPlayer player2 = new SimpleAIPlayer();

        currentMove = new Game.Move();
        game = new Game(player, player2);

        currentPlayerText = (TextView) findViewById(R.id.currentPlayerText);


        timer = new Timer();

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

       /* ListView list = (ListView) findViewById(R.id.listView);

        ArrayList<String> s = new ArrayList<String>(Arrays.asList("WHITE: e1 - d2","BLACK: a8 - b7"));
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1,s);

        s.add("WHITE: d2 - e3");
        s.add("BLACK: bc6");




        list.setAdapter(adapter);*/

    }
}
