package com.example.saper;


import com.example.saper.gamefield.Field;
import com.example.saper.gamefield.MineGenerator;
import com.example.saper.gamefield.Tile;
import javafx.application.Platform;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.FlowPane;
import javafx.scene.paint.Color;

import java.net.URL;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.TimerTask;


/**
Игровое поле сапёра, обрабатывающее события UI.
@version 1.0
*/
public class GameController implements Initializable {

    @FXML
    private FlowPane _flowPane;
    @FXML
    private Button _bRestart;
    @FXML
    private Label _lTimer;
    @FXML
    private Label _lMineCount;
    @FXML
    private Menu _debugMenu;

    private static Timer _timer;
    private static TimerTask _timerTask;
    private static boolean _isGameStarted;

    private int _gameTimeInSeconds = 0;
    private IntegerProperty _mineCount;
    private IntegerProperty _simpleTileCount;
    private Config _config;

    public static javafx.beans.value.ChangeListener<Boolean> flagListener ;
    public static javafx.beans.value.ChangeListener<Boolean> clickListener;

    private static GameDifficulty _gameDif = GameDifficulty.Easy;

    @FXML
    private void easyItemClick(ActionEvent event) {
        _gameDif = GameDifficulty.Easy;
        _isGameStarted = false;
        startGame();
    }
    @FXML
    private void normalItemClick(ActionEvent event) {
        _gameDif = GameDifficulty.Normal;
        _isGameStarted = false;
        startGame();
    }
    @FXML
    private void hardItemClick(ActionEvent event) {
        _gameDif = GameDifficulty.Hard;
        _isGameStarted = false;
        startGame();
    }
    @FXML
    private void openAllDebugClick(ActionEvent event) {
        openAll(true);
    }
    @FXML
    private void restartButtonClick(ActionEvent event) {

        if (_isGameStarted) {
            clearGameSession();
            overGame(false);
            _isGameStarted = false;
        }
        if (SaperApplication.getDif() != null) {
            startGame();
        }
    }

    /**
     * Метод перезапускает игровой таймер.
     */
    private void resetTimer() {
        _timerTask = new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(
                        () -> {
                            _gameTimeInSeconds++;

                            int minutes = _gameTimeInSeconds / 60;
                            int seconds = _gameTimeInSeconds % 60;

                            boolean extraZeroCheck4Seconds = seconds / 10 == 0;
                            boolean extraZeroCheck4Minutes = minutes / 10 == 0;

                            String extraZero4Seconds = extraZeroCheck4Seconds ? "0" : "";
                            String extraZero4Minutes = extraZeroCheck4Minutes ? "0" : "";

                            if (minutes == 55) {
                                _lTimer.setTextFill(Color.RED);
                            }

                            _lTimer.setText("Time " + extraZero4Minutes + minutes + ":" + extraZero4Seconds + seconds);

                            if (minutes == 60) {
                                overGame(false);
                            }
                        }
                );
            }
        };

        _timer = new Timer();
        _timer.schedule(_timerTask, 0, 1000);
    }

    /**
     * Открывает все игровые клетки.
     * @param isDebugging Параметр используемый для задания поведения метода. Если - {@code true}, то отобразятся все мины и числа на поле.
     *                    Если - {@code false}, то ко всем клеткам применится метод {@code setDisable}
     */
    private void openAll(boolean isDebugging) {

        Field.applyToAll(tile -> {
            if (isDebugging) {
                tile.getStyleClass().add("debug");
                if (!tile.isMine) {
                    tile.TextView.run();
                }
            }

            tile.setDisable(!isDebugging);
        });
    }

    /**
     * Метод вызывает открытие соседних клеток относительно заданной.
     * @param iPos Координата строки клетки.
     * @param jPos Координата столбца клетки.
     */
    private void callNearby(int iPos, int jPos) {
        if (!_isGameStarted) {
            _isGameStarted = true;

            Field.setStartPoint(iPos,jPos);

            startGen();

            Field.getTile(iPos, jPos).mouseHandler(MouseButton.PRIMARY);
            return;
        }

        Field.applyToAround(iPos,jPos, (coordinate) -> {
            Tile tile = Field.getTile(coordinate.getKey(),coordinate.getValue());

            if (!tile.isClicked() && !tile.isFlag()) {
                tile.mouseHandler(MouseButton.PRIMARY);
            }
        }, 1);
    }


    /**
     * Метод, вызывающийся при инициализации игровой сцены.
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        _isGameStarted = false;

        if (!SaperApplication.getDebugOption()) {
            _debugMenu.setVisible(false);
        }
        if (SaperApplication.getDif() != null) {
            _gameDif = SaperApplication.getDif();
        }

        _lTimer.setText("Time 00:00");
        startGame();
    }

    /**
     * Метод, вызывающийся при закрытии приложения.
     */
    public static void closeApp() {
        if(_timer != null) {
            _timer.cancel();
        }
    }

    /**
     * Метод, вызывающийся при старте игры. Генерирует
     * игровое поле и настройки к нему.
     */
    public void startGame() {
        clearGameSession();
        _config = _gameDif.getConfigField();

        flagListener = (observableValue, aBoolean, t1) -> {
            _mineCount.set(
                    (observableValue.getValue().booleanValue()
                    ? -1 : 1) + _mineCount.getValue()
            );

            _lMineCount.setText(Integer.toString(_mineCount.getValue()));
        };

        clickListener = (observableValue, aBoolean, t1) -> _simpleTileCount.set(_simpleTileCount.getValue() - 1);

        _bRestart.setText(": )");
        _lMineCount.setText(Integer.toString(_config.CountMines));

        new Field(_config);

        javafx.beans.value.ChangeListener<Number> numberChangeListener = (observableValue, number, t1) -> {
            if (Objects.equals(_mineCount.getValue(), _simpleTileCount.getValue()) && _mineCount.getValue() == 0) {
                overGame(true);
            }
        };

        _mineCount = new SimpleIntegerProperty(Field.getCountMines());
        _mineCount.addListener(numberChangeListener);

        _simpleTileCount = new SimpleIntegerProperty(Field.getCountSimpleTiles());
        _simpleTileCount.addListener(numberChangeListener);

        Tile.setCall((x,y) -> callNearby(x, y));

        Tile.setExplosionEvent(() -> overGame(false));

        Field.applyToAll(tile -> _flowPane.getChildren().add(tile));
    }

    /**
     * Метод, вызывающий генерацию мин.
     */
    public void startGen() {
        MineGenerator.mineGen(_config.CountMines);

        resetTimer();
    }

    /**
     * Метод очищает игровую сессию. Обнуляет таймер, очищает игровое поле.
     */
    public void clearGameSession() {
        _lTimer.setText("Time 00:00");
        _lTimer.setTextFill(Color.BLACK);
        _flowPane.getChildren().clear();
        if (_timer != null) {
            _gameTimeInSeconds = 0;
            _timer.cancel();
        }
    }

    /**
     * Метод, вызывающийся при проигрыше.
     */
    public void overGame(boolean isWin) {
        var smile = isWin ? ": D" : ": (";
        var color = isWin ? Color.GREEN : Color.DARKBLUE;

        _bRestart.setText(smile);
        _lTimer.setTextFill(color);
        _gameTimeInSeconds = 0;
        _timer.cancel();

        openAll(false);
    }

    /**
     * Возвращает поле {@link GameController#_isGameStarted}.
     * @return Возвращает true, если игра начата, иначе - false.
     */
    public static boolean getGameCondition() {
        return _isGameStarted;
    }

    /**
     * Возврщает поле {@link GameController#_gameDif}.
     * @return Перечисление {@link GameDifficulty} определяющее сложность игры.
     */
    public static GameDifficulty getGameDifficulty() {
        return _gameDif;
    }
}