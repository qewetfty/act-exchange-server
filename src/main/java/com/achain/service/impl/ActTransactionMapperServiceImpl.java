package com.achain.service.impl;

import com.achain.domain.entity.ActTransaction;
import com.achain.mapper.ActTransactionMapper;
import com.achain.service.IActTransactionMapperService;
import com.baomidou.mybatisplus.service.impl.ServiceImpl;

import org.springframework.stereotype.Service;

/**
 * @author yujianjian
 * @since 2017-12-15 下午4:45
 */
@Service
public class ActTransactionMapperServiceImpl extends ServiceImpl<ActTransactionMapper, ActTransaction>
    implements IActTransactionMapperService {

}
