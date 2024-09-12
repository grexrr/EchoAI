package grexrr.echoai.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import grexrr.echoai.model.User;
import grexrr.echoai.repository.UserRepository;

@RestController
public class UserController {
    @Autowired
    private UserRepository userRepository;

    @GetMapping("/addUser")
    public String addUser(@RequestParam String username, @RequestParam String password){
        User user = new User();
        user.setUsername(username);
        user.setPassword(password);
        userRepository.save(user);
        return "User added successfully";
    }

    @GetMapping("/getAllUsers")
    public List<User> getAllUsers(){
        return userRepository.findAll();
    }

    @GetMapping("/removeUser")
    public String removeUser(@RequestParam String username){
        User user = userRepository.findByUsername(username);
        if (user != null) {
            userRepository.delete(user);
            return "User removed successfully";
        } else {
            return "User not found";
        }
    }
}
