package cn.zoumh.qqauto.repository;

import cn.zoumh.qqauto.domain.RechargeRequest;
import cn.zoumh.qqauto.domain.UserAccount;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RechargeRequestRepository extends JpaRepository<RechargeRequest, Long>
{
    List<RechargeRequest> findTop20ByUserOrderByCreatedAtDesc(UserAccount user);
}
