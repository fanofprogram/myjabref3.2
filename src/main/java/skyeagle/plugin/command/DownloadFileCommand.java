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
        int select = JOptionPane.showConfirmDialog(frame, "是否使用代理下载文献？", "提示：", JOptionPane.YES_NO_OPTION);
        if (select == JOptionPane.OK_OPTION) {
            if (!file.exists()) {
                frame.showMessage("请设置代理。");
                return;
            }
            usingProxy = true;
        }

        dialog = new UpdateDialog(frame, "下载pdf文件");
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
        dig.output("开始进行文献下载：");

        for (BibEntry be : bes) {
            String url = be.getField("url");
            if (url != null) {
                url = CommandUtil.DOItoURL(url);
                String name = be.getField(BibEntry.KEY_FIELD) + ".pdf";
                File file = expandFilename(metaData, name);
                dig.output("进行文献" + file.getAbsolutePath() + "的下载：");
                if ((file != null) && !file.exists()) {
                    Downloading(url, dig, file);
                    if (file.exists()) {
                        FileListTableModel tm = new FileListTableModel();
                        tm.setContent(be.getField("file"));
                        ExternalFileType fileType = ExternalFileTypes.getInstance().getExternalFileTypeByExt("pdf");
                        FileListEntry fle = new FileListEntry(name, file.toString(), fileType);
                        UpdateField.getNewLink(fle, file, metaData);
                        tm.addEntry(0, fle);
                        be.setField("file", tm.getStringRepresentation());
                    } else {
                        dig.output(file.toString() + "下载失败。");
                    }
                } else {
                    dig.output(file.toString() + "文件存在。");
                }
            } else {
                int id = panel.mainTable.findEntry(be) + 1;
                dig.output("The " + id + " don't have url, con't download the pdf file.");
                continue;
            }
        }
        dig.btnCancel.setText("下载完成，关闭对话框");
    }

    private File expandFilename(MetaData md, String name) {
        Optional<String> extension = FileUtil.getFileExtension(name);
        // Find the default directory for this field type, if any:
        List<String> directories = md.getFileDirectory(extension.orElse(null));
        // Include the standard "file" directory:
        List<String> fileDir = md.getFileDirectory(Globals.FILE_FIELD);
        // Include the directory of the bib file:
        ArrayList<String> al = new ArrayList<>();
        for (String aFileDir : fileDir) {
            if (!al.contains(aFileDir)) {
                if (!aFileDir.isEmpty()) {
                    al.add(aFileDir);
                }
            }
        }
        for (String dir : directories) {
            if (!al.contains(dir)) {
                if (!dir.isEmpty()) {
                    al.add(dir);
                }
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
                }
                return new File(name);
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
            dig.output("找不到" + url + "的匹配器。");
        }
    }

}
