package com.example.SocketServer.Controller;

import com.example.SocketServer.Service.UserService;
import com.example.SocketServer.TCPServer;
import com.example.SocketServer.User.User;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Controller
@RequestMapping("/users")
public class UserController {

    private final TCPServer tcpServer;
    // Constructor-based injection
    public UserController(TCPServer tcpServer) {
        this.tcpServer = tcpServer;
    }

    @Autowired
    private WebClient.Builder webClientBuilder;

    @Autowired
    private UserService userService;


    @PostMapping("/register")
    public String createUser(@ModelAttribute User user, Model model, HttpServletResponse response) {

        if(tcpServer.getID() != tcpServer.getCurrentLeaderID()){
            String secondaryServerUrl = "";
            if(tcpServer.getServerConnection(tcpServer.getCurrentLeaderID()).getHost().equals("localhost")){
                secondaryServerUrl = "http://"+tcpServer.getServerConnection(tcpServer.getCurrentLeaderID()).getHost()+":8"+(tcpServer.getCurrentLeaderID() - 1)+"/users/create"; // Update with the actual URL
            }else{
                secondaryServerUrl = "http://"+tcpServer.getServerConnection(tcpServer.getCurrentLeaderID()).getHost()+":80/users/create"; // Update with the actual URL
            }

            webClientBuilder.build()
                    .post()
                    .uri(secondaryServerUrl)
                    .body(Mono.just(user), User.class)
                    .retrieve()
                    .bodyToMono(User.class)
                    .subscribe();
        }else{
            userService.saveUser(user);
        }


        // Create cookies for the name and password
        Cookie nameCookie = new Cookie("name", user.getName());
        Cookie passwordCookie = new Cookie("password", user.getPassword());

        // Set cookie properties if necessary
        nameCookie.setPath("/");
        passwordCookie.setPath("/");
        nameCookie.setHttpOnly(false);
        passwordCookie.setHttpOnly(false);

        // Add the cookies to the response
        response.addCookie(nameCookie);
        response.addCookie(passwordCookie);

        return "redirect:/users/login";
    }

    @PostMapping("/create")
    public User createUser(@RequestBody User user) {
        return userService.saveUser(user);
    }

    @PostMapping("/login")
    public String loginUser(@ModelAttribute User user, Model model, HttpServletResponse response) {
        User reuser = userService.findByNameAndPassword(user.getName(), user.getPassword());
        if (reuser != null) {

            // Create cookies for the name and password
            Cookie nameCookie = new Cookie("name", user.getName());
            Cookie passwordCookie = new Cookie("password", user.getPassword());

            // Set cookie properties if necessary
            nameCookie.setPath("/");
            passwordCookie.setPath("/");
            nameCookie.setHttpOnly(false);
            passwordCookie.setHttpOnly(false);

            // Add the cookies to the response
            response.addCookie(nameCookie);
            response.addCookie(passwordCookie);

            return "redirect:/chat";
        }
        return "redirect:/users/login";
    }



    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("user", new User());
        return "register";
    }

    @GetMapping("/login")
    public String showLoginForm(Model model) {
        model.addAttribute("user", new User());
        return "login";
    }
}
