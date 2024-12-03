import java.awt.event.*;
import java.io.*;
import java.net.*;
import javax.swing.*;
import java.awt.*;
import javax.swing.Timer;
import java.util.*;

public class SocialClient {
    private Socket socket;
    private PrintWriter writer;
    private BufferedReader reader;

    // Constructor initializes the client and attempts to connect to the server.
    public SocialClient(String address, int port) {
        try {
            socket = new Socket(address, port);
            writer = new PrintWriter(socket.getOutputStream(), true);
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            System.out.println("Successfully connected to the server.");
        } catch (IOException io) {
            System.out.println("Failed to connect to the server: " + io.getMessage());
            socket = null;
        }
    }

    // Sends a request to the server with action, caller, and data.
    public String sendRequest(String action, String caller, String data) {
        try {
            String requestString = action + " ; " + caller + " ; " + (data != null ? data : "");
            writer.println(requestString);
            return reader.readLine();
        } catch (IOException e) {
            System.out.println("Error encountered while sending request to the server: " + e.getMessage());
            return "Error: Could not send request.";
        }
    }

    // Overloaded sendRequest method for actions without a caller
    public String sendRequest(String action, String data) {
        try {
            String requestString = action + " ; ; " + (data != null ? data : "");
            writer.println(requestString);
            return reader.readLine();
        } catch (IOException e) {
            System.out.println("Error encountered while sending request to the server: " + e.getMessage());
            return "Error: Could not send request.";
        }
    }

    // Closes the client connection gracefully
    public void closeConnection() {
        try {
            if (reader != null)
                reader.close();
            if (writer != null)
                writer.close();
            if (socket != null)
                socket.close();
            System.out.println("Connection closed gracefully.");
        } catch (IOException e) {
            System.out.println("Encountered error while closing connection: " + e.getMessage());
        }
    }

    // Main method to run the client application
    public static void main(String[] args) {
        // Create a new SocialClient instance with server details.
        SocialClient client = new SocialClient("127.0.0.1", 4242); // Replace with actual server address and port

        // If connection to the server fails, show an error dialog and exit.
        if (client.socket == null) {
            JOptionPane.showMessageDialog(null, "Failed to connect to server.", "Connection Error",
                    JOptionPane.ERROR_MESSAGE);
            return; // Exit the application
        }

        // Initialize the main GUI
        SwingUtilities.invokeLater(() -> createInitialGUI(client));
    }

    // Creates the initial GUI with Login and Create Account buttons
    private static void createInitialGUI(SocialClient client) {
        JFrame frame = new JFrame("Social Client");
        frame.setSize(300, 150);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null); // Center the window

        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(2, 1, 10, 10));

        JButton loginButton = new JButton("Login");
        JButton createAccountButton = new JButton("Create Account");

        panel.add(loginButton);
        panel.add(createAccountButton);

        frame.add(panel);
        frame.setVisible(true);

        // Action Listener for Login Button
        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                frame.dispose(); // Close the initial window
                performLogin(client);
            }
        });

        // Action Listener for Create Account Button
        createAccountButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                frame.dispose(); // Close the initial window
                performCreateAccount(client);
            }
        });
    }

    // Performs the login process
    private static void performLogin(SocialClient client) {
        JPanel panel = new JPanel(new GridLayout(2, 2, 5, 5));

        JLabel userLabel = new JLabel("Username:");
        JTextField userField = new JTextField();
        JLabel passLabel = new JLabel("Password:");
        JPasswordField passField = new JPasswordField();

        panel.add(userLabel);
        panel.add(userField);
        panel.add(passLabel);
        panel.add(passField);

        int result = JOptionPane.showConfirmDialog(null, panel, "Login",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            String username = userField.getText().trim();
            String password = new String(passField.getPassword()).trim();

            if (username.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(null, "Username and password cannot be empty.", "Input Error",
                        JOptionPane.ERROR_MESSAGE);
                performLogin(client); // Retry login
                return;
            }

            // Send login request
            String loginResponse = client.sendRequest("loginWithPassword", username, password);

            if ("Login successful".equalsIgnoreCase(loginResponse.trim())) {
                JOptionPane.showMessageDialog(null, "Login Successful!", "Welcome",
                        JOptionPane.INFORMATION_MESSAGE);
                showChatList(client, username);
            } else if (loginResponse.contains("Incorrect Password") ||
                    loginResponse.contains("Input Error")) {
                JOptionPane.showMessageDialog(null, "Incorrect Username or Password. Please try again.", "Login Failed",
                        JOptionPane.ERROR_MESSAGE);
                createInitialGUI(client); // Return to initial GUI
            } else if (loginResponse.contains("User Not Found") ||
                    loginResponse.contains("User Error")) {
                JOptionPane.showMessageDialog(null, "User not found. Please create an account.", "Login Failed",
                        JOptionPane.ERROR_MESSAGE);
                createInitialGUI(client); // Return to initial GUI
            } else {
                JOptionPane.showMessageDialog(null, "Unexpected server response: " + loginResponse, "Server Error",
                        JOptionPane.ERROR_MESSAGE);
                createInitialGUI(client); // Return to initial GUI
            }
        } else {
            createInitialGUI(client); // Return to initial GUI if canceled
        }
    }

    // Performs the account creation process
    private static void performCreateAccount(SocialClient client) {
        JPanel panel = new JPanel(new GridLayout(2, 2, 5, 5));

        JLabel userLabel = new JLabel("Username:");
        JTextField userField = new JTextField();
        JLabel passLabel = new JLabel("Password:");
        JPasswordField passField = new JPasswordField();

        panel.add(userLabel);
        panel.add(userField);
        panel.add(passLabel);
        panel.add(passField);

        int result = JOptionPane.showConfirmDialog(null, panel, "Create Account",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            String username = userField.getText().trim();
            String password = new String(passField.getPassword()).trim();

            if (username.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(null, "Username and password cannot be empty.", "Input Error",
                        JOptionPane.ERROR_MESSAGE);
                performCreateAccount(client); // Retry account creation
                return;
            }

            if (password.length() < 6) {
                JOptionPane.showMessageDialog(null, "Password must be at least 6 characters long.", "Password Error",
                        JOptionPane.ERROR_MESSAGE);
                performCreateAccount(client); // Retry account creation
                return;
            }

            // Prepare data: username | password | default_pfp | default_bio
            String data = String.format("%s | %s | %s | %s", username, password, "Database/ProfilePicture/default.png",
                    "default bio");
            String createResponse = client.sendRequest("createUser", "", data);

            if ("User created successfully".equalsIgnoreCase(createResponse.trim())) {
                JOptionPane.showMessageDialog(null, "Account created successfully! You can now log in.", "Success",
                        JOptionPane.INFORMATION_MESSAGE);
                createInitialGUI(client); // Return to initial GUI
            } else if (createResponse.contains("Error") || createResponse.contains("Input Error")) {
                JOptionPane.showMessageDialog(null, createResponse, "Creation Failed",
                        JOptionPane.ERROR_MESSAGE);
                performCreateAccount(client); // Retry account creation
            } else {
                JOptionPane.showMessageDialog(null, "Unexpected server response: " + createResponse, "Server Error",
                        JOptionPane.ERROR_MESSAGE);
                performCreateAccount(client); // Retry account creation
            }
        } else {
            createInitialGUI(client); // Return to initial GUI if canceled
        }
    }

    // Displays the chat list after successful login
    private static void showChatList(SocialClient client, String username) {
        // Send request to get the list of chat users
        String response = client.sendRequest("getChatList", username, "");
        String[] chatUsers = response.trim().split(" \\| ");

        JFrame frame = new JFrame("Chats - " + username);
        frame.setSize(400, 600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null); // Center the window

        JPanel panel = new JPanel(new BorderLayout());

        DefaultListModel<String> listModel = new DefaultListModel<>();
        for (String user : chatUsers) {
            if (!user.trim().isEmpty()) {
                listModel.addElement(user.trim());
            }
        }

        JList<String> chatList = new JList<>(listModel);
        chatList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        chatList.setFont(new Font("Arial", Font.PLAIN, 18));

        JScrollPane scrollPane = new JScrollPane(chatList);

        JButton messageButton = new JButton("Message Someone");
        messageButton.setFont(new Font("Arial", Font.PLAIN, 16));
        messageButton.addActionListener(e -> {
            String targetUser = JOptionPane.showInputDialog(frame, "Enter username to message:");
            if (targetUser != null && !targetUser.trim().isEmpty()) {
                openChatWindow(client, username, targetUser.trim());
            }
        });

        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(messageButton, BorderLayout.SOUTH);

        chatList.addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting()) {
                String selectedUser = chatList.getSelectedValue();
                if (selectedUser != null) {
                    openChatWindow(client, username, selectedUser);
                }
            }
        });

        frame.add(panel);
        frame.setVisible(true);
    }

    // Opens the chat window with the selected user
    private static void openChatWindow(SocialClient client, String username, String targetUser) {
        JFrame chatFrame = new JFrame("Chat with " + targetUser);
        chatFrame.setSize(400, 500);
        chatFrame.setLocationRelativeTo(null); // Center the window

        JPanel panel = new JPanel(new BorderLayout());

        JTextArea chatArea = new JTextArea();
        chatArea.setEditable(false);
        chatArea.setLineWrap(true);
        chatArea.setWrapStyleWord(true);
        chatArea.setFont(new Font("Arial", Font.PLAIN, 14));

        JScrollPane scrollPane = new JScrollPane(chatArea);

        JTextField messageField = new JTextField();
        messageField.setFont(new Font("Arial", Font.PLAIN, 14));

        JButton sendButton = new JButton("Send");
        sendButton.setFont(new Font("Arial", Font.PLAIN, 14));
        sendButton.addActionListener(e -> {
            String message = messageField.getText().trim();
            if (!message.isEmpty()) {
                String data = String.format("%s | %s", targetUser, message);
                String response = client.sendRequest("sendMessage", username, data);
                if (response.toLowerCase().contains("error")) {
                    JOptionPane.showMessageDialog(chatFrame, response, "Error", JOptionPane.ERROR_MESSAGE);
                } else {
                    // Append the message to the chat area
                    chatArea.append("Me: " + message + "\n");
                    messageField.setText("");
                }
            }
        });

        JPanel inputPanel = new JPanel(new BorderLayout());
        inputPanel.add(messageField, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);

        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(inputPanel, BorderLayout.SOUTH);

        chatFrame.add(panel);
        chatFrame.setVisible(true);

        // Load existing messages
        loadChatHistory(client, username, targetUser, chatArea);

        // Start a timer to poll for new messages every 3 seconds
        Timer timer = new Timer(3000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                loadChatHistory(client, username, targetUser, chatArea);
            }
        });
        timer.start();

        // Stop the timer when the window is closed
        chatFrame.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                timer.stop();
            }
        });
    }

    // Loads the chat history between the user and target user
    private static void loadChatHistory(SocialClient client, String username, String targetUser, JTextArea chatArea) {
        String data = targetUser;
        String response = client.sendRequest("getMessage", username, data);
        if (response.toLowerCase().contains("error")) {
            // Do nothing or display an error message if needed
        } else {
            // Clear the chat area and display messages
            chatArea.setText("");
            String[] messages = response.split(" ; ");
            for (String msg : messages) {
                String[] parts = msg.split("-\\d+#");
                if (parts.length >= 2) {
                    String messageText = parts[0];
                    String messageType = parts[1].replace("#", "");
                    if (messageType.equals("S")) {
                        chatArea.append("Me: " + messageText + "\n");
                    } else if (messageType.equals("R")) {
                        chatArea.append(targetUser + ": " + messageText + "\n");
                    }
                }
            }
        }
    }
}
