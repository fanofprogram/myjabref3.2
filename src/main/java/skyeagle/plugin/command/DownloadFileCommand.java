package skyeagle.plugin.command;

import java.io.File;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

import javax.swing.JOptionPane;

import net.sf.jabref.Globals;
import net.sf.jabref.MetaData;
import net.sf.jabref.external.ExternalFileType;
import net.sf.jabref.external.ExternalFileTypes;
import net.sf.jabref.gui.BasePanel;
import net.sf.jabref.gui.FileListEntry;
import net.sf.jabref.gui.FileListTableModel;
import net.sf.jabref.gui.JabRefFrame;
import net.sf.jabref.logic.util.OS;
import net.sf.jabref.logic.util.io.FileUtil;
import net.sf.jabref.model.entry.BibEntry;
import skyeagle.plugin.getmail.UrlKeywords;
import skyeagle.plugin.getpdf.GetPdfFile;
import skyeagle.plugin.gui.UpdateDialog;

public class DownloadFileCommand {

    public UpdateDialog dialog;
    public JabRefFrame frame;
    private final File pluginDir = new File(System.getProperty("user.home") + "/.jabref/plugins");
    private final File file = new File(pluginDir, "proxy.prop");
    public Boolean usingProxy = false;

    public DownloadFileCommand(JabRefFrame f) {
        frame = f;
        int select = JOptionPane.showConfirmDialog(frame, "Use proxy to download PDF file?", "Confim",
                JOptionPane.YES_NO_OPTION);
        if (select == JOptionPane.OK_OPTION) {
            if (!file.exists()) {
                frame.showMessage("Please setup the proxy.");
                return;
            }
            usingProxy = true;
        }

        dialog = new UpdateDialog(frame, "Download the pdf file");
        DownloadFile download = new DownloadFile(this);
        Thread downThread = new Thread(download);
        downThread.start();

        dialog.setVisible(true);
    }
}

class DownloadFile implements Runnable {

    private final UpdateDialog dig;
    private final JabRefFrame frame;
    public BibEntry[] bes;
    private final BasePanel panel;
    public MetaData metaData;
    public File pdfFile;
    public Boolean usingProxy;


    public DownloadFile(DownloadFileCommand df) {
        dig = df.dialog;
        frame = df.frame;
        usingProxy = df.usingProxy;
        panel = frame.getCurrentBasePanel();
        bes = panel.mainTable.getSelectedEntries();
        metaData = panel.metaData();

    }

    @Override
    public void run() {
        dig.output("��ʼ�����������أ�");

        for (BibEntry be : bes) {
            String url = be.getField("url");
            if (url != null) {
                url = CommandUtil.DOItoURL(url);
                String name = be.getField(BibEntry.KEY_FIELD) + ".pdf";
                File file = expandFilename(metaData, name);
                dig.output("Now downloading the file " + file.getAbsolutePath() + ":");
                if ((file != null) && !file.exists()) {
                    Downloading(url, dig, file);
                    if (file.exists()) {
                        FileListTableModel tm = new FileListTableModel();
                        tm.setContent(be.getField("file"));
                        ExternalFileType fileType = ExternalFileTypes.getInstance().getExternalFileTypeByExt("pdf");
                        FileListEntry fle = new FileListEntry(name, null, fileType);
                        UpdateField.getNewLink(fle, file, metaData);
                        tm.addEntry(0, fle);
                        be.setField("file", tm.getStringRepresentation());
                    } else {
                        dig.output("Fail to download the file " + file.toString());
                    }
                } else {
                    dig.output(file.toString() + " exist");
                }
            } else {
                int id = panel.mainTable.findEntry(be) + 1;
                dig.output("The " + id + " don't have url, con't download the pdf file.");
                continue;
            }
        }
        dig.btnCancel.setText("Downloading is finished, close this dialog.");
    }

    private File expandFilename(MetaData md, String name) {
        Optional<String> extension = FileUtil.getFileExtension(name);
        // Find the default directory for this field type, if any:
        List<String> directories = md.getFileDirectory(extension.orElse(null));
        // Include the standard "file" directory:
        List<String> fileDir = md.getFileDirectory(Globals.FILE_FIELD);
        // Include the directory of the bib file:
        ArrayList<String> al = new ArrayList<>();
        for (String dir : directories) {
            if (!al.contains(dir)) {
                al.add(dir);
            }
        }
        for (String aFileDir : fileDir) {
            if (!al.contains(aFileDir)) {
                al.add(aFileDir);
            }
        }
        for (String dir : al) {
            if (dir != null) {
                    if (dir.endsWith(System.getProperty("file.separator"))) {
                        name = dir + name;
                    } else {
                        name = dir + System.getProperty("file.separator") + name;
                    }
                    // fix / and \ problems:
                    Pattern SLASH = Pattern.compile("/");
                    Pattern BACKSLASH = Pattern.compile("\\\\");
                    if (OS.WINDOWS) {
                        name = SLASH.matcher(name).replaceAll("\\\\");
                    } else {
                        name = BACKSLASH.matcher(name).replaceAll("/");
                    File fileInDir = new File(name);
                    return fileInDir;
                }
            }
        }
        return null;
    }

    private void Downloading(String url, UpdateDialog d, File file) {
        UrlKeywords tmp[] = UrlKeywords.values();

        String urlClassName = null;
        boolean isFind = false;

        for (UrlKeywords className : tmp) {
            if (className.isThisUrl(url)) {
                urlClassName = className.name();
                isFind = true;
                break;
            }
        }

        if (isFind) {
            try {
                Class<?> clazz = Class.forName("skyeagle.plugin.getpdf." + urlClassName);
                Constructor<?> con = clazz.getConstructor(String.class);
                GetPdfFile getFile = (GetPdfFile) con.newInstance(url);
                getFile.getFile(d, file, usingProxy);
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        } else {
            d.output("Program can't find the rule for " + url + " to download");
        }
    }

}
