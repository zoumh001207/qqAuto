package cn.zoumh.qqauto.repository;

import cn.zoumh.qqauto.domain.QqAccount;
import cn.zoumh.qqauto.domain.QqTaskPreference;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QqTaskPreferenceRepository extends JpaRepository<QqTaskPreference, Long>
{
    List<QqTaskPreference> findByQqAccountOrderByTaskCatalogSortOrderAsc(QqAccount qqAccount);
}
