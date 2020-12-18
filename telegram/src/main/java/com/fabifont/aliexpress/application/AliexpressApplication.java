package com.fabifont.aliexpress.application;

import com.fabifont.aliexpress.config.Config;
import com.fabifont.aliexpress.process.AliexpressProcessRunner;
import com.fabifont.aliexpress.util.Constants;
import com.fabifont.aliexpress.util.Parser;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Scanner;

public class AliexpressApplication {
  public static void main(String[] args) {
    var configFile = new File(Constants.BASE_PATH, Constants.CONFIG_NAME);
    var properties = Parser.parseConfig(configFile).orElseThrow(() -> new RuntimeException("The configuration file is corrupted!"));

    String links = properties.getProperty("links");

    Scanner sc = new Scanner(System.in);

    System.out.println("Stai farmando " + links);
    System.out.print("Inserisci il codice del bot telegram: ");
    String hash = sc.nextLine();

    if (!validateConfig(links, hash)) {
      System.err.println("Il codice inserito e' sbagliato! Mi chiudo tra 10 secondi");

      try {
        Thread.sleep(10000);
      } catch (InterruptedException e) {
      }
      return;
    }

    AliexpressProcessRunner.runOnMainThread();
  }

  private static boolean validateConfig(String plainText, String hashSalted) {
    try {
      //Declare fields for verification
      final var messageDigest = MessageDigest.getInstance("SHA-512");
      final var builder = new StringBuilder();

      //Find data
      var plainTextSalted = plainText + Constants.SALT;
      var hashSaltedBytes = messageDigest.digest(plainTextSalted.getBytes(StandardCharsets.UTF_8));
      for (var b : hashSaltedBytes) builder.append(String.format("%02x", b));

      return builder.toString().equals(hashSalted);
    } catch (NoSuchAlgorithmException e) {
      System.err.println("Impossibile effettuare la verifica iniziale!");
      return false;
    }
  }
}
