package battleship.utils;

import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.util.regex.Pattern;

public final class Utils {
    private Utils() {
        throw new UnsupportedOperationException("Utils cannot be instantiated");
    }

    public static String getFirstWordOrLine(String input) {
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

    public static String getStringAfterFirstSpace(String input) {
        final int spaceIndex = input.indexOf(' ');

        if (spaceIndex == -1) {
            return "";
        }

        return input.substring(spaceIndex + 1);
    }

    public static String sanitizeString(String input) {
        return input.replaceAll("\r", "\\r").replaceAll("\t", "\\t").replaceAll("\b", "\\b").replaceAll("\f", "\\f")
                .replaceAll("\\s+", " ");
    }

    public static String toAscii(String input) {
        final String normalized = Normalizer.normalize(input, Normalizer.Form.NFD);
        final Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        String asciiString = pattern.matcher(normalized).replaceAll("");

        final byte[] bytes = asciiString.getBytes(StandardCharsets.US_ASCII);
        asciiString = new String(bytes, StandardCharsets.US_ASCII);

        return asciiString;
    }
}
