package skyeagle.plugin.getpdf;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.Map;
import java.util.TreeMap;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import skyeagle.plugin.gui.UpdateDialog;

public class Science implements GetPdfFile {

	private final String url;

	public Science(String url) {
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
		String pdflink = null;
		Document doc = Jsoup.parse(pagecontent);
		Elements tmpnod = doc.select("a[target=_blank]");
		for (Element ele : tmpnod) {
			String tmp = ele.attr("href");
			if (tmp.indexOf("pdf") != -1) {
				pdflink = tmp;
				break;
			}
		}
		pdflink = "http://science.sciencemag.org" + pdflink;

		HttpURLConnection con = GetPDFUtil.createPDFLink(pdflink, cookies, usingProxy);
		int filesize = con.getContentLength();
		GetPDFUtil.getPDFFile(file, filesize, dig, con);
		con.disconnect();
	}

	public static void main(String[] args) throws IOException {
		String str = "http://science.sciencemag.org/content/early/2015/11/24/science.aad3749.abstract";
		File file = new File("F:\\test.pdf");
		new Science(str).getFile(null, file, true);
	}
}
