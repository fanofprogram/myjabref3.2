package skyeagle.plugin.geturlcite;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

public class Wiley implements GetCite {

	private String url;

	public Wiley(String url) {
		this.url = url;
	}

	@Override
	public String getCiteItem() {
		// �ύ������ַ
		String posturl = "http://onlinelibrary.wiley.com/documentcitationdownloadformsubmit";

		// ��ȡdoi
		String doi = null;
		try {
			Document doc = Jsoup.connect(url).timeout(30000).get();
			Elements eles=doc.select("a#wol1backlink");
			if(eles.size()!=0)
			{
				url=doc.select("a#wol1backlink").attr("href");
			}
			doi = doc.select("meta[name=citation_doi]").attr("content");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		// *************��������վģ���ύ������************************

		 // postParams��Ҫ�ύ�ı�������
		 // ��������ֱ�������ǵ������ύ����������ҳ��ѡ���ˡ�
		
		 HttpURLConnection con = null;
		 try {
		 String postParams = "doi="+URLEncoder.encode(doi, "utf-8")+
				 "&fileFormat=BIBTEX&hasAbstract=CITATION_AND_ABSTRACT";
		
		 URL u = new URL(posturl);
		 con = (HttpURLConnection) u.openConnection();
		 // �ύ����ʽΪPOST��POST ֻ��Ϊ��д���ϸ����ƣ�post�᲻ʶ��
		 con.setRequestMethod("POST");
		 con.setDoOutput(true);
		 con.setDoInput(true);
		 con.setUseCaches(false);
		 con.setRequestProperty("Content-Type",
		 "application/x-www-form-urlencoded");
		 con.setRequestProperty("User-Agent","Mozilla/5.0 (Windows NT 6.1; rv:37.0) Gecko/20100101 Firefox/37.0");
		
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

	public static void main(String[] args) {
		String str = "http://onlinelibrary.wiley.com/doi/10.1002/ange.201508492/full";
		String sb = new Wiley(str).getCiteItem();
		if (sb != null)
			System.out.println(sb);
	}
}
