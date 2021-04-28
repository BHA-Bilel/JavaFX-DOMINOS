package model;

import java.util.ArrayList;
import java.util.List;

public class Player {

    private Position position;
    private List<Domino> dominos = new ArrayList<>();

    public Player(Position position) {
        this.position = position;
    }

    public void drawDomino(Domino domino) {
        dominos.add(domino);
    }

    public void removeDomino(Domino domino) {
        dominos.remove(domino);
    }

    // GETTER SETTERS

    public List<Domino> getDominos() {
        return dominos;
    }

    public void loadOtherPlayersDominoes() {
        for (int i = 0; i < 7; i++) {
            Domino domino = new Domino(-1, -1, position);
            dominos.add(domino);
        }
    }

    public Position getPosition() {
        return position;
    }

    public boolean canPlay(int leftValue, int rightValue) {
        for (Domino domino : dominos) {
            if (domino.getLeftValue() == leftValue || domino.getRightValue() == leftValue
                    || domino.getLeftValue() == rightValue || domino.getRightValue() == rightValue) {
                return true;
            }
        }
        return false;
    }

    public void setPosition(Position position) {
        this.position = position;
    }

    public void initDrawPlayer(GameType gameType) {
        if (gameType != GameType.TwovTwo) {
            if (gameType == GameType.ThreePlayers) {
                loadOtherPlayersDominoes();
            } else {
                for (int i = 0; i < 14; i++) {
                    Domino domino = new Domino(-1, -1, position);
                    dominos.add(domino);
                }
            }
        }
    }

    public void setDominoes(List<Domino> playerDominoes) {
        this.dominos = playerDominoes;
    }
}
