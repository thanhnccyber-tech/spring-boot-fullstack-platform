package vn.utetra.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.utetra.entity.PersistentCartItem;

import java.util.List;

public interface PersistentCartItemRepository extends JpaRepository<PersistentCartItem, Long> {

    @Query("SELECT DISTINCT c FROM PersistentCartItem c JOIN FETCH c.product " +
           "WHERE c.user.id = :userId ORDER BY c.id")
    List<PersistentCartItem> findByUserIdWithProduct(@Param("userId") Long userId);

    @Modifying
    @Query("DELETE FROM PersistentCartItem c WHERE c.user.id = :userId")
    void deleteByUserId(@Param("userId") Long userId);
}
