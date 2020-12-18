package com.fabifont.aliexpress.config;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Data
public class Settings {
  // Number of completed accounts
  private AtomicInteger completed;

  // Number of twitter completed accounts
  private AtomicInteger twitterCompletedSinceChangeIp = new AtomicInteger();

  // Number of unchecked errors
  private AtomicInteger errors;

  // Number of ip bans
  private AtomicInteger ipBans;

  // Change IP flag
  private AtomicBoolean changeIp;

  //Should reverse collection
  private AtomicBoolean shouldReverseOrder;

  public Settings() {
    this(new AtomicInteger(), new AtomicInteger(), new AtomicInteger(), new AtomicInteger(), new AtomicBoolean(), new AtomicBoolean());
  }

  public void newIpError() {
    this.ipBans.addAndGet(1);
    this.changeIp.set(true);
  }
}
