package skyeagle.plugin.getpdf;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.Map;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import skyeagle.plugin.gui.UpdateDialog;

public class IEEE implements GetPdfFile {

	private final String url;

	public IEEE(String url) {
		this.url = url;
	}

	@Override
    public void getFile(UpdateDialog dig, File file, Boolean usingProxy) {

		Map<String, String> cookies = null;
		if (usingProxy) {
            cookies = GetPDFUtil.getCookies(url, GetPDFUtil.getProxy());
        } else {
            cookies = GetPDFUtil.getCookies(url);
        }

		Document doc = null;
		String pdflink=null;
		String link=null;
		try {
			doc = Jsoup.connect(url).cookies(cookies).timeout(30000).get();
			link= doc.select("a#full-text-pdf").attr("href");
			if (link.isEmpty()) {
                dig.output("cann't find the link to download pdf file, please try to use proxy or change the proxy");
				return;
			}
			link="http://ieeexplore.ieee.org"+link;
			doc=Jsoup.connect(link).cookies(cookies).timeout(30000).get();
			Elements tmpnod = doc.select("frame[frameborder=0]");
			for (Element ele : tmpnod) {
				String tmp = ele.attr("src");
				if (tmp.indexOf("http") != -1) {
					pdflink = tmp;
					break;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
            dig.output("The network don't work, please check proxy and network.");
			return;
		}

		HttpURLConnection con = GetPDFUtil.createPDFLink(pdflink, cookies,false);
		con.setRequestProperty("Referer", link);
		int filesize = con.getContentLength();
		GetPDFUtil.getPDFFile(file, filesize, dig, con);
		con.disconnect();
	}

	public static void main(String[] args) throws IOException {
		String str = "http://ieeexplore.ieee.org/xpls/abs_all.jsp?arnumber=7104418";
		File file = new File("F:\\test.pdf");
		new IEEE(str).getFile(null, file, false);
	}

}
