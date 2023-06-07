package ml.qizd.qizdlauncher;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;

public class HelloController {
    @FXML
    private TextField login;

    @FXML
    private TextField password;

    @FXML
    private Label welcomeText;

    @FXML
    private ProgressBar progressBar;

    @FXML
    protected void onSignInButtonCLick() {
        try {
            String s = ElyByApi.getAuthToken(login.getText(), password.getText());
            System.out.println(s);
            welcomeText.setText(s);
        } catch (Exception e) {
            welcomeText.setText("ERROR: " + e.getMessage());
        }
    }

    @FXML
    protected void onHelloButtonClick() {
        welcomeText.setText("Downloading Minecraft libraries");

        try {
            MinecraftDownloader.download();
        } catch (Exception e) {
            welcomeText.setText("ERROR: " + e.getMessage());
        }
    }
}