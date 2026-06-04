package com.example.be.service.category;

import com.example.be.dto.req.category.CategoryRequest;
import com.example.be.entity.category.Category;
import com.example.be.repository.category.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;

    // 1. Thêm danh mục mới (Đã có)
    @Transactional
    public Category addCategory(CategoryRequest request) {
        Category category = new Category();
        category.setName(request.getName());
        category.setDescription(request.getDescription());
        return categoryRepository.save(category);
    }

    // 2. Lấy tất cả danh mục (Đã có)
    @Transactional(readOnly = true)
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    // 3. Lấy danh mục theo ID (MỚI)
    @Transactional(readOnly = true)
    public Category getCategoryById(Integer id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy danh mục với ID: " + id));
    }

    // 4. Cập nhật danh mục (MỚI)
    @Transactional
    public Category updateCategory(Integer id, CategoryRequest request) {
        // Tìm danh mục cũ trong DB
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy danh mục với ID: " + id));

        // Kiểm tra tránh đè dữ liệu null nếu frontend/postman truyền thiếu trường
        if (request.getName() != null) {
            category.setName(request.getName());
        }
        if (request.getDescription() != null) {
            category.setDescription(request.getDescription());
        }

        return categoryRepository.save(category);
    }

    // 5. Xóa danh mục (MỚI)
    @Transactional
    public void deleteCategory(Integer id) {
        // Kiểm tra xem danh mục có tồn tại không trước khi xóa
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy danh mục với ID: " + id));

        /* LƯU Ý: Nếu có sản phẩm (Product) nào đang liên kết với category_id này,
          việc xóa sẽ bị lỗi ràng buộc khóa ngoại (Foreign Key Constraint Violation).
          Bạn cần xóa hoặc chuyển đổi category của các sản phẩm đó trước,
          hoặc thiết lập CascadeType.REMOVE / @OnDelete trong Entity.
         */
        categoryRepository.delete(category);
    }
}