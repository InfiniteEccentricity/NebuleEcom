package com.example.ecommerce.repository;

import com.example.ecommerce.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByCategory_Name(String category);
    List<Product> findByNameContainingIgnoreCaseOrCategory_NameContainingIgnoreCase(String name, String category);
    List<Product> findAllByOrderByIdDesc();
    List<Product> findAllByOrderByRatingDesc();

    @Transactional
    @Modifying
    @Query(value = "CALL ProcessBulkDiscount(:category, :discount)", nativeQuery = true)
    void applyBulkDiscount(@Param("category") String category, @Param("discount") Double discount);

    @Query(value = "CALL CalculateAdminStats()", nativeQuery = true)
    List<Object[]> getDatabaseStats();

    @Query(value = "SELECT * FROM audit_logs ORDER BY changed_at DESC LIMIT 50", nativeQuery = true)
    List<Map<String, Object>> getAuditLogs();
}
