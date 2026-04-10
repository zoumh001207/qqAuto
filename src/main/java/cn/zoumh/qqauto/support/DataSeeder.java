package cn.zoumh.qqauto.support;

import cn.zoumh.qqauto.domain.Announcement;
import cn.zoumh.qqauto.domain.Product;
import cn.zoumh.qqauto.domain.RoleType;
import cn.zoumh.qqauto.domain.UserAccount;
import cn.zoumh.qqauto.repository.AnnouncementRepository;
import cn.zoumh.qqauto.repository.ProductRepository;
import cn.zoumh.qqauto.repository.UserAccountRepository;
import java.math.BigDecimal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner
{
    private final UserAccountRepository userAccountRepository;
    private final AnnouncementRepository announcementRepository;
    private final ProductRepository productRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.seed.admin-username}")
    private String adminUsername;

    @Value("${app.seed.admin-password}")
    private String adminPassword;

    @Value("${app.seed.admin-qq}")
    private String adminQq;

    @Override
    public void run(String... args)
    {
        seedAdmin();
        seedAnnouncements();
        seedProducts();
    }

    private void seedAdmin()
    {
        if (userAccountRepository.findByUsername(adminUsername).isPresent())
        {
            return;
        }
        UserAccount admin = new UserAccount();
        admin.setUsername(adminUsername);
        admin.setDisplayName("系统管理员");
        admin.setPassword(passwordEncoder.encode(adminPassword));
        admin.setQqNumber(adminQq);
        admin.setRole(RoleType.ADMIN);
        admin.setAgentLevel("一折代理");
        admin.setBalance(new BigDecimal("9999.00"));
        admin.setCommission(new BigDecimal("9999.00"));
        userAccountRepository.save(admin);
    }

    private void seedAnnouncements()
    {
        if (announcementRepository.count() > 0)
        {
            return;
        }
        Announcement a1 = new Announcement();
        a1.setTitle("站点说明");
        a1.setContent("当前版本提供安全可控的站点管理、商品购买、消息通知、充值申请、提现申请和代理体系，不提供第三方 QQ 账号接管、Cookie 导入或自动执行任务。");
        a1.setPinned(true);
        Announcement a2 = new Announcement();
        a2.setTitle("上线范围");
        a2.setContent("首版按稳定运行优先，先交付用户端和后台管理端，支付网关与第三方自动化接入留待后续单独配置。");
        announcementRepository.saveAll(List.of(a1, a2));
    }

    private void seedProducts()
    {
        if (productRepository.count() > 0)
        {
            return;
        }
        productRepository.save(createProduct("『一月』代挂服务", "托管服务", "dg", "安全版业务展示套餐，不接第三方 QQ 自动化", "12.00", 30, true));
        productRepository.save(createProduct("『一年』空间互动服务", "互动服务", "qzone", "空间互动业务展示套餐", "88.00", 365, true));
        productRepository.save(createProduct("七折代理", "代理升级", "agent", "升级代理等级，享受更低折扣与更高佣金", "199.00", 3650, false));
    }

    private Product createProduct(String name, String category, String versionTag, String summary, String price, int days, boolean featured)
    {
        Product product = new Product();
        product.setName(name);
        product.setCategory(category);
        product.setVersionTag(versionTag);
        product.setSummary(summary);
        product.setPrice(new BigDecimal(price));
        product.setDurationDays(days);
        product.setFeatured(featured);
        return product;
    }
}
