package com.fabifont.aliexpress.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.SystemUtils;

import java.io.File;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Constants {
  public static final String BASE_PATH = System.getProperty("user.home") + "/aliexpress";
  public static final String DRIVER_NAME = "chromedriver" + (SystemUtils.IS_OS_WINDOWS ? ".exe" : "");
  public static final String IP_NAME = "ip." + (SystemUtils.IS_OS_WINDOWS ? "bat" : "sh");
  public static final String CONFIG_NAME = "config.properties";
  public static final String STOP_BAT_NAME = "stop.bat";
  public static final String START_BAT_NAME = "start.bat";
  public static final String CHROME_PROFILE = "chrome_profile";
  public static final String CHROME_ZIP_FILENAME = "profili_chrome_clean.zip";
  public static final String CHROME_ZIP_PROFILE_NAME = "clean_normale";
  public static final String CHROME_ZIP_PROFILE_EMAIL_NAME = "clean_mail";
  public static final String LEVEL_EASY = "/html/body/div[2]/div/div[2]/div/div/div/div/div[1]/div/div/div[4]/div[2]";
  public static final String LEVEL_MEDIUM = "/html/body/div[2]/div/div[2]/div/div/div/div/div[1]/div/div/div[5]/div[2]";
  public static final String LEVEL_HARD = "/html/body/div[2]/div/div[2]/div/div/div/div/div[1]/div/div/div[6]/div[2]";
  public static final String SALT = "sal3@ll!m0ne";
  public static final File LOGFILE_NAME = new File(BASE_PATH, "logfile.txt");
  public static final String OPEN_LETTER_XPATH = "/html/body/div[2]/div/div[2]/div/div/div/div/div[1]/div/div/div[4]/div[2]";
}
