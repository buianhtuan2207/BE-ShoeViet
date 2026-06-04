package com.example.be.controller.brand;

import com.example.be.dto.req.brand.BrandRequest;
import com.example.be.entity.brand.Brand;
import com.example.be.service.brand.BrandService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/brands")
@CrossOrigin(origins = "http://localhost:5173")
public class BrandController {

    @Autowired
    private BrandService brandService;

    // 1. Thêm thương hiệu mới
    @PostMapping
    public ResponseEntity<?> addBrand(@RequestBody BrandRequest request) {
        try {
            return ResponseEntity.ok(brandService.addBrand(request));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi khi thêm thương hiệu: " + e.getMessage());
        }
    }

    // 2. Lấy tất cả thương hiệu (Đã có sẵn)
    @GetMapping
    public ResponseEntity<List<Brand>> getAllBrands() {
        return ResponseEntity.ok(brandService.getAllBrands());
    }

    // 4. Cập nhật thông tin thương hiệu (MỚI THÊM)
    @PutMapping("/{id}")
    public ResponseEntity<?> updateBrand(@PathVariable Integer id, @RequestBody BrandRequest request) {
        try {
            Brand updatedBrand = brandService.updateBrand(id, request);
            return ResponseEntity.ok(updatedBrand);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi khi cập nhật: " + e.getMessage());
        }
    }

    // 5. Xóa thương hiệu theo ID (MỚI THÊM)
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteBrand(@PathVariable Integer id) {
        try {
            brandService.deleteBrand(id);
            return ResponseEntity.ok("Xóa thương hiệu thành công!");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi khi xóa: " + e.getMessage());
        }
    }
}