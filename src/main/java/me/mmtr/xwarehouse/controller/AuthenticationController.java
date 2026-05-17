package me.mmtr.xwarehouse.controller;

import me.mmtr.xwarehouse.model.dto.UserDTO;
import me.mmtr.xwarehouse.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthenticationController {
    private final UserService userService;

    public AuthenticationController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/login")
    public String loginGet() {
        return "login";
    }

    @GetMapping("/register")
    public String registerGet() {
        return "register";
    }

    @PostMapping("/register")
    public String registerPost(
            @ModelAttribute UserDTO userDTO,
            RedirectAttributes redirectAttributes
    ) {
        if (!userDTO.getPassword().equals(userDTO.getConfirmPassword())) {
            redirectAttributes.addFlashAttribute(
                    "error", "Passwords do not match."
            );
            return "redirect:/register";
        }

        boolean successfullyRegistered = userService.createUser(
                userDTO.getUsername(), userDTO.getPassword()
        );

        if (!successfullyRegistered) {
            redirectAttributes.addFlashAttribute(
                    "error", "Username is already taken."
            );
            return "redirect:/register";
        }

        return "redirect:/login";
    }
}
