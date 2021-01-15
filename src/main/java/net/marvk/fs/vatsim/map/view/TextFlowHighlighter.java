package net.marvk.fs.vatsim.map.view;

import com.google.inject.Inject;
import javafx.beans.property.IntegerProperty;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import net.marvk.fs.vatsim.map.data.Preferences;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class TextFlowHighlighter {
    private Font boldDefault;

    private Font boldMono;

    private Font standardMono;

    private Font standardDefault;

    private static final Pattern REPLACING = Pattern.compile("%[rm]");

    @Inject
    public TextFlowHighlighter(final Preferences preferences) {
        final IntegerProperty fontSizeProperty = preferences.integerProperty("general.font_size");
        fontSizeProperty.addListener((observable, oldValue, newValue) -> setFonts(newValue.doubleValue()));
        setFonts(fontSizeProperty.doubleValue());
    }

    private void setFonts(final double fontSize) {
        standardMono = Font.font(
                "B612 Mono",
                fontSize
        );
        boldMono = Font.font(
                "B612 Mono",
                FontWeight.BOLD,
                fontSize
        );
        boldDefault = Font.font(
                "B612",
                FontWeight.BOLD,
                fontSize
        );
        standardDefault = Font.font(
                "B612",
                fontSize
        );
    }

    public Text[] textFlows(final String s, final Pattern pattern, final String... items) {
        final List<MatchResult> matches = REPLACING.matcher(s).results().collect(Collectors.toList());

        if (matches.size() != items.length) {
            throw new IndexOutOfBoundsException();
        }

        if (matches.isEmpty()) {
            return new Text[]{defaultText(s)};
        }

        final ArrayList<Text> result = new ArrayList<>();

        int index = 0;
        for (int i = 0; i < matches.size(); i++) {
            final MatchResult match = matches.get(i);

            final String prefix = s.substring(index, match.start());
            if (!prefix.isBlank()) {
                result.add(defaultText(prefix));
            }
            result.addAll(createHighlightedText(items[i], pattern, "%m".equals(s.substring(match.start(), match.end()))));

            index = match.end();
        }

        if (index < s.length()) {
            result.add(defaultText(s.substring(index)));
        }

        return result.toArray(Text[]::new);
    }

    public TextFlow createHighlightedTextFlow(final String s, final Pattern pattern, final boolean mono) {
        return new TextFlow(createHighlightedText(s, pattern, mono).toArray(Text[]::new));
    }

    public TextFlow createSimpleTextFlow(final String s, final boolean mono) {
        return new TextFlow(defaultText(s, mono));
    }

    public List<Text> createHighlightedText(final String s, final Pattern pattern, final boolean mono) {
        final List<MatchResult> matches = pattern.matcher(s).results().collect(Collectors.toList());

        int index = 0;

        final List<Text> result = new ArrayList<>();
        for (final MatchResult match : matches) {
            final String prefix = s.substring(index, match.start());
            if (!prefix.isBlank()) {
                result.add(defaultText(prefix, mono));
            }
            result.add(highlightedText(s.substring(match.start(), match.end()), mono));
            index = match.end();
        }

        if (index < s.length()) {
            result.add(defaultText(s.substring(index), mono));
        }

        return result;
    }

    public Text highlightedText(final String s) {
        return highlightedText(s, false);
    }

    public Text highlightedText(final String s, final boolean mono) {
        return createText(s, true, mono);
    }

    public Text defaultText(final String s) {
        return defaultText(s, false);
    }

    public Text defaultText(final String s, final boolean mono) {
        return createText(s, false, mono);
    }

    private Text createText(final String s, final boolean bold, final boolean mono) {
        final Text text = new Text(s);
        text.setFont(getFont(bold, mono));
        text.setStyle("-fx-fill: -vatsim-text-color");
        return text;
    }

    private Font getFont(final boolean bold, final boolean mono) {
        if (bold) {
            if (mono) {
                return boldMono;
            } else {
                return boldDefault;
            }
        } else {
            if (mono) {
                return standardMono;
            } else {
                return standardDefault;
            }
        }
    }
}
