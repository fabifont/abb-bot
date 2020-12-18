package com.fabifont.aliexpress.account;

import com.fabifont.aliexpress.config.Config;
import com.fabifont.aliexpress.config.Settings;
import com.fabifont.aliexpress.exception.AddressException;
import com.fabifont.aliexpress.link.Link;
import com.google.common.base.CharMatcher;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

import static java.time.Duration.ofSeconds;

public class TenMinuteMailNetAccount extends Account {
  private final Settings settings;

  public TenMinuteMailNetAccount(Config config, Settings settings, List<Link> links) {
    super(config, settings, links);
    this.settings = settings;
    this.providerName = Account.TENMINUTEMAILNET_NAME;
  }

  @Override
  public void createMail(ChromeDriver driver) throws AddressException {
    try {
      ((JavascriptExecutor) driver).executeScript("document.body.style.zoom='75%';");
      var wait = new WebDriverWait(driver, ofSeconds(30));
      driver.get("https://10minutemail.net/");
      wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("span_mail")));

      var mailValue = "";
      do {
        mailValue = driver.findElement(By.id("span_mail")).getText();
      } while (!mailValue.contains("@"));

      this.email = mailValue;
    } catch (TimeoutException e) {
      throw new AddressException();
    }
  }

  @Override
  public Optional<String> getVerificationCode(WebDriver driver) {
    try {
      var wait = new WebDriverWait(driver, ofSeconds(30));
      wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("btn_refresh")));

      var path = "//h3[contains(text(), 'codice')]";
      var button = driver.findElement(By.id("btn_refresh"));
      wait.withTimeout(Duration.ofSeconds(30)).until(e -> {
        button.click();
        return driver.findElements(By.xpath(path)).size() > 0;
      });

      return Optional.of(CharMatcher.inRange('0', '9').retainFrom(driver.findElement(By.xpath(path)).getText()));
    } catch (StaleElementReferenceException e) {
      settings.newIpError();
      return getVerificationCode(driver);
    } catch (TimeoutException e) {
      settings.newIpError();
      return Optional.empty();
    }
  }
}
