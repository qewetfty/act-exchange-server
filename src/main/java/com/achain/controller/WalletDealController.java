package com.achain.controller;

import com.achain.utils.AsymmetricEncryptionUtils;
import com.achain.utils.SDKHttpClient;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.security.Key;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by qiangkz on 2017/8/15.
 */
@RestController
@RequestMapping("/api/wallet")
public class WalletDealController {

  @Value("${file.path}")
  private String path;

  @Value("${exchange_name_path}")
  private String exchangeNamePath;

  @Value("${exchange_transaction_query_path}")
  private String exchange_transaction_query_path;

  private final SDKHttpClient httpConfig;

  @Autowired
  public WalletDealController(SDKHttpClient sdkHttpClient) {
    this.httpConfig = sdkHttpClient;
  }


  /**
   * 查询历史
   */
  @RequestMapping(value = "/wallet_address_transaction_history/{coinType}")
  public String createWalletAccountTransactionHistory(@RequestParam(value = "blockNum") int blockNum,
                                                      @RequestParam(value = "address") String address,
                                                      @PathVariable(value = "coinType") String coinType) {
    JSONObject jsonObject = new JSONObject();
    String method = "wallet_address_transaction_history";
    JSONArray jsonArray = new JSONArray();
    jsonArray.add(blockNum);
    jsonArray.add(address);
    jsonObject.put("method", method);
    jsonObject.put("params", jsonArray);
    Map<String, String> map = new HashMap<>();
    map.put("data", getString(jsonObject, coinType));
    return httpConfig.post(exchangeNamePath + coinType, map, "UTF-8");
  }

  /**
   * 创建账户
   */
  @RequestMapping(value = "/wallet_account_create/{coinType}")
  public Map<String, String> createWalletAccount(@RequestParam(value = "accountName") String accountName,
                                                 @RequestParam(value = "address") String address,
                                                 @PathVariable(value = "coinType") String coinType) {
    Map<String, String> map = new HashMap<>();
    JSONObject jsonObject = new JSONObject();
    String method = "wallet_account_create";
    JSONArray jsonArray = new JSONArray();
    jsonArray.add(accountName);
    jsonObject.put("method", method);
    jsonObject.put("params", jsonArray);
    try (ObjectInputStream privateKeyInputStream = new ObjectInputStream(
        new FileInputStream(String.format(path, coinType)))
    ) {
      Key privateKey = (Key) privateKeyInputStream.readObject();
      map.put("data", getString(jsonObject, coinType));
      map.put("extData", AsymmetricEncryptionUtils.encrypt(privateKey, coinType + address));
      Map<String, String> map1 = new HashMap<>();
      map1.put("result", httpConfig.post(exchangeNamePath + coinType, map, "UTF-8"));
      return map1;
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }

  /**
   * 查询账
   */
  @RequestMapping(value = "/wallet_account_balance/{coinType}")
  public String wallet_account_balance(@RequestParam(value = "accountName") String accountName,
                                       @PathVariable(value = "coinType") String coinType) {
    JSONObject jsonObject = new JSONObject();
    String method = "wallet_account_balance";
    JSONArray jsonArray = new JSONArray();
    jsonArray.add(accountName);
    jsonObject.put("method", method);
    jsonObject.put("params", jsonArray);
    Map<String, String> map = new HashMap<>();
    map.put("data", getString(jsonObject, coinType));
    return httpConfig.post(exchangeNamePath + coinType, map, "UTF-8");
  }

  /**
   * 转账
   */
  @RequestMapping(value = "/wallet_transfer_to_address/{coinType}", method = RequestMethod.GET)
  public String walletTransferToAddress(@RequestParam(value = "amount_to_transfer") String amount_to_transfer,
                                        @RequestParam(value = "from_account_name") String from_account_name,
                                        @RequestParam(value = "to_address") String to_address,
                                        @RequestParam(value = "memo_message") String memo_message,
                                        @RequestParam(value = "trxId") String trxId,
                                        @PathVariable(value = "coinType") String coinType) {
    JSONObject jsonObject = new JSONObject();
    String method = "wallet_transfer_to_address";
    JSONArray jsonArray = new JSONArray();
    jsonArray.add(amount_to_transfer);
    jsonArray.add(coinType);
    jsonArray.add(from_account_name);
    jsonArray.add(to_address);
    jsonArray.add(memo_message);
    jsonObject.put("method", method);
    jsonObject.put("trxId", trxId);
    jsonObject.put("params", jsonArray);
    Map<String, String> map = new HashMap<>();
    map.put("data", getString(jsonObject, coinType));
    return httpConfig.post(exchangeNamePath + coinType, map, "UTF-8");
  }

  private String getString(JSONObject jsonObject, String coinType) {
    try (ObjectInputStream privateKeyInputStream =
             new ObjectInputStream(new FileInputStream(String.format(path, coinType)))){
      Key privateKey = (Key) privateKeyInputStream.readObject();
      String data = coinType + jsonObject.toJSONString();
      return AsymmetricEncryptionUtils.encrypt(privateKey, data);
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }

  /**
   * 查询历史
   */
  @RequestMapping(value = "/transactionList/query")
  public String transactionList(@RequestParam(value = "blockNum") int blockNum,
                                @RequestParam(value = "address") String address,
                                @RequestParam(value = "coinType") String coinType) {

    Map<String, String> map = new HashMap<>();
    map.put("start", blockNum + "");
    map.put("userAddress", address);
    map.put("coinType", coinType);
    return httpConfig.post(exchange_transaction_query_path + "/transactionList/query/" + coinType, map, "UTF-8");
  }

  /**
   * 查询提现交易记录
   * @param blockNum
   * @param address
   * @param coinType
   * @return
   */
  @RequestMapping(value = "/withdrawList/query")
  public String withdrawList(@RequestParam(value = "blockNum") int blockNum,
                                @RequestParam(value = "address") String address,
                                @RequestParam(value = "coinType") String coinType) {

    Map<String, String> map = new HashMap<>();
    map.put("start", blockNum + "");
    map.put("userAddress", address);
    map.put("coinType", coinType);
    return httpConfig.post(exchange_transaction_query_path + "/withdrawList/query/" + coinType, map, "UTF-8");
  }

  /**
   * 查询具体的交易记录
   */
  @RequestMapping(value = "/transactionInfo/query")
  public String transactionInfoQuery(@RequestParam(value = "trxId") String trxId) {
    Map<String, String> map = new HashMap<>();
    map.put("trxId", trxId);
    String url = exchange_transaction_query_path + "/transactionInfo/query";
    return httpConfig.post(url, map, "UTF-8");
  }

  /**
   * 查询具体的交易记录
   */
  @RequestMapping(value = "/generateKeyPair")
  public void generateKeyPair(@RequestParam(value = "key") String key) {
     AsymmetricEncryptionUtils.generateKeyPair(key);
  }


  /**
   * 获取当前最大区块
   * @return
   */
  @RequestMapping(value = "/getBlockNum")
  public String getBlockNum(){
    String url = exchange_transaction_query_path + "/getBlockNum";
    Map<String, String> map = new HashMap<>();
    return httpConfig.post(url, map, "UTF-8");
  }
}
