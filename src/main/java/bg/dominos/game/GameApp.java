package bg.dominos.game;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import bg.dominos.gfx.Assets;
import bg.dominos.gfx.Board;
import bg.dominos.gfx.LeftRightPane;
import bg.dominos.gfx.Piece;
import bg.dominos.gfx.TopBottomPane;
import bg.dominos.model.Domino;
import bg.dominos.model.GameType;
import bg.dominos.model.Player;
import bg.dominos.model.Position;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;

public class GameApp extends BorderPane {

    private int team1points, team2points, team3points;
    private int playerID;
    private final SpreadWinThread spreadwin;
    private final PlayThread play;
    private final BlockThread block;
    private final Handler handler;

    private final GameType GAME_TYPE;
    private Player bottomPlayer;

    private Position currentPosition;
    private Position yourPosition;

    private List<Domino> playerDominoes;
    private final List<Piece> possibleMoves = new ArrayList<>();

    private Piece selectedPiece;
    private LeftRightPane leftPane, rightPane;
    private TopBottomPane topPane, bottomPane;
    private Board board;
    private int team1_parties_won = 0, team2_parties_won = 0, team3_parties_won = 0;
    private String yourName, op1Name, op2Name;

    public GameApp(Socket playSocket, Socket spreadwinSocket, Socket blockSocket, Map<Integer, String> players_names,
                   GameType gameType) {
        this.GAME_TYPE = gameType;
        spreadwin = new SpreadWinThread(spreadwinSocket);
        play = new PlayThread(playSocket);
        block = new BlockThread(blockSocket);
        handler = new Handler(this);

        createGUI(players_names);
        startNewMatch();
    }

    public void setup_scene_effect() {
        getScene().setFill(Color.GREEN);
    }

    public void switchTurn() {
        if (currentPosition == Position.BOTTOM) {
            play.waitForYourTurn();
        }
        currentPosition = currentPosition.next(GAME_TYPE, yourPosition);
        updateGUI();
        if (currentPosition == Position.BOTTOM
                && !bottomPlayer.canPlay(board.getMostLeftPiece().getDomino().getLeftValue(),
                board.getMostRightPiece().getDomino().getRightValue())) {
            if (GAME_TYPE == GameType.TwovTwo) {
                pass();
            } else {
                draw();
            }
        }
    }

    private void updateGUI() {
        bottomPane.updateTurn();
        leftPane.updateTurn();
        topPane.updateTurn();
        rightPane.updateTurn();
    }

    private boolean canDraw() {
        if (yourPosition == Position.BOTTOM) {
            return !rightPane.getPlayer().getDominos().isEmpty();
        } else if (yourPosition == Position.TOP) {
            return !leftPane.getPlayer().getDominos().isEmpty();
        } else { // LEFT
            return !topPane.getPlayer().getDominos().isEmpty();
        }
    }

    public void draw() {
        if (currentPosition != Position.BOTTOM) {
            if (yourPosition == Position.BOTTOM) {
                rightPane.removePiece();
            } else if (yourPosition == Position.TOP) {
                leftPane.removePiece();
            } else { // LEFT
                topPane.removePiece();
            }
            if (currentPosition == Position.LEFT) {
                leftPane.drawPiece();
            } else if (currentPosition == Position.TOP) {
                topPane.drawPiece();
            } else { // RIGHT
                rightPane.drawPiece();
            }
        } else { // BOTTOM
            do {
                if (canDraw()) {
                    Piece piece = new Piece(play.Draw(), handler);
                    bottomPane.addPiece(piece);
                    if (yourPosition == Position.BOTTOM) {
                        rightPane.removePiece();
                    } else if (yourPosition == Position.TOP) {
                        leftPane.removePiece();
                    } else { // LEFT
                        topPane.removePiece();
                    }
                } else {
                    pass();
                    break;
                }
            } while (!bottomPlayer.canPlay(board.getMostLeftPiece().getDomino().getLeftValue(),
                    board.getMostRightPiece().getDomino().getRightValue()));
        }
    }

    public void pass() {
        if (currentPosition == Position.BOTTOM) {
            play.Pass();
        }
        switchTurn();
    }

    public boolean isLegalRight() {
        int mostRightvalue = board.getMostRightPiece().getDomino().getRightValue();
        return selectedPiece.getDomino().getRightValue() == mostRightvalue
                || selectedPiece.getDomino().getLeftValue() == mostRightvalue;
    }

    public boolean isLegalLeft() {
        int mostLeftValue = board.getMostLeftPiece().getDomino().getLeftValue();
        return selectedPiece.getDomino().getRightValue() == mostLeftValue
                || selectedPiece.getDomino().getLeftValue() == mostLeftValue;
    }

    public void highlightPossibleMoves(boolean highlight) {
        if (!board.isEmpty()) {
            if (highlight) {
                if (isLegalLeft())
                    possibleMoves.add(board.getMostLeftPiece());
                if (isLegalRight())
                    possibleMoves.add(board.getMostRightPiece());
                for (Piece domino : possibleMoves) {
                    domino.setHighlighted(true);
                }
            } else {
                for (Piece domino : possibleMoves) {
                    domino.setHighlighted(false);
                }
                possibleMoves.clear();
            }
        }
    }

    public void startNewMatch() {
        team1points = 0;
        team2points = 0;
        team3points = 0;
        startNewGame();
    }

    public boolean isFinished() {
        boolean finished;
        if (GAME_TYPE != GameType.ThreePlayers)
            finished = team1points >= 100 || team2points >= 100;
        else
            finished = team1points >= 100 || team2points >= 100 || team3points >= 100;

        if (finished) {
            if (team1points > team2points) {
                if (team1points > team3points) {
                    team1_parties_won++;
                } else {
                    team3_parties_won++;
                }
            } else {
                if (team2points > team3points) {
                    team2_parties_won++;
                } else {
                    team3_parties_won++;
                }
            }
            printResults();
        }
        return finished;
    }

    private void printResults() {
        int team1points = this.team1points;
        int team2points = this.team2points;
        int team3points = this.team3points;
        Platform.runLater(() -> {
            Alert alert = new Alert(AlertType.INFORMATION);
            alert.setTitle("Game results");
            String score;
            if (GAME_TYPE == GameType.TwovTwo) {
                if (currentPosition == Position.TOP || currentPosition == Position.BOTTOM) {
                    alert.setHeaderText("You won !");
                } else {
                    alert.setHeaderText("You lost !");
                }
                score = "Your points : " + team1points + "\n";
                score += "Opponents points : " + team2points + "\n\n";
                score += "Total parties won : " + "\n";
                score += "Your team : " + team1_parties_won + "\n";
                score += "Opponents team : " + team2_parties_won;
            } else if (GAME_TYPE == GameType.ThreePlayers) {
                if (currentPosition == Position.BOTTOM) {
                    alert.setHeaderText("You won !");
                } else {
                    alert.setHeaderText("You lost !");
                }
                if (yourPosition == Position.BOTTOM) {
                    score = yourName + " : " + team1points + "\n";
                    score += op1Name + " : " + team2points + "\n";
                    score += op2Name + " : " + team3points + "\n\n";
                    score += "Total parties won : " + "\n";
                    score += yourName + " : " + team1_parties_won + "\n";
                    score += op1Name + " : " + team2_parties_won + "\n";
                    score += op2Name + " : " + team3_parties_won;
                } else if (yourPosition == Position.TOP) {
                    score = yourName + " : " + team2points + "\n";
                    score += op1Name + " : " + team3points + "\n";
                    score += op2Name + " : " + team1points + "\n\n";
                    score += "Total parties won : " + "\n";
                    score += yourName + " : " + team2_parties_won + "\n";
                    score += op1Name + " : " + team3_parties_won + "\n";
                    score += op2Name + " : " + team1_parties_won;
                } else { // LEFT
                    score = yourName + " : " + team3points + "\n";
                    score += op1Name + " : " + team1points + "\n";
                    score += op2Name + " : " + team2points + "\n\n";
                    score += "Total parties won : " + "\n";
                    score += yourName + " : " + team3_parties_won + "\n";
                    score += op1Name + " : " + team1_parties_won + "\n";
                    score += op2Name + " : " + team2_parties_won;
                }
            } else {
                if (currentPosition == Position.BOTTOM) {
                    alert.setHeaderText("You won !");
                } else {
                    alert.setHeaderText("You lost !");
                }
                score = yourName + " : " + team1points + "\n";
                score += op1Name + " : " + team2points + "\n\n";
                score += "Total parties won : " + "\n";
                score += yourName + " : " + team1_parties_won + "\n";
                score += op1Name + " : " + team2_parties_won;
            }
            alert.setContentText(score);
            alert.show();
        });
    }


    protected void initPlayers() {
        bottomPlayer = new Player(Position.BOTTOM);
        bottomPlayer.setDominoes(playerDominoes);
        Player leftPlayer = new Player(Position.LEFT);
        Player topPlayer = new Player(Position.TOP);
        Player rightPlayer = new Player(Position.RIGHT);
        if (GAME_TYPE == GameType.TwovTwo) {
            leftPlayer.loadOtherPlayersDominoes();
            rightPlayer.loadOtherPlayersDominoes();
            topPlayer.loadOtherPlayersDominoes();
        } else if (GAME_TYPE == GameType.ThreePlayers) {
            if (yourPosition == Position.BOTTOM) {
                topPlayer.loadOtherPlayersDominoes();
                rightPlayer.initDrawPlayer(GAME_TYPE);
                rightPane.setName("draw dominos");
                leftPlayer.loadOtherPlayersDominoes();
            } else if (yourPosition == Position.TOP) {
                topPlayer.loadOtherPlayersDominoes();
                leftPlayer.initDrawPlayer(GAME_TYPE);
                leftPane.setName("draw dominos");
                rightPlayer.loadOtherPlayersDominoes();
            } else { // LEFT
                leftPlayer.loadOtherPlayersDominoes();
                rightPlayer.loadOtherPlayersDominoes();
                topPlayer.initDrawPlayer(GAME_TYPE);
                topPane.setName("draw dominos");
            }
        } else { // ONE VS ONE
            if (yourPosition == Position.BOTTOM) {
                topPlayer.loadOtherPlayersDominoes();
                rightPlayer.initDrawPlayer(GAME_TYPE);
                rightPane.setName("draw dominos");
                leftPane.clear();
            } else { // TOP
                topPlayer.loadOtherPlayersDominoes();
                leftPlayer.initDrawPlayer(GAME_TYPE);
                leftPane.setName("draw dominos");
                rightPane.clear();
            }
        }
        bottomPane.setPlayer(bottomPlayer);
        leftPane.setPlayer(leftPlayer);
        topPane.setPlayer(topPlayer);
        rightPane.setPlayer(rightPlayer);
    }

    public void removePiece() {
        if (currentPosition == Position.BOTTOM) {
            bottomPane.removePiece(selectedPiece);
            handler.getGame().highlightPossibleMoves(false);
            handler.getGame().getSelectedPiece().setHighlighted(false);
            handler.getGame().setSelectedPiece(null);
        } else if (currentPosition == Position.LEFT) {
            leftPane.removePiece();
        } else if (currentPosition == Position.TOP) {
            topPane.removePiece();
        } else {
            rightPane.removePiece();
        }
    }

    public boolean checkState() {
        boolean won;
        if (currentPosition == Position.BOTTOM) {
            won = bottomPane.isEmpty();
        } else if (currentPosition == Position.LEFT) {
            won = leftPane.isEmpty();
        } else if (currentPosition == Position.TOP) {
            won = topPane.isEmpty();
        } else {
            won = rightPane.isEmpty();
        }
        if (won) {
            won();
            return true;
        } else {
            boolean blocked = board.isBlocked();
            if (blocked) {
                block.blocked();
                return true;
            }
        }
        return false;
    }

    private void won() {
        if (GAME_TYPE == GameType.TwovTwo) {
            if (currentPosition == Position.TOP || currentPosition == Position.BOTTOM) {
                spreadwin.iWon();
            } else {
                spreadwin.iLost();
            }
        } else {
            if (currentPosition == Position.BOTTOM) {
                spreadwin.iWon();
            } else {
                spreadwin.iLost();
            }
        }
    }

    public void startNewGame() {
        clearDominoes();
        board = new Board(handler);
        Platform.runLater(() -> setCenter(board));
        spreadwin.getDominos();
        updateGUI();
        updateScore();
    }

    private void updateScore() {
        if (GAME_TYPE == GameType.TwovTwo) {
            topPane.updatePoints("Your points : " + team1points);
            leftPane.updatePoints("Opponents points : " + team2points);
        } else if (GAME_TYPE == GameType.ThreePlayers) {
            if (yourPosition == Position.BOTTOM) {
                bottomPane.updatePoints(yourName + " : " + team1points);
                topPane.updatePoints(op1Name + " : " + team2points);
                leftPane.updatePoints(op2Name + " : " + team3points);
            } else if (yourPosition == Position.TOP) {
                bottomPane.updatePoints(yourName + " : " + team2points);
                rightPane.updatePoints(op1Name + " : " + team3points);
                topPane.updatePoints(op2Name + " : " + team1points);
            } else { // LEFT
                bottomPane.updatePoints(yourName + " : " + team3points);
                rightPane.updatePoints(op1Name + " : " + team1points);
                leftPane.updatePoints(op2Name + " : " + team2points);
            }
        } else { // ONE VS ONE
            topPane.updatePoints(op1Name + " : " + team2points);
            bottomPane.updatePoints(yourName + " : " + team1points);
        }
    }

    private void clearDominoes() {
        bottomPane.removePieces();
        leftPane.removePieces();
        topPane.removePieces();
        rightPane.removePieces();
    }

    private void initPlayersNames(Map<Integer, String> players_names) {
        switch (GAME_TYPE) {
            case TwovTwo: {
                int game_pos = playerID;
                yourName = players_names.get(game_pos);
                bottomPane.setName(yourName);
                game_pos++;
                if (game_pos > 4)
                    game_pos = 1;
                rightPane.setName(players_names.get(game_pos));
                game_pos++;
                if (game_pos > 4)
                    game_pos = 1;
                topPane.setName(players_names.get(game_pos));
                game_pos++;
                if (game_pos > 4)
                    game_pos = 1;
                leftPane.setName(players_names.get(game_pos));
                break;
            }
            case OnevOne: {
                if (playerID == 1) {
                    yourName = players_names.get(1);
                    bottomPane.setName(yourName);
                    op1Name = players_names.get(2);
                } else {
                    yourName = players_names.get(2);
                    bottomPane.setName(yourName);
                    op1Name = players_names.get(1);
                }
                topPane.setName(op1Name);
                break;
            }
            case ThreePlayers: {
                switch (playerID) {
                    case 1: {
                        yourName = players_names.get(1);
                        bottomPane.setName(yourName);
                        op1Name = players_names.get(2);
                        topPane.setName(op1Name);
                        op2Name = players_names.get(3);
                        leftPane.setName(op2Name);
                        break;
                    }
                    case 2: {
                        yourName = players_names.get(2);
                        bottomPane.setName(yourName);
                        op1Name = players_names.get(3);
                        rightPane.setName(op1Name);
                        op2Name = players_names.get(1);
                        topPane.setName(op2Name);
                        break;
                    }
                    case 3: {
                        yourName = players_names.get(3);
                        bottomPane.setName(yourName);
                        op1Name = players_names.get(1);
                        rightPane.setName(op1Name);
                        op2Name = players_names.get(2);
                        leftPane.setName(op2Name);
                        break;
                    }
                }
                break;
            }
        }
    }

    protected void createGUI(Map<Integer, String> players_names) {
        Assets.init();

        board = new Board(handler);
        bottomPane = new TopBottomPane(handler, Position.BOTTOM);
        bottomPane.setAlignment(Pos.BOTTOM_CENTER);
        leftPane = new LeftRightPane(handler, Position.LEFT);
        leftPane.setAlignment(Pos.CENTER_LEFT);
        topPane = new TopBottomPane(handler, Position.TOP);
        topPane.setAlignment(Pos.TOP_CENTER);
        rightPane = new LeftRightPane(handler, Position.RIGHT);
        rightPane.setAlignment(Pos.CENTER_RIGHT);
        setCenter(board);
        setRight(rightPane);
        setLeft(leftPane);
        setTop(topPane);
        setBottom(bottomPane);
        setPrefSize(1280, 920);
        initPlayersNames(players_names);
    }

    // GETTERS SETTERS

    public boolean isYourTurn() {
        return currentPosition == Position.BOTTOM;
    }

    public Position getCurrentPosition() {
        return currentPosition;
    }

    public List<Piece> getPossibleMoves() {
        return possibleMoves;
    }

    public Piece getSelectedPiece() {
        return selectedPiece;
    }

    public Board getBoard() {
        return board;
    }

    public void setSelectedPiece(Piece selectedPiece) {
        this.selectedPiece = selectedPiece;
    }

    public void closeGameApp() {
        block.closeConn();
        play.closeConn();
        spreadwin.closeConn();
        Platform.runLater(() -> getChildren().clear());
    }

    private class SpreadWinThread extends Thread {

        private Socket spreadwinSocket;
        private DataInputStream dataIn;
        private DataOutputStream dataOut;

        public SpreadWinThread(Socket spreadwinSocket) {
            try {
                this.spreadwinSocket = spreadwinSocket;
                dataIn = new DataInputStream(spreadwinSocket.getInputStream());
                dataOut = new DataOutputStream(spreadwinSocket.getOutputStream());
                playerID = dataIn.readInt();
                int whoStart = dataIn.readInt();
                yourPosition = Position.getPositionByPlayerID(GAME_TYPE, playerID);
                currentPosition = Position.getCurrentPositionByPlayerID(GAME_TYPE, yourPosition, whoStart);
            } catch (IOException ignore) {
            }
        }

        public void startAnotherGame() {
            try {
                dataOut.writeInt(-1);
                dataOut.flush();
            } catch (IOException ignore) {
            }
        }

        public void getDominos() {
            List<Domino> dominoes = new ArrayList<>();
            try {
                for (int i = 0; i < 7; i++) {
                    int left = dataIn.readInt();
                    int right = dataIn.readInt();
                    dominoes.add(new Domino(left, right, Position.BOTTOM));
                }
            } catch (IOException ignore) {
            }
            playerDominoes = dominoes;
            initPlayers();
            if (currentPosition != Position.BOTTOM)
                play.waitForYourTurn();
        }

        public void iWon() {
            int value;
            if (GAME_TYPE == GameType.TwovTwo) {
                try {
                    value = dataIn.readInt();
                    value += dataIn.readInt();
                    team1points += value;
                } catch (IOException ignore) {
                }
            } else if (GAME_TYPE == GameType.ThreePlayers) {
                try {
                    value = dataIn.readInt();
                    value += dataIn.readInt();

                    if (yourPosition == Position.BOTTOM) {
                        if (currentPosition == Position.BOTTOM) { // B.P. WON
                            team1points += value;
                        } else if (currentPosition == Position.TOP) { // T.P. WON
                            team2points += value;
                        } else { // L.P. WON
                            team3points += value;
                        }
                    } else if (yourPosition == Position.TOP) {
                        if (currentPosition == Position.BOTTOM) { // T.P. WON
                            team2points += value;
                        } else if (currentPosition == Position.RIGHT) { // L.P. WON
                            team3points += value;
                        } else { // B.P. WON
                            team1points += value;
                        }
                    } else { // LEFT PLAYER
                        if (currentPosition == Position.BOTTOM) { // L.P. WON
                            team3points += value;
                        } else if (currentPosition == Position.RIGHT) { // B.P. WON
                            team1points += value;
                        } else { // T.P. WON
                            team2points += value;
                        }
                    }
                } catch (IOException ignore) {
                }
            } else {
                try {
                    value = dataIn.readInt();
                    team1points += value;
                } catch (IOException ignore) {
                }
            }
            if (playerID == 1)
                spreadwin.startAnotherGame();
            if (isFinished()) {
                startNewMatch();
            } else {
                startNewGame();
            }
        }

        public void iLost() {
            int value = getValue(playerDominoes);
            try {
                dataOut.writeInt(value);
                dataOut.flush();
            } catch (IOException ignore) {
            }
            if (GAME_TYPE == GameType.TwovTwo) {
                int teammateValue = 0;
                try {
                    teammateValue = dataIn.readInt();
                } catch (IOException ignore) {
                }
                team2points += value + teammateValue;
            } else if (GAME_TYPE == GameType.ThreePlayers) {
                int lostOPvalue = 0;
                try {
                    lostOPvalue = dataIn.readInt();
                } catch (IOException ignore) {
                }
                if (yourPosition == Position.BOTTOM) {
                    if (currentPosition == Position.BOTTOM) { // B.P. WON
                        team1points += value + lostOPvalue;
                    } else if (currentPosition == Position.TOP) { // T.P. WON
                        team2points += value + lostOPvalue;
                    } else { // L.P. WON
                        team3points += value + lostOPvalue;
                    }
                } else if (yourPosition == Position.TOP) {
                    if (currentPosition == Position.BOTTOM) { // T.P. WON
                        team2points += value + lostOPvalue;
                    } else if (currentPosition == Position.RIGHT) { // L.P. WON
                        team3points += value + lostOPvalue;
                    } else { // B.P. WON
                        team1points += value + lostOPvalue;
                    }
                } else { // LEFT PLAYER
                    if (currentPosition == Position.BOTTOM) { // L.P. WON
                        team3points += value + lostOPvalue;
                    } else if (currentPosition == Position.RIGHT) { // B.P. WON
                        team1points += value + lostOPvalue;
                    } else { // T.P. WON
                        team2points += value + lostOPvalue;
                    }
                }
            } else {
                team2points += value;
            }
            if (playerID == 1)
                spreadwin.startAnotherGame();
            if (isFinished()) {
                startNewMatch();
            } else {
                startNewGame();
            }
        }

        public void closeConn() {
            try {
                dataOut.close();
                dataIn.close();
                spreadwinSocket.close();
            } catch (IOException ignore) {
            }
        }
    }

    public class PlayThread extends Thread {
        private Socket playSocket;
        private DataInputStream dataIn;
        private DataOutputStream dataOut;
        private Thread waitThread;

        public PlayThread(Socket playSocket) {
            try {
                this.playSocket = playSocket;
                dataIn = new DataInputStream(playSocket.getInputStream());
                dataOut = new DataOutputStream(playSocket.getOutputStream());
                if (currentPosition != Position.BOTTOM) {
                    waitForYourTurn();
                }
            } catch (IOException ignore) {
            }
        }

        public void Pass() {
            try {
                dataOut.writeInt(-2);
                dataOut.flush();
            } catch (IOException ignore) {
            }
        }

        public Domino Draw() {
            try {
                dataOut.writeInt(-1);
                dataOut.flush();
                int left = dataIn.readInt();
                int right = dataIn.readInt();
                return new Domino(left, right, Position.BOTTOM);
            } catch (IOException ignore) {
            }
            return null;
        }

        public void play(int left, int right, boolean playedRight) {
            try {
                dataOut.writeInt(left);
                dataOut.writeInt(right);
                dataOut.writeInt(playedRight ? 1 : 0);
                dataOut.flush();
            } catch (IOException ignore) {
            }
        }

        public void waitForYourTurn() {
            if (waitThread == null) {
                waitThread = new Thread(() -> {
                    try {
                        do {
                            int left = dataIn.readInt(); // receiving an int and giving it to other players
                            if (left == -1) { // draw value
                                draw();
                            } else if (left == -2) { // pass value
                                pass();
                            } else {
                                int right = dataIn.readInt();
                                boolean playedRight = dataIn.readInt() == 1;
                                board.play(new Piece(new Domino(left, right, Position.CENTER), handler),
                                        playedRight);
                            }
                        } while (currentPosition != Position.BOTTOM);
                        waitThread = null;
                    } catch (IOException ignore) {
                    }
                });
                waitThread.start();
            }
        }

        public void closeConn() {
            try {
                dataOut.close();
                dataIn.close();
                playSocket.close();
            } catch (IOException ignore) {
            }
        }
    }

    private class BlockThread extends Thread {

        private Socket blockSocket;
        private DataInputStream dataIn;
        private DataOutputStream dataOut;

        public BlockThread(Socket blockSocket) {
            try {
                this.blockSocket = blockSocket;
                dataIn = new DataInputStream(blockSocket.getInputStream());
                dataOut = new DataOutputStream(blockSocket.getOutputStream());
            } catch (IOException ignore) {
            }
        }

        public void blocked() {
            try {
                dataOut.writeInt(playerID);
                dataOut.writeInt(getValue(playerDominoes));
                dataOut.flush();
                int id = dataIn.readInt();
                int value = dataIn.readInt();
                currentPosition = Position.getCurrentPositionByPlayerID(GAME_TYPE, yourPosition, id);
                if (GAME_TYPE == GameType.TwovTwo) {
                    if (currentPosition == Position.BOTTOM || currentPosition == Position.TOP) {
                        team1points += value;
                    } else {
                        team2points += value;
                    }
                } else if (GAME_TYPE == GameType.ThreePlayers) {
                    if (yourPosition == Position.BOTTOM) {
                        if (currentPosition == Position.BOTTOM) { // B.P. WON
                            team1points += value;
                        } else if (currentPosition == Position.TOP) { // T.P. WON
                            team2points += value;
                        } else { // L.P. WON
                            team3points += value;
                        }
                    } else if (yourPosition == Position.TOP) {
                        if (currentPosition == Position.BOTTOM) { // T.P. WON
                            team2points += value;
                        } else if (currentPosition == Position.RIGHT) { // L.P. WON
                            team3points += value;
                        } else { // B.P. WON
                            team1points += value;
                        }
                    } else { // LEFT PLAYER
                        if (currentPosition == Position.BOTTOM) { // L.P. WON
                            team3points += value;
                        } else if (currentPosition == Position.RIGHT) { // B.P. WON
                            team1points += value;
                        } else { // T.P. WON
                            team2points += value;
                        }
                    }
                } else {
                    if (currentPosition == Position.BOTTOM) {
                        team1points += value;
                    } else {
                        team2points += value;
                    }
                }
                if (playerID == 1)
                    spreadwin.startAnotherGame();
                if (isFinished()) {
                    startNewMatch();
                } else {
                    startNewGame();
                }
            } catch (IOException ignore) {
            }
        }

        public void closeConn() {
            try {
                dataOut.close();
                dataIn.close();
                blockSocket.close();
            } catch (IOException ignore) {
            }
        }

    }

    private int getValue(List<Domino> dominoes) {
        int cpt = 0;
        for (Domino domino : dominoes) {
            cpt += domino.getLeftValue() + domino.getRightValue();
        }
        return cpt;
    }

    public PlayThread getPlay() {
        return play;
    }

}
