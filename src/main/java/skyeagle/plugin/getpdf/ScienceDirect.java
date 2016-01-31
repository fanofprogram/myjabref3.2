package skyeagle.plugin.getpdf;

import java.io.File;
import java.net.HttpURLConnection;
import java.util.Map;
import java.util.TreeMap;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import skyeagle.plugin.gui.UpdateDialog;

public class ScienceDirect implements GetPdfFile {

    private final String url;


    public ScienceDirect(String url) {
        this.url = url;
    }

    @Override
    public void getFile(UpdateDialog dig, File file, Boolean usingProxy) {
        Map<String, String> cookies = new TreeMap<>();
        String pagecontent = GetPDFUtil.initGetPDF(url, usingProxy, cookies);
        if (pagecontent == null) {
            dig.output("网络不通，请检查代理和网络。");
            return;
        }
        Document doc = Jsoup.parse(pagecontent);
        String pdflink = doc.select("a[title=Download PDF]#pdfLink").attr("href");
        if (pdflink.isEmpty()) {
            dig.output("页面上找不到下载pdf文件的连接，请尝试使用代理或更换代理。");
            return;
        }

        HttpURLConnection con = GetPDFUtil.createPDFLink(pdflink, cookies, false);
        con.setRequestProperty("Referer", url);
        int filesize = con.getContentLength();
        if (filesize != -1) {
            GetPDFUtil.getFileByMultiThread(file, filesize, dig, pdflink, usingProxy);
        } else {
            GetPDFUtil.getPDFFile(file, filesize, dig, con);
            con.disconnect();
        }
    }

    public static void main(String[] args) {
        String str = "http://www.sciencedirect.com/science/article/pii/S0925838815318776";
        File file = new File("F:\\test.pdf");
        new ScienceDirect(str).getFile(null, file, false);
    }
}
