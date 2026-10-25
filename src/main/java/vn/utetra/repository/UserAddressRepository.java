package vn.utetra.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.utetra.entity.UserAddress;

import java.util.List;
import java.util.Optional;

public interface UserAddressRepository extends JpaRepository<UserAddress, Long> {
    List<UserAddress> findByUserIdOrderByDefaultAddressDescIdAsc(Long userId);
    Optional<UserAddress> findByIdAndUserId(Long id, Long userId);
    long countByUserId(Long userId);
}
