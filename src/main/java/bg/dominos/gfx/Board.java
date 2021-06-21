package bg.dominos.gfx;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

import bg.dominos.game.GameApp;
import bg.dominos.game.Handler;
import bg.dominos.model.Domino;
import bg.dominos.model.Position;
import javafx.application.Platform;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.layout.GridPane;

public class Board extends GridPane {

    private final Handler handler;
    private Piece leftPiece, rightPiece;
    private final List<Piece> pieces;
    private final Point2D nextLeft, nextRight;

    public Board(Handler handler) {
        this.handler = handler;
        setAlignment(Pos.CENTER);
        pieces = new ArrayList<>();
        nextLeft = new Point2D.Double();
        nextRight = new Point2D.Double();
        nextRight.setLocation(8, 3);
        nextLeft.setLocation(nextRight);
        setOnMouseClicked(e -> {
            GameApp game = handler.getGame();
            Piece selected = game.getSelectedPiece();
            if (game.isYourTurn() && isEmpty() && selected != null) {
                game.iplayed(true); // first played domino
            }
        });
    }

    public void reset_board() {
        Platform.runLater(() -> getChildren().clear());
        pieces.clear();
        leftPiece = null;
        rightPiece = null;
        nextRight.setLocation(8, 3);
        nextLeft.setLocation(nextRight);
    }

    public boolean isEmpty() {
        return rightPiece == null && leftPiece == null;
    }

    private void setupPiece(Piece piece, boolean right, int column, int row) {
        piece.setRow(row);
        piece.setColumn(column);
        piece.getDomino().setPosition(Position.CENTER);
        boolean flip = setupLogic(piece, right);
        setupDominoInGUI(piece, row, column, flip);
        piece.set_center();
        setupGraphic(piece, row);
        setConstraints(piece, column, row);
    }

    private boolean setupLogic(Piece piece, boolean right) {
        if (!isEmpty()) {
            if (right) {
                if (rightPiece.getDomino().getRightValue() != piece.getDomino().getLeftValue()) {
                    piece.getDomino().Switch();
                    return true;
                }
            } else {
                if (leftPiece.getDomino().getLeftValue() != piece.getDomino().getRightValue()) {
                    piece.getDomino().Switch();
                    return true;
                }
            }
        }
        return false;
    }

    private void setupGraphic(Piece piece, int row) {
        if (row == 0) {
            GridPane.setValignment(piece, VPos.BOTTOM);
        } else if (row == 6) {
            GridPane.setValignment(piece, VPos.TOP);
        } else if (row != 3) {
            if (row < 3) {
                GridPane.setValignment(piece, VPos.BOTTOM);
                GridPane.setHalignment(piece, HPos.LEFT);
            } else {
                GridPane.setValignment(piece, VPos.TOP);
                GridPane.setHalignment(piece, HPos.RIGHT);
            }
        }
    }

    private void setupDominoInGUI(Piece piece, int row, int column, boolean flip) {
        boolean in_corner, in_middle_row = row == 3;
        if (isEmpty()) {
            if (piece.getDomino().isDouble()) piece.setVertical(flip);
            else piece.setHorizontal(in_middle_row);
            return;
        }
        Piece otherPiece;
        if (in_middle_row) {
            in_corner = column == 0 || column == 16;
            if (column > 9) { // ONLY CHECK TOP
                otherPiece = getPiece(0, column);
            } else { // ONLY CHECK BOTTOM
                otherPiece = getPiece(6, column);
            }
        } else { // CHECK CENTER
            if (row == 0) {
                in_corner = column == 0;
                otherPiece = getPiece(3, column);
            } else if (row == 6) {
                in_corner = column == 16;
                otherPiece = getPiece(3, column);
            } else { // row in {1,2,4,5}
                piece.setVertical(flip);
                return;
            }
        }
        if (in_corner) {
            piece.setHorizontal(in_middle_row);
            return;
        }
        if (otherPiece == null) {
            if (piece.getDomino().isDouble()) {
                piece.setVertical(flip);
            } else {
                piece.setHorizontal(in_middle_row);
            }
        } else {
            if (piece.getDomino().isDouble()) {
                if (otherPiece.getDomino().isDouble()) piece.setVertical(flip);
                else piece.setHorizontal(true); // doesn't matter because it has the same l/r values
            } else {
                if (otherPiece.getDomino().isDouble()) {
                    otherPiece.setHorizontal(true); // doesn't matter because it has the same l/r values
                }
                piece.setHorizontal(in_middle_row);
            }
        }
    }

    public void play(Piece piece, boolean right) {
        handler.getGame().removePiece();
        if (!isEmpty()) {
            if (right) {
                rightPiece.getDomino().setRight(piece.getDomino());
                piece.getDomino().setLeft(rightPiece.getDomino());
                setupPiece(piece, true, (int) nextRight.getX(), (int) nextRight.getY());
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
                setupPiece(piece, false, (int) nextLeft.getX(), (int) nextLeft.getY());
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
            setupPiece(piece, right, (int) nextRight.getX(), (int) nextRight.getY());
            leftPiece = piece;
            rightPiece = piece;
            nextRight.setLocation(nextRight.getX() + 1, nextRight.getY());
            nextLeft.setLocation(nextLeft.getX() - 1, nextLeft.getY());
        }
        pieces.add(piece);
        Platform.runLater(() -> getChildren().add(piece));
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

    public int[] get_extermity() {
        return new int[]{leftPiece.getDomino().getLeftValue(), rightPiece.getDomino().getRightValue()};
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
