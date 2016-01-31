package skyeagle.plugin.command;

import java.util.ArrayList;

import net.sf.jabref.gui.JabRefFrame;
import skyeagle.plugin.getmail.ImapMail;
import skyeagle.plugin.gui.UpdateDialog;

public class UpdateGmailCommand {
	public ImapMail gmail;
	public UpdateDialog dialog;
	private final GetMails getMail;

	public UpdateGmailCommand(JabRefFrame frame) {
		dialog = new UpdateDialog(frame, "Importing Gmail content");
		gmail = new ImapMail(frame, dialog);
		getMail = new GetMails(dialog,gmail);
		Thread mail = new Thread(getMail);
		mail.start();

		dialog.setVisible(true);

		if (getMail.sbEntries.length() != 0) {
			gmail.setItems(getMail.sbEntries.toString());
        }
	}
}

class GetMails implements Runnable {
	private final UpdateDialog dialog;
	private final ImapMail gmail;
	public StringBuilder sbEntries;

	public GetMails(UpdateDialog dialog, ImapMail gmail) {
		this.dialog = dialog;
		this.gmail=gmail;
	}

	@Override
    public void run() {
		sbEntries= new StringBuilder();
        ArrayList<String> urls = new ArrayList<>();
        ArrayList<String> sbNotRec = new ArrayList<>();

		urls = gmail.getEmailContent();
        dialog.output("Start to get item infomation in the url .....");

		int numUrl = 0;
		while ((urls != null) && !dialog.stop && (numUrl < urls.size())) {
			String item = ImapMail.getItem(urls.get(numUrl),dialog);
			if (item == null) {
                dialog.output("fail to get item in" + urls.get(numUrl));
				sbNotRec.add(urls.get(numUrl));
			} else {
				sbEntries.append(item);
			}
			numUrl++;
		}

        // �ܽ���Ϣ
        dialog.output("There are " + urls.size() + " items, however the follow " + sbNotRec.size()
                + " items can't get information");
		for (int i = 0; i < sbNotRec.size(); i++) {
			dialog.output(sbNotRec.get(i));
		}
        dialog.output("all items was collected. ");
        dialog.btnCancel.setText("Close Dialog");
	}
}