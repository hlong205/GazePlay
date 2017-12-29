package net.gazeplay;

import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.ChoiceBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Pair;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.commons.gaze.configuration.Configuration;
import net.gazeplay.commons.gaze.configuration.ConfigurationBuilder;
import net.gazeplay.commons.utils.HomeUtils;
import net.gazeplay.commons.utils.games.Utils;
import net.gazeplay.commons.utils.multilinguism.Multilinguism;
import net.gazeplay.commons.utils.stats.Stats;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by schwab on 17/12/2016.
 */
@Slf4j
public class GazePlay extends Application {

    private static Scene scene;
    private static Group root;

    private static ChoiceBox<String> cbxGames;

    private static List<GameSpec> games;

    private static GamesLocator gamesLocator;

    @Override
    public void start(Stage primaryStage) {

        primaryStage.setTitle("GazePlay");

        primaryStage.setFullScreen(true);

        root = new Group();

        scene = new Scene(root, com.sun.glass.ui.Screen.getScreens().get(0).getWidth(),
                com.sun.glass.ui.Screen.getScreens().get(0).getHeight(), Color.BLACK);

        log.info(String.format("Screen size: %3d X %3d", com.sun.glass.ui.Screen.getScreens().get(0).getWidth(),
                com.sun.glass.ui.Screen.getScreens().get(0).getHeight()));

        // end of System information
        for (int i = 0; i < 5; i++)
            log.info("***********************");

        Configuration config = ConfigurationBuilder.createFromPropertiesResource().build();

        scene.getStylesheets().add(config.getCssfile());

        Utils.addStylesheets(scene.getStylesheets());

        log.info(scene.getStylesheets().toString());

        updateGames();

        HomeUtils.goHome(scene, root, cbxGames);

        primaryStage.setOnCloseRequest((WindowEvent we) -> System.exit(0));

        primaryStage.setScene(scene);

        primaryStage.show();

        // SecondScreen secondScreen = SecondScreen.launch();
    }

    /**
     * This command is called when games have to be updated (example: when language changed)
     *
     */

    public static ChoiceBox updateGames() {

        gamesLocator = new DefaultGamesLocator();

        games = gamesLocator.listGames();

        cbxGames = new ChoiceBox<>();

        Multilinguism multilinguism = Multilinguism.getSingleton();

        Configuration config = ConfigurationBuilder.createFromPropertiesResource().build();
        String language = config.getLanguage();

        List<String> gamesLabels = games.stream()
                .map(gameSpec -> new Pair<>(gameSpec, multilinguism.getTrad(gameSpec.getNameCode(), language)))
                .map(pair -> {
                    String variationHint = pair.getKey().getVariationHint();
                    if (variationHint == null) {
                        return pair.getValue();
                    }
                    return pair.getValue() + " " + variationHint;
                }).collect(Collectors.toList());

        cbxGames.getItems().addAll(gamesLabels);

        cbxGames.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {

                chooseGame(newValue.intValue());
            }
        });

        return cbxGames;
    }

    private static void chooseGame(int gameIndex) {
        HomeUtils.clear(scene, root, cbxGames);

        log.info("Game number: " + gameIndex);

        if (gameIndex == -1) {
            return;
        }

        GameSpec selectedGameSpec = games.get(gameIndex);

        log.info(selectedGameSpec.getNameCode() + " " + selectedGameSpec.getVariationHint());

        Stats stats = selectedGameSpec.launch(scene, root, cbxGames);

        HomeUtils.home(scene, root, cbxGames, stats);
    }
}
