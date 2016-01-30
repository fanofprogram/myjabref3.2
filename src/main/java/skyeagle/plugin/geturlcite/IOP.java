package skyeagle.plugin.geturlcite;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

public class IOP implements GetCite {

	private String url;

	public IOP(String url) {
		this.url = url;
	}

	@Override
	public String getCiteItem() {
		// �ύ������ַ
		String baseurl = "http://iopscience.iop.org/";

		// ��ȡarticleID
		Elements ele = null;
		String posturl = null;
		try {
			Document doc = Jsoup.connect(url).ignoreHttpErrors(true).timeout(30000).get();
			// ��ȡ�����ļ����ļ���
			//articleID = doc.select("input[name=articleID]").attr("value");
			ele = doc.select("span#articleId");
			String articleID=ele.get(0).text();
			posturl = baseurl
					+ "export?articleId="
					+ URLEncoder.encode(articleID, "utf-8")
					+ "&exportFormat=iopexport_bib&exportType=abs&navsubmit=Export%2Babstract";
		} catch (UnsupportedEncodingException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
			return null;
		} catch (IOException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
			return null;
		}

		// *************��������վģ���ύ������************************
		// ��������ֱ�������ǵ������ύ����������ҳ��ѡ���ˡ�
		//IOPʹ��get�����ύ

		HttpURLConnection con = null;
		try {
			URL u = new URL(posturl);
			con = (HttpURLConnection) u.openConnection();
			con.setDoOutput(true);
			con.setDoInput(true);
			con.setUseCaches(false);
			con.setRequestProperty("Content-Type",
					"application/x-www-form-urlencoded");
			con.setRequestProperty("User-Agent",
					"Mozilla/5.0 (Windows NT 6.1; rv:37.0) Gecko/20100101 Firefox/37.0");
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		} finally {
			if (con != null) {
				con.disconnect();
			}
		}

		// *************�������վ��ȡ���ص�����************************
		// ��ȡ��������
		StringBuilder buffer = new StringBuilder();
		try {
			// һ��Ҫ�з���ֵ�������޷��������͸�server�ˡ�
			BufferedReader br = new BufferedReader(new InputStreamReader(
					con.getInputStream(), "UTF-8"));
			String temp;
			while ((temp = br.readLine()) != null) {
				buffer.append(temp);
				buffer.append("\n");
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return buffer.toString();
	}

	public static void main(String[] args) throws IOException {
		String str = "http://iopscience.iop.org/article/10.7567/APEX.8.121301/meta";
		String sb = new IOP(str).getCiteItem();
		if (sb != null)
			System.out.println(sb);
	}
}
