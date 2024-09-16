package grexrr.echoai.service;

import java.util.List;

import org.hibernate.exception.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import grexrr.echoai.model.User;
import grexrr.echoai.repository.UserRepository;

@Service
public class UserService {
    
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public String registerUser(String username, String password){

        logger.info("Registering user with username: {}", username);
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));

        try{
            userRepository.save(user);

            //Logging
            logger.debug("Attempting to register user...");
            logger.info("User registration successful");
        } catch (DataIntegrityViolationException e){
            logger.error("Data integrity violation while registering user", e);
            return "User already exists";
        } catch (ConstraintViolationException e){
            logger.error("Constraint violation while registering user (Possible duplicate username)", e);
            return "Invalid username or password";
        } catch (Exception e){
            logger.error("Unexpected error to register user", e);
            return "An unexpected error occurred, please try again later";
        }

        return "Successfully registered"; 
    }

    //for testing purposes
    public List<User> getAllUsers(){
        return userRepository.findAll();
    }

    public String removeUser(String username, Long id){
        if (id != null){
            try{
                removeUser(id);
            } catch (Exception e){
                logger.error("Unexpected error to remove user", e);
                return "An unexpected error occurred, please try again later";
            }
            return "User" + id + "successfully removed";
        }
        
        if (username != null && !username.isEmpty()){
            User user = userRepository.findByUsername(username);
            if (user != null){
                removeUser(user.getId());
                
                logger.info("User" + user.getUsername() + "(" + user.getId() + ")" + "successfully removed");
                return "User" + user.getUsername() + "(" + user.getId() + ")" + "successfully removed";
            }
            logger.error("User not found");
            return "User not found";
        }

        logger.error("No information provided");
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
