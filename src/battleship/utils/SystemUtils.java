package battleship.utils;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;

/**
 * System-Werkzeuge
 */
public final class SystemUtils {
    /**
     * SystemUtils kann nicht instanziiert werden
     */
    private SystemUtils() {
        throw new UnsupportedOperationException("SystemUtils cannot be instantiated");
    }

    /**
     * Kopiert einen String in die System-Zwischenablage
     *
     * @param text Text, der in die System-Zwischenablage kopiert werden soll.
     */
    public static void copyToClipboard(final String text) {
        final StringSelection stringSelection = new StringSelection(text);
        final Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(stringSelection, null);
    }
}
