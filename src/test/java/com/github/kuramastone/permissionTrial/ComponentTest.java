package com.github.kuramastone.permissionTrial;

import com.github.kuramastone.permissionTrial.utils.ComponentEditor;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ComponentTest {

    @Test
    public void testReplacements() {
        TextComponent editor = new ComponentEditor("Hello {player}, you are pretty {adj}")
                .replace("{player}", "PlayerName").replace("{adj}", "cute").build();

        Assertions.assertEquals(editor.content(), "Hello PlayerName, you are pretty cute");
    }

    @Test
    public void testLiteralResult() {
        TextComponent result = new ComponentEditor("&l&aBoldGreen&2NormalRed").build();

        // test literal content
        String resultText = PlainTextComponentSerializer.plainText().serialize(result);
        Assertions.assertEquals("BoldGreenNormalRed", resultText);
    }

    /*
    Used to have an issue loading these
     */
    @Test
    public void testBufferedSpaces() {
        TextComponent result = new ComponentEditor("     &l&aBoldGreen").build();
        String resultText = PlainTextComponentSerializer.plainText().serialize(result);

        // test literal content
        Assertions.assertEquals("     BoldGreen", resultText);
    }

}
