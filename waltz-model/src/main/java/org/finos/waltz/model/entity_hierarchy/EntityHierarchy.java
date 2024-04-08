package org.finos.waltz.model.entity_hierarchy;

import org.immutables.value.Value;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Value.Immutable
public abstract class EntityHierarchy {

    public abstract List<EntityHierarchyItem> hierarchyItems();

    public Set<Long> findChildren(long parentId) {
        return hierarchyItems()
                .stream()
                .filter(t -> t.id().isPresent() && t.parentId().isPresent() && t.parentId().get() == parentId)
                .map(t -> t.id().get())
                .collect(Collectors.toSet());
    }

    public Set<Long> findAncestors(long childId) {
        return hierarchyItems()
                .stream()
                .filter(t -> t.id().isPresent() && t.parentId().isPresent() && t.id().get() == childId)
                .map(t -> t.parentId().get())
                .collect(Collectors.toSet());
    }
}
