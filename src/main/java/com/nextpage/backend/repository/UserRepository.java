package com.nextpage.backend.repository;

import com.nextpage.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByEmail(String email);
    boolean existsByNickname(String nickname);
    Optional<User> findByEmail(String email);

    @Query("SELECT u.nickname FROM User u WHERE u.id = :userId")
    Optional<String> findNicknameById(Long userId);

}
