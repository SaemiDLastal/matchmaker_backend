package com.example.MatchMaker_BE.userauth;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepo extends JpaRepository<User, Integer> {

    List<User> findAll();
    Optional<User> findByEmail(String email);

    User findUserById(Integer id);

    User findUserByEmail(String email);
}

