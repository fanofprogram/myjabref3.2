package skyeagle.plugin.command;

import java.io.File;
import java.util.ArrayList;

import net.sf.jabref.MetaData;
import net.sf.jabref.gui.BasePanel;
import net.sf.jabref.gui.FileListEntry;
import net.sf.jabref.gui.FileListTableModel;
import net.sf.jabref.gui.JabRefFrame;
import net.sf.jabref.logic.util.io.FileUtil;
import net.sf.jabref.model.entry.BibEntry;
import skyeagle.plugin.gui.UpdateDialog;

public class UpdateDetachCommand {

	public UpdateDialog dialog;
	private final GetFileLink getFile;
    private final JabRefFrame frame;
    public BibEntry[] bes;
    private final BasePanel panel;
	public MetaData metaData;

	public UpdateDetachCommand(JabRefFrame f) {
		frame = f;
        panel = frame.getCurrentBasePanel();
		bes = panel.mainTable.getSelectedEntries();
		metaData = panel.metaData();

		dialog = new UpdateDialog(frame, "Delete pdf link and pdf file");
		dialog.modifiedDialog();

		getFile = new GetFileLink(this);
		Thread fileProc = new Thread(getFile);
		fileProc.start();

		dialog.setVisible(true);
		if (dialog.flagOK) {
			DeleteUpdate(getFile.alFile);
		}
	}

	private void DeleteUpdate(ArrayList<File> alFile) {
		for (File file : alFile) {
			if (file == null) {
                frame.showMessage("文件不存在！");
			} else {
				if (!file.delete()) {
                    frame.showMessage(file.getAbsolutePath() + "无法删除。");
				}
			}
		}
		for (int i = 0; i < bes.length; i++) {
			bes[i].clearField("file");
		}
		panel.markBaseChanged();
	}


	class GetFileLink implements Runnable {
		private final UpdateDialog dig;
        public BibEntry[] be;
        private final MetaData md;
        private final BasePanel bp;
		public ArrayList<File> alFile = new ArrayList<>();

		public GetFileLink(UpdateDetachCommand ud) {
			dig = ud.dialog;
            bp = ud.panel;
            be = ud.bes;
            md = ud.metaData;
		}

		@Override
		public void run() {
            if (md != null) {
                dig.output("将要删除以下文件：");
                for (int i = 0; i < be.length; i++) {
					FileListTableModel tm = new FileListTableModel();
                    tm.setContent(be[i].getField("file"));
					int row = tm.getRowCount();
					if (row > 0) {
						for (int j = 0; j < row; j++) {
							FileListEntry fle = tm.getEntry(j);
                            String link = fle.link;
                            File file = FileUtil.expandFilename(md, link);
							dig.output(file.getAbsolutePath());
							alFile.add(file);
						}
					} else {
                        int id = bp.mainTable.findEntry(be[i]) + 1;
                        dig.output("第" + id + "条记录没有连接文件！");
					}
				}
			}
		}
	}
}
