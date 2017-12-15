package com.achain.utils;


import lombok.extern.slf4j.Slf4j;
import org.apache.http.Consts;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.config.ConnectionConfig;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.CodingErrorAction;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Created by qiangkezhen
 */
@Configuration
@Slf4j
public class HttpConfig {

  private CloseableHttpClient client;
  private PoolingHttpClientConnectionManager poolingHttpClientConnectionManager;
  private int TIMEOUT_5_MINS_IN_MILLIS = 5 * 60 * 1000;

  @PostConstruct
  private void init() throws Exception {

    RequestConfig requestConfig = RequestConfig.copy(RequestConfig.DEFAULT)
                                               .setSocketTimeout(TIMEOUT_5_MINS_IN_MILLIS)
                                               .setConnectionRequestTimeout(TIMEOUT_5_MINS_IN_MILLIS)
                                               .setConnectTimeout(TIMEOUT_5_MINS_IN_MILLIS)
                                               .build();
    ConnectionConfig connectionConfig = ConnectionConfig.copy(ConnectionConfig.DEFAULT)
                                                        .setMalformedInputAction(CodingErrorAction.IGNORE)
                                                        .setUnmappableInputAction(CodingErrorAction.IGNORE)
                                                        .setCharset(Consts.UTF_8)
                                                        .build();
    SSLContext sslcontext = createIgnoreVerifySSL();
    Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory>create()
        .register("http", PlainConnectionSocketFactory.INSTANCE)
        .register("https", new SSLConnectionSocketFactory(sslcontext))
        .build();
    poolingHttpClientConnectionManager = new PoolingHttpClientConnectionManager(socketFactoryRegistry);

    poolingHttpClientConnectionManager.setMaxTotal(1000);
    poolingHttpClientConnectionManager.setDefaultMaxPerRoute(500);
    client = HttpClients.custom()
                        .setConnectionManager(poolingHttpClientConnectionManager)
                        .setConnectionManagerShared(false)
                        .evictIdleConnections(60, TimeUnit.SECONDS)
                        .evictExpiredConnections()
                        .setDefaultRequestConfig(requestConfig)
                        .setDefaultConnectionConfig(connectionConfig)
                        .setRetryHandler(new DefaultHttpRequestRetryHandler(3, true))
                        .useSystemProperties().build();
  }

  @PreDestroy
  private void destroy() throws IOException {
    client.close();
  }

  @Bean
  public CloseableHttpClient getClient() {
    return client;
  }


  /**
   * 绕过验证
   */
  private SSLContext createIgnoreVerifySSL() throws NoSuchAlgorithmException, KeyManagementException {
    SSLContext sc = SSLContext.getInstance("SSLv3");
    // 实现一个X509TrustManager接口，用于绕过验证，不用修改里面的方法
    X509TrustManager trustManager = new X509TrustManager() {
      @Override
      public void checkClientTrusted(
          java.security.cert.X509Certificate[] paramArrayOfX509Certificate,
          String paramString) throws CertificateException {
      }

      @Override
      public void checkServerTrusted(
          java.security.cert.X509Certificate[] paramArrayOfX509Certificate,
          String paramString) throws CertificateException {
      }

      @Override
      public java.security.cert.X509Certificate[] getAcceptedIssuers() {
        return null;
      }
    };
    sc.init(null, new TrustManager[]{trustManager}, null);
    return sc;
  }


  /**
   *
   * @param url
   * @param map
   * @param charSet
   * @return
   */
  public String post(String url, final Map<String, String> map, final String charSet) {
    CloseableHttpClient httpclient = client;
    HttpPost httppost = null;
    String result = null;
    if (null == map) {
      return result;
    }
    log.info("【SDKHttpClient】｜post开始：url=[{}],args=[{}],charSet=[{}]", url, map, charSet);
    try {
      httppost = new HttpPost(url);
      List<NameValuePair> nvps = new ArrayList<>();
      for (Map.Entry<String, String> entry : map.entrySet()) {
        nvps.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
      }
      httppost.setEntity(new UrlEncodedFormEntity(nvps, Charset.forName(charSet)));
      CloseableHttpResponse response = httpclient.execute(httppost);
      log.info("【SDKHttpClient】｜POST URL:[{}],响应结果[response={}]!", url,response);
      if (null != response) {
        if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
          try {
            result = EntityUtils.toString(response.getEntity(), charSet);
            log.info("【SDKHttpClient】｜响应结果：{},[{}]", response.getStatusLine(), result);
          } finally {
            response.close();
          }
        }else {
          log.info("【SDKHttpClient】｜POST URL:[{}],响应结果[{}]!", url,response.getStatusLine().getStatusCode());
        }
      } else {
        log.info("【SDKHttpClient】｜POST URL:[{}],响应结果为空!", url);
      }
    } catch (Exception e) {
      log.error("【SDKHttpClient】｜POST URL:[{}] 出现异常[{}]!", url, e.getStackTrace());
    } finally {
        if (null != httppost) {
          httppost.releaseConnection();
        }
    }
    return result;
  }
}
