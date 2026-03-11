package com.frytes.cloudstorage.common.util;

import com.frytes.cloudstorage.common.exception.InvalidPathException;
import lombok.experimental.UtilityClass;

@UtilityClass
public class PathUtils {

    public static String sanitize(String path) {
        if (path == null || path.isBlank()) {
            return "";
        }

        if (path.contains("..")) {
            throw new InvalidPathException("Недопустимый путь: обнаружена попытка выхода за пределы директории");
        }

        return path.trim()
                .replaceAll("/+", "/")
                .replaceAll("^/", "");
    }

    public static String ensureTrailingSlash(String path) {
        if (!path.isEmpty() && !path.endsWith("/")) {
            return path + "/";
        }
        return path;
    }
    public static String replacePrefix(String fullPath, String oldPrefix, String newPrefix) {
        return fullPath.replaceFirst(
                java.util.regex.Pattern.quote(oldPrefix),
                java.util.regex.Matcher.quoteReplacement(newPrefix)
        );
    }

    public static String buildUserPath(Long userId, String path) {
        return "user-" + userId + "-files/" + path;
    }

    public static String getFileNameFromPath(String path) {
        boolean isDir = path.endsWith("/");
        if (isDir) {
            path = path.substring(0, path.length() - 1);
        }

        int lastSlashIndex = path.lastIndexOf('/');
        String name = path.substring(lastSlashIndex + 1);

        return isDir ? name + "/" : name;
    }
}