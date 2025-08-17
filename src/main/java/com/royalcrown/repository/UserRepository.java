package com.royalcrown.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.royalcrown.model.User;

@Repository
public interface UserRepository extends JpaRepository<User, String> {
    Optional<User> findByUsername(String username);
    List<User> findByFlatNo(String flatNo);
    boolean existsByUsername(String username);
}
