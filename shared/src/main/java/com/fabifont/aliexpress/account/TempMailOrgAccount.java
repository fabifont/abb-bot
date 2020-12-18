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
import java.util.Objects;
import java.util.Optional;

import static java.time.Duration.ofSeconds;

public class TempMailOrgAccount extends Account {
  private final Settings settings;

  public TempMailOrgAccount(Config config, Settings settings, List<Link> links) {
    super(config, settings, links);
    this.settings = settings;
    this.providerName = Account.TEMPMAILORG_NAME;
  }

  @Override
  public void createMail(ChromeDriver driver) throws AddressException {
    try {
      ((JavascriptExecutor) driver).executeScript("document.body.style.zoom='75%';");
      var wait = new WebDriverWait(driver, ofSeconds(30));
      driver.get("https://temp-mail.org/it/");
      wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("mail")));
      var mailValue = "";
      do {
        mailValue = driver.findElement(By.id("mail")).getAttribute("value");
      } while (!mailValue.contains("@"));

      this.email = mailValue;
    } catch (Exception e) {
      throw new AddressException();
    }
  }

  @Override
  public Optional<String> getVerificationCode(WebDriver driver) {
    try {
      var wait = new WebDriverWait(driver, ofSeconds(30));
      wait.withTimeout(Duration.ofSeconds(30)).until(e -> driver.findElements(By.tagName("span")).stream().anyMatch(ex -> ex.getAttribute("innerText").contains("codice")));
      return Optional.of(CharMatcher.inRange('0', '9').retainFrom(Objects.requireNonNull(driver.findElements(By.tagName("span")).stream().filter(ex -> ex.getAttribute("innerText").contains("codice")).findAny().orElse(null)).getAttribute("innerText")));
    } catch (StaleElementReferenceException e) {
      settings.newIpError();
      return getVerificationCode(driver);
    } catch (TimeoutException e) {
      settings.newIpError();
      return Optional.empty();
    }
  }
}
