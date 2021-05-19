package bg.dominos.gfx;

import java.util.ArrayList;
import java.util.List;

import bg.dominos.game.Handler;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import bg.dominos.model.Domino;
import bg.dominos.model.Player;
import bg.dominos.model.Position;

public class TopBottomPane extends HBox {
    private final Handler handler;
    private Player player;
    private final List<Piece> pieces;
    private final Text name;
    private final Circle turn;
    private final Paint yourTurn = Paint.valueOf("gold"), notYourTurn = Paint.valueOf("red");
    private final Position panePosition;
    private final Text points;

    public TopBottomPane(Handler handler, Position panePosition) {
        pieces = new ArrayList<>();
        this.panePosition = panePosition;
        this.handler = handler;
        setAlignment(Pos.CENTER);
        turn = new Circle(15, notYourTurn);
        HBox hbox = new HBox();
        hbox.setAlignment(Pos.CENTER);
        this.name = new Text();
        this.name.setFont(Font.font(30));
        points = new Text();
        points.setFont(Font.font(30));
        hbox.getChildren().addAll(this.name, turn);
        getChildren().addAll(points, hbox);
    }

    public boolean isEmpty() {
        return pieces.isEmpty();
    }

    private void LoadPieces() {
        Platform.runLater(() -> {
            try {
                synchronized (pieces) {
                    for (int i = 0; i < pieces.size(); i++) {
                        removePiece();
                    }
                }
                for (Domino domino : player.getDominos()) {
                    Piece piece = new Piece(domino, handler);
                    pieces.add(piece);
                    getChildren().add(piece);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
    }

    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player player) {
        try {
            if (player != null) {
                this.player = player;
                this.player.setPosition(panePosition);
                LoadPieces();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void updatePoints(String text) {
        points.setText(text);
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

    public void drawPiece() {
        Domino domino = new Domino(player.getPosition());
        player.drawDomino(domino);
        Piece piece = new Piece(domino, handler);
        pieces.add(piece);
        Platform.runLater(() -> getChildren().add(piece));
    }

    public void addPiece(Piece piece) {
        try {
            player.drawDomino(piece.getDomino());
            pieces.add(piece);
            Platform.runLater(() -> getChildren().add(piece));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void removePiece() {
        try {
            Piece removedPiece = pieces.get(0);
            player.removeDomino(pieces.get(0).getDomino());
            pieces.remove(0);
            Platform.runLater(() -> getChildren().remove(removedPiece));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void removePiece(Piece piece) {
        try {
            pieces.remove(piece);
            player.removeDomino(piece.getDomino());
            Platform.runLater(() -> getChildren().remove(piece));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void removePieces() {
        try {
            int size = pieces.size();
            for (int i = 0; i < size; i++) {
                removePiece();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void setName(String name) {
        this.name.setText(name);
    }
}
