package grexrr.echoai.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import grexrr.echoai.model.User;
import grexrr.echoai.service.UserService;

@RestController
public class UserController {
    @Autowired
    private UserService userService;

    @GetMapping("public/registerUser")
    public String registerUser(@RequestParam String username, @RequestParam String password){
        return userService.registerUser(username, password);
    }

    //for testing purposes
    @GetMapping("public/getAllUsers")
    public List<User> getAllUsers(){
        return userService.getAllUsers();
    }

    @GetMapping("public/removeUser")
    public String removeUser(@RequestParam(required = false) String username, 
                                @RequestParam(required = false) Long id){
        return userService.removeUser(username, id);
    }
}
