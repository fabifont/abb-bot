package com.fabifont.aliexpress.link;

import com.fabifont.aliexpress.config.Config;
import com.fabifont.aliexpress.config.Settings;
import lombok.experimental.UtilityClass;

@UtilityClass
public class StateChecker {
  public State checkState(Config config, Settings settings) {
    return config.getDoneLimit() != 0 && settings.getCompleted().get() == config.getDoneLimit() ? State.DONE_LIMIT : config.getErrorLimit() != 0 && settings.getErrors().get() == config.getErrorLimit() ? State.ERROR_LIMIT : State.WORKING;
  }

  public enum State {
    DONE_LIMIT,
    ERROR_LIMIT,
    WORKING
  }
}
