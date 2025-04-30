package volter.com.example.appscrumteam;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;
import java.util.List;

 public class LastDesicionController {

    @FXML
    private TextArea decisionTextArea;

    @FXML
    private Button closeButton;

    @FXML
    public void initialize() {
        closeButton.setOnAction(event -> {
            Stage stage = (Stage) closeButton.getScene().getWindow();
            stage.close();
        });
    }

    public void setDecisions(List<String> decisions) {
        StringBuilder text = new StringBuilder();
        for (String decision : decisions) {
            text.append("- ").append(decision).append("\n");
        }
        decisionTextArea.setText(text.toString());
    }
}
