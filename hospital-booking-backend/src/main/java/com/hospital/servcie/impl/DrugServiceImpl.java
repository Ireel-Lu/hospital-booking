package com.hospital.servcie.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.hospital.entity.Drug;
import com.hospital.mapper.DrugMapper;
import com.hospital.servcie.IDrugService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DrugServiceImpl extends ServiceImpl<DrugMapper, Drug> implements IDrugService {

    private final DrugMapper drugMapper;

    @Override
    public IPage<Drug> getDrugs(String keyword, Integer page, Integer size) {
        Page<Drug> pageParam = new Page<>(page,size);
        LambdaQueryWrapper<Drug> wrapper = new LambdaQueryWrapper<>();
        if(keyword!=null && !keyword.trim().isEmpty()){
            wrapper.like(Drug::getName,keyword)
                    .or()
                    .like(Drug::getSpecification,keyword);
        }
        wrapper.orderByDesc(Drug::getCreateTime);
        return drugMapper.selectPage(pageParam,wrapper);
    }

    @Override
    public Drug getDrugById(Long id) {
        return drugMapper.selectById(id);
    }

    @Override
    public List<Drug> getAllDrugs() {
        return drugMapper.selectList(null);
    }

    @Override
    public List<Drug> searchDrugs(String keyword) {
        LambdaQueryWrapper<Drug> wrapper = new LambdaQueryWrapper<>();
        if(keyword != null && !keyword.trim().isEmpty()){
            wrapper.like(Drug::getName,keyword);
        }

        wrapper.eq(Drug::getStatus,1);
        wrapper.orderByDesc(Drug::getName);
        return drugMapper.selectList(wrapper);
    }

    @Override
    public void addDrug(Drug drug) {
        drugMapper.insert(drug);
    }

    @Override
    public void updateDrug(Drug drug) {
        drugMapper.updateById(drug);
    }

    @Override
    public void deleteDrug(Long id) {
        drugMapper.deleteById(id);
    }
}
