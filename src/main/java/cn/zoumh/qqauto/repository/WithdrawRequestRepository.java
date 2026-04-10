package cn.zoumh.qqauto.repository;

import cn.zoumh.qqauto.domain.UserAccount;
import cn.zoumh.qqauto.domain.WithdrawRequest;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WithdrawRequestRepository extends JpaRepository<WithdrawRequest, Long>
{
    List<WithdrawRequest> findTop20ByUserOrderByCreatedAtDesc(UserAccount user);
}
