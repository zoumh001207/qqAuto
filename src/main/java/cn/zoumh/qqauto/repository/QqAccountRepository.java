package cn.zoumh.qqauto.repository;

import cn.zoumh.qqauto.domain.QqAccount;
import cn.zoumh.qqauto.domain.UserAccount;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QqAccountRepository extends JpaRepository<QqAccount, Long>
{
    List<QqAccount> findByUserOrderByCreatedAtDesc(UserAccount user);
}
