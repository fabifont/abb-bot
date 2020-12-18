package com.fabifont.aliexpress.util;

import lombok.experimental.UtilityClass;
import net.lingala.zip4j.ZipFile;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

@UtilityClass
public class FileUtils {
  public Optional<String> copyFromJar(String source, File targetDirectory, Class<?> clazz) {
    try {
      var uBlock = new File(targetDirectory, "/uBlock");
      if (uBlock.exists()) {
        return Optional.of(uBlock.getPath());
      }

      if (!source.endsWith(".zip")) {
        throw new IllegalArgumentException("This method only accepts zip files");
      }

      var tempSourceFile = new File(System.getProperty("user.home") + "/aliexpress", "uBlock.zip");
      Files.write(tempSourceFile.toPath(), Objects.requireNonNull(clazz.getClassLoader().getResourceAsStream(tempSourceFile.getName()), tempSourceFile.getName() + " missing in jar file!").readAllBytes());
      var zipFile = new ZipFile(tempSourceFile);
      zipFile.extractAll(targetDirectory.getPath());
      tempSourceFile.delete();
      return Optional.of(uBlock.getPath());
    } catch (IOException e) {
      e.printStackTrace();
      return Optional.empty();
    }
  }

  public void deleteFolder(File file) {
    Arrays.stream(Objects.requireNonNull(file.listFiles())).forEach(subFile ->
    {
      if (subFile.isDirectory()) {
        deleteFolder(subFile);
      } else {
        subFile.delete();
      }
    });

    file.delete();
  }

  //Creates a default file taken from the jar file if there isn't one in the expected directory
  public void createFileDefaults(File file, Class<?> clazz, Runnable callback) {
    if (!file.exists()) {
      file.getParentFile().mkdirs();
      try {
        file.createNewFile();
        Files.write(file.toPath(), Objects.requireNonNull(clazz.getClassLoader().getResourceAsStream(file.getName()), file.getName() + " missing in jar file!").readAllBytes());
      } catch (IOException e) {
        e.printStackTrace();
      }

      callback.run();
    }
  }

  //Creates a default file taken from the jar file if there isn't one in the expected directory
  public void createFolderOrDeleteContent(File file) {
    if (!file.exists()) {
      file.mkdirs();
    } else {
      Arrays.stream(Objects.requireNonNull(file.listFiles())).forEach(FileUtils::deleteFolder);
    }
  }

  public String generateMD5(String input) {
    try {
      var messageDigest = MessageDigest.getInstance("MD5");
      messageDigest.update(input.getBytes(StandardCharsets.UTF_8));

      var hashBytes = messageDigest.digest();

      var sb = new StringBuilder();
      for (byte b : hashBytes) {
        sb.append(String.format("%02x", b));
      }

      return sb.toString();
    } catch (NoSuchAlgorithmException e) {
      System.err.println("Impossibile utilizzare i metodi avanzati di caching. Contatta un releaser");
      e.printStackTrace();
      return "";
    }
  }
}
