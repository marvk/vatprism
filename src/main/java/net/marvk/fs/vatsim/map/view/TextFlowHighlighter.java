package net.marvk.fs.vatsim.map.view;

import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class TextFlowHighlighter {
    private static final Font BOLD = Font.font(
            Font.getDefault().getFamily(),
            FontWeight.BOLD,
            Font.getDefault().getSize()
    );

    private static final Font BOLD_MONO = Font.font(
            "JetBrains Mono",
            FontWeight.BOLD,
            Font.getDefault().getSize()
    );

    private static final Font MONO = Font.font(
            "JetBrains Mono",
            Font.getDefault().getSize()
    );

    private static final Pattern REPLACING = Pattern.compile("%[rm]");

    public TextFlowHighlighter() {
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

    public Text highlightedText(final String s, final boolean mono) {
        final Text text = new Text(s);
        text.setStyle("-fx-fill: -vatsim-text-color-light");
        text.setFont(mono ? BOLD_MONO : BOLD);
        return text;
    }

    public Text highlightedText(final String s) {
        return highlightedText(s, false);
    }

    public Text defaultText(final String s, final boolean mono) {
        final Text text = new Text(s);
        text.setStyle("-fx-fill: -vatsim-text-color");
        if (mono) {
            text.setFont(MONO);
        }
        return text;
    }

    public Text defaultText(final String s) {
        return defaultText(s, false);
    }

}
