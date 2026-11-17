package com.harmoni.pos.order.component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Immutable view of the stores a caller may report on, derived from the
 * organisation scope the gateway resolved for them.
 */
public final class CallerScope {

    private static final CallerScope GLOBAL = new CallerScope(true, Set.of());
    private static final CallerScope NONE = new CallerScope(false, Set.of());

    private final boolean global;
    private final Set<Integer> storeIds;

    private CallerScope(boolean global, Set<Integer> storeIds) {
        this.global = global;
        this.storeIds = Collections.unmodifiableSet(new LinkedHashSet<>(storeIds));
    }

    public static CallerScope global() {
        return GLOBAL;
    }

    public static CallerScope none() {
        return NONE;
    }

    public static CallerScope of(Set<Integer> storeIds) {
        return new CallerScope(false, storeIds);
    }

    public boolean isGlobal() {
        return global;
    }

    public Set<Integer> getStoreIds() {
        return storeIds;
    }

    public boolean canAccessStore(Integer storeId) {
        return global || storeId == null || storeIds.contains(storeId);
    }

    /**
     * Intersects the requested store ids with the caller's accessible stores.
     *
     * @param requested the requested store ids
     * @return the allowed store ids, never {@code null}
     */
    public List<Integer> filterStoreIds(Collection<Integer> requested) {
        if (requested == null) {
            return new ArrayList<>();
        }
        if (global) {
            return new ArrayList<>(requested.stream().filter(Objects::nonNull).distinct().toList());
        }
        return new ArrayList<>(requested.stream()
                .filter(Objects::nonNull)
                .filter(storeIds::contains)
                .distinct()
                .toList());
    }
}
