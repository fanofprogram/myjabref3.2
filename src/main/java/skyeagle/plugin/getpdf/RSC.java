package skyeagle.plugin.getpdf;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.Map;
import java.util.TreeMap;

import skyeagle.plugin.gui.UpdateDialog;

public class RSC implements GetPdfFile {

	private String url;

	public RSC(String url) {
		url = url.toLowerCase();
		if (url.indexOf("articlepdf") != -1)
			url = url.replaceAll("articlepdf", "articlehtml");
		else if (url.indexOf("articlelanding") != -1)
			url = url.replaceAll("articlelanding", "articlehtml");
		this.url = url;
	}

	@Override
	public void getFile(UpdateDialog dig, File file, Boolean usingProxy) {
		// ��ȡ��ַ�����ݣ�html)��cookies
		Map<String, String> cookies = new TreeMap<>();
		String pagecontent = GetPDFUtil.initGetPDF(url, usingProxy, cookies);
		if (pagecontent == null) {
			dig.output("�������ô���");
			return;
		}
		String pdflink = url.replaceAll("articlehtml", "articlepdf");
		// ��pdf������
		// ����ʹ�ô�������cookies����ʱ��ʹ��cookies�൱��ʹ���˴������Բ����ٹҴ�����
		HttpURLConnection con = GetPDFUtil.createPDFLink(pdflink, cookies, false);
		int filesize = con.getContentLength();
		// �������վ��ȡpdf�ļ�
		GetPDFUtil.getPDFFile(file, filesize, dig, con);
		con.disconnect();
	}

	public static void main(String[] args) throws IOException {
		String str = "http://pubs.rsc.org/en/Content/ArticleLanding/2015/TA/C5TA07526B";
		File file = new File("E:\\test.pdf");
		new RSC(str).getFile(new UpdateDialog(null, "down"), file, true);
	}
}
