package skyeagle.plugin.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.text.BadLocationException;

import net.sf.jabref.gui.JabRefFrame;


public class UpdateDialog extends JDialog implements ActionListener {

	/**
	 *
	 */
	private static final long serialVersionUID = -4781649571554164666L;

	public JButton btnCancel;
	public JButton btnOk;

	public Boolean flagOK = false;

	private JTextArea taskOutput;
	private JScrollPane scroll;

	public boolean display = false;

	public boolean stop = false;

	public ArrayList<String> showTexts = new ArrayList<String>();

	public static final String NEWLINE = System.getProperty("line.separator");

	public UpdateDialog(JabRefFrame frame, String frmTitle) {

		super(frame, frmTitle, true);

		init();

		this.addWindowListener(new WindowAdapter() {

			@Override
            public void windowDeactivated(WindowEvent e) {
				close();
			}
		});

	}

	private void init() {
        Toolkit kit = Toolkit.getDefaultToolkit();
        Dimension screenSize = kit.getScreenSize();
        int width = (screenSize.width / 2) - 50;
        int height = (screenSize.height / 2) - 100;
		setSize(width, height);
		Point pt = new Point();
		pt.x = screenSize.width / 4;
		pt.y = screenSize.height / 4;
		setLocation(pt);

		taskOutput = new JTextArea(15, 60);
		taskOutput.setMargin(new Insets(5, 5, 5, 5));
		taskOutput.setEditable(false);
		taskOutput.setBackground(Color.white);

		JPanel p = new JPanel(new BorderLayout());
		p.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
		scroll = new JScrollPane(taskOutput);
		scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		p.add(scroll, BorderLayout.CENTER);

        btnCancel = new JButton("Cancel");
		btnCancel.addActionListener(this);

		add(p, BorderLayout.CENTER);
		add(btnCancel, BorderLayout.PAGE_END);
	}

	@Override
    public void actionPerformed(ActionEvent e) {
		if (e.getSource() == btnCancel) {
			close();
		} else if (e.getSource() == btnOk) {
			close();
			flagOK = true;
		}
	}

	public void close() {
		stop = true;
		dispose();
	}

	public void output(String str) {
		String content = taskOutput.getText();
		taskOutput.insert(str + NEWLINE, content.length());

		int height = 10;
		Point p = new Point();
		p.setLocation(0, taskOutput.getLineCount() * height);
		scroll.getViewport().setViewPosition(p);
	}

	public void downloadRatioOutput(File file, int ratio, int totalDown, int getFileSize) {
		String content = taskOutput.getText();
        String tips = file.getAbsolutePath();
		int tmpNum = 0;
		int newNum = 0;
		String lastStr = null;
		if (getFileSize == -1) {
			lastStr = "KB";
			tmpNum = lastStr.length();
			newNum = totalDown/1024;
            tips = "The downloading size of " + tips + " is:";
		} else {
			lastStr = "%";
			tmpNum = lastStr.length();
			newNum = ratio;
            tips = "The downloading ratio of " + tips + " is:";
		}
		int beginIndex = content.indexOf(tips);
		if (beginIndex == -1) {
			taskOutput.insert(tips + newNum + lastStr, content.length());
		} else {
			beginIndex = beginIndex + tips.length();
			String oldNum = content.substring(beginIndex, content.length() - tmpNum);

			if (Integer.valueOf(oldNum) == newNum) {
                return;
            }
			try {
				taskOutput.getDocument().remove(content.length() - oldNum.length() - tmpNum, oldNum.length() + tmpNum);
				String tmp = newNum + lastStr;
				if (getFileSize != -1) {
                    if (ratio == 100) {
                        tmp = tmp + NEWLINE;
                    }
                }
				String newcontent = taskOutput.getText() + tmp;
				taskOutput.setText(newcontent);
			} catch (BadLocationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public void modifiedDialog() {
		// TODO Auto-generated method stub
        btnOk = new JButton("OK");
		btnOk.addActionListener(this);

		btnOk.setMaximumSize(new Dimension(Short.MAX_VALUE, Short.MAX_VALUE));
		btnCancel.setMaximumSize(new Dimension(Short.MAX_VALUE, Short.MAX_VALUE));

		JPanel downPan = new JPanel();
		downPan.setLayout(new BoxLayout(downPan, BoxLayout.X_AXIS));
		downPan.add(btnOk);
		downPan.add(btnCancel);
		add(downPan, BorderLayout.PAGE_END);
	}

}
