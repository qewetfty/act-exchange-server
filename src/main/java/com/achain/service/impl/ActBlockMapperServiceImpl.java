package com.achain.service.impl;

import com.achain.domain.entity.ActBlock;
import com.achain.mapper.ActBlockMapper;
import com.achain.service.IActBlockMapperService;
import com.baomidou.mybatisplus.service.impl.ServiceImpl;

import org.springframework.stereotype.Service;

/**
 * @author yujianjian
 * @since 2017-12-15 下午4:45
 */
@Service
public class ActBlockMapperServiceImpl extends ServiceImpl<ActBlockMapper, ActBlock>
    implements IActBlockMapperService {
}
