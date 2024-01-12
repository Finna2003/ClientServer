import java.io.*;
import java.net.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class Client3 {

    private static final String SERVER_ADDRESS = "localhost";
    private static final int SERVER_PORT = 1546;
    private static JTextArea textArea;
    private static JTextField inputField;
    private static JTextField privateRecipientField; // Нове поле для введення отримувача
    private static JButton sendButton; // Нова кнопка для відправлення

    private static PrintWriter out;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Клієнт");
            frame.setSize(400, 300);

            textArea = new JTextArea(15, 30);
            textArea.setEditable(false);

            JScrollPane scrollPane = new JScrollPane(textArea);
            frame.add(scrollPane, BorderLayout.CENTER);

            JPanel inputPanel = new JPanel();
            inputField = new JTextField(20);
            privateRecipientField = new JTextField(10); // Поле для введення отримувача
            sendButton = new JButton("Надіслати");

            sendButton.addActionListener(e -> {
                String message = inputField.getText();
                String recipient = privateRecipientField.getText(); // Отримувач приватного повідомлення

                // Додайте логіку для визначення, чи є це приватне повідомлення чи ні
                if (recipient.isEmpty()) {
                    sendMessage(message);
                } else {
                    sendPrivateMessage(message, recipient);
                }

                inputField.setText("");
                privateRecipientField.setText("");
            });


            inputPanel.add(inputField);
            inputPanel.add(new JLabel("Отримувач: ")); // Мітка для поля отримувача
            inputPanel.add(privateRecipientField);
            inputPanel.add(sendButton);

            frame.add(inputPanel, BorderLayout.SOUTH);
            frame.pack();
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setVisible(true);
        });

        connectToServer();
    }

    private static void connectToServer() {
        try {
            Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            String serverMessage;
            while ((serverMessage = in.readLine()) != null) {
                final String message = serverMessage;
                SwingUtilities.invokeLater(() -> textArea.append(message + "\n"));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void sendMessage(String message) {
        out.println(message);
    }

    private static void sendPrivateMessage(String message, String recipient) {
        out.println("PrivateMessage:" + recipient + ":" + message);
    }

    private static void sendToAll(String message) {
        out.println("Broadcast:" + message);
    }
}
