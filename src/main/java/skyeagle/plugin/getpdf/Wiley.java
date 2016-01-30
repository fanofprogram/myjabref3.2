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

	public void getFile(UpdateDialog dig, File file, Boolean usingProxy) {

		String basurl = "http://onlinelibrary.wiley.com";
		String link = null;
		// ��ȡ��ҳ����ַ��ȡ��pdf���ӵ�ַ
		try {
			Document doc = Jsoup.connect(url).timeout(30000).get();
			Elements eles = doc.select("a#wol1backlink");
			if (eles.size() != 0) // �������ҳ�棬���þ�ҳ�����ַ
				url = doc.select("a#wol1backlink").attr("href");
			doc = Jsoup.connect(url).timeout(30000).get();
			link = doc.select("a#journalToolsPdfLink[title=Article in pdf format]").attr("href");
		} catch (IOException e1) {
			 dig.output("ҳ�����Ӳ��ϣ��������硣");
			e1.printStackTrace();
			return;
		}
		link = basurl + link;
		link = link.replaceAll("epdf", "pdf");

		// ��ȡpdfҳ��
		String pdfpage = null;
		if (usingProxy) {
			pdfpage = GetPDFUtil.getPageContent(link, GetPDFUtil.getProxy());
		} else {
			pdfpage = GetPDFUtil.getPageContent(link);
		}
		Document doc = Jsoup.parse(pdfpage);
		String pdflink = doc.select("iframe#pdfDocument").attr("src");
		if (pdflink.isEmpty()) {
			 dig.output("ҳ�����Ҳ�������pdf�ļ������ӣ��볢��ʹ�ô�����������");
//			System.out.println("ҳ�����Ҳ�������pdf�ļ������ӣ��볢��ʹ�ô�����������");
			return;
		}

		//��ȡcookies
		Map<String, String> cookies = null;
		if (usingProxy)
			cookies = GetPDFUtil.getCookies(url, GetPDFUtil.getProxy());
		else
			cookies = GetPDFUtil.getCookies(url);
		
		// ��pdf������
		// ����ʹ�ô�������cookies����ʱ��ʹ��cookies�൱��ʹ���˴���
		//wiley�Ƚ����⣬���ص�ʱ��Ҳ������Ŵ���
		HttpURLConnection con = GetPDFUtil.createPDFLink(pdflink, cookies,usingProxy);
		// �������վ��ȡpdf�ļ�
		int filesize = con.getContentLength();
		GetPDFUtil.getPDFFile(file, filesize, dig, con);
		con.disconnect();

	}

	public static void main(String[] args) {
		String str = "http://onlinelibrary.wiley.com/doi/10.1002/ange.201508492/full";
		File file = new File("E:\\test.pdf");
		new Wiley(str).getFile(new UpdateDialog(null, "down"), file, true);
	}
}
