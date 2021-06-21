package bg.dominos.gfx;


import bg.dominos.MainApp;
import bg.dominos.game.GameApp;
import bg.dominos.game.Handler;
import bg.dominos.model.Domino;
import bg.dominos.model.Position;
import javafx.application.Platform;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class Piece extends ImageView {
    private int row, column;
    private final Domino domino;
    private boolean center = false;

    public Piece(Domino domino, Handler handler) {
        this.domino = domino;
        boolean vertical = domino.getPosition() == Position.TOP || domino.getPosition() == Position.BOTTOM
                || (domino.getPosition() == Position.CENTER && domino.isDouble());
        Image image = Assets.getDomino(domino.getLeftValue(), domino.getRightValue(), vertical);
        setImage(image);
        if (domino.getLeftValue() < domino.getRightValue()) setRotate(180);
        setPreserveRatio(true);
        if (vertical) fitHeightProperty().bind(MainApp.dominoesProperty);
        else fitWidthProperty().bind(MainApp.dominoesProperty);

        if (domino.getPosition() == Position.BOTTOM || domino.getPosition() == Position.CENTER) {
            setOnMouseClicked(e -> {
                GameApp game = handler.getGame();
                if (game.getCurrentPosition() != Position.BOTTOM) return;
                if (domino.getPosition() == Position.BOTTOM) {
                    if (game.getSelectedPiece() == null) {
                        game.setSelectedPiece(this);
                        setHighlighted(true);
                        game.highlightPossibleMoves(true);
                    } else {
                        if (game.getSelectedPiece() == this) {
                            game.highlightPossibleMoves(false);
                            setHighlighted(false);
                            game.setSelectedPiece(null);
                        } else {
                            if (game.getSelectedPiece() != null) {
                                game.getSelectedPiece().setHighlighted(false);
                                game.highlightPossibleMoves(false);
                                game.setSelectedPiece(null);
                            }
                            game.setSelectedPiece(this);
                            setHighlighted(true);
                            game.highlightPossibleMoves(true);
                        }
                    }
                } else if (domino.getPosition() == Position.CENTER) {
                    if (game.getPossibleMoves().contains(this)) {
                        game.iplayed(this == game.getBoard().getMostRightPiece() && game.isLegalRight());
                    }
                }
            });
        } else {
            if (domino.getPosition() == Position.TOP) fitWidthProperty().bind(MainApp.otherDominoesProperty);
            else fitHeightProperty().bind(MainApp.otherDominoesProperty);
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

    public void set_center() {
        center = true;
    }

    public Domino getDomino() {
        return domino;
    }

    public void setHighlighted(boolean highlighted) {
        if (highlighted) {
            ColorAdjust colorAdjust = new ColorAdjust();
            if (center) {
                colorAdjust.setHue(0.2810457547505696);
                colorAdjust.setSaturation(1.0);
            } else colorAdjust.setBrightness(-0.17254900932312012);
            setEffect(colorAdjust);
        } else {
            setEffect(null);
        }
    }

    public void setVertical(boolean flip) {
        Platform.runLater(() -> {
            setRotate(0);
            Image image = Assets.getVertical(domino.getLeftValue(), domino.getRightValue());
            setImage(image);
            if (flip) setRotate(180);
            fitHeightProperty().bind(MainApp.dominoesProperty);
            fitWidthProperty().unbind();
        });
    }

    public void setHorizontal(boolean in_middle_row) {
        Platform.runLater(() -> {
            setRotate(0);
            Image image = Assets.getHorizontal(domino.getLeftValue(), domino.getRightValue());
            setImage(image);
            if (in_middle_row && domino.getLeftValue() < domino.getRightValue()
                    || !in_middle_row && domino.getLeftValue() > domino.getRightValue())
                setRotate(180);
            fitWidthProperty().bind(MainApp.dominoesProperty);
            fitHeightProperty().unbind();
        });
    }

    public void setDrawSize(boolean vertical_pos) {
        if (vertical_pos) fitWidthProperty().bind(MainApp.drawDominoesProperty);
        else fitHeightProperty().bind(MainApp.drawDominoesProperty);
    }
}
