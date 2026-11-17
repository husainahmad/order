package com.harmoni.pos.order.component;

/**
 * Holds the {@link CallerScope} resolved by {@link CallerScopeFilter} for the
 * duration of one request.
 *
 * <p>Outside a request (scheduled work, tests) the context falls back to the
 * unrestricted legacy scope.</p>
 */
public final class CallerScopeContext {

    private static final ThreadLocal<CallerScope> HOLDER = new ThreadLocal<>();

    private CallerScopeContext() {
    }

    public static void set(CallerScope scope) {
        HOLDER.set(scope == null ? CallerScope.global() : scope);
    }

    public static CallerScope current() {
        CallerScope scope = HOLDER.get();
        return scope == null ? CallerScope.global() : scope;
    }

    public static void clear() {
        HOLDER.remove();
    }
}
