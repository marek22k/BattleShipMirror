package battleship.utils;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;

public class SystemUtils {
    public static void copyToClipboard(String ipAddress) {
        final StringSelection stringSelection = new StringSelection(ipAddress);
        final Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(stringSelection, null);
    }
}
