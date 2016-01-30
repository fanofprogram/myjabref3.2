package skyeagle.plugin.getpdf;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.Map;
import java.util.TreeMap;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import skyeagle.plugin.gui.UpdateDialog;

public class AIP implements GetPdfFile {
	private String url;

	public AIP(String url) {
		this.url = url;
	}

	public void getFile(UpdateDialog dig, File file, Boolean usingProxy) {
		// ��ȡ��ַ�����ݣ�html)��cookies
		Map<String,String> cookies=new TreeMap<>();
		String pagecontent=GetPDFUtil.initGetPDF(url, usingProxy, cookies);
		if(pagecontent==null){
			dig.output("���粻ͨ�������������硣");
			return;
		}
		// ʹ��Jsoup���html���ݽ��н���
		Document doc = Jsoup.parse(pagecontent);
		// ����Jsoup�е�ѡ����Ѱ����Ҫ�Ľڵ�, ����Ҫ�ҵ���pdf�ļ�������
		String pdflink = doc.select("a[class=pdf]").attr("href");
		if (pdflink.isEmpty()) {
			 dig.output("ҳ�����Ҳ�������pdf�ļ������ӣ��볢��ʹ�ô�����������");
//			System.out.println("ҳ�����Ҳ�������pdf�ļ������ӣ��볢��ʹ�ô�����������");
			return;
		}
		pdflink="http://scitation.aip.org"+pdflink;
		
		// ��pdf������
		// ����ʹ�ô�������cookies����ʱ��ʹ��cookies�൱��ʹ���˴������Բ����ٹҴ�����
		HttpURLConnection con = GetPDFUtil.createPDFLink(pdflink, cookies,false);
		int filesize = con.getContentLength();
		// �������վ��ȡpdf�ļ�
		GetPDFUtil.getPDFFile(file, filesize, dig, con);
		con.disconnect();
	}

	public static void main(String[] args) throws IOException {
		String str = "http://scitation.aip.org/content/aip/journal/jap/117/15/10.1063/1.4918311";
		File file = new File("F:\\test.pdf");
		new AIP(str).getFile(null, file, true);
	}

}
