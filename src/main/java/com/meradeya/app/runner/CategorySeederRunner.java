package com.meradeya.app.runner;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.meradeya.app.domain.entity.Category;
import com.meradeya.app.domain.repository.CategoryRepository;
import com.meradeya.app.dto.seed.CategorySeedEntry;
import com.meradeya.app.dto.seed.CategorySeedFile;
import com.meradeya.app.dto.seed.SeedStats;
import com.meradeya.app.service.helper.CategorySeederHelper;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Synchronises the category tree on every application startup.
 *
 * <p>Source of truth: {@code classpath:seed/categories.yaml}.
 *
 * <p>Sync rules (slug is the stable natural key):
 * <ul>
 *   <li><b>Create</b> — slug present in YAML, absent in DB.</li>
 *   <li><b>Update</b> — slug present in both; name or parent differs.</li>
 *   <li><b>Delete</b> — slug absent in YAML, present in DB. Attempted only if no listings
 *       or child categories reference the row; otherwise a warning is logged and the row
 *       is left untouched.</li>
 *   <li><b>No-op</b> — slug present in both, nothing changed.</li>
 * </ul>
 *
 */
@Slf4j
@Component
@Transactional
@RequiredArgsConstructor
public class CategorySeederRunner implements CommandLineRunner {

  private static final String SEED_RESOURCE = "classpath:seed/categories.yaml";

  private final CategoryRepository categoryRepository;
  private final CategorySeederHelper helper;
  private final ResourceLoader resourceLoader;

  //todo: revise this approach
  @Override
  public void run(String @NonNull ... args) throws Exception {
    log.info("Running category seeder");

    Resource resource = resourceLoader.getResource(SEED_RESOURCE);
    if (!resource.exists()) {
      log.warn("Categories file not found at {}, skipping.", SEED_RESOURCE);
      return;
    }

    CategorySeedFile seedFile = parse(resource);
    if (seedFile.getCategories().isEmpty()) {
      log.info("Categories file is empty.");
      return;
    }

    Map<String, Category> dbState = categoryRepository.findAll()
        .stream()
        .collect(Collectors.toMap(Category::getSlug, c -> c));

    Set<String> yamlSlugs = new HashSet<>();
    collectSlugs(seedFile.getCategories(), yamlSlugs);

    Map<String, Category> workingState = new HashMap<>(dbState);
    SeedStats stats = new SeedStats();

    for (CategorySeedEntry root : seedFile.getCategories()) {
      helper.upsert(root, null, workingState, stats);
    }

    List<Category> orphans = dbState.entrySet().stream()
        .filter(e -> !yamlSlugs.contains(e.getKey()))
        .map(Map.Entry::getValue)
        .toList();

    for (Category orphan : orphans) {
      helper.delete(orphan, stats);
    }

    log.info("Completed — created={}, updated={}, deleted={}",
        stats.getCreated(), stats.getUpdated(), stats.getDeleted());
  }

  private CategorySeedFile parse(Resource resource) throws IOException {
    ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
    mapper.findAndRegisterModules();
    try (InputStream in = resource.getInputStream()) {
      return mapper.readValue(in, CategorySeedFile.class);
    }
  }

  private void collectSlugs(List<CategorySeedEntry> entries, Set<String> slugs) {
    for (CategorySeedEntry entry : entries) {
      slugs.add(entry.getSlug());
      collectSlugs(entry.getChildren(), slugs);
    }
  }
}

