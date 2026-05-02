package com.meradeya.app.dto.seed;

import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import java.util.List;
import lombok.Data;

@Data
public class CategorySeedFile {

  @JsonSetter(nulls = Nulls.AS_EMPTY)
  private List<CategorySeedEntry> categories = List.of();
}

