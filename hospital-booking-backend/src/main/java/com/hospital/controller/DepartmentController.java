package com.hospital.controller;

import com.hospital.entity.Department;
import com.hospital.entity.Result;

import com.hospital.servcie.IDepartmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "科室管理", description = "DepartmentController")
@RestController
@RequestMapping("/department")
@RequiredArgsConstructor
public class DepartmentController {

    private final IDepartmentService departmentService;

    /**
     * 查询科室列表
     * @param keyword 搜索关键字（科室名称、描述）
     * @return 科室列表数据
     */
    @Operation(summary = "查询科室列表（list）")
    @GetMapping("/list")
    public Result list(@RequestParam(required = false) String keyword){
        return Result.success(departmentService.getDepartmentList(keyword));
    }

    /**
     * 根据Id查询科室详情
     * @param id 科室Id
     * @return 科室详情数据
     */
    @Operation(summary = "根据ID查询科室详情（getById）")
    @GetMapping("/{id}")
    public Result getById(@PathVariable Long id){
        return Result.success(departmentService.getById(id));
    }

    /**
     * 添加新的科室
     * @param department 科室对象
     * @return 添加结果
     */
    @Operation(summary = "新增科室（add）")
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/add")
    public Result add(@RequestBody Department department){
        departmentService.save(department);
        return Result.success("添加成功");
    }

    /**
     * 更新科室信息
     * @param department 科室对象
     * @return 更新结果
     */
    @Operation(summary = "更新科室信息（update）")
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/update")
    public Result update(@RequestBody Department department) {
        departmentService.updateById(department);
        return Result.success("更新成功");
    }

    /**
     * 删除科室
     * @param id 科室Id
     * @return 删除结果
     */
    @Operation(summary = "删除科室（delete）")
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/delete/{id}")
    public Result delete(@PathVariable Long id) {
        departmentService.removeById(id);
        return Result.success("删除成功");
    }
}
