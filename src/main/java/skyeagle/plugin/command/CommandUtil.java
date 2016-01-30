package skyeagle.plugin.command;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CommandUtil {
	public static String DOItoURL(String url) {
		String rex = "((https?://dx\\.doi\\.org[^/]*/)|(https?://stacks\\.iop\\.org[^/]*/))";
		Pattern pattern = Pattern.compile(rex);
		Matcher matcher = pattern.matcher(url);
		if (!matcher.find()) {
            return url;
        }
		int responseCode = 0;
		HttpURLConnection con = null;
		for (int i = 1; i < 10; i++) {
			try {
				URL u = new URL(url);
				con = (HttpURLConnection) u.openConnection();
				con.setInstanceFollowRedirects(false);
				con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
				con.setRequestProperty("User-Agent",
						"Mozilla/5.0 (Windows NT 6.1; rv:37.0) Gecko/20100101 Firefox/37.0");
				responseCode = con.getResponseCode();
				if (responseCode == HttpURLConnection.HTTP_OK) {
                    break;
                }
				String newurl = con.getHeaderField("Location");
				if((newurl==null)|(newurl.indexOf("http")==-1)) {
                    break;
                }
				url = newurl;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		con.disconnect();
		return url;
	}

	public static void main(String[] args) {
		String doi = "http://stacks.iop.org/0022-3727/49/i=4/a=045002";
		DOItoURL(doi);
	}

}
