package net.sf.jabref.importer;

import org.junit.Assert;
import org.junit.Test;

import java.io.File;

/**
 * @author Nosh&Dan
 * @version 09.11.2008 | 19:41:40
 */
public class ImportDataTest {

    public static final File FILE_IN_DATABASE = new File("src/test/resources/net/sf/jabref/importer/unlinkedFilesTestFolder/pdfInDatabase.pdf");
    public static final File FILE_NOT_IN_DATABASE = new File("src/test/resources/net/sf/jabref/importer/unlinkedFilesTestFolder/pdfNotInDatabase.pdf");
    public static final File EXISTING_FOLDER = new File("src/test/resources/net/sf/jabref/importer/unlinkedFilesTestFolder");
    public static final File NOT_EXISTING_FOLDER = new File("notexistingfolder");
    public static final File NOT_EXISTING_PDF = new File("src/test/resources/net/sf/jabref/importer/unlinkedFilesTestFolder/null.pdf");
    public static final File UNLINKED_FILES_TEST_BIB = new File("src/test/resources/net/sf/jabref/util/unlinkedFilesTestBib.bib");


    /**
     * Tests the testing environment.
     */
    @Test
    public void testTestingEnvironment() {
        Assert.assertTrue(ImportDataTest.EXISTING_FOLDER.exists());
        Assert.assertTrue(ImportDataTest.EXISTING_FOLDER.isDirectory());

        Assert.assertTrue(ImportDataTest.FILE_IN_DATABASE.exists());
        Assert.assertTrue(ImportDataTest.FILE_IN_DATABASE.isFile());

        Assert.assertTrue(ImportDataTest.FILE_NOT_IN_DATABASE.exists());
        Assert.assertTrue(ImportDataTest.FILE_NOT_IN_DATABASE.isFile());
    }

    @Test
    public void testOpenNotExistingDirectory() {
        Assert.assertFalse(ImportDataTest.NOT_EXISTING_FOLDER.exists());
        Assert.assertFalse(ImportDataTest.NOT_EXISTING_PDF.exists());
    }

}
