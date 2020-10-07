
import java.io.*;
import java.net.*;
import java.util.Date;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;
import javax.xml.soap.Text;

public class User {

    private int score = 0;
    private String username;
    private TextArea ta;

    public User (String username){
        this.username = username;
    }

    public void showUsername (){
        ta.appendText(username);
    }


}

