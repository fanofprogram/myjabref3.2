package skyeagle.plugin.getpdf;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.Map;
import java.util.TreeMap;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import skyeagle.plugin.gui.UpdateDialog;

public class Nature implements GetPdfFile {

	private final String url;

	public Nature(String url) {
		this.url = url;
	}

	@Override
    public void getFile(UpdateDialog dig, File file, Boolean usingProxy) {

		String base = "http://www.nature.com";
		Map<String, String> cookies = new TreeMap<>();
		String pagecontent = GetPDFUtil.initGetPDF(url, usingProxy, cookies);
		if (pagecontent == null) {
            dig.output("The network don't work, please check proxy and network.");
			return;
		}
		Document doc = Jsoup.parse(pagecontent);
		String pdflink = null;
		int flag = pagecontent.indexOf("Scientific Reports");
		if (flag != -1) {
            pdflink = doc.select("a[data-track-dest=link:Download as PDF]").attr("href");
        } else {
            pdflink = doc.select("a#download-pdf").attr("href");
        }
		if (pdflink.isEmpty()) {
            dig.output("cann't find the link to download pdf file, please try to use proxy or change the proxy");
			return;
		}
		pdflink = base + pdflink;
		HttpURLConnection con = null;
		if (flag != -1) {
            con = GetPDFUtil.createPDFLink(pdflink, cookies, false);
        } else {
            con = GetPDFUtil.createPDFLink(pdflink, cookies, usingProxy);
        }
		int filesize = con.getContentLength();
		GetPDFUtil.getPDFFile(file, filesize, dig, con);
		con.disconnect();
	}

	public static void main(String[] args) throws IOException {
		// String str =
		// "http://www.nature.com/nphys/journal/v11/n12/full/nphys3542.html";
		String str = "http://www.nature.com/articles/srep18386";
		File file = new File("E:\\test.pdf");
		new Nature(str).getFile(null, file, false);
	}
}
