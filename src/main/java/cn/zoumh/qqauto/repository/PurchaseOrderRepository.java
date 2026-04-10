package cn.zoumh.qqauto.repository;

import cn.zoumh.qqauto.domain.PurchaseOrder;
import cn.zoumh.qqauto.domain.UserAccount;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, Long>
{
    List<PurchaseOrder> findTop20ByUserOrderByCreatedAtDesc(UserAccount user);
}
