package grexrr.echoai.service;

import java.util.List;

import grexrr.echoai.model.User;

public interface UserService {

    public String registerUser(String username, String password);

    public List<User> getAllUsers();

    public String removeUser(String username, Long id);
    
    public String removeUser(Long id);
    
}
