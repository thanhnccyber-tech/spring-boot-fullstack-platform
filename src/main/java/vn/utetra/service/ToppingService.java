package vn.utetra.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.utetra.entity.Topping;
import vn.utetra.repository.ToppingRepository;
import java.util.List;

@Service
public class ToppingService {
    @Autowired private ToppingRepository repo;

    public List<Topping> active() { return repo.findByActiveTrue(); }
    public List<Topping> all()    { return repo.findAll(); }
    public Topping get(Long id)   { return repo.findById(id).orElseThrow(); }

    @Transactional
    public Topping save(Topping t) { return repo.save(t); }

    @Transactional
    public void delete(Long id) { repo.deleteById(id); }
}