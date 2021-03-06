package com.example.saper;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;


import java.io.IOException;

public class SaperApplication extends Application {

    private static final int WIDTH = 520;
    private static final int HEIGHT = 625;

    private static Stage StageApp;

    // для args[]
    private static GameDifficulty _dif = null;
    private static int _seed = -1;
    private static boolean _debugOption = false;

    private static void setSizeStage(int width, int height){
        StageApp.setMinHeight(height);
        StageApp.setMaxHeight(height);

        StageApp.setMinWidth(width);
        StageApp.setMaxWidth(width);
    }

    public static GameDifficulty getDif()
    {
        return _dif;
    }
    public static int getSeed() {return _seed;}
    public static boolean getDebugOption()
    {
        return _debugOption;
    }

    @Override
    public void start(Stage stage) throws IOException {
        StageApp = stage;
        setSizeStage(WIDTH, HEIGHT);
        FXMLLoader fxmlLoader = new FXMLLoader(SaperApplication.class.getResource("MainView.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        stage.setTitle("Сапёр!");
        stage.setScene(scene);


        scene.getStylesheets().add(getClass().getResource("/_config/style.css").toExternalForm());

        stage.setOnCloseRequest(e -> GameController.closeApp());

        stage.show();

    }

    public static void main(String[] args) {
        for (var arg:args) {
            String[] argWithVal = arg.split(":");

            if (argWithVal[0].equals("-dif")) //задание мложности
            {
                if (argWithVal[1].equals("hard"))
                    _dif = GameDifficulty.Hard;
                else if (argWithVal[1].equals("normal"))
                    _dif = GameDifficulty.Normal;
                else if (argWithVal[1].equals("easy"))
                    _dif = GameDifficulty.Easy;
            }
            else if (argWithVal[0].equals("-seed")) //задание сида генрации мин
            {
                try {
                    int number = Integer.parseInt(argWithVal[1]);
                    _seed = (number >= 0)?number:-1;
                }
                catch (NumberFormatException ex) {
                    _seed = -1;
                }
            }
            else if (argWithVal[0].equals("-debugtool")) //включение пункта debug в menubar
            {
                 _debugOption = true;
            }
        }

        if (_dif == null)
        {
            _dif = GameDifficulty.Easy;
        }
        launch();
    }
}