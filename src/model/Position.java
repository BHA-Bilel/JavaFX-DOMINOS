package model;

public enum Position {
    TOP, BOTTOM, RIGHT, LEFT, CENTER;

    public static Position getPositionByPlayerID(GameType type, int playerID) {
        // get your position based on your playerID (not translated to bottom)
        Position your_position = null;
        switch (type) {
            case TwovTwo -> your_position = switch (playerID) {
                case 1 -> BOTTOM;
                case 2 -> RIGHT;
                case 3 -> TOP;
                case 4 -> LEFT;
                default -> null;
            };
            case OnevOne -> your_position = switch (playerID) {
                case 1 -> BOTTOM;
                case 2 -> TOP;
                default -> null;
            };
            case ThreePlayers -> your_position = switch (playerID) {
                case 1 -> BOTTOM;
                case 2 -> TOP;
                case 3 -> LEFT;
                default -> null;
            };
        }
        return your_position;
    }

    public Position next(GameType gameType, Position yourPosition) {
        Position next_pos = null;
        switch (gameType) {
            case TwovTwo -> next_pos = switch (this) {
                case BOTTOM -> RIGHT;
                case RIGHT -> TOP;
                case TOP -> LEFT;
                case LEFT -> BOTTOM;
                default -> null;
            };
            case OnevOne -> next_pos = switch (this) {
                case TOP -> BOTTOM;
                case BOTTOM -> TOP;
                case RIGHT, LEFT, CENTER -> null;
            };
            case ThreePlayers -> {
                switch (yourPosition) {
                    case BOTTOM -> next_pos = switch (this) {
                        case TOP -> LEFT;
                        case LEFT -> BOTTOM;
                        case BOTTOM -> TOP;
                        case RIGHT, CENTER -> null;
                    };
                    case LEFT -> next_pos = switch (this) {
                        case RIGHT -> LEFT;
                        case LEFT -> BOTTOM;
                        case BOTTOM -> RIGHT;
                        case TOP, CENTER -> null;
                    };
                    case TOP -> next_pos = switch (this) {
                        case TOP -> BOTTOM;
                        case BOTTOM -> RIGHT;
                        case RIGHT -> TOP;
                        case LEFT, CENTER -> null;
                    };
                }
            }
        }
        return next_pos;
    }

    public static Position getCurrentPositionByPlayerID(GameType type, Position yourPosition, int position) {
        // relevant to player position (translated position)
        Position current_pos = null;
        switch (yourPosition) {
            case BOTTOM -> {
                switch (position) {
                    case 1 -> current_pos = BOTTOM;
                    case 2 -> {
                        if (type == GameType.TwovTwo)
                            current_pos = RIGHT;
                        else
                            current_pos = TOP;
                    }
                    case 3 -> {
                        if (type == GameType.TwovTwo)
                            current_pos = TOP;
                        else // actually a three players match because one v one match have only 2 players
                            current_pos = LEFT;
                    }
                    case 4 -> current_pos = LEFT;
                    default -> {
                    }
                }
            }
            case RIGHT -> { // occurs only in twovtwo matches
                switch (position) {
                    case 1 -> current_pos = LEFT;
                    case 2 -> current_pos = BOTTOM;
                    case 3 -> current_pos = RIGHT;
                    case 4 -> current_pos = TOP;
                    default -> {
                    }
                }
            }
            case TOP -> {
                switch (position) {
                    case 1 -> current_pos = TOP;
                    case 2 -> {
                        if (type == GameType.TwovTwo)
                            current_pos = LEFT;
                        else
                            current_pos = BOTTOM;
                    }
                    case 3 -> {
                        if (type == GameType.TwovTwo)
                            current_pos = BOTTOM;
                        else
                            current_pos = RIGHT;
                    }
                    case 4 -> current_pos = RIGHT;
                    default -> {
                    }
                }
            }
            case LEFT -> {
                switch (position) {
                    case 1 -> current_pos = RIGHT;
                    case 2 -> {
                        if (type == GameType.TwovTwo)
                            current_pos = TOP;
                        else
                            current_pos = LEFT;
                    }
                    case 3 -> {
                        if (type == GameType.TwovTwo)
                            current_pos = LEFT;
                        else
                            current_pos = BOTTOM;
                    }
                    case 4 -> current_pos = BOTTOM;
                    default -> {
                    }
                }
            }
            case CENTER -> {
            }
        }
        return current_pos;
    }
}
