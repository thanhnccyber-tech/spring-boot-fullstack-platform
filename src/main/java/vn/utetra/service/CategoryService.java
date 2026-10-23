package vn.utetra.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.utetra.entity.Category;
import vn.utetra.repository.CategoryRepository;
import java.util.List;

@Service
public class CategoryService {
    @Autowired private CategoryRepository repo;

    public List<Category> all()   { return repo.findAll(); }
    public Category get(Long id)  { return repo.findById(id).orElseThrow(); }

    @Transactional
    public Category save(Category c) { return repo.save(c); }

    @Transactional
    public void delete(Long id) { repo.deleteById(id); }
}