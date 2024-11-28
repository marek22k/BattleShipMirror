package battleship.network.commands;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Repräsentiert ein VERSION-Paket
 */
public class Version implements Command {
    /**
     * Die verwendete Implementierung
     */
    private final String implementation;
    /**
     * Die unterstützten Versionen
     */
    private final Set<String> versions;

    /**
     * Erstellt aus einem VERSION-Paket in seiner genormten Übertragungsform als
     * String eine Repräsentation des Paketes.
     *
     * @param command Das VERSION-Paket
     * @return Repräsentation des VERSION-Pakets
     */
    public static Version fromString(String command) {
        final String[] parts = command.split(" ");
        final String implementation = parts[0].strip();
        final Set<String> versions = new HashSet<>(parts.length);
        for (int i = 1; i < parts.length; i++) {
            versions.add(parts[i].strip());
        }
        return new Version(implementation, versions);
    }

    /**
     * Erstellt eine Repräsentation des VERSION-Paketes
     *
     * @param implementation
     * @param version
     */
    public Version(String implementation, Set<String> version) {
        this.implementation = implementation;
        this.versions = Collections.unmodifiableSet(new HashSet<>(version));
    }

    public Version(String implementation, String version) {
        this(implementation, Set.of(version));
    }

    @Override
    public String getFullCommand() {
        final StringBuilder builder = new StringBuilder();
        builder.append("VERSION ");

        builder.append(this.implementation);
        builder.append(' ');

        builder.append(String.join(" ", this.versions));

        builder.append("\r\n");
        return builder.toString();
    }

    public String getImplementation() {
        return this.implementation;
    }

    public boolean hasVersion(String version) {
        return this.versions.contains(version);
    }

    @Override
    public boolean isValid() {
        if (this.implementation.isBlank() || this.versions.stream().anyMatch(String::isBlank)) {
            return false;
        }
        return true;
    }
}
