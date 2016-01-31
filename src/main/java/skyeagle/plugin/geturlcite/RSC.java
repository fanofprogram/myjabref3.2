package skyeagle.plugin.geturlcite;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class RSC implements GetCite {

	private final String url;

	public RSC(String url) {
		if(url.indexOf("articlepdf")!=-1) {
            url=url.replaceAll("articlepdf", "articlelanding");
        } else if(url.indexOf("articlehtml")!=-1) {
            url=url.replaceAll("articlehtml", "articlelanding");
        }
		this.url =url;
	}

	@Override
	public String getCiteItem() {
		String baseurl = "http://pubs.rsc.org/en/content/getformatedresult/";

		String doi = null;
		String posturl=null;
		try {
			Document doc = Jsoup.connect(url).timeout(30000).get();
			doi = doc.select("input#DOI").attr("value");
			posturl = baseurl + doi.toLowerCase()
					+ "?downloadtype=article";
		} catch (UnsupportedEncodingException e2) {
			e2.printStackTrace();
			return null;
		} catch (IOException e2) {
			e2.printStackTrace();
			return null;
		}


		HttpURLConnection con = null;
		try {
			String postParams = "ResultAbstractFormat=BibTex&go=";

			URL u = new URL(posturl);
			con = (HttpURLConnection) u.openConnection();
			con.setRequestMethod("POST");
			con.setDoOutput(true);
			con.setDoInput(true);
			con.setUseCaches(false);
			con.setRequestProperty("Content-Type",
					"application/x-www-form-urlencoded");
			con.setRequestProperty("User-Agent",
					"Mozilla/5.0 (Windows NT 6.1; rv:37.0) Gecko/20100101 Firefox/37.0");

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
		String str = "http://pubs.rsc.org/en/content/articlehtml/2015/dt/c5dt00897b";
		 String sb = new RSC(str).getCiteItem();
		 if (sb != null) {
            System.out.println(sb);
        }

	}
}
