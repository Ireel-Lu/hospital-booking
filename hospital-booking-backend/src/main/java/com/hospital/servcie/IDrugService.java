package com.hospital.servcie;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.spring.service.IService;
import com.hospital.entity.Drug;

import java.util.List;

public interface IDrugService extends IService<Drug> {
    IPage<Drug> getDrugs(String keyword, Integer page, Integer size);

    Drug getDrugById(Long id);

    List<Drug> getAllDrugs();

    List<Drug> searchDrugs(String keyword);

    void addDrug(Drug drug);

    void updateDrug(Drug drug);

    void deleteDrug(Long id);
}
