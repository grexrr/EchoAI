package grexrr.echoai.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import grexrr.echoai.model.User;

public interface UserRepository extends JpaRepository<User, Long>{
    User findByUsername(String username);
    List<User> findAllByUsername(String username);
}
