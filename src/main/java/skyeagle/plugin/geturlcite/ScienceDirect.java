package skyeagle.plugin.geturlcite;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

public class ScienceDirect implements GetCite{
	private String url;

	public ScienceDirect(String url) {
		this.url = url;
	}

	public String getCiteItem() {

		// ��ȡ��ַ�����ݣ�html�������н���
		// ������Ҫʹ����Jsoup�⣬�����������⣬����html�ǳ����㡣
		// ���Ȼ�ȡdoc������ʵ����html���ݣ�
		String actionUrl = null;
		try {
			Document doc;
			doc = Jsoup.connect(url).timeout(30000).get();
			// ����Jsoup�е�ѡ����Ѱ����Ҫ�Ľڵ�
			// ����Ҫ�ҵ���������õı�
			// div#export_popup>form��ʾ����˼��
			// Ѱ������Ϊdiv��idΪexport_popup�Ľڵ������е�form�ӽڵ㡣
			Elements forms = doc.select("div#export_popup>form");
			// ���е�form�ĵ�һ��form����sciencedirect��ҳ��Դ���룬��ʵֻ��һ��form��
			Element form = forms.first();
			// ���form��action����ֵ������һ��url���������������ַ�ύ�ġ�
			actionUrl = form.attr("action");
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			 e1.printStackTrace();
			// ��ҳ�����ϻ����Ҳ������쳣�����ؿ�
			return null;
		}

		// *************��������վģ���ύ������************************
		// postParams��Ҫ�ύ�ı�������
		// ��������ֱ�������ǵ������ύ����������ҳ��ѡ���ˡ�
		// �����ύ�Ĳ�����˼�ǣ����ǵ����ø�ʽΪBIBTEX����ժҪ��
		// ������Կ���ҳԴ�ļ�
		String postParams = "citation-type=BIBTEX&zone=exportDropDown&export=Export&format=cite-abs";
		// actionUrlΪ�����ַ����Ҫ��Ϊ������ַ��
		String formUrl = url + actionUrl;
		HttpURLConnection con = null;
		try {
			URL u = new URL(formUrl);
			con = (HttpURLConnection) u.openConnection();
			// �ύ����ʽΪPOST��POST ֻ��Ϊ��д���ϸ����ƣ�post�᲻ʶ��
			con.setRequestMethod("POST");
			con.setDoOutput(true);
			con.setDoInput(true);
			con.setUseCaches(false);
			// ��ʾ���ǵ�����Ϊ���ı�������Ϊutf-8
			con.setRequestProperty("Content-Type", "text/plain; charset=utf-8");
			OutputStreamWriter osw = new OutputStreamWriter(
					con.getOutputStream(), "UTF-8");
			// ����վд������
			osw.write(postParams);
			osw.flush();
			osw.close();
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
		String str = "http://www.sciencedirect.com/science/article/pii/S0038109815004056";
		String sb = new ScienceDirect(str).getCiteItem();
		if (sb != null)
			System.out.println(sb);
	}
}
