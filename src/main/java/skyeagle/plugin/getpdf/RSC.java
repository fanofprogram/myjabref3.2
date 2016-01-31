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
            dig.output("网络不通，请检查代理和网络。");
            return;
        }
        String pdflink = url.replaceAll("articlelanding", "articlepdf");
        HttpURLConnection con = GetPDFUtil.createPDFLink(pdflink, cookies, false);
        String type = con.getHeaderField("Content-Type");
        if (type.indexOf("pdf") == -1) {
            dig.output("页面上找不到下载pdf文件的连接，请尝试使用代理或更换代理。");
            return;
        }
        int filesize = con.getContentLength();
        if (filesize != -1) {
            GetPDFUtil.getFileByMultiThread(file, filesize, dig, url, usingProxy);
        } else {
            GetPDFUtil.getPDFFile(file, filesize, dig, con);
            con.disconnect();
        }
    }

    public static void main(String[] args) {
        String str = "http://pubs.rsc.org/en/Content/ArticleLanding/2015/TA/C5TA07526B";
        File file = new File("E:\\test.pdf");
        new RSC(str).getFile(new UpdateDialog(null, "down"), file, false);
    }
}
