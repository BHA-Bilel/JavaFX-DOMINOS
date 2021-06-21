package bg.dominos.game;

import bg.dominos.gfx.*;
import bg.dominos.lang.Language;
import bg.dominos.model.Domino;
import bg.dominos.model.GameType;
import bg.dominos.model.Player;
import bg.dominos.model.Position;
import bg.dominos.popup.MyAlert;
import javafx.application.Platform;
import javafx.beans.property.StringProperty;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.layout.BorderPane;
import shared.domino.DominoComm;
import shared.domino.DominoMsg;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GameApp extends BorderPane {

    private int team1points = 0, team2points = 0, team3points = 0;
    private int playerID;
    private final GameClient gameClient;
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
    private MyAlert results_alert;

    public GameApp(Socket gameSocket, Map<Integer, String> players_names, GameType gameType) {
        this.GAME_TYPE = gameType;
        handler = new Handler(this);
        gameClient = new GameClient(gameSocket);
        gameClient.handshake();
        createGUI(players_names);
        startNewMatch();
        gameClient.start();
    }

    protected void createGUI(Map<Integer, String> players_names) {
        Assets.init_assets();
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
        initPlayersNames(players_names);
    }

    private void startNewMatch() {
        team1points = 0;
        team2points = 0;
        team3points = 0;
    }

    private void startNewGame(List<Domino> dominoes) {
        clearDominoes();
        board.reset_board();
        selectedPiece = null;
        playerDominoes = dominoes;
        initPlayers();
        update_turn_gui();
    }

    private void initPlayers() {
        bottomPlayer = new Player(Position.BOTTOM);
        bottomPlayer.setDominoes(playerDominoes);
        Player leftPlayer = new Player(Position.LEFT);
        Player topPlayer = new Player(Position.TOP);
        Player rightPlayer = new Player(Position.RIGHT);
        switch (GAME_TYPE) {
            case TwovTwo: {
                leftPlayer.loadOtherPlayersDominoes();
                rightPlayer.loadOtherPlayersDominoes();
                topPlayer.loadOtherPlayersDominoes();
                break;
            }
            case OnevOne: {
                switch (yourPosition) {
                    case BOTTOM: {
                        topPlayer.loadOtherPlayersDominoes();
                        rightPlayer.initDrawPlayer(GAME_TYPE);
                        rightPane.setDraw();
                        leftPane.clear();
                        break;
                    }
                    case TOP: {
                        topPlayer.loadOtherPlayersDominoes();
                        leftPlayer.initDrawPlayer(GAME_TYPE);
                        leftPane.setDraw();
                        rightPane.clear();
                        break;
                    }
                }
                break;
            }
            case ThreePlayers: {
                switch (yourPosition) {
                    case TOP: {
                        topPlayer.loadOtherPlayersDominoes();
                        leftPlayer.initDrawPlayer(GAME_TYPE);
                        leftPane.setDraw();
                        rightPlayer.loadOtherPlayersDominoes();
                        break;
                    }
                    case BOTTOM: {
                        topPlayer.loadOtherPlayersDominoes();
                        rightPlayer.initDrawPlayer(GAME_TYPE);
                        rightPane.setDraw();
                        leftPlayer.loadOtherPlayersDominoes();
                        break;
                    }
                    case LEFT: {
                        leftPlayer.loadOtherPlayersDominoes();
                        rightPlayer.loadOtherPlayersDominoes();
                        topPlayer.initDrawPlayer(GAME_TYPE);
                        topPane.setDraw();
                        break;
                    }
                }
                break;
            }
        }
        bottomPane.setPlayer(bottomPlayer);
        leftPane.setPlayer(leftPlayer);
        topPane.setPlayer(topPlayer);
        rightPane.setPlayer(rightPlayer);
    }

    private void update_turn_gui() {
        bottomPane.updateTurn();
        leftPane.updateTurn();
        topPane.updateTurn();
        rightPane.updateTurn();
    }

    private boolean canDraw() {
        if (yourPosition == Position.BOTTOM) {
            return !rightPane.getPlayer().getDominoes().isEmpty();
        } else if (yourPosition == Position.TOP) {
            return !leftPane.getPlayer().getDominoes().isEmpty();
        } else { // LEFT
            return !topPane.getPlayer().getDominoes().isEmpty();
        }
    }

    private void pass() {
        if (currentPosition == Position.BOTTOM) gameClient.send_msg(new DominoMsg(DominoComm.PASS));
        switchTurn();
    }

    public void switchTurn() {
        currentPosition = currentPosition.next(GAME_TYPE, yourPosition);
        update_turn_gui();
        if (currentPosition == Position.BOTTOM && bottomPlayer.no_playable_domino(board.get_extermity())) {
            if (GAME_TYPE == GameType.TwovTwo) pass();
            else {
                if (canDraw()) gameClient.send_msg(new DominoMsg(DominoComm.DRAW));
                else pass();
            }
        }
    }

    public void highlightPossibleMoves(boolean highlight) {
        if (board.isEmpty()) return;
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

    public void removePiece() {
        if (currentPosition == Position.BOTTOM) {
            bottomPane.removePiece(selectedPiece);
            highlightPossibleMoves(false);
            getSelectedPiece().setHighlighted(false);
            setSelectedPiece(null);
        } else if (currentPosition == Position.LEFT) {
            leftPane.removePiece();
        } else if (currentPosition == Position.TOP) {
            topPane.removePiece();
        } else {
            rightPane.removePiece();
        }
    }

    public boolean checkState() {
        boolean won = false;
        switch (currentPosition) {
            case TOP:
                won = topPane.isEmpty();
                break;
            case BOTTOM:
                won = bottomPane.isEmpty();
                break;
            case RIGHT:
                won = rightPane.isEmpty();
                break;
            case LEFT:
                won = leftPane.isEmpty();
                break;
        }
        if (won) return false;
        else {
            boolean blocked = board.isBlocked();
            if (blocked) {
                if (playerID == 1) gameClient.send_msg(new DominoMsg(DominoComm.BLOCKED));
                return false;
            }
        }
        return true;
    }

    private void clearDominoes() {
        bottomPane.clearPane();
        leftPane.clearPane();
        topPane.clearPane();
        rightPane.clearPane();
    }

    private void initPlayersNames(Map<Integer, String> players_names) {
        switch (GAME_TYPE) {
            case TwovTwo: {
                yourName = players_names.get(playerID);
                break;
            }
            case OnevOne: {
                if (playerID == 1) {
                    yourName = players_names.get(1);
                    op1Name = players_names.get(2);
                } else {
                    yourName = players_names.get(2);
                    op1Name = players_names.get(1);
                }
                break;
            }
            case ThreePlayers: {
                switch (playerID) {
                    case 1: {
                        yourName = players_names.get(1);
                        op1Name = players_names.get(2);
                        op2Name = players_names.get(3);
                        break;
                    }
                    case 2: {
                        yourName = players_names.get(2);
                        op1Name = players_names.get(3);
                        op2Name = players_names.get(1);
                        break;
                    }
                    case 3: {
                        yourName = players_names.get(3);
                        op1Name = players_names.get(1);
                        op2Name = players_names.get(2);
                        break;
                    }
                }
                break;
            }
        }
    }

    public void iplayed(boolean right) {
        int left_value = selectedPiece.getDomino().getLeftValue(), right_value = selectedPiece.getDomino().getRightValue();
        board.play(selectedPiece, right);
        gameClient.send_msg(new DominoMsg(right ? DominoComm.PLAYED_RIGHT : DominoComm.PLAYED_LEFT,
                new Object[]{left_value, right_value}));
        if (checkState()) switchTurn();
    }

    private class GameClient extends Thread {
        private Socket socket;
        private ObjectInputStream objIn;
        private ObjectOutputStream objOut;

        public GameClient(Socket socket) {
            try {
                this.socket = socket;
                this.socket.setSoTimeout(0);
                objOut = new ObjectOutputStream(socket.getOutputStream());
                objIn = new ObjectInputStream(socket.getInputStream());
            } catch (IOException ignore) {
            }
        }

        public void handshake() {
            try {
                playerID = objIn.readInt();
                int whoStart = objIn.readInt();
                yourPosition = Position.getPositionByPlayerID(GAME_TYPE, playerID);
                currentPosition = Position.getCurrentPositionByPlayerID(GAME_TYPE, yourPosition, whoStart);
            } catch (IOException ignore) {
            }
        }

        @Override
        public void run() {
            try {
                while (true) {
                    DominoMsg msg = (DominoMsg) objIn.readObject();
                    DominoComm msg_comm = DominoComm.values()[msg.comm];
                    switch (msg_comm) {
                        case PASS: {
                            pass();
                            break;
                        }
                        case PLAYED_LEFT:
                        case PLAYED_RIGHT: {
                            board.play(new Piece(
                                    new Domino((int) msg.adt_data[0], (int) msg.adt_data[1], Position.CENTER), handler), msg_comm == DominoComm.PLAYED_RIGHT);
                            if (checkState()) switchTurn();
                            break;
                        }
                        case DRAW: {
                            switch (yourPosition) {
                                case TOP:
                                    leftPane.removePiece();
                                    break;
                                case BOTTOM:
                                    rightPane.removePiece();
                                    break;
                                case LEFT:
                                    topPane.removePiece();
                                    break;
                            }
                            if (currentPosition == Position.BOTTOM) {
                                bottomPane.addPiece(new Piece(
                                        new Domino((int) msg.adt_data[0], (int) msg.adt_data[1],
                                                Position.BOTTOM), handler));
                                if (bottomPlayer.no_playable_domino(board.get_extermity())) {
                                    if (canDraw()) gameClient.send_msg(new DominoMsg(DominoComm.DRAW));
                                    else pass();
                                }
                            } else {
                                switch (currentPosition) {
                                    case TOP:
                                        topPane.drawPiece();
                                        break;
                                    case RIGHT:
                                        rightPane.drawPiece();
                                        break;
                                    case LEFT:
                                        leftPane.drawPiece();
                                        break;
                                }
                            }
                            break;
                        }
                        case NEW_GAME: {
                            startNewGame(Domino.to_list((Integer[]) msg.adt_data));
                            break;
                        }
                        case GAME_END: {
                            boolean won = (GAME_TYPE == GameType.TwovTwo && currentPosition == Position.TOP) ||
                                    currentPosition == Position.BOTTOM;
                            int value = (int) msg.adt_data[0];
                            switch (GAME_TYPE) {
                                case TwovTwo:
                                case OnevOne: {
                                    if (won) team1points += value;
                                    else team2points += value;
                                    break;
                                }
                                case ThreePlayers: {
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
                                    break;
                                }
                            }
                            if (isFinished()) startNewMatch();
                            break;
                        }
                        case BLOCK_RESULT: {
                            int id = (int) msg.adt_data[0];
                            int value = (int) msg.adt_data[1];
                            currentPosition = Position.getCurrentPositionByPlayerID(GAME_TYPE, yourPosition, id);
                            switch (GAME_TYPE) {
                                case TwovTwo:
                                case OnevOne: {
                                    if ((GAME_TYPE == GameType.TwovTwo && currentPosition == Position.TOP)
                                            || currentPosition == Position.BOTTOM) {
                                        team1points += value;
                                    } else {
                                        team2points += value;
                                    }
                                    break;
                                }
                                case ThreePlayers: {
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
                                    break;
                                }
                            }
                            if (isFinished()) startNewMatch();
                            break;
                        }
                    }
                }
            } catch (IOException | ClassNotFoundException ignore) {
            }
        }

        private void send_msg(DominoMsg msg) {
            try {
                objOut.writeObject(msg);
                objOut.flush();
            } catch (IOException ignore) {
            }
        }

        public void closeConn() {
            try {
                objOut.close();
                objIn.close();
                socket.close();
            } catch (IOException ignore) {
            }
        }

    }

    private boolean isFinished() {
        boolean finished;
        if (GAME_TYPE != GameType.ThreePlayers) finished = team1points >= 100 || team2points >= 100;
        else finished = team1points >= 100 || team2points >= 100 || team3points >= 100;
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
            printResults(false);
        }
        return finished;
    }

    public void printResults(boolean shortcut) {
        if (results_alert != null && results_alert.isShowing())
            if (shortcut) return;
            else results_alert.close();
        int team1points = this.team1points;
        int team2points = this.team2points;
        int team3points = this.team3points;
        StringProperty header;
        if (shortcut) header = Language.CURRENT_SC;
        else {
            if ((GAME_TYPE == GameType.TwovTwo && currentPosition == Position.TOP)
                    || currentPosition == Position.BOTTOM) header = Language.YOU_WON;
            else header = Language.YOU_LOST;
        }
        String score = "";
        switch (GAME_TYPE) {
            case TwovTwo: {
                score = Language.YOUR_PTS.getValue() + team1points + "\n";
                score += Language.OP_PTS.getValue() + team2points + "\n\n";
                score += Language.TOTAL_PT_WON.getValue() + "\n";
                score += Language.YOUR_TEAM.getValue() + team1_parties_won + "\n";
                score += Language.OP_TEAM.getValue() + team2_parties_won;
                break;
            }
            case OnevOne: {
                score = yourName + " : " + team1points + "\n";
                score += op1Name + " : " + team2points + "\n\n";
                score += Language.TOTAL_PT_WON.getValue() + "\n";
                score += yourName + " : " + team1_parties_won + "\n";
                score += op1Name + " : " + team2_parties_won;
                break;
            }
            case ThreePlayers: {
                if (yourPosition == Position.BOTTOM) {
                    score = yourName + " : " + team1points + "\n";
                    score += op1Name + " : " + team2points + "\n";
                    score += op2Name + " : " + team3points + "\n\n";
                    score += Language.TOTAL_PT_WON.getValue() + "\n";
                    score += yourName + " : " + team1_parties_won + "\n";
                    score += op1Name + " : " + team2_parties_won + "\n";
                    score += op2Name + " : " + team3_parties_won;
                } else if (yourPosition == Position.TOP) {
                    score = yourName + " : " + team2points + "\n";
                    score += op1Name + " : " + team3points + "\n";
                    score += op2Name + " : " + team1points + "\n\n";
                    score += Language.TOTAL_PT_WON.getValue() + "\n";
                    score += yourName + " : " + team2_parties_won + "\n";
                    score += op1Name + " : " + team3_parties_won + "\n";
                    score += op2Name + " : " + team1_parties_won;
                } else { // LEFT
                    score = yourName + " : " + team3points + "\n";
                    score += op1Name + " : " + team1points + "\n";
                    score += op2Name + " : " + team2points + "\n\n";
                    score += Language.TOTAL_PT_WON.getValue() + "\n";
                    score += yourName + " : " + team3_parties_won + "\n";
                    score += op1Name + " : " + team1_parties_won + "\n";
                    score += op2Name + " : " + team2_parties_won;
                }
                break;
            }
        }
        String finalScore = score;
        Platform.runLater(() -> {
            if (results_alert == null)
                results_alert = new MyAlert(Alert.AlertType.INFORMATION, Language.GR_H, header, finalScore);
            else results_alert.update(header, finalScore);
            results_alert.show();
        });
    }

    public void closeGameApp() {
        Assets.drop_assets();
        gameClient.closeConn();
        Platform.runLater(() -> getChildren().clear());
    }

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
}
