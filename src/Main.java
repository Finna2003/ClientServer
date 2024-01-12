import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class Main {

    private static final int PORT = 1546;
    private static final Map<String, ClientHandler> clientHandlers = new ConcurrentHashMap<>();
    private static final JTextArea textArea = new JTextArea();
    private static ServerSocket serverSocket;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Сервер");
            textArea.setEditable(false);
            frame.add(new JScrollPane(textArea), BorderLayout.CENTER);
            frame.setSize(300, 300);
            frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
            frame.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    try {
                        if (serverSocket != null && !serverSocket.isClosed()) {
                            serverSocket.close();
                        }
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                    System.exit(0);
                }
            });
            frame.setVisible(true);
        });

        try {
            serverSocket = new ServerSocket(PORT);
            System.out.println("Сервер запущено");
            SwingUtilities.invokeLater(() -> textArea.append("Сервер запущено\n"));
            while (true) {
                Socket clientSocket = serverSocket.accept();
                ClientHandler clientHandler = new ClientHandler(clientSocket);
                new Thread(clientHandler).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static class ClientHandler implements Runnable {
        private Socket socket;
        private String username;
        private BufferedReader in;
        private PrintWriter out;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);
                out.println("Введіть ім'я: ");
                username = in.readLine();
                sendMessage("Ваше ім'я - " + username);
                clientHandlers.put(username, this);
                SwingUtilities.invokeLater(() -> textArea.append(username + " приєднався до чату.\n"));
                String message;
                while ((message = in.readLine()) != null) {
                    final String finalUsername = username;
                    final String finalMessage = message;

                    if (message.startsWith("PrivateMessage:")) {
                        String[] tokens = message.split(":", 3);
                        if (tokens.length == 3) {
                            String recipient = tokens[1];
                            String privateMessage = tokens[2];
                            ClientHandler recipientHandler = clientHandlers.get(recipient);
                            if (recipientHandler != null) {
                                recipientHandler.sendMessage(username + " (private): " + privateMessage);
                            } else {
                                sendMessage("Користувача " + recipient + " не знайдено.");
                            }
                        } else {
                            sendMessage("Не правильний приватний формат.");
                        }
                    } else if (message.startsWith("Broadcast:")) {
                        String broadcastMessage = message.substring("Broadcast:".length()).trim();
                        sendToAll(finalUsername + " (всім): " + broadcastMessage);
                    } else {
                        SwingUtilities.invokeLater(() -> textArea.append(finalUsername + ": " + finalMessage + "\n"));
                        sendToAll(finalUsername + ": " + finalMessage);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                clientHandlers.remove(username);
                SwingUtilities.invokeLater(() -> textArea.append(username + " покинув чат.\n"));
            }
        }

        private void sendMessage(String message) {
            out.println(message);
        }
    }

    private static void sendToAll(String message) {
        for (ClientHandler handler : clientHandlers.values()) {
            handler.sendMessage(message);
        }
    }
}
