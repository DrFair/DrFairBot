package twitchBot;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class TwitchBotForm extends JFrame {

    private TwitchBot bot;

    private JPanel panel;
    private JTextPane console;
    private JScrollPane consoleScroll;
    private JTextField inputField;
    private JButton submitButton;
    private JList channelList;

    private StringBuilder htmlConsole;

    public TwitchBotForm(TwitchBot bot) {
        super("Fair Twitch Bot");
        this.bot = bot;
        setContentPane(panel);
        pack();
        setSize(500, 500);
        setVisible(true);

        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) { // Close event listener
                bot.disconnect();
            }
        });

        submitButton.addActionListener(e -> submitInputField()); // Button to send input

        inputField.addKeyListener(new KeyAdapter() { // Add enter key listener to send input
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    submitInputField();
                }
            }
        });

        inputField.requestFocus();

        htmlConsole = new StringBuilder("<html>");
    }

    private void submitInputField() {
        bot.submitCommand(inputField.getText());
        inputField.setText("");
        inputField.requestFocus();
    }

    public void writeConsoleLine(String message) {
        writeConsole(message, Color.BLACK);
        newLine();
    }

    public void writeConsole(String message, Color color) {
        String hexColor = String.format("%02X%02X%02X", color.getRed(), color.getGreen(), color.getBlue());
        htmlConsole.append("<span style=\"color:");
        htmlConsole.append(hexColor);
        htmlConsole.append("\">");
        htmlConsole.append(message);
        htmlConsole.append("</span>");
        updateConsole();
    }

    public void newLine() {
        htmlConsole.append("<br>");
        updateConsole();
    }

    private void updateConsole() {
        console.setText(htmlConsole.toString());
    }
}
