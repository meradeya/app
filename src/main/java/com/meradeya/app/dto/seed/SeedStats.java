package com.meradeya.app.dto.seed;

import lombok.Getter;

/**
 * Counters accumulated during a seeder run.
 */
@Getter
public class SeedStats {

  private int created;
  private int updated;
  private int deleted;

  public void incrementCreated() { created++; }
  public void incrementUpdated() { updated++; }
  public void incrementDeleted() { deleted++; }
}
