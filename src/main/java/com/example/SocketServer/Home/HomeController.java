package com.example.SocketServer.Home;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {
    @GetMapping("/")
    public String home(Model model) {
        // Add any necessary data to the model that you want to display in the HTML template
        return "home";
    }
}
