package com.fabifont.aliexpress.link;

import lombok.Data;

import java.util.concurrent.atomic.AtomicBoolean;

@Data
public class Link {
  private final String link;
  private final AtomicBoolean done;

  private Link(String link) {
    this.link = link;
    this.done = new AtomicBoolean();
  }

  public static Link of(String link) {
    return new Link(link);
  }
}