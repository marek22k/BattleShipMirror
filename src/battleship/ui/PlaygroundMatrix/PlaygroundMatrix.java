package battleship.ui.PlaygroundMatrix;

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

public class PlaygroundMatrix extends JPanel {
    private final JPanel[][] grid;

    private final int rows;
    private final int cols;
    private final List<FireListener> listeners; // Liste der registrierten Listener
    private static final long serialVersionUID = 1L;

    public PlaygroundMatrix(int rows, int cols) {
        this.rows = rows;
        this.cols = cols;
        this.listeners = new ArrayList<>(); // Initialisiere die Listener-Liste

        // Layout mit zusätzlicher Zeile und Spalte für Beschriftungen
        this.setLayout(new GridBagLayout());
        final GridBagConstraints gbc = new GridBagConstraints();

        // Leeres Zusatzfeld oben links
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 0.1;
        gbc.weighty = 0.1;
        final JPanel emptyCorner = new JPanel();
        emptyCorner.setPreferredSize(new Dimension(40, 40)); // Größe des leeren Feldes
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
                panel.setBackground(Color.BLACK); // Anfangsfarbe (Wasser)
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
    }

    // Methode zum Hinzufügen von FireListenern
    public void addFireListener(FireListener listener) {
        this.listeners.add(listener);
    }

    // Zusätzliche Methoden, um die Felder zu steuern (z.B. Farben ändern)
    public void setFieldColor(int col, int row, Color color) {
        if (row >= 0 && row < this.rows && col >= 0 && col < this.cols) {
            this.grid[row][col].setBackground(color);
        }
    }

    public void setShip(int col, int row) {
        this.setFieldColor(col, row, Color.DARK_GRAY);
    }

    public void setSunk(int col, int row) {
        this.setFieldColor(col, row, new Color(0, 0, 153));
    }

    public void setUnknown(int col, int row) {
        this.setFieldColor(col, row, Color.WHITE);
    }

    public void setWater(int col, int row) {
        this.setFieldColor(col, row, new Color(0, 102, 255));
    }

    // Methode zum Auslösen des Fire-Events
    private void fireEvent(int x, int y) {
        final FireEvent event = new FireEvent(x, y);
        for (final FireListener listener : this.listeners) {
            listener.fire(event);
        }
    }
}
