package com.meradeya.app.service.helper;

import com.meradeya.app.domain.entity.Category;
import com.meradeya.app.domain.repository.CategoryRepository;
import com.meradeya.app.dto.seed.CategorySeedEntry;
import com.meradeya.app.dto.seed.SeedStats;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@Transactional(propagation = Propagation.MANDATORY)
@RequiredArgsConstructor
public class CategorySeederHelper {

  private final CategoryRepository categoryRepository;

  /**
   * Upserts a single YAML entry and recurses into its children (DFS).
   * All upserts for one root subtree share the same transaction (default REQUIRED propagation).
   *
   * <p>Rules:
   * <ul>
   *   <li>Slug not in DB → INSERT</li>
   *   <li>Slug in DB, name or parent differs → UPDATE</li>
   *   <li>Slug in DB, nothing changed → no-op</li>
   * </ul>
   */
  public void upsert(CategorySeedEntry entry, Category parent,
      Map<String, Category> dbState, SeedStats stats) {

    log.info("slug={}, parent={}", entry.getSlug(),
        parent != null ? parent.getSlug() : "null");

    Category category = dbState.get(entry.getSlug());

    if (category == null) {
      category = new Category(entry.getName(), entry.getSlug());
      if (parent != null) {
        parent.addChild(category);
      }
      category = categoryRepository.save(category);
      dbState.put(entry.getSlug(), category);
      stats.incrementCreated();
      log.info("created '{}'", entry.getSlug());

    } else {
      boolean nameChanged = !entry.getName().equals(category.getName());
      UUID currentParentId = category.getParent() != null ? category.getParent().getId() : null;
      UUID expectedParentId = parent != null ? parent.getId() : null;
      boolean parentChanged = !Objects.equals(currentParentId, expectedParentId);

      if (nameChanged) {
        category.setName(entry.getName());
      }
      if (parentChanged) {
        category.detachFromParent();
        if (parent != null) {
          parent.addChild(category);
        }
      }
      if (nameChanged || parentChanged) {
        categoryRepository.save(category);
        stats.incrementUpdated();
        log.info("updated '{}' (nameChanged={}, parentChanged={})",
            entry.getSlug(), nameChanged, parentChanged);
      }
    }

    for (CategorySeedEntry child : entry.getChildren()) {
      upsert(child, category, dbState, stats);
    }

    log.info("completed slug={}", entry.getSlug());
  }

  public void delete(Category category, SeedStats stats) {
    log.info("Deleting '{}'", category.getSlug());
    categoryRepository.delete(category);
    stats.incrementDeleted();
  }
}
