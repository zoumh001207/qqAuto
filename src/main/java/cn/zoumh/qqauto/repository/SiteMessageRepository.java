package cn.zoumh.qqauto.repository;

import cn.zoumh.qqauto.domain.SiteMessage;
import cn.zoumh.qqauto.domain.UserAccount;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SiteMessageRepository extends JpaRepository<SiteMessage, Long>
{
    List<SiteMessage> findTop10ByUserOrderByCreatedAtDesc(UserAccount user);

    long countByUserAndReadFlagFalse(UserAccount user);
}
