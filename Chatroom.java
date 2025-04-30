package volter.com.example.appscrumteam;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.event.ActionEvent;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Chatroom {

    @FXML
    private VBox chatContainer;

    @FXML
    private ScrollPane chatScrollPane;

    @FXML
    private TextField Input2Field;

    @FXML
    private Button SendButton;

    @FXML
    private Button deleteMessageButton;

    @FXML
    private Button showTrioButton;

    @FXML
    private Button ClearButton;

    @FXML
    private Button Switch2Button;

    @FXML
    private Button showDecisionsButton;

    private List<addBox2Messages> chatMessages = new ArrayList<>();
    private List<String> markedDecisions = new ArrayList<>();

    private final String URL = "jdbc:mysql://localhost:3306/chatbotdb";
    private final String USER = "root";
    private final String PASSWORD = "Nc6xF76b!";

    private boolean isRetrievingTrio = false;

    @FXML
    public void initialize() {
        SendButton.setOnAction(e -> sendMessage());
        deleteMessageButton.setOnAction(e -> deleteAllMessages());
        showTrioButton.setOnAction(e -> handleShowTrio());
        ClearButton.setOnAction(e -> clearChat());
        showDecisionsButton.setOnAction(e -> showMarkedDecisions());
        loadMessages();
    }

    @FXML
    private void handleShowTrio() {
        addSystemMessage("\u2139\ufe0f Enter the Trio ID and press send to display the trio.");
        isRetrievingTrio = true;
    }

    @FXML
    private void SwitchScreen(ActionEvent event) {
        try {
            Parent newRoot = FXMLLoader.load(getClass().getResource("/chatbot.fxml"));
            Scene newScene = new Scene(newRoot);
            Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            currentStage.setScene(newScene);
            currentStage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendMessage() {
        String inputText = Input2Field.getText().trim();
        if (inputText.isEmpty()) return;

        if (isRetrievingTrio) {
            handleRetrieveTrio(inputText);
            Input2Field.clear();
            return;
        }

        saveMessageToDatabase("You: " + inputText);
        addUserMessage(inputText);
        Input2Field.clear();
    }

    private void handleRetrieveTrio(String inputText) {
        int requestedId;
        try {
            requestedId = Integer.parseInt(inputText);
        } catch (NumberFormatException e) {
            addSystemMessage("\u26a0\ufe0f Invalid ID format. Please enter a number.");
            isRetrievingTrio = false;
            return;
        }

        StringBuilder trioMessage = new StringBuilder();

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
            String query = "SELECT * FROM epic_user_story_task_trios WHERE id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setInt(1, requestedId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        String epicDesc = rs.getString("epic_description");
                        String storyDesc = rs.getString("user_story_description");
                        String taskDesc = rs.getString("task_description");

                        trioMessage.append("Epic: ").append(epicDesc).append("\n")
                                .append("User Story: ").append(storyDesc).append("\n")
                                .append("Task: ").append(taskDesc).append("\n");
                    } else {
                        trioMessage.append("\u26a0\ufe0f No trio found with ID ").append(requestedId).append(".\n");
                    }
                }
            }

            saveMessageToDatabase(trioMessage.toString());
            addSystemMessage(trioMessage.toString());

        } catch (SQLException e) {
            addSystemMessage("\u26a0\ufe0f Error retrieving trio: " + e.getMessage());
            e.printStackTrace();
        }

        isRetrievingTrio = false;
    }

    private void addUserMessage(String text) {
        UserMessage userMessage = new UserMessage(text);
        chatMessages.add(userMessage);

        HBox messageBox = new HBox(10);
        Label label = new Label("You: " + text);
        label.setStyle("-fx-text-fill: white;");
        CheckBox checkBox = new CheckBox();

        checkBox.setOnAction(event -> {
            if (checkBox.isSelected()) {
                markedDecisions.add(text);
            } else {
                markedDecisions.remove(text);
            }
        });

        messageBox.getChildren().addAll(label, checkBox);
        chatContainer.getChildren().add(messageBox);
        scrollToBottom();
    }

    private void addSystemMessage(String text) {
        SystemMessage systemMessage = new SystemMessage(text);
        chatMessages.add(systemMessage);

        HBox messageBox = new HBox(10);
        Label label = new Label(text);
        label.setStyle("-fx-text-fill: #888;");
        messageBox.getChildren().add(label);

        chatContainer.getChildren().add(messageBox);
        scrollToBottom();
    }

    private void scrollToBottom() {
        chatScrollPane.layout();
        chatScrollPane.setVvalue(1.0);
    }

    private void saveMessageToDatabase(String message) {
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
            String sql = "INSERT INTO chat (message) VALUES (?)";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, message);
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadMessages() {
        chatContainer.getChildren().clear();
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
            String sql = "SELECT message FROM chat ORDER BY id ASC";
            try (PreparedStatement stmt = conn.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String message = rs.getString("message");
                    if (message.startsWith("You: ")) {
                        addUserMessage(message.substring(4).trim());
                    } else {
                        addSystemMessage(message);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void deleteAllMessages() {
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
            String sql = "DELETE FROM chat";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.executeUpdate();
            }

            String deleteTrioQuery = "DELETE FROM epic_user_story_task_trios";
            try (PreparedStatement stmt = conn.prepareStatement(deleteTrioQuery)) {
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        chatContainer.getChildren().clear();
    }

    private void clearChat() {
        chatContainer.getChildren().clear();
    }

    @FXML
    private void showMarkedDecisions() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/lastDesicion.fxml"));
            Parent root = loader.load();

            LastDesicionController controller = loader.getController();
            controller.setDecisions(markedDecisions);

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Gemarkeerde Beslissingen");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}