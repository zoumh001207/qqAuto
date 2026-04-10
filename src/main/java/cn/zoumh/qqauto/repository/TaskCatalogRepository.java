package cn.zoumh.qqauto.repository;

import cn.zoumh.qqauto.domain.TaskCatalog;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskCatalogRepository extends JpaRepository<TaskCatalog, Long>
{
    Optional<TaskCatalog> findByTaskCode(String taskCode);

    List<TaskCatalog> findAllByOrderBySortOrderAsc();
}
