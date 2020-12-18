package com.fabifont.aliexpress.util;

import com.fabifont.aliexpress.config.Config;
import lombok.experimental.UtilityClass;
import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.devtools.Command;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.logging.Level;


@UtilityClass
public class ChromeUtils {
  //Changes IP using ADB method
  public void changeAddress(Config config) {
    try {
      Config.logger.info("Avvio il cambio dell'IP...");
      String command = "cmd /C start /wait " + config.getAdb();
      final Process process = Runtime.getRuntime().exec(command);
      process.waitFor();
      Config.logger.info("Cambio dell'IP Completato!");
    } catch (InterruptedException e) {
      Config.logger.info("Interrupt ricevuto, dovrebbe essere tutto a posto");
    } catch (IOException e) {
      Config.logger.stackTraceWithMessage("Errore durante il lancio dello script di cambio dell'IP! IP NON CAMBIATO", e);
    }
  }

  public void forceKill(Config config) {
    try {
      Config.logger.info("Chiudo le istanze di Chrome e ChromeDriver");
      Runtime.getRuntime().exec(config.getStopBat());
    } catch (IOException e) {
      Config.logger.stackTraceWithMessage("Errore durante il lancio dello script per killare chrome/chromedriver", e);
    }
  }

  public ChromeDriver createDriverWithDevTools(ChromeDriver driverMobile, String userAgent) {
    driverMobile.getDevTools().createSession();
    driverMobile.getDevTools().send(new Command<>("Emulation.setTouchEmulationEnabled", Map.of(
        "enabled", true
    )));
    driverMobile.getDevTools().send(new Command<>("Emulation.setUserAgentOverride", Map.of(
        "userAgent", userAgent,
        "platform", "Android"
    )));
    driverMobile.getDevTools().send(new Command<>("Emulation.setEmitTouchEventsForMouse", Map.of(
        "enabled", true,
        "configuration", "mobile"
    )));

    return driverMobile;
  }

  public String chromeProfileDir(String name) {
    return new File(Constants.BASE_PATH + "/" + Constants.CHROME_PROFILE, name).toString();
  }

  public void createCleanChromeProfiles(String profileBaseName) {
    final var profilesDir = new File(Constants.BASE_PATH, Constants.CHROME_PROFILE);

    // Elimina profili precedenti
    var oldProfileDir = new File(profilesDir, profileBaseName);
    var oldProfileDirMail = new File(profilesDir, profileBaseName + "mail");

    if (oldProfileDir.exists()) FileUtils.deleteFolder(oldProfileDir);
    if (oldProfileDirMail.exists()) FileUtils.deleteFolder(oldProfileDirMail);

    try {
      // Estrai lo zip nella cartella dei profili
      var zipFile = new ZipFile(new File(Constants.BASE_PATH, Constants.CHROME_ZIP_FILENAME));
      zipFile.extractAll(profilesDir.toString());

      // Rinomina le cartelle con il nome corrispondente
      File oldNameNormal = new File(profilesDir, Constants.CHROME_ZIP_PROFILE_NAME);
      File newNameNormal = new File(profilesDir, profileBaseName);
      oldNameNormal.renameTo(newNameNormal);

      File oldNameMail = new File(profilesDir, Constants.CHROME_ZIP_PROFILE_EMAIL_NAME);
      File newNameMail = new File(profilesDir, profileBaseName + "mail");
      oldNameMail.renameTo(newNameMail);
    } catch (ZipException e) {
      System.err.println("Errore durante l'estrazione dei profili di Chrome:");
      System.err.println("La funziona di caching per la riduzione del consumo dei dati potrebbe non funzionare.");
      e.printStackTrace();
    }
  }
}