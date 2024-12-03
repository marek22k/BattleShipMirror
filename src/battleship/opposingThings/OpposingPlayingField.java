package battleship.opposingThings;

import java.util.Random;

import battleship.ui.playgroundMatrix.PlaygroundMatrix;
import edu.umd.cs.findbugs.annotations.Nullable;

/**
 * Repräsentiert ein Spielfeld des Gegners
 */
public class OpposingPlayingField {
    /**
     * Alle Felder vom Spielfeld des Gegners mit ihrem Inhalt
     */
    private final OpposingFieldStatus[][] field;
    /**
     * Die Größe des Spielfelds. Es wird angenommen, dass es immer quadratisch ist.
     */
    private final int n;
    /**
     * Zufallsgenerator. Dieser wird verwendet, wenn der Computer einen zug
     * berechnen soll und dieser entscheidet ein zufälliges Feld zu treffen.
     */
    private final Random random;

    /**
     * Erstellt ein gegnerisches Spielfeld.
     *
     * @param n Größe des Spielfeldes. Es ist quadratisch.
     */
    public OpposingPlayingField(final int n) {
        this.field = new OpposingFieldStatus[n][n];
        this.n = n;
        this.random = new Random();
        /*
         * Setze alle Felder auf unbekannt. Zum Start des Spieles, wenn der Gegner noch
         * nicht angegriffen wird, ist unbekannt, welchen Inhalt die Felder des Gegners
         * haben.
         */
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                this.field[i][j] = OpposingFieldStatus.UNKNOWN;
            }
        }
    }

    /**
     * Gibt einen String zurück, welchen das Spielfeld (bzw. die Felder) in
     * ASCII-Art zeichnet.
     * `X` steht dabei für einen Treffer, `S` für ein versunkenes Schiff, `W` für
     * Wasser und `_` für unbekannt.
     *
     * @return Das Spielfeld als String
     */
    public String debugPrint() {
        /*
         * Die initiale Kapazität kann angegeben werden. Jedes Feld wird mit einem
         * Zeichen gedruckt. Dies sind also n*n-Zeichen. Zusätzlich wird nach jedem
         * Zeichen eine Leerzeichen hinzugefügt. Dies sind auch noch einmal n*n Zeichen.
         * Hinzukommt eine Leerzeile nach jeder Zeile. Dies sind n Zeichen. Insgesamt
         * sind es also n*n+n*n+n-Zeichen.
         */
        final StringBuilder sb = new StringBuilder(this.n * this.n + this.n * this.n + this.n);
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
                sb.append(symbol).append(' ');
            }
            sb.append('\n');
        }
        return sb.toString();
    }

    /**
     * Gibt einen String zurück, welches das Spielfeld beschreibt. Dabei werden alle
     * einzelne Felder, welche nicht unbekannt sind, mit Koordinaten und Inhalt
     * ausgegeben.
     *
     * @return Das Spielfeld als String
     */
    public String debugPrint2() {
        final StringBuilder sb = new StringBuilder();
        for (int y = 0; y < this.n; y++) {
            for (int x = 0; x < this.n; x++) {
                if (this.field[y][x] != OpposingFieldStatus.UNKNOWN) {
                    sb.append("x=").append(x).append(" y=").append(y).append(" : ").append(this.field[y][x]);
                }
            }
        }
        return sb.toString();
    }

    /**
     * "Verfolgt" einen Schiffskörper nach unten rechts.
     *
     * @param f
     * @return
     */
    public OpposingShipEndWithDirection followShipDownRight(final OpposingField f) {
        int x = f.getX();
        int y = f.getY();
        OpposingShipDirection direction = OpposingShipDirection.UNKNOWN;
        if (x + 1 < this.n && this.field[y][x + 1] == OpposingFieldStatus.SHIP) {
            /* Verfolge das Schiff nach rechts */
            do {
                x++;
            } while (x + 1 < this.n && this.field[y][x + 1] == OpposingFieldStatus.SHIP);
            direction = OpposingShipDirection.HORIZONTAL;
        } else if (y + 1 < this.n && this.field[y + 1][x] == OpposingFieldStatus.SHIP) {
            /* Verfolge das Schiff nach unten */
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
    public OpposingShipEndWithDirection followShipUpLeft(final OpposingField f) {
        int x = f.getX();
        int y = f.getY();
        OpposingShipDirection direction = OpposingShipDirection.UNKNOWN;
        if (x - 1 >= 0 && this.field[y][x - 1] == OpposingFieldStatus.SHIP) {
            /* Verfolge das Schiff nach links */
            do {
                x--;
            } while (x - 1 >= 0 && this.field[y][x - 1] == OpposingFieldStatus.SHIP);
            direction = OpposingShipDirection.HORIZONTAL;
        } else if (y - 1 >= 0 && this.field[y - 1][x] == OpposingFieldStatus.SHIP) {
            /* Verfolge das Schiff nach oben */
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
                /*
                 * Die Richtung des Schiffes ist bekannt und zusätzlich ist das Schiff mehr als
                 * ein Feld groß (sonst könnte keine Richtung ermittelt werden
                 */
                /*
                 * Das Schiff wurde nach unten rechts verfolgt. Es könnte sein, dass es dort
                 * noch unaufgedeckte Schiffsteile gibt (die man treffen könnte). Schaue, ob da
                 * so ist und wenn dort ein unbekanntes Feld ist, feuere.
                 */
                if (fd.getDirection() == OpposingShipDirection.HORIZONTAL) {
                    if (x + 1 < this.n && this.field[y][x + 1] == OpposingFieldStatus.UNKNOWN) {
                        return new OpposingField(x + 1, y);
                    }
                } else if (fd.getDirection() == OpposingShipDirection.VERTICAL) {
                    if (y + 1 < this.n && this.field[y + 1][x] == OpposingFieldStatus.UNKNOWN) {
                        return new OpposingField(x, y + 1);
                    }
                }
                /*
                 * Unten rechts scheint das Schiff keine unbekannten Teile mehr zu haben. Schaue
                 * also oben links nach.
                 */
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
                /*
                 * Das Schiff ist nicht versunken, hat aber an keinem Ende ein unbekanntes Feld.
                 * Das ist seltsam und sollte so nicht sein.
                 */
                throw new RuntimeException(
                        "Playing field integrity of the opponent's playing field violated. There is a ship where no fields next to its ends are unknown."
                );
            }
            /*
             * Wenn die Richtung des Schiffes unbekannt ist, schieße auf die benachbarten
             * unbekannten Felder
             */
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

            /*
             * Es gibt ein Schiff, was ein Feld groß ist und nicht nicht versunken, aber
             * alle benachbarten Felder sind bereits getroffen. Vielleicht ein U-Boot? So
             * oder so seltsam und sollte nicht passieren.
             */
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
    public OpposingField getNextField(final OpposingField f, final OpposingFieldStatus fieldStatus) {
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
    public OpposingField getNextField(final OpposingFieldStatus fieldStatus) {
        return this.getNextField(new OpposingField(0, 0), fieldStatus);
    }

    /**
     * Speichert einen Treffer im gegnerischen Spielfeld ab.
     *
     * @param f           Das Feld, welches getroffen wurden ist.
     * @param fieldStatus Der Status des Feldes, welches getroffen wurden ist.
     */
    public void hit(final OpposingField f, final OpposingFieldStatus fieldStatus) {
        /*
         * Wenn Wasser, dann Feld als Wasser markieren Wenn Schiff, dann Feld als Schiff
         * markieren Wenn Versunken, dann Feld und angrenzende Schifffelder als
         * versunken markieren
         */
        final int x = f.getX();
        final int y = f.getY();

        switch (fieldStatus) {
            case WATER, SHIP:
                /* Markiere das Feld entsprechend. */
                this.field[y][x] = fieldStatus;
                break;

            case SUNK:
                /*
                 * Dieses als Feld versenkt markieren. Dies bedeutet, dass alle angrenzenden
                 * Schiffsfelder nun auch als versunken markiert werden sollen.
                 */
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

    /**
     * Gibt an, ob ein bestimmtes Feld unbekannt ist - also noch nicht angegriffen
     * wurden ist.
     *
     * @param f Das Feld, welches überprüft werden soll.
     * @return true, wenn es noch nicht angegriffen wurden ist und unbekannt ist,
     *         sonst false.
     */
    public boolean isUnknown(final OpposingField f) {
        return this.field[f.getY()][f.getX()] == OpposingFieldStatus.UNKNOWN;
    }

    /**
     * Überträgt das Spielfeld in eine graphische Darstellung in Form einer
     * `PlaygroundMatrix`.
     *
     * @param pm PlaygroundMatrix auf die das Spielfeld gezeichnet werden soll.
     */
    public void print(final PlaygroundMatrix pm) {
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
