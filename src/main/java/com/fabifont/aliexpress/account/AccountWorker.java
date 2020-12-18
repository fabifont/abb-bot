package com.fabifont.aliexpress.account;

import com.fabifont.aliexpress.config.Config;
import com.fabifont.aliexpress.config.Settings;
import com.fabifont.aliexpress.exception.AddressException;
import com.fabifont.aliexpress.link.Link;
import com.fabifont.aliexpress.util.ChromeUtils;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

public class AccountWorker implements Callable<Boolean> {
  private final Config config;
  private final Settings settings;
  private final List<Link> links;
  private final String hash;

  public AccountWorker(Config config, Settings settings, List<Link> links, String hash) {
    this.config = config;
    this.settings = settings;
    this.links = links;
    this.hash = hash;
  }

  @Override
  public Boolean call() {
    Account mail = null;
    ChromeOptions driverMailOptions, driverTwitterOptions;
    ChromeDriver driverMail = null, driverTwitterAliexpress = null;
    try {
      // Check
      if (links.stream().allMatch(e -> e.getDone().get())) return true;

      // Build options
      var agent = config.getUserAgent();
      driverMailOptions = buildChromeOptions(agent);
      driverTwitterOptions = buildChromeOptions(agent);
      if (config.isProfileCaching()) {
        var profileDir = ChromeUtils.chromeProfileDir(hash);
        driverMailOptions.addArguments("user-data-dir=" + profileDir + "mail");
        driverTwitterOptions.addArguments("user-data-dir=" + profileDir);
      }

      // Init drivers
      driverMail = ChromeUtils.createDriverWithDevTools(new ChromeDriver(driverMailOptions), agent);
      driverTwitterAliexpress = ChromeUtils.createDriverWithDevTools(new ChromeDriver(driverTwitterOptions), agent);

      mail = Account.chooseAccountProvider(config, settings, links);
      mail.createMail(driverMail);
      Config.logger.info("Letta email temporanea!");
      mail.createLinkedTwitterAccount(driverTwitterAliexpress, driverMail);
      Config.logger.info("Creato account Twitter!");
      return mail.clickAliexpress(driverTwitterAliexpress);
    } catch (WebDriverException webDriverException) {
      if (webDriverException.getMessage().contains("net::")) {
        Config.logger.info("Connection is still not available, retrying...");
        return false;
      }

      Config.logger.stackTraceWithMessage("Eccezione non gestita!", webDriverException);
      settings.getErrors().incrementAndGet();
      return false;
    } catch (AddressException tempMailException) {
      Config.logger.info("Eccezione gestita: rinnovo IP al prossimo ciclo");
      settings.newIpError();
      if (mail != null)
        mail.setError();
      return false;
    } catch (RuntimeException runtimeException) {
      Config.logger.stackTraceWithMessage("Eccezione gestita!", runtimeException);
      return false;
    } catch (Exception e) {
      Config.logger.stackTraceWithMessage("Eccezione non gestita!", e);
      settings.getErrors().incrementAndGet();
      return false;
    } finally {
      if (driverMail != null && driverMail.getSessionId() != null) {
        driverMail.close();
        driverMail.quit();
      }

      if (driverTwitterAliexpress != null) {
        driverTwitterAliexpress.close();
        driverTwitterAliexpress.quit();
      }
    }
  }

  public ChromeOptions buildChromeOptions(String agent) {
    var mobileEmulation = Map.of(
        "deviceMetrics", Map.of(
            "width", 450,
            "height", 800,
            "pixelRatio", 3.0
        ),
        "userAgent", agent
    );

    var chromeOptions = new ChromeOptions();
    chromeOptions.addArguments("--start-maximized");
    chromeOptions.setExperimentalOption("mobileEmulation", mobileEmulation);
    chromeOptions.setExperimentalOption("excludeSwitches", Collections.singletonList("enable-automation"));
    chromeOptions.setExperimentalOption("useAutomationExtension", false);
    chromeOptions.addArguments("disable-infobars");
    chromeOptions.addArguments("--disable-blink-features=AutomationControlled");
    if (config.isAdblockerEnabled()) chromeOptions.addArguments("load-extension=" + config.getUBlockPath());
    chromeOptions.setHeadless(config.isHeadless());
    return chromeOptions;
  }
}