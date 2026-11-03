package com.hospital.booking.servcie.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.hospital.booking.dto.SourceSettingDTO;
import com.hospital.booking.entity.SourceSetting;
import com.hospital.booking.mapper.SourceSettingMapper;
import com.hospital.booking.servcie.ISourceSettingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SourceSettingServiceIml extends ServiceImpl<SourceSettingMapper, SourceSetting> implements ISourceSettingService {
    private final SourceSettingMapper sourceSettingMapper;

    @Override
    @Transactional
    public void saveSetting(SourceSettingDTO dto) {
        LambdaQueryWrapper<SourceSetting> wrapper = new LambdaQueryWrapper<>();

        wrapper.eq(SourceSetting::getDoctorId,dto.getDoctorId())
                .eq(SourceSetting::getDate,dto.getDate())
                .eq(SourceSetting::getTimeSlot,dto.getTimeSlot());

        SourceSetting existing = this.getOne(wrapper);

        if(existing != null){
            int bookedCount = existing.getTotalQuantity()-existing.getRemainingQuantity();
            int newRemaining = dto.getTotalQuantity()-bookedCount;

            existing.setTotalQuantity(dto.getTotalQuantity());
            existing.setRemainingQuantity(Math.max(newRemaining,0));//剩余数量不能为负
            this.updateById(existing);
        }else {
            SourceSetting setting = new SourceSetting();
            setting.setDoctorId(dto.getDoctorId());
            setting.setDate(dto.getDate());
            setting.setTimeSlot(dto.getTimeSlot());
            setting.setTotalQuantity(dto.getTotalQuantity());
            setting.setRemainingQuantity(dto.getTotalQuantity());
            setting.setStatus(1);
            this.save(setting);
        }

    }

    @Override
    public List<Map<String, Object>> getSettings(Long doctorId, LocalDate date) {
        return sourceSettingMapper.getDoctorSettings(doctorId,date);
    }
}
