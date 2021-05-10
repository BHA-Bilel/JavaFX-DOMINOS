package model;

public enum Position {
    TOP, BOTTOM, RIGHT, LEFT, CENTER;

    public static Position getPositionByPlayerID(GameType type, int playerID) {
        // get your position based on your playerID (not translated to bottom)
        Position your_position = null;
        switch (type) {
            case TwovTwo: {
                switch (playerID) {
                    case 1: {
                        your_position = BOTTOM;
                        break;
                    }
                    case 2: {
                        your_position = RIGHT;
                        break;
                    }
                    case 3: {
                        your_position = TOP;
                        break;
                    }
                    case 4: {
                        your_position = LEFT;
                        break;
                    }
                    default: {
                        your_position = null;
                        break;
                    }
                }
                break;
            }
            case OnevOne: {
                switch (playerID) {
                    case 1: {
                        your_position = BOTTOM;
                        break;
                    }
                    case 2: {
                        your_position = TOP;
                        break;
                    }
                    default: {
                        your_position = null;
                        break;
                    }

                }
                break;
            }
            case ThreePlayers: {
                switch (playerID) {
                    case 1: {
                        your_position = BOTTOM;
                        break;
                    }
                    case 2: {
                        your_position = TOP;
                        break;
                    }
                    case 3: {
                        your_position = LEFT;
                        break;
                    }
                    default: {
                        your_position = null;
                        break;
                    }
                }
                break;
            }
        }
        return your_position;
    }

    public Position next(GameType gameType, Position yourPosition) {
        switch (gameType) {
            case TwovTwo: {
                switch (this) {
                    case BOTTOM: {
                        return RIGHT;
                    }
                    case RIGHT: {
                        return TOP;
                    }
                    case TOP: {
                        return LEFT;
                    }
                    case LEFT: {
                        return BOTTOM;
                    }
                    default: {
                        return null;
                    }
                }
            }
            case OnevOne: {
                switch (this) {
                    case TOP: {
                        return BOTTOM;
                    }
                    case BOTTOM: {
                        return TOP;
                    }
                    default: {
                        return null;
                    }
                }
            }
            case ThreePlayers: {
                switch (yourPosition) {
                    case BOTTOM: {
                        switch (this) {
                            case TOP: {
                                return LEFT;
                            }
                            case LEFT: {
                                return BOTTOM;
                            }
                            case BOTTOM: {
                                return TOP;
                            }
                            default: {
                                return null;
                            }
                        }
                    }
                    case LEFT: {
                        switch (this) {
                            case RIGHT: {
                                return LEFT;
                            }
                            case LEFT: {
                                return BOTTOM;
                            }
                            case BOTTOM: {
                                return RIGHT;
                            }
                            default: {
                                return null;
                            }
                        }
                    }
                    case TOP: {
                        switch (this) {
                            case TOP: {
                                return BOTTOM;
                            }
                            case BOTTOM: {
                                return RIGHT;
                            }
                            case RIGHT: {
                                return TOP;
                            }
                            default: {
                                return null;
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    public static Position getCurrentPositionByPlayerID(GameType type, Position yourPosition, int position) {
        // relevant to player position (translated position)
        switch (yourPosition) {
            case BOTTOM: {
                switch (position) {
                    case 1: {
                        return BOTTOM;
                    }
                    case 2: {
                        if (type == GameType.TwovTwo) {
                            return RIGHT;
                        } else {
                            return TOP;
                        }
                    }
                    case 3: {
                        if (type == GameType.TwovTwo) {
                            return TOP;
                        } else // actually a three players match because one v one match have only 2 players
                        {
                            return LEFT;
                        }
                    }
                    case 4: {
                        return LEFT;
                    }
                    default: {
                    }
                }
            }
            case RIGHT: { // occurs only in twovtwo matches
                switch (position) {
                    case 1: {
                        return LEFT;
                    }
                    case 2: {
                        return BOTTOM;
                    }
                    case 3: {
                        return RIGHT;
                    }
                    case 4: {
                        return TOP;
                    }
                    default: {
                    }
                }
            }
            case TOP: {
                switch (position) {
                    case 1: {
                        return TOP;
                    }
                    case 2: {
                        if (type == GameType.TwovTwo) {
                            return LEFT;
                        } else {
                            return BOTTOM;
                        }
                    }
                    case 3: {
                        if (type == GameType.TwovTwo) {
                            return BOTTOM;
                        } else {
                            return RIGHT;
                        }
                    }
                    case 4: {
                        return RIGHT;
                    }
                    default: {
                    }
                }
            }
            case LEFT: {
                switch (position) {
                    case 1: {
                        return RIGHT;
                    }
                    case 2: {
                        if (type == GameType.TwovTwo) {
                            return TOP;
                        } else {
                            return LEFT;
                        }
                    }
                    case 3: {
                        if (type == GameType.TwovTwo) {
                            {
                                return LEFT;
                            }
                        } else {
                            return BOTTOM;
                        }
                    }
                    case 4: {
                        return BOTTOM;
                    }
                    default: {
                    }
                }
            }
            case CENTER: {
            }
        }
        return null;
    }
}
