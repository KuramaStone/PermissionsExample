package com.github.kuramastone.permissionTrial.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ComponentEditor {

    private String text;

    public ComponentEditor(String string) {
        this.text = string;
    }

    public ComponentEditor copy() {
        return new ComponentEditor(text);
    }

    public ComponentEditor replace(String key, String result) {
        text = text.replace(key, result);
        return this;
    }

    public TextComponent build() {
        return decorateComponent(text);
    }


    /**
     * Colors a text and returns it as a component.
     *
     * @param input
     * @return
     */
    public static TextComponent decorateComponent(String input) {
        if(input == null || input.isEmpty()) {
            return Component.text("");
        }

        input = input.replace("\\s", " "); // convert spaces

        if (!input.contains("&")) {
            return Component.text(input);
        }

        String regex = "(&[a-fr0-9klmno])?([^&]*)";

        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(input);

        List<Map.Entry<String, String>> result = new ArrayList<>();

        StringBuilder combinedColorCodes = new StringBuilder();

        while (matcher.find()) {
            // matcher.group(1) is the color code (with the '&')
            // matcher.group(2) is the text
            // ignore empty text

            if(matcher.group(0).isEmpty() && (matcher.group(2) == null || matcher.group(2).isEmpty())) {
                continue;
            }

            // if the string doesnt start with a color, it would trim the start. This manually inserts it even in that scenario
            if(matcher.group(0).isBlank()) {
                result.add(Map.entry("", matcher.group(0)));
                continue;
            }

            // Remove the '&' from the color code
            String colorCode = matcher.group(1).substring(1);

            // If there is an ongoing color code, concatenate it with the current one
            combinedColorCodes.append(colorCode);

            // If there's text, store the combined color code and the text
            if (!matcher.group(2).isEmpty()) {
                result.add(Map.entry(combinedColorCodes.toString(), matcher.group(2)));
                combinedColorCodes = new StringBuilder(); // Reset the color code for the next match
            }
        }

        TextComponent.Builder builder = Component.text();
        for (Map.Entry<String, String> set : result) {
            String codes = set.getKey();
            String msg = set.getValue();


            TextComponent comp = Component.text(msg);

            for (char c : codes.toCharArray()) {
                NamedTextColor color = colorByChar.get(c);
                TextDecoration decor = decorationMap.get(c);

                if (color != null) {
                    comp = comp.color(color);
                }
                if (decor != null) {
                    comp = comp.decorate(decor);
                }

            }

            builder.append(comp);

        }

        return builder.build();
    }

    public String getText() {
        return text;
    }

    public static final Map<Character, NamedTextColor> colorByChar = new HashMap<>();
    public static final Map<NamedTextColor, Character> charByColor = new HashMap<>();
    public static final Map<Character, TextDecoration> decorationMap = new HashMap<>();

    static {
        // Initialize the color map
        colorByChar.put('0', NamedTextColor.BLACK);
        colorByChar.put('1', NamedTextColor.DARK_BLUE);
        colorByChar.put('2', NamedTextColor.DARK_GREEN);
        colorByChar.put('3', NamedTextColor.DARK_AQUA);
        colorByChar.put('4', NamedTextColor.DARK_RED);
        colorByChar.put('5', NamedTextColor.DARK_PURPLE);
        colorByChar.put('6', NamedTextColor.GOLD);
        colorByChar.put('7', NamedTextColor.GRAY);
        colorByChar.put('8', NamedTextColor.DARK_GRAY);
        colorByChar.put('9', NamedTextColor.BLUE);
        colorByChar.put('a', NamedTextColor.GREEN);
        colorByChar.put('b', NamedTextColor.AQUA);
        colorByChar.put('c', NamedTextColor.RED);
        colorByChar.put('d', NamedTextColor.LIGHT_PURPLE);
        colorByChar.put('e', NamedTextColor.YELLOW);
        colorByChar.put('f', NamedTextColor.WHITE);
        colorByChar.forEach((k, v) -> charByColor.put(v, k));

        // Initialize the decoration map
        decorationMap.put('k', TextDecoration.OBFUSCATED);
        decorationMap.put('l', TextDecoration.BOLD);
        decorationMap.put('m', TextDecoration.STRIKETHROUGH);
        decorationMap.put('n', TextDecoration.UNDERLINED);
        decorationMap.put('o', TextDecoration.ITALIC);
    }
}
