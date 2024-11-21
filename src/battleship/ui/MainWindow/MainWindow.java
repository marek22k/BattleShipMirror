package battleship.ui.MainWindow;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import battleship.Constants;
import battleship.terminal.TerminalWindow;
import battleship.utils.NetworkUtils;
import battleship.utils.SystemUtils;

public final class MainWindow {
    private final JFrame window;

    // Log panel
    private final DefaultListModel<String> ipListModel;
    private final JTextPane logTextPane;

    private final Style logErrorStyle;
    private final Style logWarningStyle;
    private final Style logInfoStyle;

    // Server panel
    private final JLabel serverLabel;
    private final JRadioButton serverModeRadioButton;
    private final JButton serverStartButton;
    private final JButton serverStopButton;
    private final JList<String> serverIpList;
    private final JButton updateSeverIpListButton;
    private final JLabel serverStatusLabel;
    private final JLabel serverPortLabel;
    private final JLabel serverIpListLabel;

    // Client panel
    private final JLabel clientLabel;
    private final JRadioButton clientModeRadioButton;
    private final JTextField clientHostnameField;
    private final JTextField clientPortField;
    private final JLabel clientHostnameLabel;
    private final JLabel clientPortLabel;
    private final JButton clientConnectButton;

    // Settings panel
    private final JLabel maxLevelLabel;
    private final JComboBox<Integer> levelComboBox;
    private final JLabel currentLevelLabel;
    private final JLabel nameLabel;
    private final JTextField nameField;

    private int counter;
    private ConnectHandler connecthandler;
    private ServerHandler serverStartHandler;
    private ServerHandler serverStopHandler;

    private final Logger logger;

    public MainWindow(AtomicBoolean sound) {
        this.logger = Logger.getLogger(MainWindow.class.getName());
        this.logger.setLevel(Constants.logLevel);

        this.window = new JFrame("Battleship");
        this.window.setLayout(new GridLayout(2, 2, 10, 10));
        this.counter = 0;

        final EmptyBorder padding = new EmptyBorder(10, 10, 10, 10);

        /*
         * Rand um die Kacheln. Sie sind Grau, haben eine Dicke von 1 und einen
         * abgerundeten Rand.
         */
        final LineBorder border = new LineBorder(Color.GRAY, 1, true);

        /* Server Kachel (oben links */
        final JPanel serverPanel = new JPanel();
        serverPanel.setBorder(BorderFactory.createCompoundBorder(padding, border));
        serverPanel.setLayout(new GridBagLayout());
        final GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        this.serverLabel = new JLabel("Server", JLabel.LEFT);
        this.serverModeRadioButton = new JRadioButton();
        this.serverModeRadioButton.setSelected(true);

        final JPanel innerServerPanel = new JPanel(new BorderLayout());
        innerServerPanel.add(this.serverLabel, BorderLayout.WEST);
        innerServerPanel.add(this.serverModeRadioButton, BorderLayout.EAST);

        this.serverStartButton = new JButton("Start");
        this.serverStopButton = new JButton("Stop");
        final JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttonPanel.add(this.serverStartButton);
        buttonPanel.add(this.serverStopButton);

        this.serverStatusLabel = new JLabel("Status: Stopped.");
        this.serverPortLabel = new JLabel("Port: ");
        this.serverIpListLabel = new JLabel("IP addresses:");
        this.ipListModel = new DefaultListModel<>();
        this.serverIpList = new JList<>(this.ipListModel);

        final JScrollPane scrollPane = new JScrollPane(this.serverIpList);
        scrollPane.setPreferredSize(new Dimension(300, 200));

        this.updateSeverIpListButton = new JButton("Update list");

        final JPanel updateListPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        updateListPanel.add(this.updateSeverIpListButton);

        gbc.gridx = 0;
        gbc.gridy = 0;
        serverPanel.add(innerServerPanel, gbc);
        gbc.gridy++;
        serverPanel.add(buttonPanel, gbc);
        gbc.gridy++;
        serverPanel.add(this.serverStatusLabel, gbc);
        gbc.gridy++;
        serverPanel.add(this.serverPortLabel, gbc);
        gbc.gridy++;
        serverPanel.add(this.serverIpListLabel, gbc);
        gbc.gridy++;
        serverPanel.add(scrollPane, gbc);
        gbc.gridy++;
        serverPanel.add(updateListPanel, gbc);

        // Einstellungs Kachel (oben rechts)
        final JPanel settingsPanel = new JPanel();
        settingsPanel.setBorder(BorderFactory.createCompoundBorder(padding, border));
        settingsPanel.setLayout(new GridLayout(4, 1));

        final JPanel namePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        this.nameLabel = new JLabel("Name: ");
        this.nameField = new JTextField(10);
        namePanel.add(this.nameLabel);
        namePanel.add(this.nameField);

        final JPanel levelPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        this.currentLevelLabel = new JLabel("Current level: ");
        this.levelComboBox = new JComboBox<>();
        levelPanel.add(this.currentLevelLabel);
        levelPanel.add(this.levelComboBox);

        this.maxLevelLabel = new JLabel();

        final JCheckBox soundCheckbox = new JCheckBox("Sound");
        soundCheckbox.setEnabled(Constants.SOUND);
        soundCheckbox.setSelected(Constants.SOUND);

        settingsPanel.add(namePanel);
        settingsPanel.add(soundCheckbox);
        settingsPanel.add(levelPanel);
        settingsPanel.add(this.maxLevelLabel);

        // Client Kachel (unten links)
        final JPanel clientPanel = new JPanel();
        clientPanel.setBorder(BorderFactory.createCompoundBorder(padding, border));
        clientPanel.setLayout(new GridBagLayout());

        this.clientLabel = new JLabel("Client", JLabel.LEFT);
        this.clientModeRadioButton = new JRadioButton();

        final JPanel innerClientPanel = new JPanel(new BorderLayout());
        innerClientPanel.add(this.clientLabel, BorderLayout.WEST);
        innerClientPanel.add(this.clientModeRadioButton, BorderLayout.EAST);

        this.clientHostnameField = new JTextField(10);
        this.clientPortField = new JTextField(5);

        final JPanel hostPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        this.clientHostnameLabel = new JLabel("Hostname:");
        hostPanel.add(this.clientHostnameLabel);
        hostPanel.add(this.clientHostnameField);

        final JPanel portPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        this.clientPortLabel = new JLabel("Port:");
        portPanel.add(this.clientPortLabel);
        portPanel.add(this.clientPortField);

        this.clientConnectButton = new JButton("Connect");
        this.clientConnectButton.addActionListener(e -> MainWindow.this.onConnect());
        final JPanel connectButtonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        connectButtonPanel.add(this.clientConnectButton);

        gbc.gridx = 0;
        gbc.gridy = 0;
        clientPanel.add(innerClientPanel, gbc);
        gbc.gridy++;
        clientPanel.add(hostPanel, gbc);
        gbc.gridy++;
        clientPanel.add(portPanel, gbc);
        gbc.gridy++;
        clientPanel.add(connectButtonPanel, gbc);

        // Log Kachel (unten rechts)
        final JPanel logPanel = new JPanel();
        logPanel.setBorder(BorderFactory.createCompoundBorder(padding, border));
        logPanel.setLayout(new BorderLayout());
        final JLabel logLabel = new JLabel("Log:");
        this.logTextPane = new JTextPane();
        this.logTextPane.setEditable(false);

        this.logErrorStyle = this.logTextPane.addStyle("error style", null);
        StyleConstants.setForeground(this.logErrorStyle, Color.RED);

        this.logWarningStyle = this.logTextPane.addStyle("warning style", null);
        StyleConstants.setForeground(this.logWarningStyle, Color.ORANGE);

        this.logInfoStyle = this.logTextPane.addStyle("info style", null);
        StyleConstants.setForeground(this.logInfoStyle, Color.DARK_GRAY);

        final JScrollPane logScrollPane = new JScrollPane(this.logTextPane);

        logPanel.add(logLabel, BorderLayout.NORTH);
        logPanel.add(logScrollPane, BorderLayout.CENTER);

        // Radiobuttons verknüpfen
        final ButtonGroup serverClientGroup = new ButtonGroup();
        serverClientGroup.add(this.serverModeRadioButton);
        serverClientGroup.add(this.clientModeRadioButton);

        this.serverLabel.setLabelFor(serverPanel);
        this.clientLabel.setLabelFor(clientPanel);
        logLabel.setLabelFor(logPanel);
        this.clientHostnameLabel.setLabelFor(this.clientHostnameField);
        this.clientPortLabel.setLabelFor(this.clientPortField);
        this.serverIpListLabel.setLabelFor(this.serverIpList);

        // Aktiviere den Client Mode, wenn er angeklickt wird
        innerClientPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                MainWindow.this.clientModeRadioButton.setSelected(true);
            }
        });

        // Aktiviere den Server Mode, wenn er angeklickt wird
        innerServerPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                MainWindow.this.serverModeRadioButton.setSelected(true);
            }
        });

        // Aktuallisiere auf Wunsch des Nutzers, wenn es den Button anklickt, die Liste
        // mit IP-Adressen
        this.updateSeverIpListButton.addActionListener(e -> MainWindow.this.updateIpAddresses());

        // Kopiere die ausgewählte IP-Adresse des Server bei einem Doppelklick in die
        // Zwischenablage
        this.serverIpList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) { // Doppelklick
                    final String selectedIp = MainWindow.this.serverIpList.getSelectedValue();
                    if (selectedIp != null) {
                        SystemUtils.copyToClipboard(selectedIp);
                        MainWindow.this.logger.log(Level.INFO, "Copied IP address to clipboard: " + selectedIp);
                    }
                }
            }
        });

        // Wenn ein Modus umgeschaltet wird, wird der andere Modus deaktiviert und damit
        // auch deine GUI-Elemente
        this.clientModeRadioButton.addItemListener(e -> MainWindow.this.updateMode());

        this.serverModeRadioButton.addItemListener(e -> MainWindow.this.updateMode());

        this.serverStartButton.addActionListener(e -> {
            this.startServer();
        });

        this.serverStopButton.addActionListener(e -> {
            this.logger.log(Level.INFO, "Server stop requested.");
            this.stopServer();
        });

        soundCheckbox.addItemListener(e -> {
            if (e.getStateChange() == 1) {
                sound.set(true);
            } else {
                sound.set(false);
            }
        });

        // Kacheln dem Hauptfenster hinzufügen
        this.window.add(serverPanel);
        this.window.add(settingsPanel);
        this.window.add(clientPanel);
        this.window.add(logPanel);

        // Fensteroptionen
        this.window.setSize(750, 930);
        this.window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        this.enable(false);
    }

    /**
     * De- bzw. aktiviert die Möglichkeit Einstellungen (Client, Server und Settings
     * Panel) vorzunehmen.
     *
     * @param enabled
     */
    public void enable(boolean enabled) {
        this.enableSettings(enabled);
        this.enableModeSettings(enabled);

        if (enabled) {
            this.updateMode();
        } else {
            this.enableServer(false);
            this.enableClient(false);
        }
    }

    /**
     * Zeigt das Fenster an.
     */
    public void enableClient(boolean enabled) {
        this.clientHostnameField.setEnabled(enabled);
        this.clientPortField.setEnabled(enabled);
        this.clientHostnameLabel.setEnabled(enabled);
        this.clientPortLabel.setEnabled(enabled);
        this.clientConnectButton.setEnabled(enabled);
    }

    /**
     * De- bzw. aktiviert die Möglichkeit zwischen Client und Server Mode zu
     * wechseln.
     *
     * @param enabled
     */
    public void enableModeSettings(boolean enabled) {
        this.serverModeRadioButton.setEnabled(enabled);
        this.clientModeRadioButton.setEnabled(enabled);
        this.serverLabel.setEnabled(enabled);
        this.clientLabel.setEnabled(enabled);
    }

    /**
     * De- bzw. aktiviert das Server Panel.
     *
     * @param enabled
     */
    public void enableServer(boolean enabled) {
        if (!enabled) {
            /*
             * Geht nur in eine Richtung. Wenn der Server Mode aktiviert ist, bedeutet dies
             * nicht, dass der Server auch gestartet ist und daher gestoppt werden kann
             */
            this.serverStopButton.setEnabled(enabled);
        }
        this.serverStartButton.setEnabled(enabled);
        this.updateSeverIpListButton.setEnabled(enabled);
        this.serverIpList.setEnabled(enabled);
        this.serverStatusLabel.setEnabled(enabled);
        this.serverPortLabel.setEnabled(enabled);
        this.serverIpListLabel.setEnabled(enabled);
    }

    /**
     * De- bzw. aktiviert das Einstellungs Panel.
     *
     * @param enabled
     */
    public void enableSettings(boolean enabled) {
        this.currentLevelLabel.setEnabled(enabled);
        this.levelComboBox.setEnabled(enabled);
        this.maxLevelLabel.setEnabled(enabled);
        this.nameLabel.setEnabled(enabled);
        this.nameField.setEnabled(enabled);
    }

    public void firstInit() {
        this.updateIpAddresses();
        this.updateLevels(1);
        this.enable(true);
    }

    public Handler getLogHandler() {
        final Handler handler = new Handler() {
            @Override
            public void close() throws SecurityException {

            }

            @Override
            public void flush() {

            }

            @Override
            public void publish(LogRecord record) {
                if (this.isLoggable(record)) {
                    final String message = this.getFormatter().format(record);
                    if (record.getLevel() == java.util.logging.Level.SEVERE) {
                        SwingUtilities.invokeLater(() -> MainWindow.this.logError(message));
                    } else if (record.getLevel() == java.util.logging.Level.WARNING) {
                        SwingUtilities.invokeLater(() -> MainWindow.this.logWarning(message));
                    } else {
                        SwingUtilities.invokeLater(() -> MainWindow.this.logInfo(message));
                    }
                }
            }
        };
        handler.setFormatter(new SimpleFormatter());
        return handler;
    }

    /**
     * De- bzw. aktiviert das Client Panel.
     *
     * @param enabled
     */

    public String getName() {
        return this.nameField.getText();
    }

    public Integer getSelectedLevel() {
        return (Integer) this.levelComboBox.getSelectedItem();
    }

    public void log(String text, Style style) {
        try {
            final StyledDocument sd = this.logTextPane.getStyledDocument();
            sd.insertString(sd.getLength(), text, style);
        } catch (final BadLocationException e) {
            final String message = "An error has occurred during logging into the log area.";
            if (!text.contains(message)) {
                /* Avoid recursive logging */
                this.logger.log(Level.SEVERE, message, e);
            }
        }
    }

    public void logError(String text) {
        this.log(text, this.logErrorStyle);
    }

    public void logInfo(String text) {
        this.log(text, this.logInfoStyle);
    }

    public void logWarning(String text) {
        this.log(text, this.logWarningStyle);
    }

    public void setConnectHandler(ConnectHandler connecthandler) {
        this.connecthandler = connecthandler;
    }

    public void setServerStartHandler(ServerHandler serverStartHandler) {
        this.serverStartHandler = serverStartHandler;
    }

    public void setServerStopHandler(ServerHandler serverStopHandler) {
        this.serverStopHandler = serverStopHandler;
    }

    public void show() {
        this.window.setVisible(true);
    }

    public void startServer() {
        try {
            this.logger.log(Level.INFO, "Start server.");
            this.serverStartButton.setEnabled(false);
            this.serverStopButton.setEnabled(true);
            this.updateServerStatus("Started.");

            if (this.serverStartHandler != null) {
                this.logger.log(Level.FINE, "Run server start handler.");
                new Thread() {
                    @Override
                    public void run() {
                        try {
                            MainWindow.this.serverStartHandler.handle();
                        } catch (final Exception e) {
                            MainWindow.this.logger.log(Level.SEVERE, "Error starting server.", e);
                            SwingUtilities.invokeLater(() -> {
                                JOptionPane.showMessageDialog(
                                        MainWindow.this.window, "Error: " + e.getMessage(), "Error",
                                        JOptionPane.ERROR_MESSAGE
                                );
                                MainWindow.this.stopServer();
                            });
                        }
                    }
                }.start();
            }
        } catch (final Exception e) {
            this.logger.log(Level.SEVERE, "Error", e);
            JOptionPane.showMessageDialog(this.window, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            this.stopServer();
        }
    }

    public void stopServer() {
        try {
            this.logger.log(Level.INFO, "Stop server.");
            this.serverStartButton.setEnabled(true);
            this.serverStopButton.setEnabled(false);
            this.updateServerStatus("Stopped.");

            if (this.serverStartHandler != null) {
                new Thread() {
                    @Override
                    public void run() {
                        try {
                            MainWindow.this.serverStopHandler.handle();
                        } catch (final Exception e) {
                            MainWindow.this.logger.log(Level.SEVERE, "Error stopping the server.", e);
                            SwingUtilities.invokeLater(() -> {
                                JOptionPane.showMessageDialog(
                                        MainWindow.this.window, "Error stopping the server: " + e.getMessage(), "Error",
                                        JOptionPane.ERROR_MESSAGE
                                );
                                MainWindow.this.enable(true);
                            });
                        }
                    }
                }.start();
            }
        } catch (final Exception e) {
            this.logger.log(Level.SEVERE, "Error", e);
            JOptionPane.showMessageDialog(this.window, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            this.enable(true);
        }
    }

    /**
     * Aktuallisiert die Liste der angezeigten IP-Adressen.
     */
    public void updateIpAddresses() {
        try {
            final ArrayList<String> ipAddresses = NetworkUtils.getIpAddresses();
            this.ipListModel.clear();
            for (final String address : ipAddresses) {
                this.ipListModel.addElement(address);
            }
            this.logger.log(Level.INFO, "IP addresses updated.");
        } catch (final Exception e) {
            this.logger.log(Level.SEVERE, "Error when updating the IP addresses.", e);
        }
    }

    /**
     * Aktuallisiert die möglichen Level.
     *
     * @param maxLevel Das maximale Level, welches wählbar sein soll.
     */
    public void updateLevels(int maxLevel) {
        this.levelComboBox.removeAllItems();
        for (int i = 1; i <= maxLevel; i++) {
            this.levelComboBox.addItem(i);
        }
        this.maxLevelLabel.setText("Current max level: " + maxLevel);
    }

    /**
     * Wechselt von Client zum Server Modus bzw. andersherum, je nach Nutzerauswahl.
     */
    public void updateMode() {
        final boolean clientMode = this.clientModeRadioButton.isSelected();
        this.enableServer(!clientMode);
        this.enableClient(clientMode);

        this.counter++;
        if (this.counter > 12) {
            new TerminalWindow().show();
            this.counter = 0;
        }
    }

    public void updateServerPort(String text) {
        this.serverPortLabel.setText("Port: " + text);
    }

    /**
     * Setzt den Serverstatus zu einem bestimmten Text
     *
     * @param text
     */
    public void updateServerStatus(String text) {
        this.serverStatusLabel.setText("Status: " + text);
    }

    private void onConnect() {
        this.enable(false);
        String hostname;
        int port;
        try {
            hostname = this.clientHostnameField.getText();
            port = Integer.parseInt(this.clientPortField.getText());

            new Thread() {
                @Override
                public void run() {
                    try {
                        MainWindow.this.connecthandler.connect(hostname, port);
                    } catch (final Exception e) {
                        MainWindow.this.logger.log(Level.SEVERE, "Error connecting to the server.", e);
                        SwingUtilities.invokeLater(() -> {
                            JOptionPane.showMessageDialog(
                                    MainWindow.this.window, "Error connecting to the server: " + e.getMessage(),
                                    "Error", JOptionPane.ERROR_MESSAGE
                            );
                            MainWindow.this.enable(true);
                        });
                    }
                }
            }.start();
        } catch (final Exception e) {
            this.logger.log(Level.SEVERE, "Error", e);
            JOptionPane.showMessageDialog(this.window, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            this.enable(true);
        }
    }
}
