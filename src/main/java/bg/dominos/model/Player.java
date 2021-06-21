package bg.dominos.model;

import java.util.ArrayList;
import java.util.List;

public class Player {

    private Position position;
    private List<Domino> dominoes = new ArrayList<>();

    public Player(Position position) {
        this.position = position;
    }

    public void drawDomino(Domino domino) {
        dominoes.add(domino);
    }

    public void removeDomino(Domino domino) {
        dominoes.remove(domino);
    }

    public List<Domino> getDominoes() {
        return dominoes;
    }

    public void loadOtherPlayersDominoes() {
        for (int i = 0; i < 7; i++) {
            Domino domino = new Domino(-1, -1, position);
            dominoes.add(domino);
        }
    }

    public Position getPosition() {
        return position;
    }

    public boolean no_playable_domino(int[] board_extermity) {
        for (Domino domino : dominoes) {
            if (domino.getLeftValue() == board_extermity[0] || domino.getRightValue() == board_extermity[0]
                    || domino.getLeftValue() == board_extermity[1] || domino.getRightValue() == board_extermity[1]) {
                return false;
            }
        }
        return true;
    }

    public void setPosition(Position position) {
        this.position = position;
    }

    public void initDrawPlayer(GameType gameType) {
        if (gameType == GameType.TwovTwo) return;
        loadOtherPlayersDominoes();
        if (gameType == GameType.OnevOne) loadOtherPlayersDominoes();
    }

    public void setDominoes(List<Domino> playerDominoes) {
        this.dominoes = playerDominoes;
    }
}
