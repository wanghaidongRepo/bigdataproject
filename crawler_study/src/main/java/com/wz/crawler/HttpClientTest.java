package com.wz.crawler;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.junit.Test;

/**
 * 爬虫演示
 */
public class HttpClientTest {

    @Test
    public void testGet() throws Exception{
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpGet httpGet = new HttpGet("http://www.itcast.cn/");
        httpGet.setHeader("user-agent","Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 \\ " +
                                                   "(KHTML, like Gecko) Chrome/84.0.4147.105 Safari/537.36");
        CloseableHttpResponse response = httpClient.execute(httpGet);
        if (response.getStatusLine().getStatusCode() == 200){
            HttpEntity entity = response.getEntity();
            String html = EntityUtils.toString(entity, "utf-8");
            System.out.println(html);
        }
        response.close();
        httpClient.close();
    }

}
