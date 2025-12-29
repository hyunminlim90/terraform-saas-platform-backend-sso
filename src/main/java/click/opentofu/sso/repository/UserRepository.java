package click.opentofu.sso.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import click.opentofu.sso.entity.UserEntity;

public interface UserRepository extends JpaRepository<UserEntity, String> {
    Optional<UserEntity> findByEmailId(String email);
    Optional<UserEntity> findByPassword(String password);

    @Query("SELECT MAX(u.userIndex) FROM user u")
    Optional<Integer> findMaxUserIndex();
}
