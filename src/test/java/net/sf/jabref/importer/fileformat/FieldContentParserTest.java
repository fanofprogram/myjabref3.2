package net.sf.jabref.importer.fileformat;

import net.sf.jabref.Globals;
import net.sf.jabref.JabRefPreferences;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.*;

public class FieldContentParserTest {
    FieldContentParser parser;

    @BeforeClass
    public static void loadPreferences() {
        Globals.prefs = JabRefPreferences.getInstance();
    }

    @Before
    public void setUp() throws Exception {
        parser = new FieldContentParser();
    }

    @Test
    public void unifiesLineBreaks() throws Exception {
        String original = "I\r\nunify\nline\rbreaks.";
        String expected = "I\nunify\nline\nbreaks.".replaceAll("\n", Globals.NEWLINE);
        String processed = parser.format(new StringBuffer(original), "abstract").toString();

        assertEquals(expected, processed);
    }

    @Test
    public void retainsWhitespaceForMultiLineFields() throws Exception {
        String original = "I\nkeep\nline\nbreaks\nand\n\ttabs.";
        String formatted = original.replaceAll("\n", Globals.NEWLINE);

        String abstrakt = parser.format(new StringBuffer(original), "abstract").toString();
        String review = parser.format(new StringBuffer(original), "review").toString();

        assertEquals(formatted, abstrakt);
        assertEquals(formatted, review);
    }

    @Test
    public void removeWhitespaceFromNonMultiLineFields() throws Exception {
        String original = "I\nshould\nnot\ninclude\nadditional\nwhitespaces  \nor\n\ttabs.";
        String expected = "I should not include additional whitespaces or tabs.";

        String abstrakt = parser.format(new StringBuffer(original), "title").toString();
        String any = parser.format(new StringBuffer(original), "anyotherfield").toString();

        assertEquals(expected, abstrakt);
        assertEquals(expected, any);
    }
}