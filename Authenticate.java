package volter.com.example.appscrumteam;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.*;

public class Authenticate {

    @FXML
    private TextField EmailField, UsernameField, PwordField;

    @FXML
    private Button RegisterButton, LoginButton;

    private boolean isRegisterMode = true;
    private final DatabaseHandler dbHandler = new SQLDatabaseHandler();

    @FXML
    private void initialize() {
        switchToRegisterMode();
        setupButtonListeners();
    }

    private void setupButtonListeners() {
        RegisterButton.setOnAction(event -> {
            if (isRegisterMode) {
                String email = EmailField.getText().trim();
                String username = UsernameField.getText().trim();
                String password = PwordField.getText().trim();

                if (email.isEmpty() || username.isEmpty() || password.isEmpty()) {
                    showAlert("Please fill in all fields to register.");
                    return;
                }

                register(email, username, password);
                switchToLoginMode();
                showAlert("✅ Successfully registered! You can now login.");
            } else {
                switchToRegisterMode();
            }
        });

        LoginButton.setOnAction(event -> {
            if (isRegisterMode) {
                switchToLoginMode();
            } else {
                String email = EmailField.getText().trim();
                String password = PwordField.getText().trim();

                if (email.isEmpty() || password.isEmpty()) {
                    showAlert("Please enter both email and password.");
                    return;
                }

                String username = login(email, password);
                if (username != null) {
                    showAlert("✅ Login successful! Welcome, " + username);
                    loadChatbotScene();
                } else {
                    showAlert("❌ Login failed! Make a new account or try again.");
                }
            }
        });
    }

    private void switchToLoginMode() {
        isRegisterMode = false;
        UsernameField.setVisible(false);
        UsernameField.setManaged(false);
        RegisterButton.setText("Register");
        LoginButton.setText("Login");
    }

    private void switchToRegisterMode() {
        isRegisterMode = true;
        UsernameField.setVisible(true);
        UsernameField.setManaged(true);
        RegisterButton.setText("Register");
        LoginButton.setText("Login");
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void loadChatbotScene() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/chatbot.fxml"));
            Parent chatbotRoot = loader.load();
            Stage stage = (Stage) LoginButton.getScene().getWindow();
            stage.setScene(new Scene(chatbotRoot));
            stage.show();
        } catch (IOException e) {
            throw new RuntimeException("Failed to load chatbot.fxml", e);
        }
    }

    private void register(String emailx, String userx, String pwordx) {
        try (Connection connection = dbHandler.connect()) {
            String query = "INSERT INTO login (emailadress, username, pword) VALUES (?, ?, ?)";
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setString(1, emailx);
                statement.setString(2, userx);
                statement.setString(3, pwordx);
                int result = statement.executeUpdate();
                if (result > 0) {
                    System.out.println("[Registration successful]");
                }
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            showAlert("❌ Failed to register. Email may already be used.");
        }
    }

    private String login(String emailx, String pwordx) {
        String username = null;

        try (Connection connection = dbHandler.connect()) {
            String query = "SELECT username FROM login WHERE emailadress = ? AND pword = ?";
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setString(1, emailx);
                statement.setString(2, pwordx);
                try (ResultSet result = statement.executeQuery()) {
                    if (result.next()) {
                        username = result.getString("username");
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return username;
    }
}
