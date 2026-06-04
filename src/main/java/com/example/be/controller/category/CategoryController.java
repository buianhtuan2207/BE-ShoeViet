package com.example.be.controller.category;

import com.example.be.dto.req.category.CategoryRequest;
import com.example.be.entity.category.Category;
import com.example.be.service.category.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@CrossOrigin(origins = "http://localhost:5173")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    // 1. Thêm danh mục
    @PostMapping
    public ResponseEntity<?> addCategory(@RequestBody CategoryRequest request) {
        try {
            return ResponseEntity.ok(categoryService.addCategory(request));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi khi thêm danh mục: " + e.getMessage());
        }
    }

    // 2. Lấy tất cả danh mục
    @GetMapping
    public ResponseEntity<List<Category>> getAllCategories() {
        return ResponseEntity.ok(categoryService.getAllCategories());
    }

    // 3. Lấy chi tiết danh mục theo ID (MỚI)
    @GetMapping("/{id}")
    public ResponseEntity<?> getCategoryById(@PathVariable Integer id) {
        try {
            Category category = categoryService.getCategoryById(id);
            return ResponseEntity.ok(category);
        } catch (Exception e) {
            return ResponseEntity.status(404).body(e.getMessage());
        }
    }

    // 4. Cập nhật danh mục (MỚI)
    @PutMapping("/{id}")
    public ResponseEntity<?> updateCategory(@PathVariable Integer id, @RequestBody CategoryRequest request) {
        try {
            Category updatedCategory = categoryService.updateCategory(id, request);
            return ResponseEntity.ok(updatedCategory);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi khi cập nhật danh mục: " + e.getMessage());
        }
    }

    // 5. Xóa danh mục (MỚI)
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCategory(@PathVariable Integer id) {
        try {
            categoryService.deleteCategory(id);
            return ResponseEntity.ok("Xóa danh mục thành công!");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi khi xóa danh mục: " + e.getMessage());
        }
    }
}