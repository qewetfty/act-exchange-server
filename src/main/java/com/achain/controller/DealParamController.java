package com.achain.controller;

import com.achain.service.IActTransactionMapperService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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

  @Autowired
  private IActTransactionMapperService actTransactionMapperService;


  /**
   * 查询历史
   * @param start
   * @param address
   * @return
   */
  @RequestMapping(value = "/wallet_address_transaction_history", method = RequestMethod.GET)
  public Map<String, Object> createWalletAccountTransactionHistory(@RequestParam(value = "start") long start,
                                                      @RequestParam(value = "address") String address) {
      Map<String, Object> map = actTransactionMapperService.WalletAccountTransactionHistory(start, address);
      return map;
  }


}
