package net.sf.jabref.exporter.layout.format;

import net.sf.jabref.exporter.layout.LayoutFormatter;

/**
 * Remove brackets formatter.
 *
 * <example>
 *     "{Stefan Kolb}" -> "Stefan Kolb"
 * </example>
 */
public class RemoveBrackets implements LayoutFormatter
{
    @Override
    public String format(String fieldText) {
        StringBuilder builder = new StringBuilder(fieldText.length());

        for (char c: fieldText.toCharArray()) {
            if (c != '{' && c != '}') {
                builder.append(c);
            }
        }
        return builder.toString();
    }
}
