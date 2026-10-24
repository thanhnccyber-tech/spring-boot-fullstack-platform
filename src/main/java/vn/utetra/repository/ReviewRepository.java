package vn.utetra.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.utetra.entity.Review;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    @Query("SELECT r FROM Review r JOIN FETCH r.user " +
           "WHERE r.product.id = :productId ORDER BY r.createdAt DESC")
    List<Review> findByProductIdWithUser(@Param("productId") Long productId);

    @Query("SELECT r FROM Review r WHERE r.user.id = :userId AND r.product.id = :productId")
    Optional<Review> findByUserAndProduct(@Param("userId") Long userId,
                                          @Param("productId") Long productId);

    /** Trả về các dòng [productId, avg(rating), count] */
    @Query("SELECT r.product.id, AVG(r.rating), COUNT(r) FROM Review r " +
           "WHERE r.product.id IN :ids GROUP BY r.product.id")
    List<Object[]> summarize(@Param("ids") Collection<Long> ids);

    @Query("SELECT r FROM Review r JOIN FETCH r.product WHERE r.id = :id")
    Optional<Review> findByIdWithProduct(@Param("id") Long id);

    @Modifying
    @Query("DELETE FROM Review r WHERE r.product.id = :productId")
    void deleteByProductId(@Param("productId") Long productId);
}
