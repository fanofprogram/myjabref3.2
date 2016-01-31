package skyeagle.plugin.getmail;

import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Toolkit;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Constructor;
import java.net.URLDecoder;
import java.security.Security;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.TreeSet;

import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.URLName;
import javax.mail.internet.MimeUtility;
import javax.mail.search.AndTerm;
import javax.mail.search.ComparisonTerm;
import javax.mail.search.FromStringTerm;
import javax.mail.search.ReceivedDateTerm;
import javax.mail.search.SearchTerm;

import com.sun.mail.imap.IMAPMessage;
import com.sun.mail.util.QPDecoderStream;

import net.sf.jabref.gui.BasePanel;
import net.sf.jabref.gui.BibtexFields;
import net.sf.jabref.gui.ImportInspectionDialog;
import net.sf.jabref.gui.JabRefFrame;
import net.sf.jabref.importer.ParserResult;
import net.sf.jabref.importer.fileformat.BibtexParser;
import net.sf.jabref.logic.l10n.Localization;
import net.sf.jabref.model.entry.BibEntry;
import skyeagle.plugin.geturlcite.GetCite;
import skyeagle.plugin.gui.Endecrypt;
import skyeagle.plugin.gui.UpdateDialog;

public class ImapMail {

    private static String dateFormat = "yyyy-MM-dd";

    // private String host = "imap.gmail.com";
    private final String host = "64.233.189.109";
    private final String port = "993";

    private final ArrayList<String> urls = new ArrayList<>();

    private final TreeSet<String> readedDay = new TreeSet<>();

    // store info from setting dialog
    private String userName;
    private String userPassword;
    private String searchKeyword;
    private Date startdate;
    private Date enddate;

    private final JabRefFrame frame;
    private final UpdateDialog diag;

    // file used to store setting information
    private final File pluginDir = new File(System.getProperty("user.home") + "/.jabref/plugins");
    private final File gmailfile = new File(pluginDir, "GmailSetting.prop");
    private final File dayfile = new File(pluginDir, "day.prop");


    public ImapMail(JabRefFrame frame, UpdateDialog diag) {
        this.frame = frame;
        this.diag = diag;
        try {
            BufferedReader bfr = new BufferedReader(new FileReader(gmailfile));
            Properties prop = new Properties();
            prop.load(bfr);

            userName = prop.getProperty("username");
            String newPwd = prop.getProperty("password");
            userPassword = Endecrypt.convertMD5(newPwd);
            searchKeyword = prop.getProperty("searchkeyword");

            String dateString = prop.getProperty("startday");
            SimpleDateFormat sDateFormat = new SimpleDateFormat(dateFormat);
            startdate = sDateFormat.parse(dateString, new ParsePosition(0));

            dateString = prop.getProperty("endday");
            sDateFormat = new SimpleDateFormat(dateFormat);
            enddate = sDateFormat.parse(dateString, new ParsePosition(0));
            diag.output("Start to connect Gmail .....");
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public ArrayList<String> getEmailContent() {
        Security.addProvider(new com.sun.net.ssl.internal.ssl.Provider());
        Properties prop = System.getProperties();
        prop.setProperty("mail.store.protocol", "imaps");
        prop.setProperty("mail.imap.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        prop.setProperty("mail.imap.socketFactory.fallback", "false");
        prop.setProperty("mail.imap.socketFactory.port", port);
        prop.setProperty("mail.imap.auth.plain.disable", "true");
        prop.setProperty("mail.imap.auth.login.disable", "true");
        try {
            Session session = Session.getInstance(prop);
            URLName urln = new URLName("imaps", host, 993, null, userName, userPassword);

            Store store = session.getStore(urln);
            store.connect();
            diag.output("already connected Gmail.");
            diag.output("Start to get mail in Incoming Box.....");
            Folder folder = store.getFolder("INBOX");
            folder.open(Folder.READ_ONLY);
            SearchTerm stDate = new AndTerm(new ReceivedDateTerm(ComparisonTerm.GE, startdate),
                    new ReceivedDateTerm(ComparisonTerm.LE, enddate));
            SearchTerm st = new AndTerm(new FromStringTerm("scholaralerts-noreply@google.com"), stDate);
            Message[] messages = folder.search(st);

            diag.output("There are " + messages.length + " " + searchKeyword + " mails in Incoming Box.");
            if (messages.length != 0) {
                diag.output("Start to analysize all these mails.....");
                for (Message message : messages) {
                    IMAPMessage msg = (IMAPMessage) message;
                    String subject = MimeUtility.decodeText(msg.getSubject());
                    if (subject.indexOf(searchKeyword) != -1) {
                        Date sentDate = msg.getSentDate();
                        SimpleDateFormat format = new SimpleDateFormat(dateFormat);
                        String strSentDate = format.format(sentDate);
                        diag.output("Start to analysize the mail in " + strSentDate);
                        readedDay.add(strSentDate);
                        String strTmp = null;
                        if (msg.isMimeType("text/html")) {
                            if (msg.getContent() instanceof String) {
                                strTmp = msg.getContent().toString();
                            } else if (msg.getContent() instanceof QPDecoderStream) {
                                BufferedInputStream bis = new BufferedInputStream((InputStream) msg.getContent());
                                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                while (true) {
                                    int c = bis.read();
                                    if (c == -1) {
                                        break;
                                    }
                                    baos.write(c);
                                }
                                strTmp = new String(baos.toByteArray());
                                try {
                                    StringBuffer tempBuffer = new StringBuffer();
                                    int incrementor = 0;
                                    int dataLength = strTmp.length();
                                    while (incrementor < dataLength) {
                                        char charecterAt = strTmp.charAt(incrementor);
                                        if (charecterAt == '%') {
                                            tempBuffer.append("<percentage>");
                                        } else if (charecterAt == '+') {
                                            tempBuffer.append("<plus>");
                                        } else {
                                            tempBuffer.append(charecterAt);
                                        }
                                        incrementor++;
                                    }
                                    strTmp = tempBuffer.toString();
                                    strTmp = URLDecoder.decode(strTmp, "utf-8");
                                    strTmp = strTmp.replaceAll("<percentage>", "%");
                                    strTmp = strTmp.replaceAll("<plus>", "+");
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                        ArrayList<String> al = getURL(strTmp);
                        urls.addAll(al);
                    }
                }
            }
            storeDay();
            folder.close(false);
            store.close();
        } catch (Exception e) {
            frame.showMessage("can't connect Gmail, please check username , password and network.");
            diag.close();
        }
        return urls;
    }

    private void storeDay() {
        BufferedWriter bfw = null;
        BufferedReader bfr = null;
        try {
            Properties prop = new Properties();
            if (dayfile.exists()) {
                bfr = new BufferedReader(new FileReader(dayfile));
                prop.load(bfr);
                String count = prop.getProperty("count");
                if (count != null) {
                    int c = Integer.parseInt(prop.getProperty("count"));
                    for (int i = 1; i <= c; i++) {
                        String day = prop.getProperty("day" + i);
                        readedDay.add(day);
                    }
                }
            }
            prop.setProperty("count", String.valueOf(readedDay.size()));
            int i = 1;
            for (Iterator<String> it = readedDay.iterator(); it.hasNext(); i++) {
                prop.setProperty("day" + i, it.next());
            }
            bfw = new BufferedWriter(new FileWriter(dayfile));
            prop.store(bfw, "days");
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                bfw.close();
                bfr.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private ArrayList<String> getURL(String strTmp) {
        ArrayList<String> al = new ArrayList<>();
        int beginIndex = 0;
        int endIndex = 0;
        while (true) {
            beginIndex = strTmp.indexOf("href=", endIndex);
            if (beginIndex == -1) {
                break;
            }
            endIndex = strTmp.indexOf(">", beginIndex);
            beginIndex = beginIndex + 6;
            endIndex = endIndex - 1;
            String url = strTmp.substring(beginIndex, endIndex);
            al.add(url);
        }

        al.remove(al.size() - 1);
        al.remove(al.size() - 1);
        String element = null;
        for (int i = 0; i < al.size(); i++) {
            String tmpStr = al.get(i);
            int begin = tmpStr.indexOf("http", 10);
            try {
                if (begin != -1) {
                    int end = tmpStr.indexOf("&amp;", 10);
                    element = URLDecoder.decode(tmpStr.substring(begin, end), "utf-8");
                } else {
                    element = URLDecoder.decode(tmpStr.substring(0, tmpStr.length()), "utf-8");
                }
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            diag.output(element);
            al.set(i, element);
        }
        return al;
    }

    public static String getItem(String itemUrl, UpdateDialog diag) {
        UrlKeywords tmp[] = UrlKeywords.values();

        String urlClassName = null;
        String itemString = null;
        boolean isFind = false;

        for (UrlKeywords className : tmp) {
            if (className.isThisUrl(itemUrl)) {
                urlClassName = className.name();
                isFind = true;
                break;
            }
        }

        if (isFind) {
            try {
                Class<?> clazz = Class.forName("skyeagle.plugin.geturlcite." + urlClassName);
                Constructor<?> con = clazz.getConstructor(String.class);
                GetCite getCite = (GetCite) con.newInstance(itemUrl);
                itemString = getCite.getCiteItem();
                if (itemString != null) {
                    diag.output("Getting  the reference in " + itemUrl + " is done.");
                } else {
                    diag.output("cann't connect the " + itemUrl + ", please check the network.");
                    return null;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else {
            diag.output("cann't find the rule which correspond to" + itemUrl);
        }
        return itemString;
    }

    public void setItems(String sbEntries) {
        try {
            BasePanel panel = frame.getCurrentBasePanel();
            ParserResult pr = BibtexParser.parse(new StringReader(sbEntries.toString()));
            List<BibEntry> entries = new ArrayList<>(pr.getDatabase().getEntries());
            ImportInspectionDialog diagImporter = new ImportInspectionDialog(frame, panel,
                    BibtexFields.DEFAULT_INSPECTION_FIELDS, Localization.lang("Import"), false);
            diagImporter.addEntries(entries);
            diagImporter.entryListComplete();

            diagImporter.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
            Toolkit kit = Toolkit.getDefaultToolkit();
            Dimension screenSize = kit.getScreenSize();
            Point pt = new Point();
            pt.x = screenSize.width / 4;
            pt.y = screenSize.height / 4;
            diagImporter.setLocation(pt);
            diagImporter.setVisible(true);
            diagImporter.toFront();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
