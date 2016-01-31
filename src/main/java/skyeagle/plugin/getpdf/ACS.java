package skyeagle.plugin.getpdf;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.Map;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import skyeagle.plugin.gui.UpdateDialog;

public class ACS implements GetPdfFile {

    private final String url;


    public ACS(String url) {
        this.url = url;
    }

    @Override
    public void getFile(UpdateDialog dig, File file, Boolean usingProxy) {

        String pdfurl = "http://pubs.acs.org";
        Document doc = null;
        try {
            doc = Jsoup.connect(url).ignoreContentType(true).timeout(30000).get();
        } catch (IOException e) {
            e.printStackTrace();
        }
        String pdflink = doc.select("a[title^=High-Res]").attr("href");
        pdfurl = pdfurl + pdflink;

        Map<String, String> cookies = null;
        if (usingProxy) {
            cookies = GetPDFUtil.getCookies(pdfurl, GetPDFUtil.getProxy());
        } else {
            cookies = GetPDFUtil.getCookies(pdfurl);
        }

        HttpURLConnection con = GetPDFUtil.createPDFLink(pdfurl, cookies, false);
        int filesize = con.getContentLength();
        GetPDFUtil.getPDFFile(file, filesize, dig, con);
        con.disconnect();
    }

    public static void main(String[] args) {
        String str = "http://pubs.acs.org/doi/abs/10.1021/acsami.5b01460";
        File file = new File("F:\\test.pdf");
        new ACS(str).getFile(new UpdateDialog(null, "down"), file, false);
    }
}
