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

	private String url;

	public ScienceDirect(String url) {
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
		String pdflink = doc.select("a[title=Download PDF]#pdfLink").attr("href");
		if (pdflink.isEmpty()) {
			 dig.output("ҳ�����Ҳ�������pdf�ļ������ӣ��볢��ʹ�ô�����������");
//			System.out.println("ҳ�����Ҳ�������pdf�ļ������ӣ��볢��ʹ�ô�����������");
			return;
		}

		// ��pdf������
		// ����ʹ�ô�������cookies����ʱ��ʹ��cookies�൱��ʹ���˴������Բ����ٹҴ�����
		HttpURLConnection con = GetPDFUtil.createPDFLink(pdflink, cookies,false);
		con.setRequestProperty("Referer", url); // ����Ϊû����䣬һֱ���ز��ˣ�����һ�����ԭ��
		int filesize = con.getContentLength();
		// �������վ��ȡpdf�ļ�
		GetPDFUtil.getPDFFile(file, filesize, dig, con);
		con.disconnect();
	}

	public static void main(String[] args) throws IOException {
		String str = "http://www.sciencedirect.com/science/article/pii/S0925838815318776";
		File file = new File("F:\\test.pdf");
		new ScienceDirect(str).getFile(null, file, false);
	}
}
