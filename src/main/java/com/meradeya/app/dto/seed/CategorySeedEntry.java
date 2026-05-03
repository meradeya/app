package com.meradeya.app.dto.seed;

import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import java.util.List;
import lombok.Data;

@Data
public class CategorySeedEntry {

  private String slug;
  private String name;

  @JsonSetter(nulls = Nulls.AS_EMPTY)
  private List<CategorySeedEntry> children = List.of();
}

