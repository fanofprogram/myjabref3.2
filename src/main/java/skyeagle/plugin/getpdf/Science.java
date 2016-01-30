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

	private String url;

	public Science(String url) {
		this.url = url;
	}

	@Override
	public void getFile(UpdateDialog dig, File file, Boolean usingProxy) {
		// TODO Auto-generated method stub

		// ��ȡ��ַ�����ݣ�html)��cookies
		Map<String, String> cookies = new TreeMap<>();
		String pagecontent = GetPDFUtil.initGetPDF(url, usingProxy, cookies);
		if (pagecontent == null) {
			dig.output("���粻ͨ�������������硣");
			return;
		}
		// ʹ��Jsoup���html���ݽ��н���
		String pdflink = null;
		Document doc = Jsoup.parse(pagecontent);
		// ����Jsoup�е�ѡ����Ѱ����Ҫ�Ľڵ�, ����Ҫ�ҵ���pdf�ļ�������
		Elements tmpnod = doc.select("a[target=_blank]");
		for (Element ele : tmpnod) {
			String tmp = ele.attr("href");
			if (tmp.indexOf("pdf") != -1) {
				pdflink = tmp;
				break;
			}
		}
		pdflink = "http://science.sciencemag.org" + pdflink;

		// ��pdf������
		// science���������׵�ʱ��Ҳ��Ҫ���ϴ���
		HttpURLConnection con = GetPDFUtil.createPDFLink(pdflink, cookies, usingProxy);
		int filesize = con.getContentLength();
		// �������վ��ȡpdf�ļ�
		GetPDFUtil.getPDFFile(file, filesize, dig, con);
		con.disconnect();
	}

	public static void main(String[] args) throws IOException {
		String str = "http://science.sciencemag.org/content/early/2015/11/24/science.aad3749.abstract";
		File file = new File("F:\\test.pdf");
		new Science(str).getFile(null, file, true);
	}
}
