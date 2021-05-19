package bg.dominos.gfx;


import bg.dominos.game.Handler;
import javafx.application.Platform;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import bg.dominos.model.Domino;
import bg.dominos.model.Position;

public class Piece extends StackPane {
    private int row, column;
    private final Domino domino;
    private Rectangle border;
    private ImageView dominoPiece;
    private Color color = Color.GRAY;

    public Piece(Domino domino, Handler handler) {
        this.domino = domino;
        dominoPiece = Assets.getDomino(domino);
        Platform.runLater(() -> getChildren().add(dominoPiece));
        if (domino.getPosition() == Position.BOTTOM) {
            border = new Rectangle(dominoPiece.getFitWidth(), dominoPiece.getFitHeight());
            border.setOpacity(0.5);
            border.setFill(Color.TRANSPARENT);
            Platform.runLater(() -> getChildren().add(border));
        }
        if (domino.getPosition() == Position.BOTTOM || domino.getPosition() == Position.CENTER) {
            setOnMouseClicked(e -> {
                if (handler.getGame().getCurrentPosition() == Position.BOTTOM) {
                    if (domino.getPosition() == Position.BOTTOM) {
                        if (handler.getGame().getSelectedPiece() == null) {
                            handler.getGame().setSelectedPiece(this);
                            setHighlighted(true);
                            handler.getGame().highlightPossibleMoves(true);
                        } else {
                            if (handler.getGame().getSelectedPiece() == this) {
                                handler.getGame().highlightPossibleMoves(false);
                                setHighlighted(false);
                                handler.getGame().setSelectedPiece(null);
                            } else {
                                if (handler.getGame().getSelectedPiece() != null) {
                                    handler.getGame().getSelectedPiece().setHighlighted(false);
                                    handler.getGame().highlightPossibleMoves(false);
                                    handler.getGame().setSelectedPiece(null);
                                }
                                handler.getGame().setSelectedPiece(this);
                                setHighlighted(true);
                                handler.getGame().highlightPossibleMoves(true);
                            }
                        }
                    } else if (domino.getPosition() == Position.CENTER) {
                        if (handler.getGame().getPossibleMoves().contains(this)) {
                            handler.getGame().getBoard().play(handler.getGame().getSelectedPiece(), this == handler.getGame().getBoard().getMostRightPiece()
                                    && handler.getGame().isLegalRight());
                        }
                    }
                }
            });
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (this == obj)
            return true;
        if (this.getClass() != obj.getClass())
            return false;
        Piece other = (Piece) obj;
        return other.getDomino().equals(domino);
    }

    public int getRow() {
        return row;
    }

    public void setRow(int row) {
        this.row = row;
    }

    public int getColumn() {
        return column;
    }

    public void setColumn(int column) {
        this.column = column;
    }

    public void setBorderFill(Color color) {
        this.color = color;
    }

    public Domino getDomino() {
        return domino;
    }

    public void setHighlighted(boolean highlighted) {
        if (highlighted) {
            border.setFill(color);
        } else {
            border.setFill(Color.TRANSPARENT);
        }
    }

    public void setVertical(boolean right) {
        setRotate(180);
        border = new Rectangle();
        border.setOpacity(0.5);
        border.setFill(Color.TRANSPARENT);
        border.setWidth(50);
        border.setHeight(100);
        Platform.runLater(() -> {
            getChildren().remove(dominoPiece);
            dominoPiece = Assets.getVertical(domino.getLeftValue(), domino.getRightValue(), right);
            getChildren().add(dominoPiece);
            getChildren().add(border);
        });
    }

    public void setHorizontal(boolean right) {
        border = new Rectangle();
        border.setOpacity(0.5);
        border.setFill(Color.TRANSPARENT);
        border.setWidth(100);
        border.setHeight(50);
        Platform.runLater(() -> {
            getChildren().remove(dominoPiece);
            dominoPiece = Assets.getHorizontal(domino.getLeftValue(), domino.getRightValue(), right);
            getChildren().add(dominoPiece);
            getChildren().add(border);
        });
    }

    public void setHorizontal() {
        border = new Rectangle();
        border.setOpacity(0.5);
        border.setFill(Color.TRANSPARENT);
        border.setWidth(100);
        border.setHeight(50);
        Platform.runLater(() -> {
            getChildren().remove(dominoPiece);
            dominoPiece = Assets.getHorizontal(domino.getLeftValue(), domino.getRightValue());
            getChildren().add(dominoPiece);
            getChildren().add(border);
        });
    }
}
