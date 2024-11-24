package battleship.ui.GameWindow;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
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
import battleship.ui.PlaygroundMatrix.PlaygroundMatrix;
import battleship.utils.Utils;

public final class GameWindow {
    private final JFrame window;
    private final JButton computerButton;
    private final JButton withdrawButton;
    private final JTextField chatInputField;
    private final JTextPane chatOutputPane;
    private final JButton sendButton;
    private final Style chatSystemStyle;
    private final Style chatUserStyle;
    private final Style chatPeerStyle;
    private final PlaygroundMatrix playersField;
    private final PlaygroundMatrix opponentField;
    private MessageHandler messagehandler;
    private ClickHandler withdrawhandler;
    private ClickHandler computermovehandler;
    private final int rows;
    private final int cols;
    private final Logger logger;

    public GameWindow(int rows, int cols) {
        this.logger = Logger.getLogger(GameWindow.class.getName());
        this.logger.setLevel(Constants.logLevel);

        this.rows = rows;
        this.cols = cols;
        this.messagehandler = null;

        this.window = new JFrame("Battleship");

        // Fensteroptionen
        this.window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Haupt-Panel mit einem BorderLayout
        final JPanel mainPanel = new JPanel(new BorderLayout());

        // Panel für beide Spielfelder nebeneinander
        final JPanel gamePanel = new JPanel(new GridLayout(1, 2, 10, 10)); // 1 Zeile, 2 Spalten, Abstand 10px

        // Spielfeld des Spielers
        this.opponentField = new PlaygroundMatrix(this.rows, this.cols);
        gamePanel.add(this.opponentField);

        // Spielfeld des Gegners
        this.playersField = new PlaygroundMatrix(this.rows, this.cols);
        gamePanel.add(this.playersField);

        // Panel für Chat und Buttons
        final JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.Y_AXIS));

        // Textbox (JTextPane für farbigen Text)
        this.chatOutputPane = new JTextPane();
        this.chatOutputPane.setPreferredSize(new Dimension(300, 100));
        this.chatOutputPane.setEditable(false);

        this.chatSystemStyle = this.chatOutputPane.addStyle("system style", null);
        StyleConstants.setForeground(this.chatSystemStyle, new Color(179, 107, 0));

        this.chatUserStyle = this.chatOutputPane.addStyle("user style", null);
        StyleConstants.setForeground(this.chatUserStyle, Color.GRAY);

        this.chatPeerStyle = this.chatOutputPane.addStyle("peer style", null);
        StyleConstants.setForeground(this.chatPeerStyle, Color.BLACK);

        final JScrollPane chatScrollPane = new JScrollPane(this.chatOutputPane);
        controlPanel.add(chatScrollPane);

        // Panel für Textfeld und Send-Button
        final JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new BoxLayout(inputPanel, BoxLayout.X_AXIS));

        // Textfeld
        this.chatInputField = new JTextField();
        this.chatInputField.setMaximumSize(new Dimension(200, 30)); // Maximalgröße für das Textfeld
        inputPanel.add(this.chatInputField);

        // Send-Button
        this.sendButton = new JButton("Send");
        inputPanel.add(this.sendButton);

        // InputPanel hinzufügen
        controlPanel.add(inputPanel);

        // Abstand schaffen
        controlPanel.add(Box.createRigidArea(new Dimension(0, 20)));

        // Panel für Withdraw-Button und Computer-Button
        final JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));

        // Computer-Button
        this.computerButton = new JButton("Computer");
        buttonPanel.add(this.computerButton);

        // Abstand zwischen den Buttons
        buttonPanel.add(Box.createRigidArea(new Dimension(10, 0)));

        // Withdraw-Button
        this.withdrawButton = new JButton("Withdraw");
        buttonPanel.add(this.withdrawButton);

        // buttonPanel zu controlPanel hinzufügen
        controlPanel.add(buttonPanel);

        // Panels hinzufügen
        mainPanel.add(gamePanel, BorderLayout.CENTER); // Beide Spielfelder
        mainPanel.add(controlPanel, BorderLayout.EAST); // Chat und Buttons

        this.sendButton.addActionListener(e -> GameWindow.this.messageEvent());
        this.chatInputField.addActionListener(e -> {
            this.messageEvent();
        });
        this.withdrawButton.addActionListener(e -> GameWindow.this.withdrawEvent());
        this.computerButton.addActionListener(e -> GameWindow.this.computerMoveEvent());

        // Haupt-Panel zum Fenster hinzufügen
        this.window.add(mainPanel);
    }

    public void close() {
        this.window.dispose();
    }

    public void disableWithdraw() {
        this.withdrawButton.setEnabled(false);
    }

    public void enableComputerMove(boolean b) {
        this.computerButton.setEnabled(b);
    }

    public PlaygroundMatrix getOpponentField() {
        return this.opponentField;
    }

    public PlaygroundMatrix getPlayersField() {
        return this.playersField;
    }

    public void playersTurn(boolean b) {
        this.enableComputerMove(b);
    }

    public void setComputerMoveHandler(ClickHandler computermovehandler) {
        this.computermovehandler = computermovehandler;
    }

    public void setMessageHandler(MessageHandler messagehandler) {
        this.messagehandler = messagehandler;
    }

    public void setWithdrawHandler(ClickHandler withdrawhandler) {
        this.withdrawhandler = withdrawhandler;
    }

    public void show() {
        this.window.pack();
        this.window.setVisible(true);
    }

    public void writeMessage(String from, String text, Style style) {
        try {
            final StyledDocument sd = this.chatOutputPane.getStyledDocument();
            sd.insertString(sd.getLength(), text + "\n", style);
        } catch (final BadLocationException e) {
            this.logger.log(Level.SEVERE, "Error when displaying the text message", e);
        }
    }

    public void writeMessageFromPeer(String name, String text) {
        this.writeMessage(name, text, this.chatPeerStyle);
    }

    public void writeMessageFromSystem(String text) {
        this.writeMessage("System", text, this.chatSystemStyle);
    }

    public void writeMessageFromUser(String name, String text) {
        this.writeMessage(name, text, this.chatUserStyle);
    }

    private void computerMoveEvent() {
        this.playersTurn(false);
        if (this.computermovehandler != null) {
            try {
                new Thread() {
                    @Override
                    public void run() {
                        try {
                            GameWindow.this.computermovehandler.click();
                        } catch (final Exception e) {
                            GameWindow.this.logger.log(Level.SEVERE, "Error computer moving.", e);
                            SwingUtilities.invokeLater(() -> {
                                JOptionPane.showMessageDialog(
                                        GameWindow.this.window, "Error computer moving: " + e.getMessage(), "Error",
                                        JOptionPane.ERROR_MESSAGE
                                );
                            });
                        }
                    }
                }.start();
            } catch (final Exception e) {
                this.logger.log(Level.SEVERE, "Error", e);
                JOptionPane
                        .showMessageDialog(this.window, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void messageEvent() {
        this.chatInputField.setEnabled(false);
        this.sendButton.setEnabled(false);

        final String text = this.chatInputField.getText();
        this.chatInputField.setText("");
        this.chatInputField.setEnabled(true);
        this.sendButton.setEnabled(true);

        if (this.messagehandler != null && !text.isBlank()) {
            try {
                new Thread() {
                    @Override
                    public void run() {
                        try {
                            GameWindow.this.messagehandler.message(Utils.sanitizeString(text));
                        } catch (final Exception e) {
                            GameWindow.this.logger.log(Level.SEVERE, "Error connecting to the server.", e);
                            SwingUtilities.invokeLater(() -> {
                                JOptionPane.showMessageDialog(
                                        GameWindow.this.window, "Error connecting to the server: " + e.getMessage(),
                                        "Error", JOptionPane.ERROR_MESSAGE
                                );
                            });
                        }
                    }
                }.start();
            } catch (final Exception e) {
                this.logger.log(Level.SEVERE, "Error", e);
                JOptionPane
                        .showMessageDialog(this.window, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void withdrawEvent() {
        this.disableWithdraw();
        this.enableComputerMove(false);
        if (this.withdrawhandler != null) {
            try {
                new Thread() {
                    @Override
                    public void run() {
                        try {
                            GameWindow.this.withdrawhandler.click();
                        } catch (final Exception e) {
                            GameWindow.this.logger.log(Level.SEVERE, "Error withdrawing.", e);
                            SwingUtilities.invokeLater(() -> {
                                JOptionPane.showMessageDialog(
                                        GameWindow.this.window, "Error withdrawing: " + e.getMessage(), "Error",
                                        JOptionPane.ERROR_MESSAGE
                                );
                            });
                        }
                    }
                }.start();
            } catch (final Exception e) {
                this.logger.log(Level.SEVERE, "Error", e);
                JOptionPane
                        .showMessageDialog(this.window, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
