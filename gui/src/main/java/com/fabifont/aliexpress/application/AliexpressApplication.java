package com.fabifont.aliexpress.application;

import com.fabifont.aliexpress.config.Config;
import com.fabifont.aliexpress.config.Settings;
import com.fabifont.aliexpress.process.AliexpressProcessRunner;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXToggleButton;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AliexpressApplication extends Application {
  private final Settings settings = new Settings();
  private Config config = new Config();
  private ExecutorService applicationThread;
  private AliexpressProcessRunner runner;

  public static void main(String[] args) {
    launch(args);
  }

  @Override
  public void start(Stage primaryStage) throws IOException {
    final Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getClassLoader().getResource("aliexpress.fxml")));

    var links = (TextField) root.lookup("#links");
    links.setText(config.getLinkProperty());

    var doneLimit = (TextField) root.lookup("#doneLimit");
    doneLimit.setText(String.valueOf(config.getDoneLimit()));

    var errorLimit = (TextField) root.lookup("#errorsLimit");
    errorLimit.setText(String.valueOf(config.getErrorLimit()));

    var adblocker = (JFXToggleButton) root.lookup("#adblocker");
    adblocker.setSelected(config.isAdblockerEnabled());

    var headless = (JFXToggleButton) root.lookup("#headless");
    headless.setSelected(config.isHeadless());

    var shipping = (JFXToggleButton) root.lookup("#shipping");
    shipping.setSelected(config.isShipToEnabled());

    var caching = (JFXToggleButton) root.lookup("#caching");
    caching.setSelected(config.isProfileCaching());

    var reverse = (JFXToggleButton) root.lookup("#reverse");
    reverse.setSelected(config.isReverseEnabled());

    var fallback = (JFXToggleButton) root.lookup("#fallback");
    fallback.setSelected(config.isFallback());

    var start = (JFXButton) root.lookup("#start");
    start.setOnMouseClicked(e -> {
      try {
        if (config.serialize(links.getText(), doneLimit.getText(), errorLimit.getText(), adblocker.isSelected(), headless.isSelected(), shipping.isSelected(), caching.isSelected(), reverse.isSelected(), fallback.isSelected())) {
          var action = start.getText().equals("START");
          links.setDisable(action);
          doneLimit.setDisable(action);
          errorLimit.setDisable(action);
          adblocker.setDisable(action);
          headless.setDisable(action);
          shipping.setDisable(action);
          caching.setDisable(action);
          reverse.setDisable(action);
          fallback.setDisable(action);
          start.setText(action ? "STOP" : "START");
          if (action) startBot();
          else stopBot();
        } else {
          showError(null);
        }
      } catch (Exception ex) {
        ex.printStackTrace();
        showError(ex.getMessage());
      }
    });

    var reload = (JFXButton) root.lookup("#reload");
    reload.setOnMouseClicked(e -> {
      var isRunning = start.getText().equals("STOP");
      if (isRunning) stopBot();
      this.config = new Config();
      if (isRunning) startBot();
    });

    primaryStage.setTitle("Aliexpress Bot");
    primaryStage.setResizable(false);
    primaryStage.setScene(new Scene(root));
    primaryStage.show();
  }

  private void showError(String message) {
    var alert = new Alert(Alert.AlertType.ERROR, message == null ? "Invalid options provided!" : message, ButtonType.OK);
    alert.showAndWait();
  }

  private void startBot() {
    this.applicationThread = Executors.newSingleThreadExecutor();
    this.runner = new AliexpressProcessRunner(config, settings);
    applicationThread.execute(runner);
  }

  private void stopBot() {
    if (applicationThread != null) applicationThread.shutdownNow();
    if (runner != null) runner.sendStop();
  }
}
