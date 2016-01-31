package skyeagle.plugin.geturlcite;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class Nature implements GetCite {

	private final String url;
	private final String NEWLINE = System.getProperty("line.separator");
	public Nature(String url) {
		this.url = url;
	}

	@Override
	public String getCiteItem() {
		String ris="http://www.nature.com";
		try {
			Document doc=Jsoup.connect(url).ignoreHttpErrors(true).timeout(60000).get();
			String links=doc.select("a[href$=.ris]").attr("href");
			ris=ris+links;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}

				HttpURLConnection con = null;
				try {
					URL u = new URL(ris);
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
				StringBuilder sb=new StringBuilder();
				StringBuilder authors=new StringBuilder();
				try {
					BufferedReader br = new BufferedReader(new InputStreamReader(
							con.getInputStream(), "UTF-8"));

					sb.append("@article{"+NEWLINE);
					String temp;
					while ((temp = br.readLine()) != null) {
						if(temp.indexOf("AU")!=-1){
							String author=temp.substring(6);
							authors.append(author+" and ");
							continue;
						}else if(temp.indexOf("TI")!=-1){
							sb.append("title={"+temp.substring(6)+"},"+NEWLINE);
						}else if(temp.indexOf("JA")!=-1){
							sb.append("Journal={"+temp.substring(6)+"},"+NEWLINE);
						}else if(temp.indexOf("PY")!=-1){
							sb.append("Year={"+temp.substring(6, 10)+"},"+NEWLINE);
						}else if(temp.indexOf("UR  -")!=-1){
							sb.append("Url={"+temp.substring(6)+"},"+NEWLINE);
						}else if(temp.indexOf("SP")!=-1){
							sb.append("Pages={"+temp.substring(6)+"},"+NEWLINE);
						}else if(temp.indexOf("VL")!=-1){
							sb.append("Volume={"+temp.substring(6)+"},"+NEWLINE);
						}

					}
				} catch (Exception e) {
					e.printStackTrace();
					return null;
				}

				String tmpStr=authors.toString();
				sb.append("author={"+tmpStr.substring(0, tmpStr.length()-5)+"}"+NEWLINE);
				sb.append("}");
				return sb.toString();
	}

    public static void main(String[] args) {
		String str = "http://www.nature.com/articles/srep18386";
		String sb = new Nature(str).getCiteItem();
		if (sb != null) {
            System.out.println(sb);
        }
	}
}
