package vn.utetra.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.utetra.entity.Favorite;

import java.util.List;
import java.util.Optional;

public interface FavoriteRepository extends JpaRepository<Favorite, Long> {

    @Query("SELECT f FROM Favorite f JOIN FETCH f.product WHERE f.userId = :userId ORDER BY f.createdAt DESC")
    List<Favorite> findByUserIdWithProduct(@Param("userId") Long userId);

    Optional<Favorite> findByUserIdAndProductId(Long userId, Long productId);

    boolean existsByUserIdAndProductId(Long userId, Long productId);

    @Query("SELECT f.product.id FROM Favorite f WHERE f.userId = :userId")
    List<Long> findProductIdsByUserId(@Param("userId") Long userId);
}
