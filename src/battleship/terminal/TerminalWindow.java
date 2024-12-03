package battleship.terminal;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import battleship.Constants;

/**
 * Ein kleines Fenster, welches ein einfaches Terminal darstellt und
 * Shell-Befehle ausführen kann. Es bietet die Möglichkeit, dass Befehle Dinge
 * ausgeben und das der Prozess manuell gestoppt werden kann.
 */
public final class TerminalWindow {
    /*
     * Das eigentliche Terminal-Fenster
     */
    private final JFrame window;

    /*
     * Das Terminal-Ausgaben-Feld (mehrzeilig)
     */
    private final JTextPane terminalArea;
    private final JScrollPane terminalScrollPane;
    private final Style terminalStdoutStyle;
    private final Style terminalStderrStyle;
    private final Style terminalSystemMessageStyle;

    /*
     * Das Label, welchen den Status des Prozesses anzeigt
     */
    private final JLabel statusLabel;
    /*
     * Der farbige Punkt, welcher zusätzlich zum Label den Prozess-Status anzeigt
     */
    private final JLabel colorDot;
    private Color dotColor;

    /*
     * Einzeilig Feld, wo man den Befehl, welches ausgeführt werden kann, eingibt.
     */
    private final JTextField commandInputField;

    private final JButton startStopButton;

    private final Logger logger;

    private Process process;
    private ProcessStatus processstatus;
    private final Object processLock = new Object();
    private Thread stdoutThread;
    private Thread stderrThread;
    private Thread exitWaitingThread;
    private Thread destroyWaitingThread;
    private Thread windowClosingThread;

    public TerminalWindow() {
        this.logger = Logger.getLogger(TerminalWindow.class.getName());
        this.logger.setLevel(Constants.LOG_LEVEL);

        this.window = new JFrame("Terminal");
        this.window.setLayout(new BorderLayout());
        this.processstatus = ProcessStatus.STOPPED;
        this.windowClosingThread = null; // Ermöglichst später das manuelle Setzen einer Aktion beim Fensterschließen

        // Bereich für die Ausgabe
        this.terminalArea = new JTextPane();
        this.terminalArea.setEditable(false);
        this.terminalArea.setPreferredSize(new Dimension(600, 350));

        // Verschiedene Farbstyle, jenachdem in welchem Stream der Prozess die Ausgaben
        // ausgibt oder ob die Mitteilung von uns und nicht vom Prozess kommt.
        this.terminalStdoutStyle = this.terminalArea.addStyle("stdout style", null);
        StyleConstants.setForeground(this.terminalStdoutStyle, Color.BLACK);

        this.terminalStderrStyle = this.terminalArea.addStyle("stderr style", null);
        StyleConstants.setForeground(this.terminalStderrStyle, Color.RED);

        this.terminalSystemMessageStyle = this.terminalArea.addStyle("system message style", null);
        StyleConstants.setForeground(this.terminalSystemMessageStyle, Color.GREEN);

        // Wrapper mit Scrollen für die Ausgabe
        this.terminalScrollPane = new JScrollPane(this.terminalArea);
        this.window.add(this.terminalScrollPane, BorderLayout.CENTER);

        // Panel für den Status, Befehls-Eingabe-Feld und Start/Stop-Button
        final JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new BorderLayout());

        // Status-Panel
        final JPanel statusPanel = new JPanel();
        statusPanel.setLayout(new FlowLayout(FlowLayout.LEFT));

        // Erstellen des Farbpunktes
        this.dotColor = Color.RED;
        this.colorDot = new JLabel() {
            private static final long serialVersionUID = 1L;

            @Override
            protected void paintComponent(final Graphics g) {
                super.paintComponent(g);
                g.setColor(TerminalWindow.this.dotColor);
                g.fillOval(0, 0, this.getWidth(), this.getHeight());
            }
        };
        this.colorDot.setPreferredSize(new Dimension(15, 15));

        this.statusLabel = new JLabel();
        statusPanel.add(this.colorDot);
        statusPanel.add(this.statusLabel);
        bottomPanel.add(statusPanel, BorderLayout.NORTH);

        // Befehls-Eingabe-Feld und Start/Stop-Button
        final JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new BorderLayout());
        this.commandInputField = new JTextField();
        this.commandInputField.setColumns(40);
        this.startStopButton = new JButton();
        inputPanel.add(this.commandInputField, BorderLayout.CENTER);
        inputPanel.add(this.startStopButton, BorderLayout.EAST);
        bottomPanel.add(inputPanel, BorderLayout.SOUTH);

        this.window.add(bottomPanel, BorderLayout.SOUTH);

        // Beide Labels fungieren nicht dafür eine Eingabemöglichkeit zu benennen
        this.colorDot.setLabelFor(null);
        this.statusLabel.setLabelFor(null);

        this.window.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        this.window.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(final WindowEvent event) {
                synchronized (TerminalWindow.this.processLock) {
                    TerminalWindow.this.stopProcess("Window closed.");
                    if (
                        TerminalWindow.this.windowClosingThread == null
                                || !TerminalWindow.this.windowClosingThread.isAlive()
                    ) {
                        TerminalWindow.this.windowClosingThread = new Thread(() -> {
                            try {
                                if (
                                    TerminalWindow.this.destroyWaitingThread != null
                                            && TerminalWindow.this.destroyWaitingThread.isAlive()
                                ) {
                                    TerminalWindow.this.destroyWaitingThread.join();
                                }
                                SwingUtilities.invokeLater(() -> TerminalWindow.this.window.dispose());
                            } catch (final Exception exc) {
                                if (TerminalWindow.this.process != null && TerminalWindow.this.process.isAlive()) {
                                    SwingUtilities.invokeLater(
                                            () -> TerminalWindow.this.printToSystemMessage(
                                                    "Process could not be completed. Therefore, the window cannot be closed."
                                            )
                                    );
                                } else {
                                    TerminalWindow.this.logger.log(
                                            Level.SEVERE,
                                            "A strange error has occurred. The thread to terminate the process was not successfully executed to the end, but the process is terminated.",
                                            event
                                    );
                                    SwingUtilities.invokeLater(() -> TerminalWindow.this.window.dispose());
                                }
                            }
                        });
                        TerminalWindow.this.windowClosingThread.start();
                    }
                }
            }
        });

        this.startStopButton.addActionListener(e -> {
            synchronized (this.processLock) {
                if (this.processstatus == ProcessStatus.STOPPED) {
                    this.startProcess();
                } else {
                    this.stopProcess("Stop requested");
                }
            }
        });

        this.commandInputField.addActionListener(e -> {
            synchronized (this.processLock) {
                if (this.processstatus == ProcessStatus.STOPPED) {
                    this.startProcess();
                } else {
                    this.stopProcess("Stop requested");
                }
            }
        });

        this.setStopped();
    }

    /**
     * Zeigt das Fenster an
     */
    public void show() {
        this.window.pack();
        this.window.setVisible(true);
    }

    /**
     * Gibt einen bestimmten Text in einem bestimmten Style aus.
     *
     * @param text  Text
     * @param style Style
     */
    private void printText(final String text, final Style style) {
        try {
            final StyledDocument sd = this.terminalArea.getStyledDocument();
            sd.insertString(sd.getLength(), text, style);
        } catch (final BadLocationException e) {
            this.logger.log(Level.SEVERE, "An error has occurred during printing.", e);
            this.logger.log(Level.INFO, "The original message is: " + text);
        }
    }

    /**
     * Gibt Text im `stderr`-Style aus.
     *
     * @param text Text
     */
    private void printToStdErr(final String text) {
        this.printText(text, this.terminalStderrStyle);
    }

    /**
     * Gibt Text im `stdout`-Style aus.
     *
     * @param text Text
     */
    private void printToStdOut(final String text) {
        this.printText(text, this.terminalStdoutStyle);
    }

    /**
     * Gibt Text im system-Style aus.
     *
     * @param text Text
     */
    private void printToSystemMessage(final String text) {
        this.printText(text, this.terminalSystemMessageStyle);
    }

    /**
     * Setzt die Farbe des Prozesstatusfarbpunkts
     *
     * @param color Die neue Farbe
     */
    private void setDotColor(final Color color) {
        this.dotColor = color;
        this.colorDot.repaint();
    }

    /**
     * Setzt die GUI-Elemente auf "Not ready". Es deaktiviert die meisten
     * GUI-Elemente, sodass keine Eingaben oder Nutzerinteraktionen mehr möglich
     * sind. Die Prozessstatusfarbe wird aus schwarz geändert und ein entsprechender
     * Text wird im Status-Label angezeigt.
     */
    private void setNotReady() {
        this.setDotColor(Color.BLACK);
        this.startStopButton.setEnabled(false);
        this.startStopButton.setText("---");
        this.commandInputField.setEnabled(false);
        this.terminalArea.setEnabled(false);
        this.terminalScrollPane.setEnabled(false);
        this.statusLabel.setEnabled(false);
        this.colorDot.setEnabled(false);
        this.setStatusLabel("Not ready");
    }

    /**
     * Setzt die GUI-Elemente auf "Ready". Dem Nutzer ist größmögliche Interaktion
     * erlaubt, sodass er einen Befehl eingeben kann und den Prozess starten kann.
     */
    private void setReady() {
        this.startStopButton.setEnabled(true);
        this.commandInputField.setEnabled(true);
        this.terminalArea.setEnabled(true);
        this.statusLabel.setEnabled(true);
        this.colorDot.setEnabled(true);
        this.terminalScrollPane.setEnabled(true);
    }

    /**
     * Setzt die GUI-Elemente auf "Running". Deaktiviert die Eingabe von neuen
     * Befehlen für den Nutzer.
     */
    private void setRunning() {
        this.setDotColor(Color.GREEN);
        this.startStopButton.setText("Stop");
        this.setStatusLabel("Running");
        this.setReady();
        this.commandInputField.setEnabled(false);
    }

    /**
     * Ändert den Prozess-Status-Text.
     *
     * @param text
     */
    private void setStatusLabel(final String text) {
        this.statusLabel.setText("Status: " + text);
    }

    /**
     * Setzt die GUI-Elemente auf "Stopped", überschneidet sich bis auf die
     * Prozessstatusfarbe und gesetzte Text nicht von `setReady()`.
     */
    private void setStopped() {
        this.setDotColor(Color.RED);
        this.startStopButton.setText("Start");
        this.setStatusLabel("Stopped");
        this.setReady();
    }

    /**
     * Startet den Prozess
     */
    @SuppressWarnings("deprecation")
    private void startProcess() {
        try {
            this.setNotReady();
            final String command = this.commandInputField.getText();

            try {
                // Die Funktion ist veraltet und sollte nicht mehr verwendet werden, jedoch
                // scheint es keine andere Java-Funktion zu geben, welche ein Befehl
                // Eins-Zu-Eins in der Kommandozeile ausführt.
                // Daher auch oben das `@SuppressWarnings`.
                this.process = Runtime.getRuntime().exec(command);
            } catch (final Exception e) {
                this.logger.log(Level.FINE, "Failed to start process.", e);
                this.printToSystemMessage(e.getMessage() + "\n");
                this.setStopped();
                return;
            }

            SwingUtilities.invokeLater(() -> this.terminalArea.setText(""));

            // Thread um die `stdout` Ausgabe des Prozesses umzuleiten
            this.stdoutThread = new Thread(() -> {
                try (
                        BufferedReader reader = new BufferedReader(
                                new InputStreamReader(this.process.getInputStream(), StandardCharsets.UTF_8)
                        )
                ) {
                    while (true) {
                        final String line = reader.readLine();
                        if (Thread.currentThread().isInterrupted()) {
                            break;
                        }
                        if (line == null) {
                            synchronized (this.processLock) {
                                this.stopProcess("Null line in stdout");
                            }
                            break;
                        }
                        SwingUtilities.invokeLater(() -> this.printToStdOut(line + "\n"));
                    }
                } catch (final IOException e) {
                    synchronized (this.processLock) {
                        if (this.processstatus == ProcessStatus.RUNNING) {
                            this.logger.log(Level.SEVERE, "Error", e);
                        }
                        this.stopProcess("IOException in stdout");
                    }
                }
            });
            this.stdoutThread.start();

            // Thread um die `stderr` Ausgabe des Prozesses umzuleiten
            this.stderrThread = new Thread(() -> {
                try (
                        BufferedReader reader = new BufferedReader(
                                new InputStreamReader(this.process.getErrorStream(), StandardCharsets.UTF_8)
                        )
                ) {
                    while (true) {
                        final String line = reader.readLine();
                        if (Thread.currentThread().isInterrupted()) {
                            break;
                        }
                        if (line == null) {
                            synchronized (this.processLock) {
                                this.stopProcess("Null line in stderr");
                            }
                            break;
                        }
                        SwingUtilities.invokeLater(() -> this.printToStdErr(line + "\n"));
                    }
                } catch (final IOException e) {
                    synchronized (this.processLock) {
                        if (this.processstatus == ProcessStatus.RUNNING) {
                            this.logger.log(Level.SEVERE, "Error", e);
                        }
                        this.stopProcess("IOException in stderr");
                    }
                }
            });
            this.stderrThread.start();

            // Thread, um zu reagieren, wenn der Prozess beendet ist.
            this.exitWaitingThread = new Thread(() -> {
                try {
                    this.process.waitFor();
                    synchronized (this.processLock) {
                        this.stopProcess("Process was finished.");
                    }
                } catch (final Exception e) {
                    synchronized (this.processLock) {
                        if (this.processstatus == ProcessStatus.RUNNING) {
                            this.logger.log(Level.SEVERE, "Error", e);
                        }
                        this.stopProcess("Exception while waiting for process end.");
                    }
                }

            });
            this.exitWaitingThread.start();

            this.setRunning();
            this.processstatus = ProcessStatus.RUNNING;
        } catch (final Exception e) {
            this.logger.log(Level.SEVERE, "Error", e);
            synchronized (this.processLock) {
                this.stopProcess("Failed to start process.");
            }
        }
    }

    /**
     * Stoppt den Prozess
     *
     * @param reason Ein Grund, warum der Prozess beendet werden soll. Der Grund
     *               wird dem Nutzer angezeigt.
     */
    private void stopProcess(final String reason) {
        synchronized (this.processLock) {
            if (this.processstatus != ProcessStatus.STOPPED && this.processstatus != ProcessStatus.WAITING_FOR_END) {
                this.logger.log(Level.INFO, "Process finished: " + reason);

                SwingUtilities.invokeLater(() -> this.printToSystemMessage("Process finished: " + reason + "\n"));

                // Beende alle Threads, welche auf dem Prozess angewiesen sind oder auf ihn
                // warten
                this.exitWaitingThread.interrupt();
                this.stdoutThread.interrupt();
                this.stderrThread.interrupt();

                if (this.process.isAlive()) {
                    this.process.destroy();
                }

                this.processstatus = ProcessStatus.WAITING_FOR_END;
                SwingUtilities.invokeLater(this::setNotReady);
                // Das Beenden des Prozesses garantiert nicht sein sofortiges Ende. Es wird also
                // ein weiterer Thread angelegt, welcher darauf wartet, dass der Prozess
                // wirklich zu Ende ist.
                this.destroyWaitingThread = new Thread(() -> {
                    final long lastTime = System.currentTimeMillis();
                    while (this.process.isAlive()) {
                        if (Thread.currentThread().isInterrupted()) {
                            SwingUtilities.invokeLater(
                                    () -> this
                                            .printToSystemMessage("Process not finished. But don't wait any longer.\n")
                            );
                            return;
                        }
                        // Wenn der Prozess zu lange braucht, um sich zu beenden, soll das Programm
                        // nicht "einfrieren", sondern den Prozess zwangsweise beenden.
                        if (System.currentTimeMillis() - lastTime >= 10000) {
                            SwingUtilities.invokeLater(
                                    () -> this.printToSystemMessage("Wait for the process to finish...\n")
                            );
                            this.process.destroyForcibly();
                        }
                        try {
                            Thread.sleep(100);
                        } catch (final InterruptedException e) {
                            SwingUtilities.invokeLater(
                                    () -> this
                                            .printToSystemMessage("Process not finished. But don't wait any longer.\n")
                            );
                            return;
                        }
                    }
                    SwingUtilities.invokeLater(
                            () -> this.printToSystemMessage(
                                    "Process finished with code: " + this.process.exitValue() + "\n"
                            )
                    );
                    SwingUtilities.invokeLater(this::setStopped);
                    synchronized (this.processLock) {
                        this.processstatus = ProcessStatus.STOPPED;
                    }
                });
                this.destroyWaitingThread.start();
            }
        }
    }
}
