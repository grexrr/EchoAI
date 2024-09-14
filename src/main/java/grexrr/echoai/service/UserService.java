package grexrr.echoai.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import grexrr.echoai.model.User;
import grexrr.echoai.repository.UserRepository;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

    public String registerUser(String username, String password){
        //Unique ID already ensured by JPA
        User user = new User();
        user.setUsername(username);
        user.setPassword(password);

        userRepository.save(user);
        return "User successfully registered";
    }

    public List<User> getAllUsers(){
        return userRepository.findAll();
    }

    public String removeUser(String username, Long id){
        if (id != null){
            removeUser(id);
            return "User" + id + "successfully removed";
        }
        
        if (username != null && !username.isEmpty()){
            List<User> users = userRepository.findAllByUsername(username);
            if (users.isEmpty()){
                return "User not found";
            }
            if (users.size() == 1){
                User user = users.get(0);
                return removeUser(user.getId());
            } else {
                return "Multiple entries found, please specify the ID";
            }
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
