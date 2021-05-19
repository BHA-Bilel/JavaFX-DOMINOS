package shared;

public enum RoomPosition {
    BOTTOM, RIGHT, TOP, LEFT;

    public RoomPosition teammate_with() {
        switch (this) {
            case BOTTOM:
                return TOP;
            case LEFT:
                return RIGHT;
            case TOP:
                return BOTTOM;
            case RIGHT:
                return LEFT;
            default:
                return null;
        }
    }

}
