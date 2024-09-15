package grexrr.echoai.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import grexrr.echoai.model.User;
import grexrr.echoai.repository.UserRepository;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public String registerUser(String username, String password){
        //Unique ID already ensured by JPA
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));

        userRepository.save(user);
        return "User successfully registered";
    }

    //for testing purposes
    public List<User> getAllUsers(){
        return userRepository.findAll();
    }

    public String removeUser(String username, Long id){
        if (id != null){
            removeUser(id);
            return "User" + id + "successfully removed";
        }
        
        if (username != null && !username.isEmpty()){
            User user = userRepository.findByUsername(username);
            if (user != null){
                removeUser(user.getId());
                return "User" + user.getUsername() + "(" + user.getId() + ")" + "successfully removed";
            }
            return "User not found";
        }

        return "No information provided";
    }
    
    public String removeUser(Long id){
        if (!userRepository.findById(id).isPresent()){
            return "User not found";
        }
        userRepository.deleteById(id);
        return "User successfully removed";
    }
}
