package skyeagle.plugin.geturlcite;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class AIP implements GetCite {

	private final String url;

	public AIP(String url) {
		this.url = url;
	}

	@Override
	public String getCiteItem() {
		String baseurl = "http://scitation.aip.org/";

		String formUrl = null;
		try {
			Document doc = Jsoup.connect(url).timeout(30000).get();
			formUrl= doc.select(":contains(bibtex)").attr("href");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		String posturl=baseurl+formUrl;

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
		StringBuilder buffer = new StringBuilder();
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(
					con.getInputStream(), "UTF-8"));
			String temp;
			String eid=null;
			while ((temp = br.readLine()) != null) {
				if (temp.indexOf("eid")!=-1) {
					eid = temp.substring(temp.length()-7, temp.length()-1);
				}
				if(temp.indexOf("pages")!=-1)
				{
					temp="   pages = \""+eid+"\",";
				}
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
		String str = "http://scitation.aip.org/content/aip/journal/jap/117/15/10.1063/1.4918311";
		String sb = new AIP(str).getCiteItem();
		if (sb != null) {
            System.out.println(sb);
        }
	}
}
