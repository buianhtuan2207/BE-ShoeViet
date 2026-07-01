package com.example.be.repository.category;

import com.example.be.dto.res.category.CategoryResponse;
import com.example.be.entity.category.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Pageable;

import java.util.List;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Integer> {
    @Query("SELECT new com.example.be.dto.res.category.CategoryResponse(" +
            "c.id, c.name, c.description, c.imageUrl, COUNT(p.id), c.isActive) " +
            "FROM Category c LEFT JOIN Product p ON c.id = p.category.id " +
            "GROUP BY c.id, c.name, c.description, c.imageUrl, c.isActive")
    List<CategoryResponse> findAllCategoriesWithProductCount();

    @Query("SELECT new com.example.be.dto.res.category.CategoryResponse(" +
            "c.id, c.name, c.description, c.imageUrl, COUNT(p.id), c.isActive) " +
            "FROM Category c LEFT JOIN Product p ON c.id = p.category.id " +
            "WHERE c.isActive = true " +
            "GROUP BY c.id, c.name, c.description, c.imageUrl, c.isActive " +
            "ORDER BY COUNT(p.id) DESC")
    List<CategoryResponse> findTopActiveCategories(Pageable pageable);
}
