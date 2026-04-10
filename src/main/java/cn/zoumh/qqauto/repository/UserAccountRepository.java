package cn.zoumh.qqauto.repository;

import cn.zoumh.qqauto.domain.UserAccount;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserAccountRepository extends JpaRepository<UserAccount, Long>
{
    Optional<UserAccount> findByUsername(String username);
}
