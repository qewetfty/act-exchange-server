package com.achain.conf;


import com.achain.domain.entity.ActBlock;
import com.achain.service.IActBlockMapperService;
import com.achain.utils.SDKHttpClient;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.mapper.EntityWrapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import javax.annotation.PostConstruct;

import lombok.extern.slf4j.Slf4j;

/**
 * @author yujianjian
 * @since 2017-11-29 下午5:22
 */
@Component
@Slf4j
public class Config {


    @Value("${wallet_url}")
    public String walletUrl;

    @Value("${rpc_user}")
    public String rpcUser;


    @Value("${contract_id}")
    public String contractId;

    @Value("${act_addresses}")
    public String actAddresses;

    public long headerBlockCount;

    public List<String> checkActAddress;

    public List<String> contractIds;

    private final SDKHttpClient httpClient;


    @Autowired
    private IActBlockMapperService actBlockMapperService;


    @Autowired
    public Config(SDKHttpClient httpClient) {
        this.httpClient = httpClient;
    }


    @PostConstruct
    public void getHeaderBlockCount() {
        checkActAddress = Arrays.asList(actAddresses.split(","));
        contractIds = Arrays.asList(contractId.split(","));
        EntityWrapper<ActBlock> wrapper = new EntityWrapper<>();
        wrapper.orderBy("block_num", false);
        ActBlock actBlock = actBlockMapperService.selectOne(wrapper);
        if (Objects.nonNull(actBlock)) {
            headerBlockCount = actBlock.getBlockNum();
        } else {
            String result = httpClient.post(walletUrl, rpcUser, "blockchain_get_block_count", new JSONArray());
            JSONObject createTaskJson = JSONObject.parseObject(result);
            headerBlockCount = createTaskJson.getLong("result");
        }
    }


}
