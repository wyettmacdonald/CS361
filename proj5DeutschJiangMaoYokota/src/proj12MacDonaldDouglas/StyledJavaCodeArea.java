/*
 * File: StyledJavaCodeArea.java
 * CS361 Project 9
 * Names: Douglas Abrams, Martin Deutsch, Robert Durst, Matt Jones
 * Date: 11/20/2018
 * This file contains the StyledJavaCodeArea class, which extends the CodeArea class
 * to handle syntax highlighting.
 */

package proj12MacDonaldDouglas;

import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;
import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;
import org.reactfx.Subscription;
import java.time.Duration;
import java.util.Collection;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class extends the CodeArea class from RichTextFx to handle
 * syntax highlighting.
 *
 * @author Douglas Abrams
 * @author Martin Deutsch
 * @author Robert Durst
 * @author Matt Jones
 */
public class StyledJavaCodeArea extends CodeArea {
    /**
     * a list of key words to be highlighted
     */
    private static final String[] KEYWORDS = new String[] {
            "abstract", "assert", "boolean", "break", "byte",
            "case", "catch", "char", "class", "const",
            "continue", "default", "do", "double", "else",
            "enum", "extends", "final", "finally", "float",
            "for", "goto", "if", "implements", "import",
            "instanceof", "int", "interface", "long", "native",
            "new", "package", "private", "protected", "public",
            "return", "short", "static", "strictfp", "super",
            "switch", "synchronized", "this", "throw", "throws",
            "transient", "try", "void", "volatile", "while", "var"
    };

    /**
     * regular expressions of characters to be highlighted
     */
    private static final String KEYWORD_PATTERN = "\\b(" + String.join("|", KEYWORDS) + ")\\b";
    private static final String PAREN_PATTERN = "\\(|\\)";
    private static final String BRACE_PATTERN = "\\{|\\}";
    private static final String BRACKET_PATTERN = "\\[|\\]";
    private static final String SEMICOLON_PATTERN = "\\;";
    private static final String STRING_PATTERN = "\"([^\"\\\\]|\\\\.)*\"";
    private static final String COMMENT_PATTERN = "//[^\n]*" + "|" + "/\\*(.|\\R)*?\\*/";
    private static final String INTEGER_PATTERN = "(?<![\\w])(?<![\\d.])[0-9]+(?![\\d.])(?![\\w])";

    /**
     * patterns to be highlighted
     */
    private static final Pattern PATTERN = Pattern.compile(
            "(?<KEYWORD>" + KEYWORD_PATTERN + ")"
                    + "|(?<PAREN>" + PAREN_PATTERN + ")"
                    + "|(?<BRACE>" + BRACE_PATTERN + ")"
                    + "|(?<BRACKET>" + BRACKET_PATTERN + ")"
                    + "|(?<SEMICOLON>" + SEMICOLON_PATTERN + ")"
                    + "|(?<STRING>" + STRING_PATTERN + ")"
                    + "|(?<COMMENT>" + COMMENT_PATTERN + ")"
                    + "|(?<INTEGER>" + INTEGER_PATTERN + ")"
    );

    /**
     * Constructor that sets initial text and sets the behavior of the code area on text change
     */
    public StyledJavaCodeArea(String content) {
        this.setOnKeyPressed((event) -> {
            this.handleTextChange();
        });
        this.appendText(content);
        this.highlightText();
        this.setParagraphGraphicFactory(LineNumberFactory.get(this));
        this.createContextMenu();
    }

    /**
     * Helper function to highlight the text within the StyledJavaCodeArea.
     */
    private void highlightText() {
        this.setStyleSpans(0, this.computeHighlighting(this.getText()));
    }

    /**
     * Handles the text change action.
     * Listens to the text changes and highlights the keywords in real-time.
     */
    private void handleTextChange() {
        Subscription cleanupWhenNoLongerNeedIt = this

                // plain changes = ignore style changes that are emitted when syntax highlighting is reapplied
                // multi plain changes = save computation by not rerunning the code multiple times
                // when making multiple changes (e.g. renaming a method at multiple parts in file)
                .multiPlainChanges()

                // do not emit an event until 100 ms have passed since the last emission of previous stream
                .successionEnds(Duration.ofMillis(100))

                // run the following code block when previous stream emits an event
                .subscribe(ignore -> this.highlightText());
    }


    /**
     * Computes the highlighting of substrings of text to return the style of each substring.
     *
     * @param text string to compute highlighting of
     * @return StyleSpans Collection Object
     */
    private static StyleSpans<Collection<String>> computeHighlighting(String text) {
        Matcher matcher = PATTERN.matcher(text);
        int lastKwEnd = 0;
        StyleSpansBuilder<Collection<String>> spansBuilder
                = new StyleSpansBuilder<>();
        while(matcher.find()) {
            String styleClass =
                    matcher.group("KEYWORD") != null ? "keyword" :
                            matcher.group("PAREN") != null ? "paren" :
                                    matcher.group("BRACE") != null ? "brace" :
                                            matcher.group("BRACKET") != null ? "bracket" :
                                                    matcher.group("SEMICOLON") != null ? "semicolon" :
                                                            matcher.group("STRING") != null ? "string" :
                                                                    matcher.group("COMMENT") != null ? "comment" :
                                                                            matcher.group("INTEGER") != null ? "integer" :
                                                                                    null; /* never happens */ assert styleClass != null;
            spansBuilder.add(Collections.emptyList(), matcher.start() - lastKwEnd);
            spansBuilder.add(Collections.singleton(styleClass), matcher.end() - matcher.start());
            lastKwEnd = matcher.end();
        }
        spansBuilder.add(Collections.emptyList(), text.length() - lastKwEnd);
        return spansBuilder.create();
    }

    /**
     * Handle a pop-up window. When a new tab is created a related ContextMenu will also be created.
     * When right button is clicked it will show up with some functional menus.
     * If the primary button is clicked outside of the menu the menu will be hidden.
     */
    private void createContextMenu() {
        // setup context menu
        ContextMenu contextMenu = new ContextMenu();
        MenuItem cut = new MenuItem("Cut");
        MenuItem copy = new MenuItem("Copy");
        MenuItem paste = new MenuItem("Paste");
        MenuItem undo = new MenuItem("Undo");
        MenuItem redo = new MenuItem("Redo");
        MenuItem selectAll = new MenuItem("Select All");

        // set onAction for each option in the context menu
        contextMenu.getItems().addAll(cut, copy, paste, undo, redo, selectAll);
        cut.setOnAction(event -> this.cut());
        copy.setOnAction(event -> this.copy());
        paste.setOnAction(event -> this.paste());
        undo.setOnAction(event -> this.undo());
        redo.setOnAction(event -> this.redo());
        selectAll.setOnAction(event -> this.selectAll());

        // sets the onMousePressed to show the context menu when the secondary button
        // is pressed and to hide it if the primary button is pressed and the menu is
        // already showing
        this.setOnMousePressed(event -> {
            if (event.isSecondaryButtonDown()) {
                contextMenu.show(this, event.getScreenX(), event.getScreenY());
            } else if (event.isPrimaryButtonDown() && contextMenu.isShowing()) {
                contextMenu.hide();
            }
        });
    }
}
