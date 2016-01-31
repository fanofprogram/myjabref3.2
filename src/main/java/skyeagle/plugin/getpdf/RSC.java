package skyeagle.plugin.getpdf;

import java.io.File;
import java.net.HttpURLConnection;
import java.util.Map;
import java.util.TreeMap;

import skyeagle.plugin.gui.UpdateDialog;

public class RSC implements GetPdfFile {

    private final String url;


    public RSC(String url) {
        url = url.toLowerCase();
        if (url.indexOf("articlepdf") != -1) {
            url = url.replaceAll("articlepdf", "articlelanding");
        } else if (url.indexOf("articlehtml") != -1) {
            url = url.replaceAll("articlehtml", "articlelanding");
        }
        this.url = url;
    }

    @Override
    public void getFile(UpdateDialog dig, File file, Boolean usingProxy) {
        Map<String, String> cookies = new TreeMap<>();
        String pagecontent = GetPDFUtil.initGetPDF(url, usingProxy, cookies);
        if (pagecontent == null) {
            dig.output("The network don't work, please check proxy and network.");
            return;
        }
        String pdflink = url.replaceAll("articlelanding", "articlepdf");
        HttpURLConnection con = GetPDFUtil.createPDFLink(pdflink, cookies, false);
        String type = con.getHeaderField("Content-Type");
        if (type.indexOf("pdf") == -1) {
            dig.output("cann't find the link to download pdf file, please try to use proxy or change the proxy");
            return;
        }
        int filesize = con.getContentLength();
        GetPDFUtil.getPDFFile(file, filesize, dig, con);
        con.disconnect();
    }

    public static void main(String[] args) {
        String str = "http://pubs.rsc.org/en/Content/ArticleLanding/2015/TA/C5TA07526B";
        File file = new File("E:\\test.pdf");
        new RSC(str).getFile(new UpdateDialog(null, "down"), file, false);
    }
}
