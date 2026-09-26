package vn.utetra.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.utetra.entity.Topping;
import java.util.List;

public interface ToppingRepository extends JpaRepository<Topping, Long> {
    List<Topping> findByActiveTrue();
}