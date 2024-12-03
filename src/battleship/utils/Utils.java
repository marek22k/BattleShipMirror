package battleship.utils;

import java.text.Normalizer;

import com.google.common.base.CharMatcher;

/**
 * Verschiedene Werkzeuge
 */
public final class Utils {
    /**
     * Utils kann nicht instanziiert werden
     */
    private Utils() {
        throw new UnsupportedOperationException("Utils cannot be instantiated");
    }

    /**
     * Extrahiert aus einem String das erste Wort (alle Zeichen vor dem ersten
     * Leerzeichen) oder falls die Zeile nur aus einem Wort besteht schneidet es die
     * darauf folgenden Zeilen ab.
     *
     * @param input Der String, aus welchem das erste Wort bzw. die erste Zeile
     *              extrahiert werden soll.
     * @return Das erste Wort aus dem String bzw. die erste Zeile, wenn in der Zeile
     *         nur ein Wort ist.
     */
    public static String getFirstWordOrLine(final String input) {
        final int spaceIndex = input.indexOf(' ');
        final int newLineIndex = input.indexOf("\r\n");

        if (spaceIndex == -1 && newLineIndex == -1) {
            return input;
        }

        int endIndex = input.length();
        if (spaceIndex != -1 && (newLineIndex == -1 || spaceIndex < newLineIndex)) {
            endIndex = spaceIndex;
        } else if (newLineIndex != -1) {
            endIndex = newLineIndex;
        }

        return input.substring(0, endIndex);
    }

    /**
     * Extrahiert alle Zeichen ab dem ersten Leerzeichen.
     *
     * @param input String, aus welchem alle Zeichen nach dem ersten Leerzeichen
     *              extrahiert werden soll.
     * @return Alle Zeichen nach dem ersten Leerzeichen
     */
    public static String getStringAfterFirstSpace(final String input) {
        final int spaceIndex = input.indexOf(' ');

        if (spaceIndex == -1) {
            return "";
        }

        return input.substring(spaceIndex + 1);
    }

    /**
     * Entfernt "gefährliche" Zeichen aus einem String. Dies können beispielsweise
     * Kontrollzeichen sein.
     *
     * @param input Der unbehandelte String
     * @return Der behandelte String
     */
    public static String sanitizeString(final String input) {
        return CharMatcher.anyOf("\r\t\b\f").replaceFrom(input, "\\$0").replaceAll("\\s+", " ");
    }

    /**
     * Konvertiert einen String in ASCII-Zeichen. Dafür wird dieser als erstes
     * Sonderbuchstaben durch ihren Basisbuchstaben ersetzt und danach werden alle
     * verbleibenden nicht-ASCII-Zeichen entfernt.
     *
     * @param input Der String, welcher in einen ASCII-Sring umgewandelt werden soll
     * @return Der ASCII-String
     */
    public static String toAscii(final String input) {
        final String normalized = Normalizer.normalize(input, Normalizer.Form.NFD);
        final String asciiOnly = CharMatcher.ascii().retainFrom(normalized);

        return asciiOnly;
    }
}
