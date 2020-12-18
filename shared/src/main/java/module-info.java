module shared {
  requires lombok;
  requires org.seleniumhq.selenium.chrome_driver;
  requires org.seleniumhq.selenium.support;
  requires org.apache.commons.lang3;
  requires zip4j;
  requires java.net.http;
  exports com.fabifont.aliexpress.account;
  exports com.fabifont.aliexpress.config;
  exports com.fabifont.aliexpress.util;
  exports com.fabifont.aliexpress.process;
  exports com.fabifont.aliexpress.exception;
  exports com.fabifont.aliexpress.link;
}