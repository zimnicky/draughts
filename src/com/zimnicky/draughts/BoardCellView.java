package com.zimnicky.draughts;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

public class BoardCellView extends View{

    private Paint highlightPaint;
    private Paint canSelectPiecePaint;
    private Paint canSelectCellPaint;
    private Paint whitePiecePaint;
    private Paint blackPiecePaint;
    private Paint blackCellPaint;
    private Paint queenPaint;
    private Paint opponentsPathPaint;


    private boolean highlighted;
    private boolean canSelect;
    private boolean opponentsPath;
    private int row;
    private int col;
    private Board.Cell cell;

    private void init(){
        cell = Board.Cell.EMPTY;
        highlighted = false;
        highlightPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        highlightPaint.setColor(Color.GREEN);
        highlightPaint.setStyle(Paint.Style.STROKE);
        highlightPaint.setStrokeWidth(5);

        whitePiecePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        whitePiecePaint.setColor(Color.WHITE);

        blackPiecePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        blackPiecePaint.setColor(Color.RED);

        blackCellPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        blackCellPaint.setColor(Color.BLACK);

        canSelectCellPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        canSelectCellPaint.setColor(Color.rgb(185,185,185));

        canSelectPiecePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        canSelectPiecePaint.setColor(Color.rgb(40,0,220));

        queenPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        queenPaint.setColor(Color.BLACK);
        queenPaint.setStyle(Paint.Style.STROKE);
        queenPaint.setStrokeWidth(2 + getHeight()/30.f);

        opponentsPathPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        opponentsPathPaint.setColor(Color.rgb(177,220,174));

    }


    public BoardCellView(Context context, int row, int col){
        super(context);
        init();
        this.row = row;
        this.col = col;
    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }

    public boolean isHighlighted() {
        return highlighted;
    }

    public void setHighlighted(boolean val){
        highlighted = val;
        invalidate();
    }

    public boolean isOpponentsPath() {
        return opponentsPath;
    }

    public void setOpponentsPath(boolean opponentsPath) {
        this.opponentsPath = opponentsPath;
    }

    public void setCell(Board.Cell cell) {
        this.cell = cell;
        this.invalidate();
    }

    public boolean isCanSelect() {
        return canSelect;
    }

    public void setCanSelect(boolean canSelect) {
        this.canSelect = canSelect;
    }

    public Board.Cell getCell() {
        return cell;
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        int size = width > height ? height : width;
        setMeasuredDimension(size, size);
    }

    protected void onDraw(Canvas canvas) {

        if (!canSelect) {
            if (!opponentsPath) {
                super.onDraw(canvas);
            } else {
                canvas.drawRect(0, 0, getHeight(), getWidth(), opponentsPathPaint);
            }
        }
        else {
            if (cell == Board.Cell.EMPTY) {
                canvas.drawRect(0, 0, getHeight(), getWidth(), canSelectCellPaint);
            } else {
                canvas.drawRect(0, 0, getHeight(), getWidth(), canSelectPiecePaint);
            }
        }

        if (cell == Board.Cell.EMPTY || cell == Board.Cell.INVALID){
            return;
        }

        Paint piecePaint = null;
        if (cell.isBlack()) {
            piecePaint = blackPiecePaint;
        } else {
            piecePaint = whitePiecePaint;
        }
        canvas.drawCircle(getHeight()/2, getWidth()/2, getHeight()/2 - 6, piecePaint);
        if (cell.isQueen()){
            //canvas.drawLine(0, getWidth()/2, getHeight(), getWidth()/2, queenPaint);
            //canvas.drawLine(getHeight()/2, 0, getHeight()/2, getWidth(), queenPaint);
            canvas.drawCircle(getHeight()/2, getWidth()/2, getHeight()/2 - (5 + getHeight()/12.5f), queenPaint   );
        }

        if (highlighted) {
            canvas.drawRect(0, 0, getHeight(), getWidth(), highlightPaint);
        }

    }

}
