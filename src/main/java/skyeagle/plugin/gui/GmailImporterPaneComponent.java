package skyeagle.plugin.gui;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextPane;

import net.sf.jabref.Globals;
import net.sf.jabref.JabRefPreferences;
import net.sf.jabref.gui.BasePanel;
import net.sf.jabref.gui.GUIGlobals;
import net.sf.jabref.gui.IconTheme;
import net.sf.jabref.gui.JabRefFrame;
import net.sf.jabref.gui.SidePaneComponent;
import net.sf.jabref.gui.SidePaneManager;
import net.sf.jabref.logic.l10n.Localization;
import net.sf.jabref.model.entry.BibEntry;
import skyeagle.plugin.command.DownloadFileCommand;
import skyeagle.plugin.command.UpdateDetachCommand;
import skyeagle.plugin.command.UpdateFieldCommand;
import skyeagle.plugin.command.UpdateGmailCommand;

public class GmailImporterPaneComponent extends SidePaneComponent implements ActionListener {

    private static final long serialVersionUID = 1L;

    private final Action action = new PluginAction();

    private final GridBagLayout gbl = new GridBagLayout();
    private final GridBagConstraints con = new GridBagConstraints();

    private final JButton btnUpdate = new JButton(IconTheme.getImage("rank"));
    private final JButton btnSettings = new JButton(IconTheme.getImage("run"));
    private final JButton btnDown = new JButton(IconTheme.getImage("pdf"));
    private final JButton btnProxy = new JButton(IconTheme.getImage("search"));
    private final JButton btnDetach = new JButton(IconTheme.getImage("duplicate"));
    private final JButton btnField = new JButton(IconTheme.getImage("arrow"));

    private final SidePaneManager manager;
    private final JabRefFrame frame;
    private GmailSettingDialog settingDialog;

    private final File pluginDir = new File(System.getProperty("user.home") + "/.jabref/plugins");
    private final File file = new File(pluginDir, "GmailSetting.prop");

    private BasePanel pan;
    private BibEntry[] bes;


    public GmailImporterPaneComponent(SidePaneManager manager, JabRefFrame frame) {
        super(manager, null, "Import and Download");
        this.manager = manager;
        this.frame = frame;
        this.pan = frame.getCurrentBasePanel();

        int fontSize = GUIGlobals.CURRENTFONT.getSize();

        Dimension butDim = new Dimension(fontSize, fontSize + 10);

        btnUpdate.setPreferredSize(butDim);
        btnUpdate.setMinimumSize(butDim);
        btnUpdate.addActionListener(this);
        btnUpdate.setText("Update Gmail");
        btnUpdate.setToolTipText("Get Reference from Gmail.");

        btnSettings.addActionListener(this);
        btnSettings.setToolTipText("Settings");

        btnDown.setPreferredSize(butDim);
        btnDown.setMinimumSize(butDim);
        btnDown.addActionListener(this);
        btnDown.setText("Download pdf");
        btnDown.setToolTipText("Download pdf files from web.");

        btnProxy.addActionListener(this);
        btnProxy.setToolTipText("Setup the Proxy for pdf file download");

        btnField.setPreferredSize(butDim);
        btnField.setMinimumSize(butDim);
        btnField.addActionListener(this);
        btnField.setText("Update Fields");
        btnField.setToolTipText("Reload the fields from web.");

        btnDetach.addActionListener(this);
        btnDetach.setToolTipText("Delete pdf link and pdf file.");

        JPanel main = new JPanel();
        main.setLayout(gbl);
        con.gridwidth = GridBagConstraints.REMAINDER;
        con.fill = GridBagConstraints.BOTH;
        con.weightx = 1;

        JPanel split = new JPanel();
        split.setLayout(new BoxLayout(split, BoxLayout.LINE_AXIS));
        btnUpdate.setMaximumSize(new Dimension(Short.MAX_VALUE, Short.MAX_VALUE));
        split.add(btnUpdate);
        split.add(btnSettings);

        JPanel downPan = new JPanel();
        downPan.setLayout(new BoxLayout(downPan, BoxLayout.LINE_AXIS));
        btnDown.setMaximumSize(new Dimension(Short.MAX_VALUE, Short.MAX_VALUE));
        downPan.add(btnDown);
        downPan.add(btnProxy);

        JPanel fieldPan = new JPanel();
        fieldPan.setLayout(new BoxLayout(fieldPan, BoxLayout.LINE_AXIS));
        btnField.setMaximumSize(new Dimension(Short.MAX_VALUE, Short.MAX_VALUE));
        fieldPan.add(btnField);
        fieldPan.add(btnDetach);

        JTextPane author = new JTextPane();
        author.setText("This plugin is written by ChaoWang.");

        gbl.setConstraints(split, con);
        main.add(split);

        gbl.setConstraints(downPan, con);
        main.add(downPan);

        gbl.setConstraints(fieldPan, con);
        main.add(fieldPan);

        gbl.setConstraints(author, con);
        main.add(author);

        main.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
        add(main);
        setName("Import and Download");
    }

    @Override
    public void setActiveBasePanel(BasePanel panel) {
        super.setActiveBasePanel(panel);
        if (panel == null) {
            boolean status = Globals.prefs.getBoolean(JabRefPreferences.PLUGIN_VISIBLE);
            manager.hide("Import and Download");
            Globals.prefs.putBoolean(JabRefPreferences.PLUGIN_VISIBLE, status);
        } else {
            if (Globals.prefs.getBoolean(JabRefPreferences.PLUGIN_VISIBLE)) {
                manager.show("Import and Download");
            }
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == btnSettings) {
            settingDialog = new GmailSettingDialog(frame, "Gmail Importer Setting");
            settingDialog.setVisible(true);
        } else if (e.getSource() == btnUpdate) {
            if (!file.exists()) {
                frame.showMessage("Please input the username and password.");
                return;
            } else {
                settingDialog = new GmailSettingDialog(frame, "Gmail Importer Setting");
                if (settingDialog.isUsernameOk() && settingDialog.isPasswordOk() && settingDialog.isSearchKeywordOk()) {
                    new UpdateGmailCommand(frame);
                }
            }
        } else if (e.getSource() == btnProxy) {
            if (!file.exists()) {
                frame.showMessage("Please input IP and port of proxy.");
                return;
            } else {
                new ProxyDialog(frame);
            }

        } else if (e.getSource() == btnDown) {
            if (isEntriesNotNull()) {
                new DownloadFileCommand(frame);
            }

        } else if (e.getSource() == btnDetach) {
            if (isEntriesNotNull()) {
                new UpdateDetachCommand(frame);
            }
        } else if (e.getSource() == btnField) {
            if (isEntriesNotNull()) {
                new UpdateFieldCommand(frame);
            }
        }

    }

    public Boolean isEntriesNotNull() {
        pan = frame.getCurrentBasePanel();
        if (pan != null) {
            bes = pan.mainTable.getSelectedEntries();
            if ((bes != null) && (bes.length > 0)) {
                return true;
            } else {
                frame.showMessage("Please select the item your want to operate");
            }
        }
        return false;
    }

    @Override
    public void componentOpening() {
        Globals.prefs.putBoolean("Import and Download", true);
    }

    @Override
    public void componentClosing() {
        Globals.prefs.putBoolean("Import and Download", false);
    }

    public Action getAction() {
        return action;
    }

    class PluginAction extends AbstractAction {

        public PluginAction() {
            super(Localization.lang("Import and Download"), IconTheme.JabRefIcon.WWW.getSmallIcon());
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (!manager.hasComponent(GmailImporterPaneComponent.this.getTitle())) {
                manager.register(GmailImporterPaneComponent.this.getTitle(), GmailImporterPaneComponent.this);
            }
            if (frame.getTabbedPane().getTabCount() > 0) {
                manager.toggle(GmailImporterPaneComponent.this.getTitle());
            }
        }
    }
}
