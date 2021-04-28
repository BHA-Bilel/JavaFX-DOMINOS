package gfx;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

import game.Handler;
import model.Domino;
import model.Position;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;

public class Board extends GridPane {

    private final Handler handler;
    private Piece leftPiece, rightPiece;
    private final List<Piece> pieces = new ArrayList<>();
    private final Point2D nextLeft = new Point2D.Double(), nextRight = new Point2D.Double();

    public Board(Handler handler) {
        this.handler = handler;
        setAlignment(Pos.CENTER);
        nextRight.setLocation(8, 3);
        nextLeft.setLocation(nextRight);
        setOnMouseClicked(e -> {
            if (handler.getGame().isYourTurn() && isEmpty() && handler.getGame().getSelectedPiece() != null) {
                play(handler.getGame().getSelectedPiece(), true);
            }
        });
    }

    public boolean isEmpty() {
        return rightPiece == null && leftPiece == null;
    }

    private Piece setupPiece(Piece piece, boolean right, int column, int row) {
        piece.setRow(row);
        piece.setColumn(column);
        piece.getDomino().setPosition(Position.CENTER);
        piece = setupLogic(piece, right);
        if (canIsetItVertical(piece, row, column))
            piece.setVertical(false);
        else
            piece.setHorizontal(row == 0 || row == 6);
        piece = setupGraphic(piece, row);
        setConstraints(piece, column, row);
        return piece;
    }

    private Piece setupLogic(Piece piece, boolean right) {
        if (!isEmpty()) {
            if (right) {
                if (rightPiece.getDomino().getRightValue() != piece.getDomino().getLeftValue()) {
                    piece.getDomino().Switch();
                }
            } else {
                if (leftPiece.getDomino().getLeftValue() != piece.getDomino().getRightValue()) {
                    piece.getDomino().Switch();
                }
            }
        }
        return piece;
    }

    private Piece setupGraphic(Piece piece, int row) {
        if (row == 0) {
            piece.setTranslateY(50);
        } else if (row == 6) {
            piece.setTranslateY(-50);
        } else if (row != 3) {
            if (row < 3) {
                piece.setTranslateX(-25);
                piece.setTranslateY(25);
            } else {
                piece.setTranslateX(25);
                piece.setTranslateY(-25);
            }
        }
        return piece;
    }

    private boolean canIsetItVertical(Piece piece, int row, int column) {
        boolean otherConditions;
        Piece otherPiece;
        if (row == 3) {
            otherConditions = column != 0 && column != 16;
            if (column > 9) { // ONLY CHECK TOP
                otherPiece = getPiece(0, column);
            } else { // ONLY CHECK BOTTOM
                otherPiece = getPiece(6, column);
            }
        } else {
            if (row == 0) {// CHECK CENTER
                otherConditions = column != 0;
                otherPiece = getPiece(3, column);

            } else if (row == 6) { // ROW = 6
                // CHECK CENTER
                otherConditions = column != 16;
                otherPiece = getPiece(3, column);
            } else {
                return true;
            }
        }
        if (otherPiece == null) {
            if (isEmpty())
                return piece.getDomino().isDouble();
            else
                return piece.getDomino().isDouble() && otherConditions;
        } else {
            if (piece.getDomino().isDouble()) {
                return otherPiece.getDomino().isDouble();
            } else {
                if (otherPiece.getDomino().isDouble()) {
                    otherPiece.setHorizontal();
                }
                return false;
            }
        }
    }

    public void play(Piece piece, boolean right) {
        if (handler.getGame().getCurrentPosition() == Position.BOTTOM) {
            handler.getGame().getPlay().play(piece.getDomino().getLeftValue(), piece.getDomino().getRightValue(),
                    right);
        }
        handler.getGame().removePiece();
        if (!isEmpty()) {
            if (right) {
                rightPiece.getDomino().setRight(piece.getDomino());
                piece.getDomino().setLeft(rightPiece.getDomino());
                piece = setupPiece(piece, true, (int) nextRight.getX(), (int) nextRight.getY());
                rightPiece = piece;
                int nextColumn = (int) nextRight.getX();
                int nextRow = (int) nextRight.getY();
                if (nextColumn == 16) {
                    if (nextRow < 6) {
                        nextRow++;
                    } else {
                        nextColumn--;
                    }
                } else if (nextRow == 6) {
                    nextColumn--;
                } else {
                    nextColumn++;
                }
                nextRight.setLocation(nextColumn, nextRow);
            } else {
                leftPiece.getDomino().setLeft(piece.getDomino());
                piece.getDomino().setRight(leftPiece.getDomino());
                piece = setupPiece(piece, false, (int) nextLeft.getX(), (int) nextLeft.getY());
                leftPiece = piece;
                int nextColumn = (int) nextLeft.getX();
                int nextRow = (int) nextLeft.getY();
                if (nextColumn == 0) {
                    if (nextRow > 0) {
                        nextRow--;
                    } else {
                        nextColumn++;
                    }
                } else if (nextRow == 0) {
                    nextColumn++;
                } else {
                    nextColumn--;
                }
                nextLeft.setLocation(nextColumn, nextRow);
            }
        } else {
            piece = setupPiece(piece, right, (int) nextRight.getX(), (int) nextRight.getY());
            leftPiece = piece;
            rightPiece = piece;
            nextRight.setLocation(nextRight.getX() + 1, nextRight.getY());
            nextLeft.setLocation(nextLeft.getX() - 1, nextLeft.getY());
        }
        piece.setBorderFill(Color.GOLD);
        pieces.add(piece);
        final Piece insert = piece;
        Platform.runLater(() -> getChildren().add(insert));

        if (!handler.getGame().checkState()) {
            handler.getGame().switchTurn();
        }
    }

    /**
     * count the number of each number that are in the table
     */
    private int[] count() {
        int[] cpt = new int[7];
        Domino temp = leftPiece.getDomino();
        while (temp != null) {
            cpt[temp.getLeftValue()]++;
            if (temp.getLeftValue() != temp.getRightValue()) {
                cpt[temp.getRightValue()]++;
            }
            temp = temp.getRight();
        }
        return cpt;
    }

    /**
     * checks if the road is blocked
     */
    public boolean isBlocked() {
        int[] cpt = count();
        boolean left = false, right = false;
        for (int i = 0; i < 7; i++) {
            if (cpt[i] == 7) {
                if (i == leftPiece.getDomino().getLeftValue()) {
                    left = true;
                }
                if (i == rightPiece.getDomino().getRightValue()) {
                    right = true;
                }
            }
        }
        return left && right;
    }

    public Piece getMostLeftPiece() {
        return leftPiece;
    }

    public Piece getMostRightPiece() {
        return rightPiece;
    }

    public Piece getPiece(int row, int column) {
        for (Piece piece : pieces) {
            if (piece.getRow() == row && piece.getColumn() == column)
                return piece;
        }
        return null;
    }

}
