package com.example.be.controller.product;

import com.example.be.dto.req.product.ProductRequest;
import com.example.be.dto.res.product.ProductResponse;
import com.example.be.entity.product.Product;
import com.example.be.service.product.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@CrossOrigin(origins = "http://localhost:5173")
public class ProductController {

    @Autowired
    private ProductService productService;

    @GetMapping
    public ResponseEntity<List<ProductResponse>> getAllProducts() {
        return ResponseEntity.ok(productService.getAllProducts());
    }

    @PostMapping
    public ResponseEntity<?> addProduct(@RequestBody ProductRequest request) {
        try {
            Product newProduct = productService.addProduct(request);
            return ResponseEntity.ok(newProduct);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi khi thêm sản phẩm: " + e.getMessage());
        }
    }
}
