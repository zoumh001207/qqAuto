package cn.zoumh.qqauto.support;

import cn.zoumh.qqauto.domain.Announcement;
import cn.zoumh.qqauto.domain.Product;
import cn.zoumh.qqauto.domain.QqAccount;
import cn.zoumh.qqauto.domain.QqAccountStatus;
import cn.zoumh.qqauto.domain.QqTaskPreference;
import cn.zoumh.qqauto.domain.RoleType;
import cn.zoumh.qqauto.domain.TaskCatalog;
import cn.zoumh.qqauto.domain.TaskExecutionStatus;
import cn.zoumh.qqauto.domain.UserAccount;
import cn.zoumh.qqauto.repository.AnnouncementRepository;
import cn.zoumh.qqauto.repository.ProductRepository;
import cn.zoumh.qqauto.repository.QqAccountRepository;
import cn.zoumh.qqauto.repository.QqTaskPreferenceRepository;
import cn.zoumh.qqauto.repository.TaskCatalogRepository;
import cn.zoumh.qqauto.repository.UserAccountRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
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
    private final TaskCatalogRepository taskCatalogRepository;
    private final QqAccountRepository qqAccountRepository;
    private final QqTaskPreferenceRepository qqTaskPreferenceRepository;
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
        UserAccount admin = seedAdmin();
        seedAnnouncements();
        seedProducts();
        seedTaskCatalog();
        seedAdminQqAccounts(admin);
    }

    private UserAccount seedAdmin()
    {
        return userAccountRepository.findByUsername(adminUsername).orElseGet(() -> {
            UserAccount admin = new UserAccount();
            admin.setUsername(adminUsername);
            admin.setDisplayName("系统管理员");
            admin.setPassword(passwordEncoder.encode(adminPassword));
            admin.setQqNumber(adminQq);
            admin.setRole(RoleType.ADMIN);
            admin.setAgentLevel("核心账户");
            admin.setBalance(new BigDecimal("9999.00"));
            admin.setCommission(new BigDecimal("9999.00"));
            return userAccountRepository.save(admin);
        });
    }

    private void seedAnnouncements()
    {
        if (announcementRepository.count() > 0)
        {
            return;
        }
        Announcement a1 = new Announcement();
        a1.setTitle("系统公告");
        a1.setContent("当前版本按你给出的原型重构，重点交付 QQ 账号管理、任务列表、套餐购买和后台运行监控。");
        a1.setPublished(true);
        a1.setPinned(true);

        Announcement a2 = new Announcement();
        a2.setTitle("运行说明");
        a2.setContent("本版本为自用版，不接短信验证和支付网关，订单与开通流程按后台人工确认处理。");
        a2.setPublished(true);

        announcementRepository.saveAll(List.of(a1, a2));
    }

    private void seedProducts()
    {
        if (productRepository.count() > 0)
        {
            return;
        }
        productRepository.save(createProduct("入门版", "代挂套餐", "starter", "1 个 QQ 号，自动完成基础任务", "6.00", 30, false));
        productRepository.save(createProduct("标准版", "代挂套餐", "standard", "3 个 QQ 号，适合日常自用", "12.00", 30, true));
        productRepository.save(createProduct("豪华版", "代挂套餐", "premium", "10 个 QQ 号，多账号统一管理", "28.00", 30, false));
    }

    private void seedTaskCatalog()
    {
        if (taskCatalogRepository.count() > 0)
        {
            return;
        }
        taskCatalogRepository.save(createTask("coupon", "领福利券", "🎟", "简单", "HTTP接口", 1));
        taskCatalogRepository.save(createTask("friend", "添加好友", "👥", "困难", "HTTP接口", 2));
        taskCatalogRepository.save(createTask("like", "点赞说说", "👍", "简单", "HTTP接口", 3));
        taskCatalogRepository.save(createTask("post", "发布说说", "📝", "中等", "HTTP接口", 4));
        taskCatalogRepository.save(createTask("checkin", "签到打卡", "✅", "简单", "HTTP接口", 5));
        taskCatalogRepository.save(createTask("music", "音乐听歌", "🎵", "中等", "HTTP接口", 6));
        taskCatalogRepository.save(createTask("coin", "金币兑换", "🪙", "简单", "HTTP接口", 7));
        taskCatalogRepository.save(createTask("feed", "浏览动态", "👀", "简单", "HTTP接口", 8));
        taskCatalogRepository.save(createTask("gold", "福利金豆", "💰", "中等", "HTTP接口", 9));
        taskCatalogRepository.save(createTask("novel", "阅读小说", "📚", "中等", "HTTP接口", 10));
        taskCatalogRepository.save(createTask("listen", "波点音乐", "🎶", "中等", "HTTP接口", 11));
        taskCatalogRepository.save(createTask("game", "体验游戏", "🎮", "中等", "HTTP接口", 12));
    }

    private void seedAdminQqAccounts(UserAccount admin)
    {
        if (!qqAccountRepository.findByUserOrderByCreatedAtDesc(admin).isEmpty())
        {
            return;
        }

        Product standard = productRepository.findAll().stream().filter(Product::isFeatured).findFirst().orElseThrow();
        Product starter = productRepository.findAll().stream().findFirst().orElseThrow();

        QqAccount first = createAccount(admin, "2455230059", "2455230059", "默认主号", standard.getName(), 29, 3, QqAccountStatus.RUNNING);
        QqAccount second = createAccount(admin, adminQq, adminQq, "管理员备用号", starter.getName(), 25, 8, QqAccountStatus.RUNNING);

        seedTaskPreferences(first, false);
        seedTaskPreferences(second, true);
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

    private TaskCatalog createTask(String code, String name, String icon, String difficultyTag, String category, int sortOrder)
    {
        TaskCatalog task = new TaskCatalog();
        task.setTaskCode(code);
        task.setName(name);
        task.setIcon(icon);
        task.setDifficultyTag(difficultyTag);
        task.setCategory(category);
        task.setSortOrder(sortOrder);
        return task;
    }

    private QqAccount createAccount(UserAccount admin,
                                    String qqNumber,
                                    String rawPassword,
                                    String nickname,
                                    String packageName,
                                    int expireInDays,
                                    int lastRunMinutes,
                                    QqAccountStatus status)
    {
        QqAccount account = new QqAccount();
        account.setUser(admin);
        account.setQqNumber(qqNumber);
        account.setQqPassword(passwordEncoder.encode(rawPassword));
        account.setNickname(nickname);
        account.setPackageName(packageName);
        account.setStatus(status);
        account.setExpireAt(LocalDateTime.now().plusDays(expireInDays));
        account.setLastRunAt(LocalDateTime.now().minusMinutes(lastRunMinutes));
        return qqAccountRepository.save(account);
    }

    private void seedTaskPreferences(QqAccount account, boolean includePending)
    {
        List<TaskCatalog> tasks = taskCatalogRepository.findAllByOrderBySortOrderAsc();
        for (TaskCatalog task : tasks)
        {
            QqTaskPreference preference = new QqTaskPreference();
            preference.setQqAccount(account);
            preference.setTaskCatalog(task);
            preference.setEnabled(true);
            if (includePending && task.getSortOrder() % 4 == 0)
            {
                preference.setLastStatus(TaskExecutionStatus.PENDING);
            }
            else
            {
                preference.setLastStatus(TaskExecutionStatus.DONE);
            }
            qqTaskPreferenceRepository.save(preference);
        }
    }
}
