package com.harmoni.pos.order.component;

import com.harmoni.pos.order.business.service.rest.RestClientService;
import com.harmoni.pos.order.config.MenuProperties;
import com.harmoni.pos.order.http.response.RestAPIResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Expands the caller's {@code X-Scopes} header into the store ids they may
 * report on.
 *
 * <p>The order service only stores store ids, so brand and chain assignments
 * are expanded by the menu service, whose internal scope endpoint already
 * understands them. A missing or blank header keeps the legacy unrestricted
 * behaviour; a header that cannot be expanded falls back to the store
 * assignments it carries, so an unavailable menu service cannot widen access.</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CallerScopeService {

    public static final String HEADER_SCOPES = "X-Scopes";
    public static final String HEADER_USERNAME = "X-Username";

    private final RestClientService restClientService;
    private final MenuProperties menuProperties;

    /**
     * Resolves a raw {@code X-Scopes} header value.
     *
     * @param header   comma separated {@code TYPE:id} assignments, may be {@code null}
     * @param username the caller's username, forwarded to the menu scope endpoint
     * @return the resolved scope
     */
    public CallerScope resolve(String header, String username) {
        if (header == null || header.isBlank()) {
            return CallerScope.global();
        }
        if (containsGlobal(header)) {
            return CallerScope.global();
        }
        try {
            RestAPIResponse response = restClientService
                    .get(username == null ? "" : username, menuProperties.getScopeStores())
                    .block();
            if (response != null && response.getData() instanceof Map<?, ?> data) {
                if (Boolean.TRUE.equals(data.get("global"))) {
                    return CallerScope.global();
                }
                return CallerScope.of(readStoreIds(data.get("storeIds")));
            }
        } catch (RuntimeException exception) {
            log.warn("Could not expand caller scope, falling back to store assignments", exception);
        }
        return CallerScope.of(parseStoreScopes(header));
    }

    private static boolean containsGlobal(String header) {
        for (String token : header.split(",")) {
            if (token.trim().equalsIgnoreCase("GLOBAL")) {
                return true;
            }
        }
        return false;
    }

    private static Set<Integer> readStoreIds(Object value) {
        Set<Integer> storeIds = new LinkedHashSet<>();
        if (value instanceof Collection<?> collection) {
            for (Object item : collection) {
                Integer id = parseInt(item);
                if (id != null) {
                    storeIds.add(id);
                }
            }
        }
        return storeIds;
    }

    private static Set<Integer> parseStoreScopes(String header) {
        Set<Integer> storeIds = new LinkedHashSet<>();
        for (String token : header.split(",")) {
            String[] parts = token.trim().split(":");
            if (parts.length == 2 && parts[0].trim().toUpperCase(Locale.ROOT).equals("STORE")) {
                Integer id = parseInt(parts[1]);
                if (id != null) {
                    storeIds.add(id);
                }
            }
        }
        return storeIds;
    }

    private static Integer parseInt(Object value) {
        if (value == null) {
            return null;
        }
        try {
            return Integer.valueOf(value.toString().trim());
        } catch (NumberFormatException exception) {
            return null;
        }
    }
}
