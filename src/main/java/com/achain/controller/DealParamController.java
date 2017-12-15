package com.achain.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.achain.utils.AsymmetricEncryptionUtils;
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
@RequestMapping("/api")
public class DealParamController {

//  @Value("${file.path}")
  private String path;
//  @Value("${walletName}")
  private String walletName;


  /**
   * 查询历史
   * @param blockNum
   * @param address
   * @return
   */
  @RequestMapping(value = "/wallet_address_transaction_history", method = RequestMethod.GET)
  public String createWalletAccountTransactionHistory(@RequestParam(value = "blockNum") int blockNum,
                                                      @RequestParam(value = "address") String address) {
      JSONObject jsonObject = new JSONObject();
      String method = "wallet_address_transaction_history";
      JSONArray jsonArray = new JSONArray();
      jsonArray.add(blockNum);
      jsonArray.add(address);
    jsonObject.put("method", method);
    jsonObject.put("params", jsonArray);
    return getString(jsonObject);
  }

  /**
   * 创建账户
   * @param accountName
   * @param address
   * @return
   */
  @RequestMapping(value = "/wallet_account_create", method = RequestMethod.GET)
  public Map<String, String> createWalletAccount(@RequestParam(value = "accountName") String accountName,
                                                 @RequestParam(value = "address") String address) {
    Map<String, String> map = new HashMap<>();
    JSONObject jsonObject = new JSONObject();
    String method = "wallet_account_create";
    JSONArray jsonArray = new JSONArray();
    jsonArray.add(accountName);
    jsonObject.put("method", method);
    jsonObject.put("params", jsonArray);
    try {
      ObjectInputStream privateKeyInputStream = new ObjectInputStream(new FileInputStream(path));
      Key privateKey = (Key) privateKeyInputStream.readObject();
      String data = walletName + jsonObject.toJSONString();
      String encodeData = AsymmetricEncryptionUtils.encrypt(privateKey, data);
      map.put("data", encodeData);
      map.put("extData", AsymmetricEncryptionUtils.encrypt(privateKey, walletName + address));
      return map;
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }

  /**
   * 查询账
   * @param accountName
   * @return
   */
  @RequestMapping(value = "/wallet_account_balance", method = RequestMethod.GET)
  public String wallet_account_balance(@RequestParam(value = "accountName") String accountName) {
    JSONObject jsonObject = new JSONObject();
    String method = "wallet_account_balance";
    JSONArray jsonArray = new JSONArray();
    jsonArray.add(accountName);
    jsonObject.put("method", method);
    jsonObject.put("params", jsonArray);
    return getString(jsonObject);
  }

  /**
   * 转账
   * @param amount_to_transfer
   * @param from_account_name
   * @param to_address
   * @param memo_message
   * @param trxId
   * @return
   */
  @RequestMapping(value = "/wallet_transfer_to_address", method = RequestMethod.GET)
  public String walletTransferToAddress(@RequestParam(value = "amount_to_transfer") String amount_to_transfer,
                                        @RequestParam(value = "from_account_name") String from_account_name,
                                        @RequestParam(value = "to_address") String to_address,
                                        @RequestParam(value = "memo_message") String memo_message,
                                        @RequestParam(value = "trxId") String trxId) {
    JSONObject jsonObject = new JSONObject();
    String method = "wallet_transfer_to_address";
    JSONArray jsonArray = new JSONArray();
    jsonArray.add(amount_to_transfer);
    jsonArray.add("ACT");
    jsonArray.add(from_account_name);
    jsonArray.add(to_address);
    jsonArray.add(memo_message);
    jsonObject.put("method", method);
    jsonObject.put("trxId", trxId);
    jsonObject.put("params", jsonArray);
    return getString(jsonObject);
  }

  private String getString(JSONObject jsonObject) {
    try {
      ObjectInputStream privateKeyInputStream = new ObjectInputStream(new FileInputStream(path));
      Key privateKey = (Key) privateKeyInputStream.readObject();
      String data = walletName + jsonObject.toJSONString();
      return AsymmetricEncryptionUtils.encrypt(privateKey, data);
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }
}
