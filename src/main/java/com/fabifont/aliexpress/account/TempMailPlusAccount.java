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

public class TempMailPlusAccount extends Account {
  private final Settings settings;

  public TempMailPlusAccount(Config config, Settings settings, List<Link> links) {
    super(config, settings, links);
    this.settings = settings;
    this.providerName = Account.TEMPMAILPLUS_NAME;
  }

  @Override
  public void createMail(ChromeDriver driver) throws AddressException {
    try {
      ((JavascriptExecutor) driver).executeScript("document.body.style.zoom='75%';");
      var wait = new WebDriverWait(driver, ofSeconds(30));
      driver.get("https://tempmail.plus/en/#!");
      wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("pre_button")));

      var mailValue = "";
      do {
        mailValue = driver.findElement(By.id("pre_button")).getAttribute("value");
      } while (mailValue.length() < 1);

      mailValue += driver.findElement(By.id("domain")).getText();

      this.email = mailValue;
    } catch (TimeoutException e) {
      throw new AddressException();
    }
  }

  @Override
  public Optional<String> getVerificationCode(WebDriver driver) {
    try {
      var wait = new WebDriverWait(driver, ofSeconds(30));
      wait.withTimeout(Duration.ofSeconds(30)).until(e -> driver.findElements(By.tagName("span")).stream().anyMatch(ex -> ex.getAttribute("innerText").contains("codice di convalida per Twitter")));
      return Optional.of(CharMatcher.inRange('0', '9').retainFrom(driver.findElements(By.tagName("span")).stream().filter(ex -> ex.getAttribute("innerText").contains("codice di convalida per Twitter")).findFirst().orElseThrow(NullPointerException::new).getAttribute("innerText")));
    } catch (NullPointerException e) {
      return getVerificationCode(driver);
    } catch (StaleElementReferenceException e) {
      settings.newIpError();
      return getVerificationCode(driver);
    } catch (TimeoutException e) {
      settings.newIpError();
      return Optional.empty();
    }
  }
}
