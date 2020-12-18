package com.fabifont.aliexpress.config;


import com.fabifont.aliexpress.link.Link;
import com.fabifont.aliexpress.util.Constants;
import com.fabifont.aliexpress.util.FileUtils;
import com.fabifont.aliexpress.util.Logger;
import com.fabifont.aliexpress.util.Parser;
import lombok.Data;
import org.apache.commons.lang3.SystemUtils;
import org.apache.commons.lang3.Validate;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Properties;
import java.util.stream.Collectors;

@Data
public class Config {
  public static Logger logger = new Logger(Constants.LOGFILE_NAME);
  private List<List<Link>> threadLinksList;
  private String userAgent;
  private String password;
  private String adb;
  private int doneLimit;
  private int errorLimit;
  private boolean adblockerEnabled;
  private String uBlockPath;
  private boolean shipToEnabled;
  private boolean isHeadless;
  private boolean profileCaching;
  private boolean reverseEnabled;
  private List<String> hashes;
  private Properties properties;
  private String stopBat;
  private String telegramBotChatId;
  private String telegramBotToken;
  private String provider;
  private int maxTwitterPerIP;

  public Config() {
    // Il welcome message non lo sto a scrivere nel log, lo scrivo solo sul terminale
    System.out.println("# Welcome to the Aliexpress Bot - Version 69420");
    System.out.println("# Developed by Fabifont, Auties00 and thegoldgoat");

    Config.logger.info("Caricamento della configurazione in corso...");

    // Controlla che il driver sia installato
    var driverFile = new File(Constants.BASE_PATH, Constants.DRIVER_NAME);
    FileUtils.createFileDefaults(driverFile, getClass(), () -> Config.logger.info("ChromeDriver caricato: " + Constants.DRIVER_NAME));

    System.setProperty("java.util.concurrent.ForkJoinPool.common.parallelism", "0");
    System.setProperty("webdriver.chrome.driver", driverFile.toPath().toString());
    System.setProperty("webdriver.chrome.args", "--disable-logging");
    System.setProperty("webdriver.chrome.silentOutput", "true");

    var fileAdb = new File(Constants.BASE_PATH, Constants.IP_NAME);
    FileUtils.createFileDefaults(fileAdb, getClass(), () -> Config.logger.info("Adb installato: " + Constants.IP_NAME));

    this.adb = fileAdb.getPath();

    var configFile = new File(Constants.BASE_PATH, Constants.CONFIG_NAME);
    FileUtils.createFileDefaults(configFile, getClass(), () ->
    {
      Config.logger.info("File di configurazione non trovato, ne ho creata una copia di default in " + configFile.getAbsolutePath());
      System.exit(0);
    });

    if(SystemUtils.IS_OS_WINDOWS) {
      var stopBatFile = new File(Constants.BASE_PATH, Constants.STOP_BAT_NAME);
      FileUtils.createFileDefaults(stopBatFile, getClass(), () ->
      {
        Config.logger.info("stop.bat non trovato, ne ho creata una copia di default in " + stopBatFile.getAbsolutePath());
        System.exit(0);
      });

      this.stopBat = stopBatFile.getPath();

      var startBatFile = new File(Constants.BASE_PATH, Constants.START_BAT_NAME);
      FileUtils.createFileDefaults(startBatFile, getClass(), () ->
      {
        Config.logger.info("start.bat non trovato, ne ho creata una copia di default in " + startBatFile.getAbsolutePath());
        System.exit(0);
      });
    }

    this.properties = Parser.parseConfig(configFile).orElseThrow(() -> new RuntimeException("The configuration file is corrupted!"));

    this.threadLinksList = Parser.parseList(properties.getProperty("links")).stream().map(a -> a.stream().map(Link::of).collect(Collectors.toList())).collect(Collectors.toList());

    Validate.notEmpty(threadLinksList, "The configuration file is corrupted, please use the following format for your links: [link1,link2],[link3,link4]");

    this.userAgent = Objects.requireNonNull(properties.getProperty("user_agent", null), "User agent field cannot be null!");
    this.password = properties.getProperty("password");

    this.doneLimit = Parser.parseInteger(properties.getProperty("done_limit")).orElse(0);
    this.errorLimit = Parser.parseInteger(properties.getProperty("error_limit")).orElse(0);

    this.adblockerEnabled = Parser.parseBoolean(properties.getProperty("adblocker")).orElse(false);

    this.uBlockPath = FileUtils.copyFromJar("uBlock.zip", new File(System.getProperty("user.home") + "/aliexpress/"), getClass()).orElseThrow(() -> new RuntimeException("Cannot create or find uBlock directory"));

    this.shipToEnabled = Parser.parseBoolean(properties.getProperty("ship_to")).orElse(true);
    this.isHeadless = Parser.parseBoolean(properties.getProperty("headless")).orElse(false);
    this.profileCaching = Parser.parseBoolean(properties.getProperty("profile_caching")).orElse(true);

    this.provider = properties.getProperty("provider", "10mm");
    this.maxTwitterPerIP = Parser.parseInteger(properties.getProperty("max_per_ip")).orElse(3);

    if (this.profileCaching) {
      this.hashes = new ArrayList<>();
      var stringBuilder = new StringBuilder();
      threadLinksList.forEach(threadLinks ->
      {
        threadLinks.forEach(link -> stringBuilder.append(link.getLink()));
        this.hashes.add(FileUtils.generateMD5(stringBuilder.toString()));
      });

      // Controlla che esista lo zip dei profili 'puliti' ed in caso installalo
      var fileChromeProf = new File(Constants.BASE_PATH, Constants.CHROME_ZIP_FILENAME);
      FileUtils.createFileDefaults(fileChromeProf, getClass(), () -> Config.logger.info("Profilo installato: " + Constants.CHROME_ZIP_FILENAME));

      // Crea la cartella base per i profili 'reali'
      var profilesDir = new File(Constants.BASE_PATH, Constants.CHROME_PROFILE);
      FileUtils.createFolderOrDeleteContent(profilesDir);
    } else {
      this.hashes = null;
    }

    // Telegram bot configuration
    this.telegramBotChatId = properties.getProperty("tg_chatId", "");
    this.telegramBotToken = properties.getProperty("tg_botToken", "");
    this.reverseEnabled = Parser.parseBoolean(properties.getProperty("reverse")).orElse(false);
    Config.logger.info("Configurazione caricata correttamente");
  }

  private void setLinks(String input) {
    this.threadLinksList = Parser.parseList(properties.getProperty("links")).stream().map(a -> a.stream().map(Link::of).collect(Collectors.toList())).collect(Collectors.toList());
    properties.setProperty("links", input);
  }

  public String getLinkProperty() {
    return properties.getProperty("links", "");
  }

  private void setDoneLimit(int doneLimit) {
    this.doneLimit = doneLimit;
    properties.setProperty("done_limit", String.valueOf(doneLimit));
  }

  private void setErrorLimit(int errorLimit) {
    this.errorLimit = errorLimit;
    properties.setProperty("error_limit", String.valueOf(errorLimit));
  }

  private void setAdblockerEnabled(boolean adblockerEnabled) {
    this.adblockerEnabled = adblockerEnabled;
    properties.setProperty("adblocker", String.valueOf(adblockerEnabled));
  }

  private void setHeadless(boolean headless) {
    this.isHeadless = headless;
    properties.setProperty("headless", String.valueOf(headless));
  }

  private void setShipping(boolean shipping) {
    this.shipToEnabled = shipping;
    properties.setProperty("ship_to", String.valueOf(shipping));
  }

  private void setProfileCaching(boolean caching) {
    this.profileCaching = caching;
    properties.setProperty("profile_caching", String.valueOf(caching));
  }

  private void setReverseEnabled(boolean reverseEnabled) {
    this.reverseEnabled = reverseEnabled;
    properties.setProperty("reverse", String.valueOf(reverseEnabled));
  }

  private void saveConfig() throws IOException {
    var configFile = new File(Constants.BASE_PATH, Constants.CONFIG_NAME);
    properties.store(new FileWriter(configFile), null);
  }

  public boolean serialize(String links, String doneLimit, String errorLimit, boolean adblocker, boolean headless, boolean shipping, boolean caching, boolean reverse) {
    try {
      setLinks(links);
      setDoneLimit(Integer.parseInt(doneLimit));
      setErrorLimit(Integer.parseInt(errorLimit));
      setAdblockerEnabled(adblocker);
      setHeadless(headless);
      setShipping(shipping);
      setProfileCaching(caching);
      setReverseEnabled(reverse);
      saveConfig();
      return true;
    } catch (Exception e) {
      return false;
    }
  }
}
