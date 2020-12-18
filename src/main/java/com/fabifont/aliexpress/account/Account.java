package com.fabifont.aliexpress.account;

import com.fabifont.aliexpress.config.Config;
import com.fabifont.aliexpress.config.Settings;
import com.fabifont.aliexpress.exception.AddressException;
import com.fabifont.aliexpress.link.Link;
import com.fabifont.aliexpress.util.Constants;
import lombok.Setter;
import lombok.SneakyThrows;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.time.LocalDate;
import java.util.*;

import static java.time.Duration.ofSeconds;

public abstract class Account {

  protected static final String TEMPMAILPLUS_NAME = "TempMailPlus";
  protected static final String TEMPMAILORG_NAME = "TempMailOrg";
  protected static final String TENMINUTEMAILNET_NAME = "TenMinuteMailNet";
  private static ArrayList<ProvidersRatio> ratioProviders =
      new ArrayList<>(Arrays.asList(
          new ProvidersRatio(TENMINUTEMAILNET_NAME)
      ));
  private final Config config;
  private final Settings settings;
  private final List<Link> links;
  protected String email;
  protected String providerName;
  @Setter
  protected int errori_consecutivi;


  public Account(Config config, Settings settings, List<Link> links) {
    this.config = config;
    this.settings = settings;
    this.links = links;
  }

  public static synchronized Account chooseAccountProvider(Config config, Settings settings, List<Link> links) {

    // Cerca il provider con il ratio migliore
    ProvidersRatio chosenProvider = ratioProviders.get(0);
    Config.logger.info(String.format("%s ha ratio %f", chosenProvider.name, chosenProvider.getRatio()));
    for (int i = 1; i < ratioProviders.size(); i++) {
      ProvidersRatio current = ratioProviders.get(i);
      Config.logger.info(String.format("%s ha ratio %f", current.name, current.getRatio()));
      if (chosenProvider.getRatio() < current.getRatio()) {
        chosenProvider = current;
      }
    }

    Config.logger.info(String.format("Provider scelto => %s", chosenProvider.name));

    Account futureAccount;
    switch (chosenProvider.name) {
      case TEMPMAILORG_NAME:
        futureAccount = new TempMailOrgAccount(config, settings, links);
        break;

      case TEMPMAILPLUS_NAME:
        futureAccount = new TempMailPlusAccount(config, settings, links);
        break;

      case TENMINUTEMAILNET_NAME:
        futureAccount = new TenMinuteMailNetAccount(config, settings, links);
        break;

      default:
        Config.logger.error("Errore nell'algoritmo di scelta del provider mail: Nome del provider non trovato: " + chosenProvider.name);
        futureAccount = new TempMailPlusAccount(config, settings, links);
    }

    futureAccount.setErrori_consecutivi(chosenProvider.errori_consecutivi);

    chosenProvider.totali += Math.pow(2, chosenProvider.errori_consecutivi);

    return futureAccount;
  }

  private static synchronized ProvidersRatio getProviderByName(String providerName) throws Exception {
    Iterator<ProvidersRatio> i = ratioProviders.iterator();

    while (i.hasNext()) {
      ProvidersRatio current = i.next();
      if (current.name.equals(providerName)) {
        return current;
      }
    }

    Config.logger.error(String.format("Non ho trovato il provider %s nella lista dei providers.", providerName));
    throw new Exception();
  }

  public void createLinkedTwitterAccount(ChromeDriver driver, ChromeDriver driverMail) throws AddressException {
    var wait = new WebDriverWait(driver, ofSeconds(50));
    driver.get("https://twitter.com/i/flow/signup");
    wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//*[@role=\"button\"][@dir=\"auto\"]")));

    // Fill info fields
    var date = pickRandomDate();
    driver.findElement(By.xpath("//*[@role=\"button\"][@dir=\"auto\"]")).click();
    driver.findElement(By.xpath("//input[@name=\"Data di nascita\"]")).sendKeys(date.get("day") + "/" + date.get("month") + "/" + date.get("year"));
    driver.findElement(By.xpath("//input[@name=\"name\"]")).sendKeys(email);
    driver.findElement(By.xpath("//input[@type=\"email\"]")).sendKeys(email);

    // Go next
    waitForNext(driver, wait);
    driver.findElement(By.xpath("//span[text()=\"Avanti\"]\n")).click();
    driver.findElement(By.xpath("//span[text()=\"Avanti\"]\n")).click();
    driver.findElement(By.xpath("//span[text()=\"Iscriviti\"]")).click();

    // Verify code
    var verificationCode = getVerificationCode(driverMail).orElseThrow(AddressException::new);
    Config.logger.info("Letto codice di verifica di Twitter: " + verificationCode);
    wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//input[@name=\"verfication_code\"]")));
    driver.findElement(By.xpath("//input[@name=\"verfication_code\"]")).sendKeys(verificationCode);
    wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//span[text()=\"Avanti\"]")));
    driver.findElement(By.xpath("//span[text()=\"Avanti\"]")).click();

    // Enter password and submit
    try {
      wait.withTimeout(Duration.ofSeconds(5)).until(ExpectedConditions.elementToBeClickable(By.xpath("//input[@name=\"password\"]")));
      driver.findElement(By.xpath("//input[@name=\"password\"]")).sendKeys(config.getPassword());
      waitForNext(driver, wait);
      driver.findElement(By.xpath("//span[text()=\"Avanti\"]")).click();
    } catch (TimeoutException e) {
      throw new AddressException();
    }

    wait.withTimeout(Duration.ofSeconds(14)).until(e -> driver.getPageSource().contains("Scegli una foto") || driver.getPageSource().contains("photo"));
    settings.getTwitterCompletedSinceChangeIp().incrementAndGet();

    driverMail.close();
    driverMail.quit();
  }

  public boolean clickAliexpress(ChromeDriver driver) throws Exception {
    var wait = new WebDriverWait(driver, ofSeconds(30));

    //Switch shipping location to USA without specifying the state or the city
    if (config.isShipToEnabled()) {
      driver.get("https://m.aliexpress.com/account.html#/shipto");
      tryToChangeShippingAddress(driver, wait, 5);
    }

    //Signup with twitter
    driver.get("https://login.aliexpress.com/h5.htm?type=register&fromMsite=true&return_url=http://47.88.68.33/amdc/mobileDispatch?appkey=21371601&deviceId=Xqtt4OcexSsDAE%2BY91bpkUsV&platform=android&v=4.0");
    wait.until(ExpectedConditions.elementToBeClickable(By.xpath("/html/body/div/section/div/div[3]/a")));
    driver.findElement(By.xpath("/html/body/div/section/div/div[3]/a")).click();

    // Switch tab
    var tabs = new ArrayList<>(driver.getWindowHandles());
    driver.switchTo().window(tabs.get(1));

    if (driver.getPageSource().contains("Il nome utente e la password inseriti non sono validi")) {
      throw new AddressException();
    }

    //I have no clue why how this can happen but better safe than sorry
    if (driver.getPageSource().contains("limited")) {
      throw new AddressException();
    }

    //Check for IP ban
    try {
      wait.withTimeout(Duration.ofSeconds(10)).until(ExpectedConditions.elementToBeClickable(By.id("allow")));
      driver.findElement(By.id("allow")).click();
    } catch (TimeoutException e) {
      throw new AddressException();
    }

    this.setSuccess();

    //Wait for login to be successful
    //While logging in: 2 tabs
    //When logged in: 1 tab
    wait.withTimeout(ofSeconds(60)).until(__ -> driver.getWindowHandles().size() == 1);
    tabs = new ArrayList<>(driver.getWindowHandles());
    driver.switchTo().window(tabs.get(0));

    //Click operation
    var success = true;

    for (var linkWrapper : links) {
      Config.logger.info("Sto per cliccare il link " + linkWrapper.getLink());

      driver.get(linkWrapper.getLink());
      if (driver.getPageSource().contains("ps,")) {
        Config.logger.info("Link farmato completamente " + linkWrapper.getLink());
        linkWrapper.getDone().set(true);
        break;
      }

      final String openPath = "//*[@id=\"root\"]/div/div/div[2]/div[4]";

      wait.withTimeout(Duration.ofSeconds(30)).until(ExpectedConditions.elementToBeClickable(By.xpath(openPath)));
      driver.findElement(By.xpath(openPath)).click();
      waitForSuccess(driver, wait, openPath);
      Thread.sleep(1000L);
      success = false;
      settings.getCompleted().incrementAndGet();
    }

    return success;
  }

  private Map<String, String> pickRandomDate() {
    final var random = new Random();
    var minDay = (int) LocalDate.of(1920, 1, 1).toEpochDay();
    var maxDay = (int) LocalDate.of(2000, 1, 1).toEpochDay();
    var randomDay = minDay + random.nextInt(maxDay - minDay);
    var date = LocalDate.ofEpochDay(randomDay);
    return Map.of(
        "year", String.valueOf(date.getYear()),
        "month", String.format("%02d", date.getMonthValue()),
        "day", String.format("%02d", date.getDayOfMonth())
    );
  }

  private void tryToChangeShippingAddress(ChromeDriver driver, WebDriverWait wait, int maxTimes) {
    try {
      wait.withTimeout(Duration.ofSeconds(5)).until(ExpectedConditions.elementToBeClickable(By.xpath("//*[@id=\"account-section\"]/div/div/div[2]/div/div/ul/div[1]")));
      driver.findElement(By.xpath("//*[@id=\"account-section\"]/div/div/div[2]/div/div/ul/div[1]")).click();
    } catch (TimeoutException e) {
      if (maxTimes <= 0) throw new RuntimeException("Account section error!");
      driver.navigate().refresh();
      tryToChangeShippingAddress(driver, wait, maxTimes - 1);
    }
  }

  private void waitForNext(WebDriver driver, WebDriverWait wait) {
    wait.withTimeout(Duration.ofSeconds(5)).until(i ->
    {
      var button = driver.findElement(By.xpath("//div[@class=\"css-18t94o4 css-1dbjc4n r-urgr8i r-42olwf r-sdzlij r-1phboty r-rs99b7 r-1w2pmg r-174vidy r-ydfevp r-1ny4l3l r-1fneopy r-o7ynqc r-6416eg r-lrvibr\"]\n"));
      var enabled = button.getAttribute("data-focusable");
      return enabled != null && enabled.equals("true");
    });
  }

  //Waits for success
  private void waitForSuccess(ChromeDriver driver, WebDriverWait wait, String openPath) {
    wait.withTimeout(Duration.ofSeconds(20)).until(e ->
    {
      try {
        Thread.sleep(2000);
      } catch (InterruptedException interruptedException) {
        interruptedException.printStackTrace();
      }
      if (driver.getPageSource().contains("Open") || driver.getPageSource().contains("Apri")) {
        try {
          Thread.sleep(2000);
        } catch (InterruptedException interruptedException) {
          interruptedException.printStackTrace();
        }
        if (!(driver.getPageSource().contains("Thank you for your help!") || driver.getPageSource().contains("Grazie dell'aiuto!"))) {
          wait.withTimeout(Duration.ofSeconds(5)).until(ExpectedConditions.elementToBeClickable(By.xpath(openPath)));
          driver.findElement(By.xpath(openPath)).click();
        }
      }
      return (driver.getPageSource().contains("Thank you for your help!") || driver.getPageSource().contains("Grazie dell'aiuto!"));
    });
  }

  @SneakyThrows
  public void setSuccess() {
    Config.logger.info(this.providerName + " ha avuto successo");
    ProvidersRatio providersRatio = getProviderByName(this.providerName);

    providersRatio.errori_consecutivi = 0;

    // Togli la penalità esponenziale in caso di errori consecutivi, ed aggiungi solo 1 al numero di tentativi totali
    providersRatio.totali -= Math.pow(2, this.errori_consecutivi);
    providersRatio.totali++;

    // Incrementa il numero di successi
    providersRatio.successi++;
  }

  @SneakyThrows
  public void setError() {
    Config.logger.info(this.providerName + " ha dato errore");
    ProvidersRatio providersRatio = getProviderByName(this.providerName);

    providersRatio.errori_consecutivi++;
  }

  public abstract void createMail(ChromeDriver driver) throws AddressException;

  public abstract Optional<String> getVerificationCode(WebDriver driver);
}
