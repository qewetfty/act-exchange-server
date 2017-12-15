package com.achain.controller;

import com.achain.conf.Config;
import com.achain.service.IActTransactionMapperService;
import com.achain.utils.SDKHttpClient;
import com.alibaba.fastjson.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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

    @Autowired
    private Config config;

    @Autowired
    private SDKHttpClient httpClient;


    /**
     * 查询历史
     *
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

    @RequestMapping(value = "/wallet_transfer_to_address/{coinType}", method = RequestMethod.GET)
    public String walletTransferToAddress(@RequestParam(value = "amount_to_transfer") String amount_to_transfer,
                                          @RequestParam(value = "from_account_name") String from_account_name,
                                          @RequestParam(value = "to_address") String to_address,
                                          @RequestParam(value = "memo_message") String memo_message,
                                          @RequestParam(value = "trxId") String trxId,
                                          @PathVariable(value = "coinType") String coinType) {
        JSONArray params = new JSONArray();
        String url = config.walletUrl;
        String rpcUser = config.rpcUser;
        String contractId = config.contractId;
        String result;
        if ("ACT".equals(coinType)) {
            params.add(amount_to_transfer);
            params.add(coinType);
            params.add(from_account_name);
            params.add(to_address);
            params.add(memo_message);
            result = httpClient.post(url, rpcUser, "wallet_transfer_to_address", params);
        } else {
            String param = to_address + "|" + amount_to_transfer + "|";
            params.add(contractId);
            params.add(from_account_name);
            params.add("transfer_to");
            params.add(param);
            params.add("ACT");
            params.add("1");
            result = httpClient.post(url, rpcUser, "call_contract", params);
        }
        return result;
    }

}
