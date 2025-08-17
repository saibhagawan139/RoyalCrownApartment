package com.royalcrown.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.royalcrown.model.Flat;

@Repository
public interface FlatRepository extends JpaRepository<Flat, String> {
    // Standard CRUD available from JpaRepository
}
