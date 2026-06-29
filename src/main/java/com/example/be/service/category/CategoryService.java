package com.example.be.service.category;

import com.example.be.dto.req.category.CategoryRequest;
import com.example.be.dto.res.category.CategoryResponse;
import com.example.be.entity.category.Category;
import com.example.be.repository.category.CategoryRepository;
import com.example.be.repository.product.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

    // 1. Thêm danh mục mới (Đã có)
    @Transactional
    public Category addCategory(CategoryRequest request) {
        Category category = new Category();
        category.setName(request.getName());
        category.setDescription(request.getDescription());
        category.setImageUrl(request.getImageUrl());
        category.setIsActive(request.getIsActive() != null ? request.getIsActive() : true);
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
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy danh mục với ID: " + id));

        if (request.getName() != null) {
            category.setName(request.getName());
        }
        if (request.getDescription() != null) {
            category.setDescription(request.getDescription());
        }
        if (request.getImageUrl() != null) {
            category.setImageUrl(request.getImageUrl());
        }

        if (request.getIsActive() != null) {
            category.setIsActive(request.getIsActive());
        }
        return categoryRepository.save(category);
    }

    // 5. Xóa danh mục (MỚI)
    @Transactional
    public void deleteCategory(Integer id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy danh mục!"));

        // Kiểm tra xem danh mục có đang chứa sản phẩm nào không
        long count = productRepository.countByCategoryId(id);
        if (count > 0) {
            throw new RuntimeException("Không thể xóa danh mục vì đang chứa " + count + " sản phẩm!");
        }

        categoryRepository.delete(category);
    }

    @Transactional(readOnly = true)
    public List<CategoryResponse> getAllCategoriesWithCount() {
        return categoryRepository.findAllCategoriesWithProductCount();
    }

    @Transactional(readOnly = true)
    public List<CategoryResponse> getHomeCategories() {
        return categoryRepository.findTopActiveCategories(PageRequest.of(0, 3));
    }
}