package game2048;

import java.util.Formatter;
import java.util.Observable;


/** The state of a game of 2048.
 *  @author TODO: Alec Luk
 */
public class Model extends Observable {
    /**
     * Current contents of the board.
     */
    private Board board;
    /**
     * Current score.
     */
    private int score;
    /**
     * Maximum score so far.  Updated when game ends.
     */
    private int maxScore;
    /**
     * True iff game is ended.
     */
    private boolean gameOver;

    /* Coordinate System: column C, row R of the board (where row 0,
     * column 0 is the lower-left corner of the board) will correspond
     * to board.tile(c, r).  Be careful! It works like (x, y) coordinates.
     */

    /**
     * Largest piece value.
     */
    public static final int MAX_PIECE = 2048;

    /**
     * A new 2048 game on a board of size SIZE with no pieces
     * and score 0.
     */
    public Model(int size) {
        board = new Board(size);
        score = maxScore = 0;
        gameOver = false;
    }

    /**
     * A new 2048 game where RAWVALUES contain the values of the tiles
     * (0 if null). VALUES is indexed by (row, col) with (0, 0) corresponding
     * to the bottom-left corner. Used for testing purposes.
     */
    public Model(int[][] rawValues, int score, int maxScore, boolean gameOver) {
        int size = rawValues.length;
        board = new Board(rawValues, score);
        this.score = score;
        this.maxScore = maxScore;
        this.gameOver = gameOver;
    }

    /**
     * Return the current Tile at (COL, ROW), where 0 <= ROW < size(),
     * 0 <= COL < size(). Returns null if there is no tile there.
     * Used for testing. Should be deprecated and removed.
     */
    public Tile tile(int col, int row) {
        return board.tile(col, row);
    }

    /**
     * Return the number of squares on one side of the board.
     * Used for testing. Should be deprecated and removed.
     */
    public int size() {
        return board.size();
    }

    /**
     * Return true iff the game is over (there are no moves, or
     * there is a tile with value 2048 on the board).
     */
    public boolean gameOver() {
        checkGameOver();
        if (gameOver) {
            maxScore = Math.max(score, maxScore);
        }
        return gameOver;
    }

    /**
     * Return the current score.
     */
    public int score() {
        return score;
    }

    /**
     * Return the current maximum game score (updated at end of game).
     */
    public int maxScore() {
        return maxScore;
    }

    /**
     * Clear the board to empty and reset the score.
     */
    public void clear() {
        score = 0;
        gameOver = false;
        board.clear();
        setChanged();
    }

    /**
     * Add TILE to the board. There must be no Tile currently at the
     * same position.
     */
    public void addTile(Tile tile) {
        board.addTile(tile);
        checkGameOver();
        setChanged();
    }

    /**
     * Tilt the board toward SIDE. Return true iff this changes the board.
     * <p>
     * 1. If two Tile objects are adjacent in the direction of motion and have
     * the same value, they are merged into one Tile of twice the original
     * value and that new value is added to the score instance variable
     * 2. A tile that is the result of a merge will not merge again on that
     * tilt. So each move, every tile will only ever be part of at most one
     * merge (perhaps zero).
     * 3. When three adjacent tiles in the direction of motion have the same
     * value, then the leading two tiles in the direction of motion merge,
     * and the trailing tile does not.
     */
    public boolean tilt(Side side) {
        boolean changed;
        changed = false;
        board.setViewingPerspective(side);
        // TODO: Modify this.board (and perhaps this.score) to account
        // for the tilt to the Side SIDE. If the board changed, set the
        // changed local variable to true
        /** iterate through every single column of the board*/
        for (int c = 0; c < board.size(); c += 1) {
            if (AddFirstNumberAppearBelow(c) == true) {
                changed = true;
            }
        }
        for (int col = 0; col < board.size(); col += 1) {
            if (MoveToNull(col) == true) {
                changed = true;
            }
        }
        board.setViewingPerspective(Side.NORTH);
        checkGameOver();
        if (changed) {
            setChanged();
        }
        return changed;
    }

    /**
     * helper method that takes in a column number
     * and returns True if that tile is null or has value
     */
     public boolean AddFirstNumberAppearBelow(int c) {
         int move = 0;
         for (int row = 3; row >= 1; row -= 1) {
             if (board.tile(c, row) == null) {
                 continue;
             } else {
                 int row_below_index = row - 1;
                 while (row_below_index >= 0) {
                     if (board.tile(c, row_below_index) != null) {
                         if (MoveTile(c, row, row_below_index) == true) {
                             move ++;
                         }
                         break;
                     } else if (board.tile(c, row_below_index) == null) {
                         row_below_index--;
                     }
                 }
             }
         } if (move != 0) {
             return true;
         } else {
             return false;
         }
     }

     /** helper method that moves the tile if called */
     public boolean MoveTile(int col, int row_to, int row_from) {
         if (board.tile(col, row_from) != null & board.tile(col, row_to) != null) {
             if (board.tile(col, row_from).value() == board.tile(col, row_to).value()) {
                 board.move(col, row_to, board.tile(col, row_from));
                 score += board.tile(col, row_to).value();
                 return true;
             } else if (board.tile(col, row_to).value() != board.tile(col, row_from).value()) {
                 return false;
             }
         } return false;
     }

     /** helper method that moves the bottom tile that are not null to the null tiles above */
    public boolean MoveToNull(int col) {
        int move = 0;
        for (int row = 3; row >= 0; row -= 1) {
            Tile t = board.tile(col, row);
            int row_below = row - 1;
            if (t == null) {
                while (row_below >= 0) {
                    if (board.tile(col, row_below) == null) {
                        row_below --;
                    } else if (board.tile(col, row_below) != null) {
                        board.move(col, row, board.tile(col, row_below));
                        move ++;
                        break;
                    }
                }
            } else if (t != null) {
                continue;
            }
        }
        if (move != 0) {
            return true;
        } else {
            return false;
        }
     }

    /** Checks if the game is over and sets the gameOver variable
     *  appropriately.
     */
    private void checkGameOver() {
        gameOver = checkGameOver(board);
    }

    /** Determine whether game is over. */
    private static boolean checkGameOver(Board b) {
        return maxTileExists(b) || !atLeastOneMoveExists(b);
    }

    /** Returns true if at least one space on the Board is empty.
     *  Empty spaces are stored as null.
     * */
    public static boolean emptySpaceExists(Board b) {
        for (int col_num = 0; col_num < b.size(); col_num += 1) {
            for (int row_num = 0; row_num < b.size(); row_num += 1) {
                if (b.tile(col_num, row_num) == null) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Returns true if any tile is equal to the maximum valid value.
     * Maximum valid value is given by MAX_PIECE. Note that
     * given a Tile object t, we get its value with t.value().
     */
    public static boolean maxTileExists(Board b) {
        for (int col_number = 0; col_number < b.size(); col_number += 1) {
            for (int row_number = 0; row_number < b.size(); row_number += 1) {
                if (b.tile(col_number, row_number) != null) {
                    if (b.tile(col_number, row_number).value() == MAX_PIECE) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Returns true if there are any valid moves on the board.
     * There are two ways that there can be valid moves:
     * 1. There is at least one empty space on the board.
     * 2. There are two adjacent tiles with the same value.
     */
    public static boolean atLeastOneMoveExists(Board b) {
        if (emptySpaceExists(b) == true) {
            return true;
        }
        for (int c = 0; c < b.size(); c ++) {
            for (int r = 0; r < b.size(); r ++) {
                if (c != b.size() - 1) {
                    if (b.tile(c, r).value() == b.tile(c + 1, r).value()) {
                        return true;
                    }
                    if (b.tile(c + 1, r) == null) {
                        return true;
                    }
                }
                if (r != b.size() - 1) {
                    if (b.tile(c, r).value() == b.tile(c, r + 1).value()) {
                        return true;
                    }
                    if (b.tile( c, r + 1) == null) {
                        return true;
                    }
                }
            }
        } return false;
    }

    @Override
     /** Returns the model as a string, used for debugging. */
    public String toString() {
        Formatter out = new Formatter();
        out.format("%n[%n");
        for (int row = size() - 1; row >= 0; row -= 1) {
            for (int col = 0; col < size(); col += 1) {
                if (tile(col, row) == null) {
                    out.format("|    ");
                } else {
                    out.format("|%4d", tile(col, row).value());
                }
            }
            out.format("|%n");
        }
        String over = gameOver() ? "over" : "not over";
        out.format("] %d (max: %d) (game is %s) %n", score(), maxScore(), over);
        return out.toString();
    }

    @Override
    /** Returns whether two models are equal. */
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        } else if (getClass() != o.getClass()) {
            return false;
        } else {
            return toString().equals(o.toString());
        }
    }

    @Override
    /** Returns hash code of Model’s string. */
    public int hashCode() {
        return toString().hashCode();
    }
}
