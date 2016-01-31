package skyeagle.plugin.getpdf;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.Map;
import java.util.TreeMap;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import skyeagle.plugin.gui.UpdateDialog;

public class IOP implements GetPdfFile {

	private final String url;

	public IOP(String url) {
		this.url = url;
	}

    @Override
    public void getFile(UpdateDialog dig, File file, Boolean usingProxy) {

		String base="http://iopscience.iop.org";
		Map<String, String> cookies = new TreeMap<>();
		String pagecontent = GetPDFUtil.initGetPDF(url, usingProxy, cookies);
		if (pagecontent == null) {
            dig.output("网络不通，请检查代理和网络。");
			return;
		}
		Document doc = Jsoup.parse(pagecontent);
		String pdflink = doc.select("a[class$=pdf-button-main]").attr("href");
		if (pdflink.isEmpty()) {
            dig.output("页面上找不到下载pdf文件的连接，请尝试使用代理或更换代理。");
			return;
		}
		pdflink=base+pdflink;

		HttpURLConnection con = GetPDFUtil.createPDFLink(pdflink, cookies,false);
		int filesize = con.getContentLength();
        if (filesize != -1) {
            GetPDFUtil.getFileByMultiThread(file, filesize, dig, url, usingProxy);
        } else {
            GetPDFUtil.getPDFFile(file, filesize, dig, con);
            con.disconnect();
        }
	}

	public static void main(String[] args) throws IOException {
		String str = "http://iopscience.iop.org/article/10.1088/0022-3727/49/4/045002/meta";
		File file = new File("F:\\test.pdf");
		new IOP(str).getFile(null, file, true);
	}

}
