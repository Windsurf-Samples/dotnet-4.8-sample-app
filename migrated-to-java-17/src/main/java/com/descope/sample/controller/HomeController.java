package com.descope.sample.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * MVC Controller for serving HTML views.
 * This replaces the HomeController.cs from the .NET application.
 * 
 * Migrated from: DescopeSampleApp/Controllers/HomeController.cs
 * 
 * .NET MVC Controller pattern:
 * - Inherits from Controller
 * - Returns ActionResult
 * - Uses ViewBag for passing data
 * 
 * Spring MVC Controller pattern:
 * - Annotated with @Controller
 * - Returns String (view name)
 * - Uses Model for passing data
 */
@Controller
public class HomeController {

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("title", "Home Page");
        return "home";
    }

    @GetMapping("/login")
    public String login(Model model) {
        return "login";
    }

    @GetMapping("/authenticated")
    public String authenticated(Model model) {
        return "authenticated";
    }
}
