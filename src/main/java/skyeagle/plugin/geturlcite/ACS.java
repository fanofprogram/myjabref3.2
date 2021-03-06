package skyeagle.plugin.geturlcite;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class ACS implements GetCite {

	private final String url;

	public ACS(String url) {
		this.url = url;
	}

	@Override
	public String getCiteItem() {
		String posturl = "http://pubs.acs.org/action/downloadCitation";

		String doi = null;
		String downloadFileName = null;
		try {
			String baseurl = "http://pubs.acs.org/doi/abs/";
			doi = url.substring(baseurl.length(), url.length());
			String downloadCiteUrl = "http://pubs.acs.org/action/showCitFormats?doi="
					+ URLEncoder.encode(doi, "utf-8");
			Document doc = Jsoup.connect(downloadCiteUrl).timeout(30000).get();
			downloadFileName = doc.select("input[name=downloadFileName]").attr(
					"value");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}

		Map<String, String> cookies = null;
		try {
			Response response = Jsoup.connect(posturl).timeout(20000).execute();
			cookies = response.cookies();
		} catch (IOException e1) {
			e1.printStackTrace();
		}


		HttpURLConnection con = null;
		try {
			String postParams = "doi=" + URLEncoder.encode(doi, "utf-8")
					+ "&downloadFileName="
					+ URLEncoder.encode(downloadFileName, "utf-8")
					+ "&include=abs&format=bibtex&direct=true"
					+ "&submit=Download"
					+ URLEncoder.encode("Citation(s)", "utf-8");

			URL u = new URL(posturl);
			con = (HttpURLConnection) u.openConnection();
			con.setRequestMethod("POST");
			con.setDoOutput(true);
			con.setDoInput(true);
			con.setUseCaches(false);
			con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			con.setRequestProperty("User-Agent","Mozilla/5.0 (Windows NT 6.1; rv:37.0) Gecko/20100101 Firefox/37.0");
			Set<String> set = cookies.keySet();
			for (Iterator<String> it = set.iterator(); it.hasNext();) {
				String tmp = it.next();
				String value = cookies.get(tmp);
				con.setRequestProperty("Cookie", tmp + "=" + value);
			}

            @SuppressWarnings("resource")
            OutputStreamWriter osw = new OutputStreamWriter(
					con.getOutputStream(), "UTF-8");
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

		StringBuilder buffer = new StringBuilder();
		try {
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
		String str = "http://pubs.acs.org/doi/abs/10.1021/acsami.5b01460";
		String sb = new ACS(str).getCiteItem();
		if (sb != null) {
            System.out.println(sb);
        }
	}
}
