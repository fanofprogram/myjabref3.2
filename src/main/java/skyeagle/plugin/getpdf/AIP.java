package skyeagle.plugin.getpdf;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.Map;
import java.util.TreeMap;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import skyeagle.plugin.gui.UpdateDialog;

public class AIP implements GetPdfFile {

    private final String url;


    public AIP(String url) {
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
        Document doc = Jsoup.parse(pagecontent);
        String pdflink = doc.select("a[class=pdf]").attr("href");
        if (pdflink.isEmpty()) {
            dig.output("cann't find the link to download pdf file, please try to use proxy or change the proxy");
            return;
        }
        pdflink = "http://scitation.aip.org" + pdflink;

        HttpURLConnection con = GetPDFUtil.createPDFLink(pdflink, cookies, false);
        int filesize = con.getContentLength();
        GetPDFUtil.getPDFFile(file, filesize, dig, con);
        con.disconnect();
    }

    public static void main(String[] args) throws IOException {
        String str = "http://scitation.aip.org/content/aip/journal/jap/117/15/10.1063/1.4918311";
        File file = new File("F:\\test.pdf");
        new AIP(str).getFile(null, file, true);
    }

}
