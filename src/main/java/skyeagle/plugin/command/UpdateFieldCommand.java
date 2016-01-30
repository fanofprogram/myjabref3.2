package skyeagle.plugin.command;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import net.sf.jabref.Globals;
import net.sf.jabref.MetaData;
import net.sf.jabref.external.ExternalFileType;
import net.sf.jabref.gui.BasePanel;
import net.sf.jabref.gui.FileListEntry;
import net.sf.jabref.gui.FileListTableModel;
import net.sf.jabref.gui.JabRefFrame;
import net.sf.jabref.importer.fileformat.BibtexParser;
import net.sf.jabref.logic.labelPattern.LabelPatternUtil;
import net.sf.jabref.logic.util.io.FileUtil;
import net.sf.jabref.model.DuplicateCheck;
import net.sf.jabref.model.database.BibDatabase;
import net.sf.jabref.model.entry.BibEntry;
import skyeagle.plugin.getmail.ImapMail;
import skyeagle.plugin.gui.UpdateDialog;

public class UpdateFieldCommand {

	public JabRefFrame frame;
	public UpdateDialog dialog;
    public BibEntry[] bes;
	public BasePanel panel;

	public UpdateFieldCommand(JabRefFrame f) {
		frame = f;
        panel = frame.getCurrentBasePanel();
		bes = panel.mainTable.getSelectedEntries();

        dialog = new UpdateDialog(frame, "Update the item");

		UpdateField updateField = new UpdateField(this);
		Thread update = new Thread(updateField);
		update.start();

		dialog.setVisible(true);
	}

}

class UpdateField implements Runnable {
	private final UpdateDialog dig;
    public BibEntry[] bes;
	public BasePanel panel;
    public BibDatabase database;
	public MetaData metaData;
	public JabRefFrame frame;

	final static int TYPE_MISMATCH = -1, NOT_EQUAL = 0, EQUAL = 1, EMPTY_IN_ONE = 2, EMPTY_IN_TWO = 3,
			EMPTY_IN_BOTH = 4;

	public UpdateField(UpdateFieldCommand ud) {
		dig = ud.dialog;
		bes = ud.bes;
		panel = ud.panel;
		database = panel.database();
		metaData = panel.metaData();
		frame = ud.frame;
	}

	@Override
    public void run() {
		for (int i = 0; i < bes.length; i++) {

			String url = bes[i].getField("url");

			if (url != null) {
				url=CommandUtil.DOItoURL(url);
				String item = ImapMail.getItem(url, dig);
				if (item == null) {
                    dig.output("��ַ" + url + "�������û�ȡʧ��");
				} else {
                    BibEntry oldEntry = bes[i];
                    BibEntry newEntry = BibtexParser.singleFromString(item);
					checkAndUpdate(oldEntry, newEntry);
				}
			} else {
				int id = panel.mainTable.findEntry(bes[i]) + 1;
                dig.output("��" + id + "����¼û����ַ���޷�����");
				continue;
			}

		}
        dig.btnCancel.setText("�رնԻ���");
	}

    private void checkAndUpdate(BibEntry oldEntry, BibEntry newEntry) {
		String[] fields = { "year", "volume", "pages" };
        String oldKey = oldEntry.getField(BibEntry.KEY_FIELD);
		for (int i = 0; i < fields.length; i++) {
			int result = compareSingleField(fields[i], oldEntry, newEntry);
			if ((result == NOT_EQUAL) || (result == EMPTY_IN_ONE)) {
				int id = panel.mainTable.findEntry(oldEntry) + 1;
				String oldField = oldEntry.getField(fields[i]);
				String newField = newEntry.getField(fields[i]);
                dig.output("����" + id + "����¼��" + fields[i] + "��" + oldField + "��Ϊ" + newField);
				oldEntry.setField(fields[i], newField);
			}
		}
		LabelPatternUtil.makeLabel(metaData, database, oldEntry);
        String newKey = oldEntry.getField(BibEntry.KEY_FIELD);
		if (oldKey != newKey) {
			fileChangeName(oldEntry);
		}
		panel.markBaseChanged();
	}

    private void fileChangeName(BibEntry entry) {
		FileListTableModel tm = new FileListTableModel();
		tm.setContent(entry.getField("file"));
		int row = tm.getRowCount();
		if (row > 0) {
			for (int j = 0; j < row; j++) {
				FileListEntry fle = tm.getEntry(j);
                ExternalFileType filetype = fle.type;
				if ("PDF" == filetype.getName()) {
                    String link = fle.link;
                    File file = FileUtil.expandFilename(metaData, link);
                    String newLink = entry.getField(BibEntry.KEY_FIELD) + ".pdf";
					if ((file != null) && file.exists()) {
						File dir = file.getParentFile();
						File newFile = new File(dir, newLink);
                        dig.output("���ļ�" + file.getAbsolutePath() + "�����˸�����");
						file.renameTo(newFile);
						getNewLink(fle, newFile,metaData);
						entry.setField("file", tm.getStringRepresentation());
					}
				}
			}
		} else {
			int id = panel.mainTable.findEntry(entry) + 1;
            dig.output("��" + id + "����¼û�������ļ�������Ҫ�����ļ�������");
		}
	}

    private int compareSingleField(String field, BibEntry one, BibEntry two) {
		String s1 = one.getField(field), s2 = two.getField(field);

		if (s1 == null) {
			if (s2 == null) {
                return EMPTY_IN_BOTH;
            } else {
                return EMPTY_IN_ONE;
            }
		} else if (s2 == null) {
            return EMPTY_IN_TWO;
        }

		// Util.pr(field+": '"+s1+"' vs '"+s2+"'");
		if (field.equals("pages")) {
			// Pages can be given with a variety of delimiters, "-", "--", " -
			// ", " -- ".
			// We do a replace to harmonize these to a simple "-":
			// After this, a simple test for equality should be enough:
			s1 = s1.replaceAll("[- ]+", "-");
			s2 = s2.replaceAll("[- ]+", "-");
			if (s1.equals(s2)) {
                return EQUAL;
            } else {
                return NOT_EQUAL;
            }

		} else {
			s1 = s1.toLowerCase();
			s2 = s2.toLowerCase();
            double similarity = DuplicateCheck.correlateByWords(s1, s2);
			if (similarity > 0.8) {
                return EQUAL;
            } else {
                return NOT_EQUAL;
            }
		}
	}

	public static void getNewLink(FileListEntry fle, File fl,MetaData metaData) {
		// See if we should trim the file link to be relative to the file
		// directory:
        Optional<String> extension = FileUtil.getFileExtension(fl.toString());
        // Find the default directory for this field type, if any:
        List<String> directories = metaData.getFileDirectory(extension.orElse(null));
        // Include the standard "file" directory:
        List<String> fileDir = metaData.getFileDirectory(Globals.FILE_FIELD);
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
		try {
            for (int i = 0; i < al.size(); i++) {
				String canPath;
                canPath = (new File(al.get(i))).getCanonicalPath();
				if (fl.isAbsolute()) {
					String flPath = fl.getCanonicalPath();
					if ((flPath.length() > canPath.length()) && (flPath.startsWith(canPath))) {
						String relFileName = fl.getCanonicalPath().substring(canPath.length());
                        fle.link = relFileName;
						break;
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
}
