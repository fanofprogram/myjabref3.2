package skyeagle.plugin.getpdf;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.Map;
import java.util.TreeMap;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import skyeagle.plugin.gui.UpdateDialog;

public class Springer implements GetPdfFile {
	private final String url;

	public Springer(String url) {
		this.url = url;
	}

    @Override
    public void getFile(UpdateDialog dig, File file, Boolean usingProxy) {
		String base = "http://link.springer.com";
		Map<String, String> cookies = new TreeMap<>();
		String pagecontent = GetPDFUtil.initGetPDF(url, usingProxy, cookies);
		if (pagecontent == null) {
            dig.output("网络不通，请检查代理和网络。");
			return;
		}
		Document doc = Jsoup.parse(pagecontent);
		String pdflink = doc.select("a#abstract-actions-download-article-pdf-link").attr("href");
		if (pdflink.isEmpty()) {
            dig.output("页面上找不到下载pdf文件的连接，请尝试使用代理或更换代理。");
			return;
		}
		pdflink = base + pdflink;

		HttpURLConnection con = GetPDFUtil.createPDFLink(pdflink, cookies, false);
		int filesize = con.getContentLength();
        if (filesize != -1) {
            GetPDFUtil.getFileByMultiThread(file, filesize, dig, url, usingProxy);
        } else {
            GetPDFUtil.getPDFFile(file, filesize, dig, con);
            con.disconnect();
        }
	}

	public static void main(String[] args) throws IOException {
		String str = "http://link.springer.com/article/10.1007%2Fs11664-015-4303-6";
		File file = new File("E:\\test.pdf");
		new Springer(str).getFile(null, file, true);
	}
}
