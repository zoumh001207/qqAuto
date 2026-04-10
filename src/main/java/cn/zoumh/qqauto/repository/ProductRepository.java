package cn.zoumh.qqauto.repository;

import cn.zoumh.qqauto.domain.Product;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long>
{
    List<Product> findAllByActiveTrueOrderByFeaturedDescCreatedAtDesc();
}
