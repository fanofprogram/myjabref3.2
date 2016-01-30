package skyeagle.plugin.geturlcite;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class Springer implements GetCite {

	private String url;

	public Springer(String url) {
		this.url = url;
	}

	@Override
	public String getCiteItem() {
		// �ύ������ַ������ַ
		String beginUrl="http://link.springer.com/";
		String endUrl=url.substring(beginUrl.length(), url.length());
		String baseurl=beginUrl+"export-citation/"+endUrl;
		String posturl=baseurl+".bib";

		// *************��������վģ���ύ������************************
		// AIP��վ����ʹ��post�ύ�ģ��õ�get
		HttpURLConnection con = null;
		try {
			URL u = new URL(posturl);
			con = (HttpURLConnection) u.openConnection();
			con.setDoOutput(true);
			con.setDoInput(true);
			con.setUseCaches(false);
			con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			con.setRequestProperty("User-Agent","Mozilla/5.0 (Windows NT 6.1; rv:37.0) Gecko/20100101 Firefox/37.0");
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

	public static void main(String[] args) {
		String str = "http://link.springer.com/chapter/10.1007/978-3-319-16640-7_3";
		String sb = new Springer(str).getCiteItem();
		if (sb != null)
			System.out.println(sb);
	}
}
