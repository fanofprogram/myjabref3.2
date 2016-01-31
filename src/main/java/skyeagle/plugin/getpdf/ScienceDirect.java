package skyeagle.plugin.getpdf;

import java.io.File;
import java.io.IOException;
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
		Map<String,String> cookies=new TreeMap<>();
		String pagecontent=GetPDFUtil.initGetPDF(url, usingProxy, cookies);
		if(pagecontent==null){
            dig.output("The network don't work, please check proxy and network.");
			return;
		}
		Document doc = Jsoup.parse(pagecontent);
		String pdflink = doc.select("a[title=Download PDF]#pdfLink").attr("href");
		if (pdflink.isEmpty()) {
            dig.output("cann't find the link to download pdf file, please try to use proxy or change the proxy");
			return;
		}

		HttpURLConnection con = GetPDFUtil.createPDFLink(pdflink, cookies,false);
        con.setRequestProperty("Referer", url);
		int filesize = con.getContentLength();
		GetPDFUtil.getPDFFile(file, filesize, dig, con);
		con.disconnect();
	}

	public static void main(String[] args) throws IOException {
		String str = "http://www.sciencedirect.com/science/article/pii/S0925838815318776";
		File file = new File("F:\\test.pdf");
		new ScienceDirect(str).getFile(null, file, false);
	}
}
