package grexrr.echoai.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import grexrr.echoai.model.User;

public interface UserRepository extends JpaRepository<User, Long>{
    
}
