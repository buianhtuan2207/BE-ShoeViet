package com.example.be.controller.brand;

import com.example.be.dto.req.brand.BrandRequest;
import com.example.be.service.brand.BrandService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/brands")
@CrossOrigin(origins = "http://localhost:5173")
public class BrandController {

    @Autowired
    private BrandService brandService;

    @PostMapping
    public ResponseEntity<?> addBrand(@RequestBody BrandRequest request) {
        return ResponseEntity.ok(brandService.addBrand(request));
    }

    @GetMapping
    public ResponseEntity<?> getAllBrands() {
        return ResponseEntity.ok(brandService.getAllBrands());
    }
}
