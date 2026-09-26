package vn.utetra.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.utetra.entity.Category;

public interface CategoryRepository extends JpaRepository<Category, Long> {
}