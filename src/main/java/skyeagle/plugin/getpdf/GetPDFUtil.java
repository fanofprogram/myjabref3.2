package skyeagle.plugin.getpdf;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
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
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
            dig.output("网络连接出现问题，文件没有下载完整，请重新下载。");
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

    public static void getFileByMultiThread(File file, int filesize, UpdateDialog dig, String pdfurl,
            Boolean useproxy) {

        // 定义一个缓冲的线程值 线程池的大小根据任务变化
        ExecutorService threadPool = Executors.newCachedThreadPool();

        //假设是4个线程去下载资源。
        //平均每一个线程下载的文件大小.
        int threadCount = 4;
        int blockSize = filesize / threadCount;
        for (int threadId = 1; threadId <= threadCount; threadId++) {
            //第一个线程下载的开始位置
            int startIndex = (threadId - 1) * blockSize;
            int endIndex = (threadId * blockSize) - 1;
            if (threadId == threadCount) {//最后一个线程下载的长度要稍微长一点
                endIndex = filesize;
            }
            threadPool
                    .execute(new DownLoadThread(dig, pdfurl, threadId, startIndex, endIndex, file, filesize, useproxy));
        }

        File parentdir = file.getParentFile();
        String filename = file.getName();
        ArrayList<File> tmpfiles = new ArrayList<>();
        for (int threadId = 1; threadId <= threadCount; threadId++) {
            File tmpf = new File(parentdir, filename + ".tmp" + threadId);
            tmpfiles.add(tmpf);
        }
        //所有线程都结束后进行文件合并
        threadPool.shutdown();
        while (true) {
            if (threadPool.isTerminated()) {
                mergeFiles(file, tmpfiles);
                //删除文件片
                for (File f : tmpfiles) {
                    f.delete();
                }
                break;
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

    }

    public static void mergeFiles(File outFile, ArrayList<File> files) {
        int BUFSIZE = 1024 * 8;
        FileChannel outChannel = null;
        try {
            outChannel = new FileOutputStream(outFile).getChannel();
            for (File f : files) {
                FileChannel fc = new FileInputStream(f).getChannel();
                ByteBuffer bb = ByteBuffer.allocate(BUFSIZE);
                while (fc.read(bb) != -1) {
                    bb.flip();
                    outChannel.write(bb);
                    bb.clear();
                }
                fc.close();
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        } finally {
            try {
                if (outChannel != null) {
                    outChannel.close();
                }
            } catch (IOException ignore) {
            }
        }
    }
}

class DownLoadThread extends Thread {

    public static int threadCount = 4;
    public static int runningThread = 4;
    private final int threadId;
    private int startIndex;
    private final int endIndex;
    private final File file;
    private final String path;
    private final File parentdir;
    private final int filesize;
    private final UpdateDialog dig;
    private final Boolean useproxy;

    public static int alltotal = 0;


    /**
     * @param path 下载文件在服务器上的路径
     * @param threadId 线程Id
     * @param startIndex 线程下载的开始位置
     * @param endIndex  线程下载的结束位置
     */
    public DownLoadThread(UpdateDialog dig, String path, int threadId, int startIndex, int endIndex, File file,
            int filesize, Boolean useproxy) {
        super();
        this.threadId = threadId;
        this.startIndex = startIndex;
        this.endIndex = endIndex;
        this.path = path;
        this.file = file;
        this.filesize = filesize;
        this.dig = dig;
        this.useproxy = useproxy;
        parentdir = file.getParentFile();
    }

    @Override
    public void run() {
        int total = 0;//已经下载的数据长度
        try {
            //检查是否存在记录下载长度的文件，如果存在读取这个文件

            File tmp_file = new File(parentdir, threadId + ".txt");
            if (tmp_file.exists() && (tmp_file.length() > 0)) {
                FileInputStream fio = new FileInputStream(tmp_file);
                byte[] temp = new byte[1024];
                int len = fio.read(temp);
                String downloadlen = new String(temp, 0, len);
                int downloadInt = Integer.parseInt(downloadlen);
                startIndex = downloadInt;//修改下载的真实的开始位置
                fio.close();
            }

            URL url = new URL(path);
            HttpURLConnection conn = null;
            if (useproxy) {
                conn = (HttpURLConnection) url.openConnection(GetPDFUtil.getProxy());
            } else {
                conn = (HttpURLConnection) url.openConnection();
            }
            conn.setConnectTimeout(5000);
            conn.setRequestMethod("GET");
            //重要:请求服务器下载部分文件 指定文件的位置
            conn.setRequestProperty("Range", "bytes=" + startIndex + "-" + endIndex);
            //从服务器请求全部资源返回200 ok如果从服务器请求部分资源 返回 206 ok
            int code = conn.getResponseCode();
            InputStream is = conn.getInputStream();//已经设置了请求的位置，返回的是当前位置对应的文件的输入流
            String filename = file.getName();
            File tmp_pdf_file = new File(parentdir, filename + ".tmp" + threadId);
            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(tmp_pdf_file));

            int len = 0;
            byte[] buffer = new byte[1024];
            Object obj = new Object();
            while ((len = is.read(buffer)) != -1) {
                bos.write(buffer, 0, len);
                total += len;
                int ratio = 0;
                synchronized (obj) {
                    alltotal = alltotal + len;
                    ratio = (int) (((float) alltotal / (float) filesize) * 100);
                }
                dig.downloadRatioOutput(file, ratio, alltotal, filesize);
                //                System.out.println(ratio);
            }
            is.close();
            bos.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            File tmp_f = new File(parentdir, threadId + ".txt");
            BufferedOutputStream rec_bos;
            try {
                rec_bos = new BufferedOutputStream(new FileOutputStream(tmp_f));
                rec_bos.write(("" + (total + startIndex)).getBytes());
                rec_bos.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            runningThread--;
            if (runningThread == 0) {//所有的线程执行完毕
                for (int i = 1; i <= threadCount; i++) {
                    File f = new File(parentdir, i + ".txt");
                    f.delete();
                }

            }
        }
    }

}
