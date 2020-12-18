package com.fabifont.aliexpress.util;

import lombok.experimental.UtilityClass;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@UtilityClass
public class Parser {
  public Optional<Properties> parseConfig(File file) {
    try (var in = new FileInputStream(file)) {
      var prop = new Properties();
      prop.load(in);
      return Optional.of(prop);
    } catch (IOException e) {
      return Optional.empty();
    }
  }

  public Optional<Integer> parseInteger(String in) {
    try {
      return Optional.of(Integer.parseInt(in));
    } catch (Exception e) {
      return Optional.empty();
    }
  }

  public Optional<Boolean> parseBoolean(String in) {
    try {
      if (!in.equals("true") && !in.equals("false")) {
        return Optional.empty();
      }

      return Optional.of(Boolean.parseBoolean(in));
    } catch (Exception e) {
      return Optional.empty();
    }
  }

  public <T> Optional<T> unboxFuture(Future<T> future) {
    try {
      return Optional.of(future.get());
    } catch (InterruptedException | ExecutionException e) {
      return Optional.empty();
    }
  }

  public List<List<String>> parseList(String source) {
    return Pattern
        .compile("\\[(.*?)]")
        .matcher(source)
        .results()
        .map(MatchResult::group)
        .map(e -> e.replace("[", "").replaceAll("]", ""))
        .map(e -> Arrays.asList(e.split(",")))
        .collect(Collectors.toList());
  }
}