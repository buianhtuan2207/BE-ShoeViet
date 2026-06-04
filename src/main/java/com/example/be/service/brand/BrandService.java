package com.example.be.service.brand;

import com.example.be.dto.req.brand.BrandRequest;
import com.example.be.entity.brand.Brand;
import com.example.be.repository.brand.BrandRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class BrandService {

    @Autowired
    private BrandRepository brandRepository;

    // 1. Thêm thương hiệu mới
    @Transactional
    public Brand addBrand(BrandRequest request) {
        Brand brand = new Brand();
        brand.setName(request.getName());
        brand.setDescription(request.getDescription());
        return brandRepository.save(brand);
    }

    // 2. Lấy tất cả thương hiệu
    @Transactional(readOnly = true)
    public List<Brand> getAllBrands() {
        return brandRepository.findAll();
    }

    // 4. Cập nhật thương hiệu (MỚI THÊM)
    @Transactional
    public Brand updateBrand(Integer id, BrandRequest request) {
        // Kiểm tra xem thương hiệu cũ có tồn tại hay không
        Brand brand = brandRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thương hiệu với ID: " + id));

        // Kiểm tra tránh đè dữ liệu null nếu frontend truyền thiếu trường
        if (request.getName() != null) {
            brand.setName(request.getName());
        }
        if (request.getDescription() != null) {
            brand.setDescription(request.getDescription());
        }

        return brandRepository.save(brand);
    }

    // 5. Xóa thương hiệu (MỚI THÊM)
    @Transactional
    public void deleteBrand(Integer id) {
        // Kiểm tra sự tồn tại trước khi xóa để tránh crash DB
        Brand brand = brandRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thương hiệu với ID: " + id));
        brandRepository.delete(brand);
    }
}