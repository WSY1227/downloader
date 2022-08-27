package com.xu.downloader;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @ClassName: Downloader
 * @Description: 下载类
 * @author: XU
 * @date: 2022年08月27日 19:41
 **/

public class Downloader {
    Integer threadNum = 10;

    /**
     * 下载单文件保存到本地
     *
     * @param source    原图片网址
     * @param targetDir 目标目录,要确保已存在
     */
    public void download(String source, String targetDir) {
        InputStream is = null;
        OutputStream ot = null;

        try {
            /*截取文件名*/
            String fileName = source.substring(source.lastIndexOf("/"));
            /*文件指定*/
            File targetFile = new File(targetDir + "/" + fileName);
            /*判断文件是否存在*/
            if (!targetFile.exists()) {
                /*创建文件*/
                targetFile.createNewFile();
            }
            /*创建网络连接*/
            URL url = new URL(source);
            /*打开网络连接*/
            URLConnection connection = url.openConnection();
            /*获取网络的输入流*/
            is = connection.getInputStream();
            /*输出流连接到输出文件*/
            ot = new FileOutputStream(targetFile);
            /*该数组用来存入从输入文件中读取到的数据*/
            byte[] bs = new byte[1024];
            /*变量len用来存储每次读取数据后的返回值*/
            int len = 0;
            /*while循环：每次从输入文件读取数据后，都写入到输出文件中*/
            while ((len = is.read(bs)) != -1) {
                ot.write(bs, 0, len);
            }
            System.out.println("[INFO]图片下载完毕：" + source + "\n\t ->" + targetFile.getPath() + "(" + Math.floor(targetFile.length() / 1024) + "kb)");
            /*关闭流*/
            if (ot != null) {
                ot.close();
            }
            if (is != null) {
                ot.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {

        }
    }

    /**
     * 从指定文件读取下载地址，批量下载网络资源
     *
     * @param targetDir   下载文件的存储目录
     * @param downloadTxt download.txt完整路径
     */
    public void multiDownloadFromFile(String targetDir, String downloadTxt) {
        File dir = new File(targetDir);
        //如果目录不存在则创建目录
        if (!dir.exists()) {
            dir.mkdirs();
            System.out.println("[INFO]发现下载目录[" + dir.getPath() + "]不存在，自动创建");
        }
        //创建下载链接集合
        List<String> resources = new ArrayList<>();
        BufferedReader reader = null;
        ExecutorService threadPool = null;
        try {
            reader = new BufferedReader(new FileReader(downloadTxt));
            String lien = null;
            while ((lien = reader.readLine()) != null) {
                resources.add(lien);
            }
//            resources.forEach(System.out::println);
            threadPool = Executors.newFixedThreadPool(this.threadNum);
            Downloader downloader = this;
            ExecutorService finalThreadPool = threadPool;
            resources.forEach(resource -> {
                finalThreadPool.execute(() -> {
                    downloader.download(resource, targetDir);
                });
            });
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (threadPool != null) {
                threadPool.shutdown();
            }
        }

    }

    /**
     * 开始多线程下载
     *
     * @param proDri config.properties所在的目录
     */
    public void start(String proDri) {
        //指定config.properties完整路径
        File proFile = new File(proDri + "\\config.properties");
        //实例化Properties属性类
        Properties properties = new Properties();
        Reader reader = null;
        try {
            //实例化FileRead类
            reader = new FileReader(proFile);
            //通过properties对象读取配置文件
            properties.load(reader);
            //通过getProperties方法得到对应的选项值
            String threadNum = properties.getProperty("thread-num");
            this.threadNum = Integer.valueOf(threadNum);
            String targetDir = properties.getProperty("target-dir");
            this.multiDownloadFromFile(targetDir, proDri + "\\download.txt");
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    public static void main(String[] args) {
        Downloader downloader = new Downloader();
        downloader.start("D:\\16692\\Videos\\java\\lianxi\\downloader\\src");
    }
}
