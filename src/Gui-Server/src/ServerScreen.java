import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import java.io.IOException;

/**
 * Created by Gerta on 13/03/2017.
 */
public class ServerScreen extends BorderPane{

    private BorderPane serverScreen;
    private Button button;

    public ServerScreen() throws IOException {
        this.serverScreen = new BorderPane();
        this.button = new Button("Start");
        this.drawScene();
    }

    public void drawScene() {
        this.getStylesheets().add("serverScreenStyle.css");
        this.setBottom(button);

        this.setPrefWidth(800);
        this.setPrefHeight(600);

        serverScreen.setId("serverScreen");
        button.setId("button");

    }
}