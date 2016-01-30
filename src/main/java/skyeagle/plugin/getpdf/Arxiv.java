package skyeagle.plugin.getpdf;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.Map;
import skyeagle.plugin.gui.UpdateDialog;

public class Arxiv implements GetPdfFile {
	private String url;
	public Arxiv(String url) {
		this.url = url.replaceAll("abs", "pdf");
	}
	public void getFile(UpdateDialog dig, File file, Boolean usingProxy) {
		// ��ȡ��ַ�����ݣ�html)��cookies
		Map<String,String> cookies=GetPDFUtil.getCookies(url);
		// ��pdf������
		//arxiv����ֱ������pdf�ļ�
		HttpURLConnection con = GetPDFUtil.createPDFLink(url, cookies,false);
		int filesize = con.getContentLength();
		// �������վ��ȡpdf�ļ�
		GetPDFUtil.getPDFFile(file, filesize, dig, con);
		con.disconnect();
	}
	public static void main(String[] args) throws IOException {
		String str = "http://arxiv.org/pdf/1504.06082";
		File file = new File("F:\\test.pdf");
		new Arxiv(str).getFile(null, file, false);
	}
}
