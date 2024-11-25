package battleship.utils;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.util.function.Consumer;
import java.util.regex.Pattern;

import edu.umd.cs.findbugs.annotations.CheckReturnValue;

public final class Utils {
    private Utils() {
        throw new UnsupportedOperationException("Utils cannot be instantiated");
    }

    @CheckReturnValue
    public static String writerToString(Consumer<PrintWriter> writer) {
        final StringWriter stringWriter = new StringWriter();

        try (PrintWriter printWriter = new PrintWriter(stringWriter)) {
            writer.accept(printWriter);
        }

        return stringWriter.toString();
    }

    @CheckReturnValue
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

    @CheckReturnValue
    public static String getStringAfterFirstSpace(String input) {
        final int spaceIndex = input.indexOf(' ');

        if (spaceIndex == -1) {
            return "";
        }

        return input.substring(spaceIndex + 1);
    }

    @CheckReturnValue
    public static String sanitizeString(String input) {
        return input.replaceAll("\r", "\\r").replaceAll("\t", "\\t").replaceAll("\b", "\\b").replaceAll("\f", "\\f")
                .replaceAll("\\s+", " ");
    }

    @CheckReturnValue
    public static String toAscii(String input) {
        final String normalized = Normalizer.normalize(input, Normalizer.Form.NFD);
        final Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        final String stringWithoutDiacriticalMarks = pattern.matcher(normalized).replaceAll("");

        final byte[] bytes = stringWithoutDiacriticalMarks.getBytes(StandardCharsets.US_ASCII);
        final String asciiString = new String(bytes, StandardCharsets.US_ASCII);

        return asciiString;
    }
}
