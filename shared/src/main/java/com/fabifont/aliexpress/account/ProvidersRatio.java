package com.fabifont.aliexpress.account;

public class ProvidersRatio {
  public int successi = 1;
  public int totali = 1;
  public int errori_consecutivi = 0;
  public String name;

  public ProvidersRatio(String _name) {
    this.name = _name;
  }

  public float getRatio() {
    return (float) this.successi / this.totali;
  }
}
