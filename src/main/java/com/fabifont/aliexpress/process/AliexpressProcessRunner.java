package com.fabifont.aliexpress.process;

import com.fabifont.aliexpress.account.AccountWorker;
import com.fabifont.aliexpress.config.Config;
import com.fabifont.aliexpress.config.Settings;
import com.fabifont.aliexpress.link.StateChecker;
import com.fabifont.aliexpress.util.ChromeUtils;
import com.fabifont.aliexpress.util.Parser;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class AliexpressProcessRunner implements Runnable {
  private final Config config;
  private final Settings settings;
  private ExecutorService executor;

  public AliexpressProcessRunner(Config config, Settings settings) {
    this(config, settings, null);
  }

  public static void runOnMainThread() {
    var runnable = new AliexpressProcessRunner(new Config(), new Settings());
    runnable.run();
  }

  public void sendStop() {
    if (executor != null) executor.shutdownNow();
  }

  @Override
  public void run() {
    try {
      // Kill chrome and chromedriver
      ChromeUtils.forceKill(config);

      // New links list without completed links
      var newThreadLinksList = config.getThreadLinksList()
          .stream()
          .map(list -> list
              .stream()
              .filter(link -> !link.getDone().get())
              .collect(Collectors.toList()))
          .filter(e -> e.size() > 0)
          .collect(Collectors.toList());

      // Reverse if enabled
      if (config.isReverseEnabled() && settings.getShouldReverseOrder().get()) {
        newThreadLinksList.forEach(threadLinks ->
        {
          if (threadLinks.size() > 1)
            Collections.reverse(threadLinks);
        });

        settings.getShouldReverseOrder().set(!settings.getShouldReverseOrder().get());
      }

      // IP change
      if (settings.getChangeIp().get() || newThreadLinksList.size() > 1 || settings.getTwitterCompletedSinceChangeIp().get() >= config.getMaxTwitterPerIP()) {
        ChromeUtils.changeAddress(config);
        settings.getTwitterCompletedSinceChangeIp().set(0);
        settings.getChangeIp().set(false);
      }

      // Start an executor with a thread for every link
      this.executor = Executors.newFixedThreadPool(newThreadLinksList.size());

      // Create an arraylist of tasks and populate it
      final var tasks = new ArrayList<Callable<Boolean>>();

      //Setup queue
      for (int index = 0; index < newThreadLinksList.size() && index < config.getMaxTwitterPerIP(); index++) {
        if (config.isProfileCaching()) {
          String hash = config.getHashes().get(index);
          ChromeUtils.createCleanChromeProfiles(hash);
          tasks.add(new AccountWorker(config, settings, newThreadLinksList.get(index), hash));
        } else tasks.add(new AccountWorker(config, settings, newThreadLinksList.get(index), ""));
      }

      // Invoke all the tasks, unbox the results and check that every result is true
      var finalResult = executor.invokeAll(tasks).stream().allMatch(e -> Parser.unboxFuture(e).orElse(false));

      // Check state for settings in config.properties and loaded by Config.java
      switch (StateChecker.checkState(config, settings)) {
        case WORKING:
          // If the application is allowed to still run check if it has work left to do
          if (!finalResult) {
            Config.logger.stampaResoconto(settings, config);
            run();
          }
          break;
        // The application cannot continue to run as it hit the max number of successful accounts specified in config.properties
        case DONE_LIMIT:
          Config.logger.info("Raggiunto il limite dei click");
          break;
        // The application cannot continue to run as it hit the max number of errors specified in config.properties
        case ERROR_LIMIT:
          Config.logger.info("Raggiunto il limite degli errori");
          break;
      }

      // Print resume if application is done
      Config.logger.stampaResoconto(settings, config);
      System.exit(0);
    } catch (InterruptedException ex) {
      ex.printStackTrace();
    }
  }
}
