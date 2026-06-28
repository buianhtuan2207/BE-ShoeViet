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

        brand.setLogo(request.getLogo());

        if (request.getIsAction() != null) {
            brand.setIsAction(request.getIsAction());
        }

        return brandRepository.save(brand);
    }

    // 2. Lấy tất cả thương hiệu
    @Transactional(readOnly = true)
    public List<Brand> getAllBrands() {
        return brandRepository.findAll();
    }

    // 4. Cập nhật thương hiệu
    @Transactional
    public Brand updateBrand(Integer id, BrandRequest request) {
        Brand brand = brandRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thương hiệu với ID: " + id));

        if (request.getName() != null) {
            brand.setName(request.getName());
        }
        if (request.getDescription() != null) {
            brand.setDescription(request.getDescription());
        }

        if (request.getLogo() != null) {
            brand.setLogo(request.getLogo());
        }

        if (request.getIsAction() != null) {
            brand.setIsAction(request.getIsAction());
        }

        return brandRepository.save(brand);
    }

    // 5. Xóa thương hiệu
    @Transactional
    public void deleteBrand(Integer id) {
        Brand brand = brandRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thương hiệu với ID: " + id));
        brandRepository.delete(brand);
    }
}