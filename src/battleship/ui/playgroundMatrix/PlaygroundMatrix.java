package battleship.ui.playgroundMatrix;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

/**
 * Graphisches Spielfeld.
 */
public class PlaygroundMatrix extends JPanel {
    private static final long serialVersionUID = 3185851666830761797L;

    /*
     * Speichert die einzelnen Felder in Form von JPanels.
     */
    private final JPanel[][] grid;

    /*
     * Anzahl der Zeilen
     */
    private final int rows;

    /*
     * Anzahl der Reihen
     */
    private final int cols;

    /*
     * Liste von Listeners, welche aufgerufen werden, wenn der Benutzer auf ein Feld
     * klickt und damit feuert.
     */
    private final List<FireListener> listeners;

    /*
     * Faktor um die Größe (wie es später angezeigt wird, in Pixeln) im Verhältnis
     * zur eigentlichen Spielfeldgröße zu beschreiben.
     */
    private static final int RESIZE_FACTOR = 40;

    /**
     * Erstellt ein neues graphisches Spielfeld
     *
     * @param rows Anzahl der Zeilen
     * @param cols Anzahl der Reihen
     */
    public PlaygroundMatrix(int rows, int cols) {
        this.rows = rows;
        this.cols = cols;
        this.listeners = new ArrayList<>();

        this.setLayout(new GridBagLayout());
        final GridBagConstraints gbc = new GridBagConstraints();

        // Leeres Zusatzfeld oben links
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 0.1;
        gbc.weighty = 0.1;
        final JPanel emptyCorner = new JPanel();
        // Größe des leeren Feldes
        emptyCorner.setPreferredSize(new Dimension(40, 40));
        this.add(emptyCorner, gbc);

        // Buchstabenfelder oben
        for (int i = 0; i < cols; i++) {
            gbc.gridx = i + 1;
            gbc.gridy = 0;
            gbc.fill = GridBagConstraints.BOTH;
            gbc.weightx = 0.1;
            gbc.weighty = 0.1;
            final JLabel label = new JLabel(String.valueOf((char) ('A' + i)), SwingConstants.CENTER);
            label.setBorder(BorderFactory.createLineBorder(Color.BLACK));
            this.add(label, gbc);
        }

        // Zahlenfelder links
        for (int i = 0; i < rows; i++) {
            gbc.gridx = 0;
            gbc.gridy = i + 1;
            gbc.fill = GridBagConstraints.BOTH;
            gbc.weightx = 0.1;
            gbc.weighty = 0.1;
            final JLabel label = new JLabel(String.valueOf(i + 1), SwingConstants.CENTER);
            label.setPreferredSize(new Dimension(40, 40)); // Breite der Zahlenfelder
            label.setBorder(BorderFactory.createLineBorder(Color.BLACK));
            label.setLabelFor(null);
            this.add(label, gbc);
        }

        // Spielfeld erstellen
        this.grid = new JPanel[rows][cols];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                gbc.gridx = j + 1;
                gbc.gridy = i + 1;
                gbc.fill = GridBagConstraints.BOTH;
                gbc.weightx = 0.1;
                gbc.weighty = 0.1;
                final JPanel panel = new JPanel();
                panel.setBackground(Color.BLACK); // Anfangsfarbe (Leer/Unbekannt)
                panel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY)); // Helle Linien
                this.grid[i][j] = panel;
                this.add(panel, gbc);

                // MouseListener hinzufügen, um auf Klicks zu reagieren
                final int x = j;
                final int y = i;
                panel.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        // Fire-Event auslösen
                        PlaygroundMatrix.this.fireEvent(x, y);
                    }
                });
            }
        }
        this.setPreferredSize(new Dimension(cols * RESIZE_FACTOR, rows * RESIZE_FACTOR));
    }

    /**
     * Registriert einen neuen FireListener
     *
     * @param listener Der zu registrierende FireListener
     */
    public void addFireListener(FireListener listener) {
        this.listeners.add(listener);
    }

    /**
     * Setzt ein bestimmtes Feld aus eine Farbe
     *
     * @param col   Zeilennummer des Feldes (beginnend bei 0)
     * @param row   Spaltennummer des Feldes (beginnend bei 0)
     * @param color Die Farbe auf welche das Feld gesetzt werden soll
     */
    public void setFieldColor(int col, int row, Color color) {
        if (row >= 0 && row < this.rows && col >= 0 && col < this.cols) {
            this.grid[row][col].setBackground(color);
        }
    }

    /**
     * Setzt ein bestimmtes Feld auf die Schiffsfarbe (Dunkelgrau)
     *
     * @param col Zeilennummer des Feldes (beginnend bei 0)
     * @param row Spaltennummer des Feldes (beginnend bei 0)
     */
    public void setShip(int col, int row) {
        this.setFieldColor(col, row, Color.DARK_GRAY);
    }

    /**
     * Setzt ein bestimmtes Feld auf die versunkene Schiffsfarbe (Dunkelblau)
     *
     * @param col Zeilennummer des Feldes (beginnend bei 0)
     * @param row Spaltennummer des Feldes (beginnend bei 0)
     */
    public void setSunk(int col, int row) {
        this.setFieldColor(col, row, new Color(0, 0, 153));
    }

    /**
     * Setzt ein bestimmtes Feld auf die Unbekanntes-Feld-Farbe (Weiß)
     *
     * @param col Zeilennummer des Feldes (beginnend bei 0)
     * @param row Spaltennummer des Feldes (beginnend bei 0)
     */
    public void setUnknown(int col, int row) {
        this.setFieldColor(col, row, Color.WHITE);
    }

    /**
     * Setzt ein bestimmtes Feld auf die Wasserfarbe (blau)
     *
     * @param col Zeilennummer des Feldes (beginnend bei 0)
     * @param row Spaltennummer des Feldes (beginnend bei 0)
     */
    public void setWater(int col, int row) {
        this.setFieldColor(col, row, new Color(0, 102, 255));
    }

    /**
     * Löst ein FireEvent aus. Ruft alle registrierten Listener auf.
     *
     * @param x X-Koordinate / Zeilennummer
     * @param y Y-Koordinate / Spaltennummer
     */
    private void fireEvent(int x, int y) {
        final FireEvent event = new FireEvent(x, y);
        for (final FireListener listener : this.listeners) {
            listener.fire(event);
        }
    }
}
