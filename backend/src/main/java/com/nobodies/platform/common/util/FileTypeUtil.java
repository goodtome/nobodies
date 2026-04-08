package com.nobodies.platform.common.util;

import java.util.Arrays;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

public final class FileTypeUtil {
    private FileTypeUtil() {
    }

    public static Set<String> parseAllowedTypes(String configValue) {
        return Arrays.stream(configValue.split(","))
            .map(item -> item.trim().toLowerCase(Locale.ROOT))
            .collect(Collectors.toSet());
    }

    public static String normalizeExtension(String fileName) {
        int idx = fileName.lastIndexOf('.');
        return idx >= 0 ? fileName.substring(idx + 1).toLowerCase(Locale.ROOT) : "";
    }
}
