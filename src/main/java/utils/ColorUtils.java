package com.igngecko.zyphercrates.utils;

import net.md_5.bungee.api.ChatColor;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ColorUtils {

    // Pattern to match hex color codes like &#RRGGBB
    private static final Pattern HEX_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})");

    /**
     * Translates both standard (&) and hex (&#RRGGBB) color codes.
     *
     * @param text The text to translate.
     * @return The translated text.
     */
    public static String translate(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }

        Matcher matcher = HEX_PATTERN.matcher(text);
        StringBuffer buffer = new StringBuffer(text.length() + 4 * 8); // Preallocate buffer space

        while (matcher.find()) {
            String group = matcher.group(1);
            // Append replacement, using ChatColor.of for hex codes
            matcher.appendReplacement(buffer, ChatColor.of("#" + group).toString());
        }
        matcher.appendTail(buffer);

        // Translate standard & codes after hex codes are processed
        return ChatColor.translateAlternateColorCodes('&', buffer.toString());
    }

    /**
     * Translates colors in a list of strings.
     *
     * @param list The list of strings to translate.
     * @return A new list with translated strings.
     */
    public static List<String> translate(List<String> list) {
        if (list == null || list.isEmpty()) {
            return list;
        }
        return list.stream()
                   .map(ColorUtils::translate)
                   .collect(Collectors.toList());
    }
}
