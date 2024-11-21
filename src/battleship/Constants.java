package battleship;

import java.util.List;
import java.util.logging.Level;

/**
 * Beinhaltet verschiedene Konstanten, welche das Spiel benötigt.
 */
public class Constants {
    /**
     * Protokollversion, welche das aktuelle Spiel implementiert.
     */
    public static final String PROTOCOL_VERSION = "1.1.0";

    /**
     * Name unserer Implementierung
     */
    public static final String IMPLEMENTATION = "BandurasBattleShip";

    /**
     * Standardport für Schiffeversenken
     */
    public static final int SERVER_PORT = 51525;

    /**
     * Liste, welche die Anzahl und Größe der Schiffe pro Level definiert.
     */
    public static final List<List<Integer>> LEVELS = List.of(
            /* Erstes Semester */
            List.of(
                    Math.abs(8 - 6) /* Programmieren 1 */, Math.abs(8 - 6) /* Grundlagen der Informatik */,
                    Math.abs(8 - 6) /* Mathematik 1 */, Math.abs(8 - 6) /* Theoretische Informatik */,
                    Math.abs(8 - 4) /* Startprojekt */, Math.abs(8 - 2) /* Englisch */
            ),
            /* Zweites Semester */
            List.of(
                    Math.abs(8 - 6) /* Programmieren 2 */, Math
                            .abs(8 - 6) /* Datenbanksysteme 1 */,
                    Math.abs(8 - 6) /* Mathematik 2 */, Math.abs(8 - 6) /* Statistik */, Math.abs(8 - 6) /*
                                                                                                          * Algorithmen
                                                                                                          * und
                                                                                                          * Datenstrukturen
                                                                                                          */
            ),
            /* Drittes Semester */
            List.of(
                    Math.abs(8 - 6) /* Programmieren 3 */, Math.abs(8 - 6) /* Datenbanksysteme 2 */,
                    Math.abs(8 - 6) /* Mathematik 3 */, Math.abs(8 - 6) /* Betriebssysteme und Netze 1 */,
                    Math.abs(8 - 4) /* Programmierprojekt */, Math.abs(8 - 2) /* Betriebswirtschaft */
            ),
            /* Viertes Semester */
            List.of(
                    Math.abs(8 - 6) /* Webtechnologien */, Math.abs(8 - 6) /* Software Engineering 1 */,
                    Math.abs(8 - 6) /* Computergrafik 1 */, Math.abs(8 - 6) /* Betriebssysteme und Netze 2 */,
                    Math.abs(8 - 4) /* Seminar */, Math.abs(8 - 2) /* Ergänzendes Fach BWL */
            ),
            /* Fünftes Semester */
            List.of(
                    Math.abs(8 - 6) /* Wahlpflichtfach Informatik 1 */, Math
                            .abs(8 - 6) /* Software Engineering 2 */,
                    Math.abs(8 - 6) /* Computergrafik 2 */, Math.abs(8 - 10) /* Praxisprojekt 1 */, Math.abs(8 - 2) /*
                                                                                                                     * Ergänzendes
                                                                                                                     * Fach
                                                                                                                     * 1
                                                                                                                     */
            ),
            /* Sechstes Semester */
            List.of(
                    Math.abs(8 - 6) /* Wahlpflichtfach Informatik 2 */, Math.abs(8 - 7) /* Praxisprojekt 2 */,
                    Math.abs(8 - 15) /* Bachelor-Arbeit mit Kolloquium */, Math.abs(8 - 2) /* Ergänzendes Fach 2 */
            )
    );

    /**
     * Größe der Spielfelder von den einzelnen Leveln
     */
    public static final int[] LEVEL_SIZES = {14, 15, 16, 17, 18, 19};

    /**
     * Anzahl der Level
     */
    public static final int NUMBER_OF_LEVELS = LEVELS.size();

    /**
     * Standardloglevel
     */
    public static final Level logLevel = Level.ALL;

    /**
     * Gibt an, ob das Spiel Sound unterstützen soll.
     */
    public static final boolean SOUND = true;

    /* https://opengameart.org/content/tiny-naval-battle-sounds-set */
    public static final String hit1Filename = "/Hit1.wav";
    public static final String hit2Filename = "/Hit2.wav";
    public static final String waterFilename = "/Water.wav";
    /* https://opengameart.org/content/victory-4 */
    public static final String victoryFilename = "/Victory.wav";
}
