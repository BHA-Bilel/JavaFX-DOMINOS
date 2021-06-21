package bg.dominos.gfx;

import bg.dominos.MainApp;
import bg.dominos.game.Handler;
import bg.dominos.lang.Language;
import bg.dominos.model.Domino;
import bg.dominos.model.Player;
import bg.dominos.model.Position;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;

import java.util.ArrayList;
import java.util.List;

public class LeftRightPane extends VBox {
    private final Handler handler;
    private Player player;
    private final List<Piece> pieces;
    private final Label name;
    private final Circle turn;
    private final Paint yourTurn = Paint.valueOf("gold"), notYourTurn = Paint.valueOf("red");
    private final Position panePosition;
    private boolean draw_size;

    public LeftRightPane(Handler handler, Position panePosition) {
        pieces = new ArrayList<>();
        this.panePosition = panePosition;
        this.handler = handler;
        setAlignment(Pos.CENTER);
        turn = new Circle();
        turn.radiusProperty().bind(MainApp.fontProperty.divide(3));
        turn.setFill(notYourTurn);
        this.name = new Label();
        getChildren().addAll(this.name, turn);
    }

    public boolean isEmpty() {
        return pieces.isEmpty();
    }

    private void LoadPieces() {
        Platform.runLater(() -> {
            for (Domino domino : player.getDominoes()) {
                Piece piece = new Piece(domino, null);
                if (draw_size) piece.setDrawSize(false);
                pieces.add(piece);
                getChildren().add(piece);
            }
        });
    }

    public void drawPiece() {
        Domino domino = new Domino(player.getPosition());
        player.drawDomino(domino);
        Piece piece = new Piece(domino, handler);
        pieces.add(piece);
        Platform.runLater(() -> getChildren().add(piece));
    }

    public void removePiece() {
        Piece removedPiece = pieces.get(0);
        player.removeDomino(pieces.get(0).getDomino());
        pieces.remove(0);
        Platform.runLater(() -> getChildren().remove(removedPiece));
    }

    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player player) {
        if (player != null) {
            this.player = player;
            this.player.setPosition(panePosition);
            LoadPieces();
        }
    }

    public void updateTurn() {
        Platform.runLater(() -> {
            if (handler.getGame().getCurrentPosition() == panePosition) {
                turn.setFill(yourTurn);
            } else {
                turn.setFill(notYourTurn);
            }
        });
    }

    public void clearPane() {
        pieces.clear();
        Platform.runLater(() -> {
            getChildren().clear();
            getChildren().addAll(this.name, turn);
        });
    }

    public void setDraw() {
        draw_size = true;
        Platform.runLater(() -> {
            this.name.textProperty().bind(Language.DRAW_DOMINOES);
            getChildren().remove(turn);
        });
    }

    public void clear() {
        Platform.runLater(() -> getChildren().clear());
    }
}
