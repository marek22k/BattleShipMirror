package battleship.opposingThings;

import java.io.PrintWriter;
import java.util.Random;

import battleship.ui.playgroundMatrix.PlaygroundMatrix;
import edu.umd.cs.findbugs.annotations.Nullable;

public class OpposingPlayingField {
    private final OpposingFieldStatus[][] field;
    private final int n;
    private final Random random;

    public OpposingPlayingField(int n) {
        this.field = new OpposingFieldStatus[n][n];
        this.n = n;
        this.random = new Random();
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                this.field[i][j] = OpposingFieldStatus.UNKNOWN;
            }
        }
    }

    public void debugPrint(PrintWriter pw) {
        for (final OpposingFieldStatus[] row : this.field) {
            for (final OpposingFieldStatus column : row) {
                char symbol;
                switch (column) {
                    case SHIP:
                        symbol = 'X';
                        break;

                    case SUNK:
                        symbol = 'S';
                        break;

                    case WATER:
                        symbol = 'W';
                        break;

                    default:
                        symbol = '_';
                        break;
                }
                pw.print(symbol + " ");
            }
            pw.println();
        }
    }

    public void debugPrint2(PrintWriter pw) {
        for (int y = 0; y < this.n; y++) {
            for (int x = 0; x < this.n; x++) {
                if (this.field[y][x] != OpposingFieldStatus.UNKNOWN) {
                    pw.println("x=" + x + " y=" + y + " : " + this.field[y][x]);
                }
            }
        }
    }

    /**
     * "Verfolgt" einen Schiffskörper nach unten rechts.
     *
     * @param f
     * @return
     */
    public OpposingShipEndWithDirection followShipDownRight(OpposingField f) {
        int x = f.getX();
        int y = f.getY();
        OpposingShipDirection direction = OpposingShipDirection.UNKNOWN;
        if (x + 1 < this.n && this.field[y][x + 1] == OpposingFieldStatus.SHIP) {
            do {
                x++;
            } while (x + 1 < this.n && this.field[y][x + 1] == OpposingFieldStatus.SHIP);
            direction = OpposingShipDirection.HORIZONTAL;
        } else if (y + 1 < this.n && this.field[y + 1][x] == OpposingFieldStatus.SHIP) {
            do {
                y++;
            } while (y + 1 < this.n && this.field[y + 1][x] == OpposingFieldStatus.SHIP);
            direction = OpposingShipDirection.VERTICAL;
        }
        /*
         * Für den Fall, dass es ein Schiff ist, welches mehr als ein Feld belegt, aber
         * wir bereits am Ende sind und daher nicht "gegangen" sind und keine
         * Schiffsrichtung bestimmten konnten.
         */
        if (direction == OpposingShipDirection.UNKNOWN) {
            if (x - 1 >= 0 && this.field[y][x - 1] == OpposingFieldStatus.SHIP) {
                direction = OpposingShipDirection.HORIZONTAL;
            } else if (y - 1 >= 0 && this.field[y - 1][x] == OpposingFieldStatus.SHIP) {
                direction = OpposingShipDirection.VERTICAL;
            }
        }

        return new OpposingShipEndWithDirection(direction, new OpposingField(x, y));
    }

    /**
     * "Verfolgt" einen Schiffskörper nach oben links.
     *
     * @param f
     * @return
     */
    public OpposingShipEndWithDirection followShipUpLeft(OpposingField f) {
        int x = f.getX();
        int y = f.getY();
        OpposingShipDirection direction = OpposingShipDirection.UNKNOWN;
        if (x - 1 >= 0 && this.field[y][x - 1] == OpposingFieldStatus.SHIP) {
            do {
                x--;
            } while (x - 1 >= 0 && this.field[y][x - 1] == OpposingFieldStatus.SHIP);
            direction = OpposingShipDirection.HORIZONTAL;
        } else if (y - 1 >= 0 && this.field[y - 1][x] == OpposingFieldStatus.SHIP) {
            do {
                y--;
            } while (y - 1 >= 0 && this.field[y - 1][x] == OpposingFieldStatus.SHIP);
            direction = OpposingShipDirection.VERTICAL;
        }
        /*
         * Für den Fall, dass es ein Schiff ist, welches mehr als ein Feld belegt, aber
         * wir bereits am Ende sind und daher nicht "gegangen" sind und keine
         * Schiffsrichtung bestimmten konnten.
         */
        if (direction == OpposingShipDirection.UNKNOWN) {
            if (x + 1 < this.n && this.field[y][x + 1] == OpposingFieldStatus.SHIP) {
                direction = OpposingShipDirection.HORIZONTAL;
            } else if (y + 1 < this.n && this.field[y + 1][x] == OpposingFieldStatus.SHIP) {
                direction = OpposingShipDirection.VERTICAL;
            }
        }

        return new OpposingShipEndWithDirection(direction, new OpposingField(x, y));
    }

    public OpposingField getComputerMove() {
        /*
         * Wenn es Schifffelder (also nicht versenkt) gibt, dann gehe zu einem dieser
         * Felder. Versuche die Richtung des Schiffes zu bestimmen. Wenn keine Richtung
         * bestimmbar ist (es gibt aktuell nur ein Schifffeld ohne umliegende
         * Schifffelder), dann schaue die benachbarten Felder nach dem ersten
         * unbekannten (bzw. das erste nicht-Wasser) Feld an, feuere auf dieses. Wenn
         * eine Richtung bestimmbar ist (es gibt benachbarte Schifffelder), dann Gehe
         * zum Anfang des Schiffes, schaue ob es in Richtung des Schiffes ein Wasserfeld
         * gibt: Wenn ja, gehe zum Ende des Schiffes. dieses. Schaue, ob es in Richtung
         * des Schiffes ein unbekanntes Feld gibt: Wenn ja, feurere auf dieses. Wenn
         * nein, löse einen Intigritätsfehler aus:
         * "Playing field integrity of the opponent's playing field violated. There is a ship where no field next to it is unknown."
         * Wenn nein, feuere auf das Feld. Anfang des Schiffes = oben links Ende des
         * Schiffes = unten rechts Wenn es keine Schifffelder gibt, gehe solange bis ein
         * unbekanntes Feld existiert und schieße. TODO: Vielleicht random Felder
         * ausprobieren
         */
        final OpposingField shipField = this.getNextField(OpposingFieldStatus.SHIP);
        if (shipField != null) {
            /* Es gibt ein Schiffsfeld */
            OpposingShipEndWithDirection fd = this.followShipDownRight(shipField);
            int x = fd.getEnd().getX();
            int y = fd.getEnd().getY();
            if (fd.getDirection() != OpposingShipDirection.UNKNOWN) {
                if (fd.getDirection() == OpposingShipDirection.HORIZONTAL) {
                    if (x + 1 < this.n && this.field[y][x + 1] == OpposingFieldStatus.UNKNOWN) {
                        return new OpposingField(x + 1, y);
                    }
                } else if (fd.getDirection() == OpposingShipDirection.VERTICAL) {
                    if (y + 1 < this.n && this.field[y + 1][x] == OpposingFieldStatus.UNKNOWN) {
                        return new OpposingField(x, y + 1);
                    }
                }
                fd = this.followShipUpLeft(shipField);
                x = fd.getEnd().getX();
                y = fd.getEnd().getY();
                if (fd.getDirection() == OpposingShipDirection.HORIZONTAL) {
                    if (x - 1 >= 0 && this.field[y][x - 1] == OpposingFieldStatus.UNKNOWN) {
                        return new OpposingField(x - 1, y);
                    }
                } else if (fd.getDirection() == OpposingShipDirection.VERTICAL) {
                    if (y - 1 >= 0 && this.field[y - 1][x] == OpposingFieldStatus.UNKNOWN) {
                        return new OpposingField(x, y - 1);
                    }
                }
                throw new RuntimeException(
                        "Playing field integrity of the opponent's playing field violated. There is a ship where no fields next to its ends are unknown."
                );
            }
            if (x - 1 >= 0 && this.field[y][x - 1] == OpposingFieldStatus.UNKNOWN) {
                return new OpposingField(x - 1, y);
            }
            if (y - 1 >= 0 && this.field[y - 1][x] == OpposingFieldStatus.UNKNOWN) {
                return new OpposingField(x, y - 1);
            }
            if (x + 1 < this.n && this.field[y][x + 1] == OpposingFieldStatus.UNKNOWN) {
                return new OpposingField(x + 1, y);
            }
            if (y + 1 < this.n && this.field[y + 1][x] == OpposingFieldStatus.UNKNOWN) {
                return new OpposingField(x, y + 1);
            }

            throw new RuntimeException(
                    "Playing field integrity of the opponent's playing field violated. There is a one-field ship that has not sunk."
            );
        }

        final int startx = this.random.nextInt(this.n);
        final int starty = this.random.nextInt(this.n);
        final OpposingField startField = new OpposingField(startx, starty);
        final OpposingField result = this.getNextField(startField, OpposingFieldStatus.UNKNOWN);

        if (result == null) {
            throw new RuntimeException(
                    "Playing field integrity of the opponent's playing field violated. All fields are known and therefore all ships are sunk. However, a computer move has been requested. This makes no sense, as we should have won."
            );
        }

        return result;
    }

    /**
     * Sucht nach dem nächsten Feld, welches einen bestimmten Status hat. Es wird am
     * dem Feld f gesucht. Wenn ab dem Feld f kein solcher Status gefunden wurden
     * ist, wird das Feld von Anfang an durchsucht. Wenn dann auch nicht dieser
     * Status gefunden wird, wird null zurückgegeben.
     *
     * @param f
     * @param fieldStatus
     * @return
     */
    @Nullable
    public OpposingField getNextField(OpposingField f, OpposingFieldStatus fieldStatus) {
        /*
         * Das Feld könnte wie folgt aussehen:
         * AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA
         * AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA
         * BBBBBBBBBBBBBBFCCCCCCCCCCCCCCCCCCCC
         * DDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDD
         * DDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDD
         * Dabei ist F das Feld f. Als erstes soll der Bereich von F bis zum Ende
         * durchsucht werden, also der Bereich F, der Bereich C und der Bereich D. Wenn
         * das gesuchte Feld dann noch nicht gefunden wurden ist, soll vom Anfang an
         * gesucht werden - also der Bereich C und D.
         */
        int x;
        int y;
        /* Hier wird der Bereich F und C durchsucht */
        for (y = f.getY(), x = f.getX(); x < this.n; x++) {
            if (this.field[y][x] == fieldStatus) {
                return new OpposingField(x, y);
            }
        }
        /* Hier wird der Bereich D durchsucht */
        for (y = f.getY() + 1; y < this.n; y++) {
            for (x = 0; x < this.n; x++) {
                if (this.field[y][x] == fieldStatus) {
                    return new OpposingField(x, y);
                }
            }
        }

        /* Hier wird der Bereich A durchsucht */
        for (y = 0; y < f.getY(); y++) {
            for (x = 0; x < this.n; x++) {
                if (this.field[y][x] == fieldStatus) {
                    return new OpposingField(x, y);
                }
            }
        }
        /* Hier wird der Bereich B durchsucht */
        for (y = f.getY(), x = 0; x < f.getX(); x++) {
            if (this.field[y][x] == fieldStatus) {
                return new OpposingField(x, y);
            }
        }
        return null;
    }

    /**
     * Sucht nach dem nächsten Feld, welches einen bestimmten Status hat. Wenn kein
     * Feld mit dieser Status gefunden wird, wird null zurückgegeben.
     *
     * @param fieldStatus
     * @return
     */
    @Nullable
    public OpposingField getNextField(OpposingFieldStatus fieldStatus) {
        return this.getNextField(new OpposingField(0, 0), fieldStatus);
    }

    public void hit(OpposingField f, OpposingFieldStatus fieldStatus) {
        /*
         * Wenn Wasser, dann Feld als Wasser markieren Wenn Schiff, dann Feld als Schiff
         * markieren Wenn Versunken, dann Feld und angrenzende Schifffelder als
         * versunken markieren
         */
        final int x = f.getX();
        final int y = f.getY();

        switch (fieldStatus) {
            case WATER, SHIP:
                this.field[y][x] = fieldStatus;
                break;

            case SUNK:
                /* Dieses als Feld versenkt markieren */
                this.field[y][x] = fieldStatus;
                /* Alle Schiffsfelder links als versenkt markieren */
                for (int i = x - 1; i >= 0 && this.field[y][i] == OpposingFieldStatus.SHIP; i--) {
                    this.field[y][i] = fieldStatus;
                }
                /* Alle Schiffsfelder oben als versenkt markieren */
                for (int i = y - 1; i >= 0 && this.field[i][x] == OpposingFieldStatus.SHIP; i--) {
                    this.field[i][x] = fieldStatus;
                }
                /* Alle Schiffsfelder rechts als versenkt markieren */
                for (int i = x + 1; i <= this.n && this.field[y][i] == OpposingFieldStatus.SHIP; i++) {
                    this.field[y][i] = fieldStatus;
                }
                /* Alle Schiffsfelder unten als versenkt markieren */
                for (int i = y + 1; i <= this.n && this.field[i][x] == OpposingFieldStatus.SHIP; i++) {
                    this.field[i][x] = fieldStatus;
                }
                break;

            default:
                throw new RuntimeException("Unsupported field status passed.");
        }
    }

    public boolean isUnknown(OpposingField f) {
        return this.field[f.getY()][f.getX()] == OpposingFieldStatus.UNKNOWN;
    }

    public void print(PlaygroundMatrix pm) {
        for (int y = 0; y < this.field.length; y++) {
            for (int x = 0; x < this.field[y].length; x++) {
                switch (this.field[y][x]) {
                    case SHIP:
                        pm.setShip(x, y);
                        break;

                    case SUNK:
                        pm.setSunk(x, y);
                        break;

                    case WATER:
                        pm.setWater(x, y);
                        break;

                    default:
                        pm.setUnknown(x, y);
                        break;
                }
            }
        }
    }
}
