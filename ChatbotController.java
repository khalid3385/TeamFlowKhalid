package volter.com.example.appscrumteam;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.event.ActionEvent;
import java.sql.*;

public class ChatbotController {

    @FXML
    private TextArea chatArea;

    @FXML
    private TextField userInput;

    @FXML
    private Button sendButton;

    @FXML
    private Button clearButton;

    @FXML
    private Button epicButton;

    @FXML
    private Button showEpicButton;

    @FXML
    private Button returnButton;

    @FXML
    private Button sendEpicButton;

    @FXML Button Switch1Button;

    @FXML
    private Button deleteAllButton;

    private Connection connection;
    private String currentEpic;
    private String currentUserStory;
    private int currentStep = 0;
    private boolean isEpicCreationMode = false;
    private boolean isDeleteMode = false;
    private boolean isFullDeleteReady = false;

    @FXML
    public void initialize() {
        String url = "jdbc:mysql://localhost:3306/chatbotdb";
        String user = "root";
        String password = "Nc6xF76b!";

        try {
            connection = DriverManager.getConnection(url, user, password);
        } catch (SQLException e) {
            e.printStackTrace();
            chatArea.appendText("Error: Could not connect to database.\n");
        }

        sendButton.setOnAction(e -> handleSend());
        epicButton.setOnAction(e -> startEpicCreation());
        clearButton.setOnAction(e -> chatArea.clear());
        showEpicButton.setOnAction(event -> handleShowEpics());
        returnButton.setOnAction(e -> returnToChatbot());

        sendEpicButton.setDisable(true);
    }

    @FXML
    private void Switch2Screen(ActionEvent event) {
        try {
            Parent newRoot = FXMLLoader.load(getClass().getResource("/chat.fxml"));
            Scene newScene = new Scene(newRoot);

            Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            currentStage.setScene(newScene);
            currentStage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleDeleteAll() {
        try {
            String deleteTasksQuery = "DELETE FROM chatbotdb.tasks";
            try (PreparedStatement stmt = connection.prepareStatement(deleteTasksQuery)) {
                stmt.executeUpdate();
            }

            String deleteUserStoriesQuery = "DELETE FROM chatbotdb.user_stories";
            try (PreparedStatement stmt = connection.prepareStatement(deleteUserStoriesQuery)) {
                stmt.executeUpdate();
            }

            String deleteEpicsQuery = "DELETE FROM chatbotdb.epics";
            try (PreparedStatement stmt = connection.prepareStatement(deleteEpicsQuery)) {
                stmt.executeUpdate();
            }

            chatArea.appendText("[All epics, user stories, and tasks have been deleted.]\n");

        } catch (SQLException e) {
            e.printStackTrace();
            chatArea.appendText("[Error deleting data.]\n");
        }
    }

    @FXML
    private void toggleDeleteMode() {
        if (!isDeleteMode) {
            isDeleteMode = true;
            chatArea.appendText("🗑 [Delete Mode] Enter ID to delete a trio using Send Epic.\n");

            sendButton.setDisable(true);
            clearButton.setDisable(true);
            epicButton.setDisable(true);
            showEpicButton.setDisable(true);
            returnButton.setDisable(false);
            sendEpicButton.setDisable(false);
        } else {
            if (!isFullDeleteReady) {
                isFullDeleteReady = true;
                chatArea.appendText("⚠️ [Warning] Press ❌ again to delete EVERYTHING.\n");
            } else {
                handleDeleteAll();
                isDeleteMode = false;
                isFullDeleteReady = false;
            }
        }
    }

    @FXML
    private void startEpicCreation() {
        isEpicCreationMode = true;
        currentStep = 0;
        chatArea.appendText("------Create an Epic------\n");

        sendButton.setDisable(true);
        clearButton.setDisable(true);
        epicButton.setDisable(true);
        showEpicButton.setDisable(true);
        returnButton.setDisable(false);
        sendEpicButton.setDisable(false);
        userInput.clear();
    }

    private void returnToChatbot() {
        isEpicCreationMode = false;
        currentStep = 0;

        chatArea.appendText("[Returning to chatbot mode...]\n");

        sendButton.setDisable(false);
        clearButton.setDisable(false);
        epicButton.setDisable(false);
        showEpicButton.setDisable(false);
        returnButton.setDisable(true);
        sendEpicButton.setDisable(true);
        userInput.clear();
    }

    @FXML
    private void sendEpic() {
        String input = userInput.getText().trim();

        if (isDeleteMode) {
            try {
                int idToDelete = Integer.parseInt(input);
                deleteTrioById(idToDelete);
                chatArea.appendText("✅ Trio with ID " + idToDelete + " deleted.\n");
            } catch (NumberFormatException e) {
                chatArea.appendText("⚠️ Invalid ID. Please enter a valid number.\n");
            }

            userInput.clear();
            isDeleteMode = false;

            sendButton.setDisable(false);
            clearButton.setDisable(false);
            epicButton.setDisable(false);
            showEpicButton.setDisable(false);
            sendEpicButton.setDisable(true);
            return;
        }

        if (input.isEmpty()) {
            chatArea.appendText("------Create an Epic------\n");
            return;
        }

        switch (currentStep) {
            case 0:
                currentEpic = input;
                storeEpic(currentEpic);
                chatArea.appendText("📘 Epic: " + currentEpic + "\n");
                chatArea.appendText("---Create a User Story---\n");
                currentStep = 1;
                break;

            case 1:
                currentUserStory = input;
                storeUserStory(currentEpic, currentUserStory);
                chatArea.appendText("📗 User Story: " + currentUserStory + "\n");
                chatArea.appendText("------Create a Task------\n");
                currentStep = 2;
                break;

            case 2:
                storeTask(currentUserStory, input);
                chatArea.appendText("📒 Task: " + input + "\n");

                storeEpicUserStoryTask(currentEpic, currentUserStory, input);

                chatArea.appendText("✅ [Epic, User Story, and Task successfully created!]\n");
                returnToChatbot();
                break;
        }

        userInput.clear();
    }

    private void deleteTrioById(int trioId) {
        String deleteTrioQuery = "DELETE FROM epic_user_story_task_trios WHERE id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(deleteTrioQuery)) {
            stmt.setInt(1, trioId);
            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                chatArea.appendText("✅ Trio with ID " + trioId + " deleted.\n");
            } else {
                chatArea.appendText("⚠️ No trio found with ID " + trioId + ".\n");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            chatArea.appendText("⚠️ Error deleting trio.\n");
        }
    }







    private void storeEpicUserStoryTask(String epic, String userStory, String task) {
        String query = "INSERT INTO epic_user_story_task_trios (epic_description, user_story_description, task_description) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, epic);
            stmt.setString(2, userStory);
            stmt.setString(3, task);
            stmt.executeUpdate();

            ResultSet generatedKeys = stmt.getGeneratedKeys();
            if (generatedKeys.next()) {
                int trioId = generatedKeys.getInt(1);
                chatArea.appendText("🔢 Trio ID: " + trioId + "\n");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void handleSend() {
        String userInputText = userInput.getText().trim();

        if (userInputText.isEmpty()) {
            return;
        }

        if (isEpicCreationMode) {
            switch (currentStep) {
                case 1:
                    currentUserStory = userInputText;
                    storeUserStory(currentEpic, currentUserStory);
                    chatArea.appendText("You: " + currentUserStory + "\n");
                    chatArea.appendText("Bot: Now, provide the Task.\n");
                    currentStep = 2;
                    break;
                case 2:
                    storeTask(currentUserStory, userInputText);
                    chatArea.appendText("You: " + userInputText + "\n");
                    chatArea.appendText("[Epic, User Story, and Task successfully created!]\n");
                    returnToChatbot();
                    break;
            }

            userInput.clear();
            chatArea.setScrollTop(Double.MAX_VALUE);
            return;
        }

        String response = getResponse(userInputText);

        if (response != null) {
            chatArea.appendText("You: " + userInputText + "\n");
            chatArea.appendText("Bot: " + response + "\n");
        } else {
            chatArea.appendText("Bot: I don't understand that.\n");
        }

        userInput.clear();
        chatArea.setScrollTop(Double.MAX_VALUE);
    }

    private String getResponse(String keyword) {
        String response = null;

        String query = "SELECT response FROM chatbot WHERE keyword = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, keyword);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                response = rs.getString("response");
            }
        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
        }

        return response;
    }

    private void storeEpic(String epicDescription) {
        String query = "INSERT INTO chatbotdb.epics (description) VALUES (?)";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, epicDescription);
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void storeUserStory(String epicDescription, String storyDescription) {
        try {
            String query = "SELECT id FROM chatbotdb.epics WHERE description = ?";
            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                stmt.setString(1, epicDescription);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        int epicId = rs.getInt("id");

                        String insert = "INSERT INTO chatbotdb.user_stories (epic_id, description) VALUES (?, ?)";
                        try (PreparedStatement insertStmt = connection.prepareStatement(insert)) {
                            insertStmt.setInt(1, epicId);
                            insertStmt.setString(2, storyDescription);
                            insertStmt.executeUpdate();
                        }
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void storeTask(String storyDescription, String taskDescription) {
        try {
            String query = "SELECT id FROM chatbotdb.user_stories WHERE description = ?";
            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                stmt.setString(1, storyDescription);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        int storyId = rs.getInt("id");

                        String insert = "INSERT INTO chatbotdb.tasks (story_id, description) VALUES (?, ?)";
                        try (PreparedStatement insertStmt = connection.prepareStatement(insert)) {
                            insertStmt.setInt(1, storyId);
                            insertStmt.setString(2, taskDescription);
                            insertStmt.executeUpdate();
                        }
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleShowEpics() {
        StringBuilder output = new StringBuilder();

        try {
            String query = "SELECT * FROM chatbotdb.epic_user_story_task_trios";
            try (PreparedStatement stmt = connection.prepareStatement(query);
                 ResultSet rs = stmt.executeQuery()) {

                while (rs.next()) {
                    int trioId = rs.getInt("id");
                    String epicDescription = rs.getString("epic_description");
                    String userStoryDescription = rs.getString("user_story_description");
                    String taskDescription = rs.getString("task_description");

                    output.append("\n");
                    output.append("🔢 Trio ID: ").append(trioId).append("\n");
                    output.append("📘 Epic: ").append(epicDescription).append("\n");
                    output.append("📗 User Story: ").append(userStoryDescription).append("\n");
                    output.append("📒 Task: ").append(taskDescription).append("\n");
                }

            }
            chatArea.setText(output.toString());

        } catch (SQLException e) {
            e.printStackTrace();
            chatArea.appendText("[Error displaying epics, user stories, and tasks.]\n");
        }
    }
}
