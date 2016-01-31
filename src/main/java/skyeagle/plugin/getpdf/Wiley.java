package skyeagle.plugin.getpdf;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.Map;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import skyeagle.plugin.gui.UpdateDialog;

public class Wiley implements GetPdfFile {

	private String url;

	public Wiley(String url) {
		this.url = url;
	}

	@Override
    public void getFile(UpdateDialog dig, File file, Boolean usingProxy) {

		String basurl = "http://onlinelibrary.wiley.com";
		String link = null;
		try {
			Document doc = Jsoup.connect(url).timeout(30000).get();
			Elements eles = doc.select("a#wol1backlink");
            if (eles.size() != 0) {
                url = doc.select("a#wol1backlink").attr("href");
            }
			doc = Jsoup.connect(url).timeout(30000).get();
			link = doc.select("a#journalToolsPdfLink[title=Article in pdf format]").attr("href");
		} catch (IOException e1) {
            dig.output("网络不通，请检查代理和网络。");
			e1.printStackTrace();
			return;
		}
		link = basurl + link;
		link = link.replaceAll("epdf", "pdf");

		String pdfpage = null;
		if (usingProxy) {
			pdfpage = GetPDFUtil.getPageContent(link, GetPDFUtil.getProxy());
		} else {
			pdfpage = GetPDFUtil.getPageContent(link);
		}
		Document doc = Jsoup.parse(pdfpage);
		String pdflink = doc.select("iframe#pdfDocument").attr("src");
		if (pdflink.isEmpty()) {
            dig.output("页面上找不到下载pdf文件的连接，请尝试使用代理或更换代理。");
			return;
		}

		Map<String, String> cookies = null;
		if (usingProxy) {
            cookies = GetPDFUtil.getCookies(url, GetPDFUtil.getProxy());
        } else {
            cookies = GetPDFUtil.getCookies(url);
        }

		HttpURLConnection con = GetPDFUtil.createPDFLink(pdflink, cookies,usingProxy);
		int filesize = con.getContentLength();
        if (filesize != -1) {
            GetPDFUtil.getFileByMultiThread(file, filesize, dig, url, usingProxy);
        } else {
            GetPDFUtil.getPDFFile(file, filesize, dig, con);
            con.disconnect();
        }
	}

	public static void main(String[] args) {
		String str = "http://onlinelibrary.wiley.com/doi/10.1002/ange.201508492/full";
		File file = new File("E:\\test.pdf");
		new Wiley(str).getFile(new UpdateDialog(null, "down"), file, true);
	}
}
