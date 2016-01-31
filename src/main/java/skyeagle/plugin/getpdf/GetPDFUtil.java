package skyeagle.plugin.getpdf;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;

import skyeagle.plugin.gui.UpdateDialog;

public class GetPDFUtil {

	public GetPDFUtil() {

	}

	public static Proxy getProxy() {
		try {
			File pluginDir = new File(System.getProperty("user.home") + "/.jabref/plugins");
			File proxyfile = new File(pluginDir, "proxy.prop");
			BufferedReader bfr;
			bfr = new BufferedReader(new FileReader(proxyfile));
			Properties prop = new Properties();
			prop.load(bfr);
			String ip = prop.getProperty("ip");
			String port = prop.getProperty("port");
			InetSocketAddress addr = new InetSocketAddress(ip, Integer.valueOf(port));
			Proxy proxy = new Proxy(Proxy.Type.HTTP, addr);
			return proxy;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static String getPageContent(String url, Proxy proxy) {
		HttpURLConnection con = null;
		try {
			URL u = new URL(url);
			if (proxy != null) {
                con = (HttpURLConnection) u.openConnection(proxy);
            } else {
                con = (HttpURLConnection) u.openConnection();
            }
		} catch (Exception e) {
			e.printStackTrace();
		}
		String pagecontent = getPage(con);
		con.disconnect();
		return pagecontent;
	}

	public static String getPageContent(String url) {

		return getPageContent(url, null);
	}

	public static String getPage(HttpURLConnection con) {
		con.setDoOutput(true);
		con.setDoInput(true);
		con.setUseCaches(false);
		con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
		con.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; rv:37.0) Gecko/20100101 Firefox/37.0");
		StringBuilder buffer = new StringBuilder();
		try {
			int res = con.getResponseCode();
			BufferedReader br = null;
			if ((res == HttpURLConnection.HTTP_UNAUTHORIZED) | (res == HttpURLConnection.HTTP_FORBIDDEN)) {
                br = new BufferedReader(new InputStreamReader(con.getErrorStream(), "UTF-8"));
            } else {
                br = new BufferedReader(new InputStreamReader(con.getInputStream(), "UTF-8"));
            }
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

	public static HttpURLConnection createPDFLink(String pdflink, Map<String, String> cookies, Boolean usingProxy) {

		CookieManager cookieManager = new CookieManager();
		CookieHandler.setDefault(cookieManager);

		HttpURLConnection con = null;
		try {
			URL u = new URL(pdflink);

			if (usingProxy) {
                con = (HttpURLConnection) u.openConnection(getProxy());
            } else {
                con = (HttpURLConnection) u.openConnection();
            }

			con.setDoOutput(true);
			con.setDoInput(true);
			con.setUseCaches(false);
			con.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; rv:37.0) Gecko/20100101 Firefox/37.0");
		} catch (Exception e) {
			e.printStackTrace();
		}

		Set<String> set = cookies.keySet();
		for (Iterator<String> it = set.iterator(); it.hasNext();) {
			String tmp = it.next();
			String value = cookies.get(tmp);
			con.setRequestProperty("Cookie", tmp + "=" + value);
		}
		return con;
	}

	public static void getPDFFile(File file, int filesize, UpdateDialog dig, HttpURLConnection con) {

		int ratio = 0;
		int totalBytesRead = 0;
		InputStream in = null;
		OutputStream out = null;
		try {
			out = new BufferedOutputStream(new FileOutputStream(file));
			in = new BufferedInputStream(con.getInputStream());
			int bytesRead = 0;
            byte[] buffer = new byte[512];
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
				totalBytesRead += bytesRead;
				ratio = (int) (((float) totalBytesRead / (float) filesize) * 100);
				dig.downloadRatioOutput(file, ratio, totalBytesRead, filesize);
			}
			if (filesize == -1) {
                dig.output(UpdateDialog.NEWLINE);
            }
		} catch (Exception e) {
			e.printStackTrace();
            dig.output("Downloading of PDF file don't finish, please download it again.");
			file.delete();
		} finally {
			try {
				out.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public static Map<String, String> getCookies(String pdflink, Proxy proxy) {
		Map<String, String> cookies = new TreeMap<>();
		HttpURLConnection con = null;
		try {
			URL u = new URL(pdflink);
			if (proxy != null) {
                con = (HttpURLConnection) u.openConnection(proxy);
            } else {
                con = (HttpURLConnection) u.openConnection();
            }
			con.setRequestMethod("GET");
			con.setDoOutput(true);
			con.setDoInput(true);
			con.setUseCaches(false);
			con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			con.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; rv:37.0) Gecko/20100101 Firefox/37.0");
		} catch (Exception e) {
			e.printStackTrace();
		}
		String key = null;
		for (int i = 1; (key = con.getHeaderFieldKey(i)) != null; i++) {
			if (key.equalsIgnoreCase("set-cookie")) {
				String cookie = null;
				cookie = con.getHeaderField(i);
				int i1 = cookie.indexOf("=");
				int i2 = cookie.indexOf(";");
				if ((i1 != -1) && (i2 != -1)) {
					String _value = cookie.substring(i1 + 1, i2);
					String _key = cookie.substring(0, i1);
					cookies.put(_key, _value);
				}
			}
		}
		con.disconnect();
		return cookies;
	}

	public static Map<String, String> getCookies(String url) {
		return getCookies(url, null);
	}

	public static String initGetPDF(String url, Boolean usingProxy, Map<String, String> cookies) {
		Proxy proxy = null;
		if (usingProxy) {
			proxy = GetPDFUtil.getProxy();
			if (proxy == null) {
				return null;
			}
		}
		String pagecontent = null;
		if (usingProxy) {
			pagecontent = getPageContent(url, proxy);
			cookies = getCookies(url, proxy);
		} else {
			pagecontent = getPageContent(url);
			cookies = getCookies(url);
		}
		return pagecontent;
	}


}
