package org.finos.waltz.model.entity_hierarchy;

import org.immutables.value.Value;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Value.Immutable
public abstract class EntityHierarchy {

    public abstract List<EntityHierarchyItem> hierarchyItems();

    public Set<Long> findChildren(Long parentId) {
        if(parentId == null) {
            return Collections.emptySet();
        } else {
            return hierarchyItems()
                    .stream()
                    .filter(t -> t.id().isPresent() && t.parentId().isPresent() && t.parentId().get().equals(parentId))
                    .map(t -> t.id().get())
                    .collect(Collectors.toSet());
        }
    }

    public Set<Long> findAncestors(Long childId) {
        if(childId == null) {
            return Collections.emptySet();
        } else {
            return hierarchyItems()
                    .stream()
                    .filter(t -> t.id().isPresent() && t.parentId().isPresent() && t.id().get().equals(childId))
                    .map(t -> t.parentId().get())
                    .collect(Collectors.toSet());
        }
    }
}
