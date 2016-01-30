package skyeagle.plugin.gui;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Container;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import net.sf.jabref.gui.JabRefFrame;

public class GmailSettingDialog extends JDialog implements ActionListener {

	private static final long serialVersionUID = 1L;
	private final JTextField username, gmailKeyword;
	private final JPasswordField password;
	private DateChooser dateChooserStart,dateChooserEnd;
	private final JabRefFrame parent;
	private final JButton okButton, cancelButton, applyButton;

	private final File pluginDir=new File(System.getProperty("user.home")+"/.jabref/plugins");
	private final File gmailfile = new File(pluginDir,"GmailSetting.prop");

	private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

	GmailSettingDialog(JabRefFrame prentFrame, String title) {
		super(prentFrame, title, false);
		parent = prentFrame;
		Container container = getContentPane();

		JPanel inputPanel = new JPanel();
		GridLayout gLayout = new GridLayout(5, 2);
		gLayout.setVgap(5);
		inputPanel.setLayout(gLayout);

		JLabel userLabel = new JLabel("Gmail username:");
		inputPanel.add(userLabel);
		username = new JTextField(15);
		username.addActionListener(this);
		inputPanel.add(username);

		JLabel pwdLabel = new JLabel("Gmail password:");
		inputPanel.add(pwdLabel);
		password = new JPasswordField(20);
		password.addActionListener(this);
		inputPanel.add(password);

		JLabel searchLabel = new JLabel("Google scholar keyword:");
		inputPanel.add(searchLabel);
		gmailKeyword = new JTextField(20);
		gmailKeyword.addActionListener(this);
		inputPanel.add(gmailKeyword);

		JLabel dateLabel = new JLabel("Start Date:");
		inputPanel.add(dateLabel);

		if (!gmailfile.exists()) {
			dateChooserStart = new DateChooser();
			dateChooserEnd = new DateChooser();
		} else {
			try {
				BufferedReader bfr = new BufferedReader(new FileReader(gmailfile));
				Properties prop = new Properties();
				prop.load(bfr);
				username.setText(prop.getProperty("username"));
				String newPwd = prop.getProperty("password");
				String pwd = Endecrypt.convertMD5(newPwd);
				password.setText(pwd);
				gmailKeyword.setText(prop.getProperty("searchkeyword"));

				String dateString = prop.getProperty("startday");
				Date date = sdf.parse(dateString, new ParsePosition(0));
				dateChooserStart = new DateChooser(date);

				dateString = prop.getProperty("endday");
				date = sdf.parse(dateString, new ParsePosition(0));
				dateChooserEnd = new DateChooser(date);

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		inputPanel.add(dateChooserStart);

		JLabel dateLabe2 = new JLabel("End Date:");
		inputPanel.add(dateLabe2);
		inputPanel.add(dateChooserEnd);

		inputPanel.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));
		container.add("Center", inputPanel);

		JPanel btnPanel = new JPanel();
		FlowLayout fLayout = new FlowLayout(FlowLayout.CENTER);
		fLayout.setHgap(35);
		btnPanel.setLayout(fLayout);
        cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(this);
        okButton = new JButton("OK");
		okButton.addActionListener(this);
        applyButton = new JButton("Apply");
		applyButton.addActionListener(this);
		btnPanel.add(applyButton);
		btnPanel.add(okButton);
		btnPanel.add(cancelButton);
		container.add("South", btnPanel);

		Point pt = parent.getLocation();
		Dimension dm = parent.getSize();
		setLocation(pt.x + ((int) dm.getWidth() / 3), pt.y + ((int) dm.getHeight()
				/ 3));

		pack();
	}

	@Override
    public void actionPerformed(ActionEvent event) {
		Object source = event.getSource();
		String userName = username.getText().trim();
		String pwd = new String(password.getPassword()).trim();
		String searchKeyword = gmailKeyword.getText().trim();
		Date Start = dateChooserStart.getDate();
		Date End = dateChooserEnd.getDate();
		String StartDay = sdf.format(Start);
		String EndDay = sdf.format(End);

		if (source == cancelButton) {
            dispose();
        } else if (isUsernameOk() && isPasswordOk() && isSearchKeywordOk()) {
			if ((source == applyButton)) {
				applyBtnSetting(userName, pwd, searchKeyword, StartDay,EndDay);
			} else {
				if (source == okButton) {
					applyBtnSetting(userName, pwd, searchKeyword, StartDay,EndDay);
				}
				dispose();
			}
		}
	}

	public boolean isSearchKeywordOk() {
		if (gmailKeyword.getText().isEmpty()) {
            parent.showMessage("Kewords of Google scholar can't be empty!");
			return false;
		}
		return true;
	}

	public boolean isPasswordOk() {
		// TODO Auto-generated method stub
		if (new String(password.getPassword()).isEmpty()) {
            parent.showMessage("Please input your password.");
			return false;
		}
		return true;
	}

	public boolean isUsernameOk() {
		// TODO Auto-generated method stub
		if (username.getText().isEmpty()) {
            parent.showMessage("username is empty");
			return false;
		} else if (username.getText().indexOf('@') != -1) {
            parent.showMessage("Don't need input @gmail.com!");
			return false;
		}
		return true;
	}

	private void applyBtnSetting(String userName, String pwd,
			String searchKeyword, String startDay, String endDay) {
		String newPwd = Endecrypt.convertMD5(pwd);
		Properties prop = new Properties();
		prop.setProperty("username", userName);
		prop.setProperty("password", newPwd);
		prop.setProperty("searchkeyword", searchKeyword);
		prop.setProperty("startday", startDay);
		prop.setProperty("endday", endDay);

		try {
			BufferedWriter bfw = new BufferedWriter(new FileWriter(gmailfile));
			prop.store(bfw, "Gmail Setting");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
