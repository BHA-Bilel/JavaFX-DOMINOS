package bg.dominos.model;

public enum GameType {
    TwovTwo(4), OnevOne(2), ThreePlayers(3);

    public int players;

    GameType(int players) {
        this.players = players;
    }

}
