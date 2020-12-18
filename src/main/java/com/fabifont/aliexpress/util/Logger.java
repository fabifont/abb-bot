package com.fabifont.aliexpress.util;

import com.fabifont.aliexpress.config.Config;
import com.fabifont.aliexpress.config.Settings;

import java.io.*;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

public class Logger {
  FileWriter outputFile;
  boolean enableFileOutput;

  public Logger(File logFile) {
    if (logFile != null) {
      enableFileOutput = true;
      try {
        outputFile = new FileWriter(logFile);
        this.info("Logging su file abilitato: " + logFile.getAbsolutePath());
      } catch (IOException e) {
        enableFileOutput = false;
        this.stackTraceWithMessage("Errore durante l'apertura del file di logging. Logging su file disabilitato.", e);
      }
    } else {
      enableFileOutput = false;
    }
  }

  public void stampaResoconto(Settings settings, Config config) {
    this.info("--------------------");
    this.info(settings.getCompleted().get() + " click con successo");
    this.info(settings.getErrors().get() + " errori");
    this.info(settings.getIpBans().get() + " ban di IP");
    this.info("--------------------");

    if (config.getTelegramBotChatId().equals("") || config.getTelegramBotToken().equals("")) {
      return;
    }

    final var client = HttpClient.newHttpClient();
    final var text = String.format("%s click con successo\n%s errori\n%s ban di IP", settings.getCompleted().get(), settings.getErrors().get(), settings.getIpBans().get());
    final var body = String.format("text=%s&chat_id=%s", URLEncoder.encode(text, StandardCharsets.UTF_8), config.getTelegramBotChatId());
    final var request = HttpRequest.newBuilder()
        .uri(URI.create(String.format("https://api.telegram.org/bot%s/sendmessage", config.getTelegramBotToken())))
        .header("Content-Type", "application/x-www-form-urlencoded")
        .POST(HttpRequest.BodyPublishers.ofString(body))
        .build();
    client.sendAsync(request, HttpResponse.BodyHandlers.discarding());
  }

  public void warningLog(String message) {
    String completeMessage = "[WARN]: " + message + "\n";
    logMessage(completeMessage, System.out);
  }

  public void info(String message) {
    String completeMessage = "[INFO]: " + message + "\n";
    logMessage(completeMessage, System.out);
  }

  public void stackTraceWithMessage(String message, Exception e) {
    this.error(message);
    this.stackTrace(e);
  }

  public void error(String message) {
    String completeMessage = "[ERROR]: " + message + "\n";
    logMessage(completeMessage, System.err);
  }

  public void stackTrace(Exception e) {
    StringWriter sw = new StringWriter();
    e.printStackTrace(new PrintWriter(sw));
    String completeMessage = "[TRACE]: " + sw.toString() + "\n";
    logMessage(completeMessage, System.err);
  }

  private void logMessage(String completeMessage, PrintStream stream) {
    stream.print(completeMessage);

    if (enableFileOutput) {
      try {
        outputFile.write(completeMessage);
        outputFile.flush();
      } catch (IOException e) {
        enableFileOutput = false;
        this.stackTraceWithMessage("Errore durante il logging su file. Logging disabilitato", e);
      }
    }
  }
}
