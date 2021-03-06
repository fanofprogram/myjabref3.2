/*  Copyright (C) 2003-2015 JabRef contributors.
    This program is free software; you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation; either version 2 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License along
    with this program; if not, write to the Free Software Foundation, Inc.,
    51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
*/
package net.sf.jabref.importer.fileformat;

import java.io.InputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;

import net.sf.jabref.importer.ImportFormatReader;
import net.sf.jabref.importer.OutputPrinter;
import net.sf.jabref.model.entry.BibEntry;
import net.sf.jabref.model.entry.AuthorList;

import java.util.regex.Pattern;

import net.sf.jabref.bibtex.EntryTypes;

/**
 * INSPEC format importer.
 */
public class InspecImporter extends ImportFormat {

    /**
     * Return the name of this import format.
     */
    @Override
    public String getFormatName() {
        return "INSPEC";
    }

    /*
     *  (non-Javadoc)
     * @see net.sf.jabref.imports.ImportFormat#getCLIId()
     */
    @Override
    public String getCLIId() {
        return "inspec";
    }

    /**
     * Check whether the source is in the correct format for this importer.
     */
    @Override
    public boolean isRecognizedFormat(InputStream stream) throws IOException {
        // Our strategy is to look for the "PY <year>" line.
        BufferedReader in = new BufferedReader(ImportFormatReader.getReaderDefaultEncoding(stream));
        //Pattern pat1 = Pattern.compile("PY:  \\d{4}");
        Pattern pat1 = Pattern.compile("Record.*INSPEC.*");

        //was PY \\\\d{4}? before
        String str;

        while ((str = in.readLine()) != null) {
            //Inspec and IEEE seem to have these strange " - " between key and value
            //str = str.replace(" - ", "");
            //System.out.println(str);

            if (pat1.matcher(str).find()) {
                return true;
            }
        }

        return false;
    }

    /**
     * Parse the entries in the source, and return a List of BibEntry objects.
     */
    @Override
    public List<BibEntry> importEntries(InputStream stream, OutputPrinter status) throws IOException {
        ArrayList<BibEntry> bibitems = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        String str;
        try (BufferedReader in = new BufferedReader(ImportFormatReader.getReaderDefaultEncoding(stream))) {
            while ((str = in.readLine()) != null) {
                if (str.length() < 2) {
                    continue;
                }
                if (str.indexOf("Record") == 0) {
                    sb.append("__::__").append(str);
                } else {
                    sb.append("__NEWFIELD__").append(str);
                }
            }
        }
        String[] entries = sb.toString().split("__::__");
        String type = "";
        HashMap<String, String> h = new HashMap<>();
        for (String entry : entries) {
            if (entry.indexOf("Record") != 0) {
                continue;
            }
            h.clear();

            String[] fields = entry.split("__NEWFIELD__");
            for (String s : fields) {
                //System.out.println(fields[j]);
                String f3 = s.substring(0, 2);
                String frest = s.substring(5);
                if ("TI".equals(f3)) {
                    h.put("title", frest);
                } else if ("PY".equals(f3)) {
                    h.put("year", frest);
                } else if ("AU".equals(f3)) {
                    h.put("author",
                            AuthorList.fixAuthor_lastNameFirst(frest.replace(",-", ", ").replace(";", " and ")));
                } else if ("AB".equals(f3)) {
                    h.put("abstract", frest);
                } else if ("ID".equals(f3)) {
                    h.put("keywords", frest);
                } else if ("SO".equals(f3)) {
                    int m = frest.indexOf('.');
                    if (m >= 0) {
                        String jr = frest.substring(0, m);
                        h.put("journal", jr.replace("-", " "));
                        frest = frest.substring(m);
                        m = frest.indexOf(';');
                        if (m >= 5) {
                            String yr = frest.substring(m - 5, m);
                            h.put("year", yr);
                            frest = frest.substring(m);
                            m = frest.indexOf(':');
                            if (m >= 0) {
                                String pg = frest.substring(m + 1).trim();
                                h.put("pages", pg);
                                h.put("volume", frest.substring(1, m));
                            }
                        }
                    }

                } else if ("RT".equals(f3)) {
                    frest = frest.trim();
                    if ("Journal-Paper".equals(frest)) {
                        type = "article";
                    } else if ("Conference-Paper".equals(frest) || "Conference-Paper; Journal-Paper".equals(frest)) {
                        type = "inproceedings";
                    } else {
                        type = frest.replace(" ", "");
                    }
                }
            }
            BibEntry b = new BibEntry(DEFAULT_BIBTEXENTRY_ID, EntryTypes.getTypeOrDefault(type)); // id assumes an existing database so don't
            // create one here
            b.setField(h);

            bibitems.add(b);

        }

        return bibitems;
    }
}
