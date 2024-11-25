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
    private final JFrame window;
    private final JTextPane terminalArea;
    private final JScrollPane terminalScrollPane;
    private final Style terminalStdoutStyle;
    private final Style terminalStderrStyle;
    private final Style terminalSystemMessageStyle;
    private final JLabel statusLabel;
    private final JTextField commandInputField;
    private final JButton startStopButton;
    private final JLabel colorDot;
    private Color dotColor = Color.RED;
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
        this.logger.setLevel(Constants.logLevel);

        this.window = new JFrame("Terminal");
        this.window.setLayout(new BorderLayout());
        this.processstatus = ProcessStatus.STOPPED;
        this.windowClosingThread = null;

        // Textfeld (multiline) oben
        this.terminalArea = new JTextPane();
        this.terminalArea.setEditable(false);

        this.terminalStdoutStyle = this.terminalArea.addStyle("stdout style", null);
        StyleConstants.setForeground(this.terminalStdoutStyle, Color.BLACK);

        this.terminalStderrStyle = this.terminalArea.addStyle("stderr style", null);
        StyleConstants.setForeground(this.terminalStderrStyle, Color.RED);

        this.terminalSystemMessageStyle = this.terminalArea.addStyle("system message style", null);
        StyleConstants.setForeground(this.terminalSystemMessageStyle, Color.GREEN);

        this.terminalScrollPane = new JScrollPane(this.terminalArea);
        this.window.add(this.terminalScrollPane, BorderLayout.CENTER);

        // Panel für den Status und Input-Field/Button
        final JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new BorderLayout());

        // Status-Bereich
        final JPanel statusPanel = new JPanel();
        statusPanel.setLayout(new FlowLayout(FlowLayout.LEFT));

        // Farbpunkterstellung (rot, rund)
        this.colorDot = new JLabel() {
            private static final long serialVersionUID = 1L;

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.setColor(TerminalWindow.this.dotColor);
                g.fillOval(0, 0, this.getWidth(), this.getHeight());
            }
        };

        this.statusLabel = new JLabel();
        statusPanel.add(this.colorDot);
        statusPanel.add(this.statusLabel);
        bottomPanel.add(statusPanel, BorderLayout.NORTH);

        // Input-Field und Button
        final JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new BorderLayout());
        this.commandInputField = new JTextField();
        this.startStopButton = new JButton();
        inputPanel.add(this.commandInputField, BorderLayout.CENTER);
        inputPanel.add(this.startStopButton, BorderLayout.EAST);
        bottomPanel.add(inputPanel, BorderLayout.SOUTH);

        this.window.add(bottomPanel, BorderLayout.SOUTH);

        this.statusLabel.setLabelFor(null);

        this.terminalArea.setPreferredSize(new Dimension(600, 350));
        this.colorDot.setPreferredSize(new Dimension(15, 15));
        this.commandInputField.setColumns(40);

        this.window.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        this.window.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
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
                                            e
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
        this.window.setLocationRelativeTo(null); // Zentriert das Fenster

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

    public void show() {
        this.window.pack();
        this.window.setVisible(true);
    }

    private void printText(String text, Style style) {
        try {
            final StyledDocument sd = this.terminalArea.getStyledDocument();
            sd.insertString(sd.getLength(), text, style);
        } catch (final BadLocationException e) {
            this.logger.log(Level.SEVERE, "An error has occurred during printing.", e);
            this.logger.log(Level.INFO, "The original message is: " + text);
        }
    }

    private void printToStdErr(String text) {
        this.printText(text, this.terminalStderrStyle);
    }

    private void printToStdOut(String text) {
        this.printText(text, this.terminalStdoutStyle);
    }

    private void printToSystemMessage(String text) {
        this.printText(text, this.terminalSystemMessageStyle);
    }

    private void setDotColor(Color color) {
        this.dotColor = color;
        this.colorDot.repaint();
    }

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

    private void setReady() {
        this.startStopButton.setEnabled(true);
        this.commandInputField.setEnabled(true);
        this.terminalArea.setEnabled(true);
        this.statusLabel.setEnabled(true);
        this.colorDot.setEnabled(true);
        this.terminalScrollPane.setEnabled(true);
    }

    private void setRunning() {
        this.setDotColor(Color.GREEN);
        this.startStopButton.setText("Stop");
        this.setStatusLabel("Running");
        this.setReady();
    }

    private void setStatusLabel(String text) {
        this.statusLabel.setText("Status: " + text);
    }

    private void setStopped() {
        this.setDotColor(Color.RED);
        this.startStopButton.setText("Start");
        this.setStatusLabel("Stopped");
        this.setReady();
    }

    @SuppressWarnings("deprecation")
    /* TODO: Is there a more modern function? */
    private void startProcess() {
        try {
            this.setNotReady();
            final String command = this.commandInputField.getText();

            try {
                this.process = Runtime.getRuntime().exec(command);
            } catch (final Exception e) {
                this.printToSystemMessage(e.getMessage() + "\n");
                return;
            }

            SwingUtilities.invokeLater(() -> this.terminalArea.setText(""));

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

    private void stopProcess(String reason) {
        synchronized (this.processLock) {
            if (this.processstatus != ProcessStatus.STOPPED && this.processstatus != ProcessStatus.WAITING_FOR_END) {
                this.logger.log(Level.INFO, "Process finished: " + reason);

                SwingUtilities.invokeLater(() -> this.printToSystemMessage("Process finished: " + reason + "\n"));

                this.exitWaitingThread.interrupt();
                this.stdoutThread.interrupt();
                this.stderrThread.interrupt();

                if (this.process.isAlive()) {
                    this.process.destroy();
                }

                this.processstatus = ProcessStatus.WAITING_FOR_END;
                SwingUtilities.invokeLater(this::setNotReady);
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
