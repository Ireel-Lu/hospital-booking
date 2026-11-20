package com.hospital.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.hospital.entity.Drug;
import com.hospital.entity.Result;
import com.hospital.servcie.IDrugService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "药品管理", description = "DrugController")
@RestController
@RequestMapping("/drug")
@RequiredArgsConstructor
public class DrugController {

    private final IDrugService drugService;

    /**
     * 分页查询药品列表
     */
    @Operation(summary = "分页查询药品列表（list）")
    @GetMapping("/list")
    public Result list(@RequestParam(defaultValue = "1") Integer page,
                       @RequestParam(defaultValue = "10") Integer size,
                       @RequestParam(required = false) String keyword){
        IPage<Drug> pageResult = drugService.getDrugs(keyword,page,size);
        return Result.success(pageResult);
    }

    /**
     * 根据ID查询药品详情
     * @param id 药品ID
     * @return 药品详情
     */
    @Operation(summary = "根据ID查询药品详情（getById）")
    @GetMapping("/{id}")
    public Result getById(@PathVariable Long id) {
        Drug drug = drugService.getDrugById(id);
        if (drug == null) {
            return Result.error("药品不存在");
        }
        return Result.success(drug);
    }

    /**
     * 查询所有药品（不分页）
     * 用于医生写病历时选择药品
     * @return 所有药品列表
     */
    @Operation(summary = "查询所有药品（getAll）")
    @GetMapping("/all")
    public Result getAll() {
        List<Drug> drugs = drugService.getAllDrugs();
        return Result.success(drugs);
    }

    /**
     * 搜索药品（不分页，用于下拉选择）
     * @param keyword 搜索关键字
     * @return 匹配的药品列表
     */
    @Operation(summary = "搜索药品（search）")
    @GetMapping("/search")
    public Result search(@RequestParam(required = false) String keyword) {
        List<Drug> drugs = drugService.searchDrugs(keyword);
        return Result.success(drugs);
    }

    /**
     * 添加新药品
     * @param drug 药品对象，包含药品名称、规格、生产厂家、价格等信息
     * @return 添加结果
     */
    @Operation(summary = "新增药品（add）")
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/add")
    public Result add(@Valid @RequestBody Drug drug) {
        drugService.addDrug(drug);
        return Result.success("添加成功");
    }

    /**
     * 更新药品信息
     * @param drug 药品对象，必须包含ID字段
     * @return 更新结果
     */
    @Operation(summary = "更新药品信息（update）")
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/update")
    public Result update(@RequestBody Drug drug) {
        if (drug.getId() == null) {
            return Result.error("ID不能为空");
        }
        drugService.updateDrug(drug);
        return Result.success("更新成功");
    }

    /**
     * 删除药品
     * @param id 药品ID
     * @return 删除结果
     */
    @Operation(summary = "删除药品（delete）")
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Long id) {
        drugService.deleteDrug(id);
        return Result.success("删除成功");
    }

}
