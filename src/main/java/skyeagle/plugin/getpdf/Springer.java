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
	private String url;

	public Springer(String url) {
		this.url = url;
	}

	public void getFile(UpdateDialog dig, File file, Boolean usingProxy) {
		String base = "http://link.springer.com";
		// ��ȡ��ַ�����ݣ�html)��cookies
		Map<String, String> cookies = new TreeMap<>();
		String pagecontent = GetPDFUtil.initGetPDF(url, usingProxy, cookies);
		if (pagecontent == null) {
			dig.output("���粻ͨ�������������硣");
			return;
		}
		// ʹ��Jsoup���html���ݽ��н���
		Document doc = Jsoup.parse(pagecontent);
		// ����Jsoup�е�ѡ����Ѱ����Ҫ�Ľڵ�, ����Ҫ�ҵ���pdf�ļ�������
		String pdflink = doc.select("a#abstract-actions-download-article-pdf-link").attr("href");
		if (pdflink.isEmpty()) {
			dig.output("ҳ�����Ҳ�������pdf�ļ������ӣ��볢��ʹ�ô�����������");
//			System.out.println("ҳ�����Ҳ�������pdf�ļ������ӣ��볢��ʹ�ô�����������");
			return;
		}
		pdflink = base + pdflink;

		// ��pdf������
		// ����ʹ�ô�������cookies����ʱ��ʹ��cookies�൱��ʹ���˴������Բ����ٹҴ�����
		HttpURLConnection con = GetPDFUtil.createPDFLink(pdflink, cookies, false);
		int filesize = con.getContentLength();
		// �������վ��ȡpdf�ļ�
		GetPDFUtil.getPDFFile(file, filesize, dig, con);
		con.disconnect();
	}

	public static void main(String[] args) throws IOException {
		String str = "http://link.springer.com/article/10.1007%2Fs11664-015-4303-6";
		File file = new File("E:\\test.pdf");
		new Springer(str).getFile(null, file, true);
	}
}
