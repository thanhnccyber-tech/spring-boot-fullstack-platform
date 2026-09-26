package vn.utetra.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import vn.utetra.entity.Branch;
import vn.utetra.repository.BranchRepository;
import java.util.List;

@Service
public class BranchService {
    @Autowired private BranchRepository repo;
    public List<Branch> active() { return repo.findByActiveTrue(); }
    public List<Branch> all() { return repo.findAll(); }
    public Branch save(Branch b) { return repo.save(b); }
    public Branch get(Long id) { return repo.findById(id).orElseThrow(); }
    public void delete(Long id) { repo.deleteById(id); }
}