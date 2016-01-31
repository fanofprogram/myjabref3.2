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
	private final String url;

	public ScienceDirect(String url) {
		this.url = url;
	}

	@Override
    public String getCiteItem() {

		String actionUrl = null;
		try {
			Document doc;
			doc = Jsoup.connect(url).timeout(30000).get();
			Elements forms = doc.select("div#export_popup>form");
			Element form = forms.first();
			actionUrl = form.attr("action");
		} catch (IOException e1) {
			 e1.printStackTrace();
			return null;
		}

		String postParams = "citation-type=BIBTEX&zone=exportDropDown&export=Export&format=cite-abs";
		String formUrl = url + actionUrl;
		HttpURLConnection con = null;
		try {
			URL u = new URL(formUrl);
			con = (HttpURLConnection) u.openConnection();
			con.setRequestMethod("POST");
			con.setDoOutput(true);
			con.setDoInput(true);
			con.setUseCaches(false);
			con.setRequestProperty("Content-Type", "text/plain; charset=utf-8");
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
		String str = "http://www.sciencedirect.com/science/article/pii/S0038109815004056";
		String sb = new ScienceDirect(str).getCiteItem();
		if (sb != null) {
            System.out.println(sb);
        }
	}
}
