package vn.utetra.controller.admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import vn.utetra.repository.UserRepository;
import vn.utetra.service.BranchService;

@Controller
@RequestMapping("/admin/users")
public class AdminUserController {

    @Autowired private UserRepository userRepo;
    @Autowired private BranchService branchService;

    @GetMapping
    public String list(Model model) {
        model.addAttribute("users", userRepo.findAll());
        return "admin/users";
    }
}