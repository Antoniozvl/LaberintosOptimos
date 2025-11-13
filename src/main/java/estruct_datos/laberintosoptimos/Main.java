package estruct_datos.laberintosoptimos;

import estruct_datos.laberintosoptimos.Laberinto.GeneradorLaberinto;
import estruct_datos.laberintosoptimos.Laberinto.Laberinto;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class Main extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        /*FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/hello-view.fxml"));
        Scene escena = new Scene(fxmlLoader.load());
        stage.setTitle("Hello World");
        stage.setScene(escena);
        stage.setMaximized(true);
        stage.show();*/

        GeneradorLaberinto generador  = new GeneradorLaberinto();
        Laberinto laberinto = generador.generar(18, 13);
        laberinto.printAscii();

        System.exit(0);
    }

    public static void main(String[] args) {
        launch(args);
    }
}

//pull