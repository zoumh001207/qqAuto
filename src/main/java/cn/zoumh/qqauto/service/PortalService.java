package cn.zoumh.qqauto.service;

import cn.zoumh.qqauto.domain.Announcement;
import cn.zoumh.qqauto.domain.OrderStatus;
import cn.zoumh.qqauto.domain.Product;
import cn.zoumh.qqauto.domain.PurchaseOrder;
import cn.zoumh.qqauto.domain.QqAccount;
import cn.zoumh.qqauto.domain.QqAccountStatus;
import cn.zoumh.qqauto.domain.QqTaskPreference;
import cn.zoumh.qqauto.domain.RechargeRequest;
import cn.zoumh.qqauto.domain.RequestStatus;
import cn.zoumh.qqauto.domain.RoleType;
import cn.zoumh.qqauto.domain.SiteMessage;
import cn.zoumh.qqauto.domain.TaskCatalog;
import cn.zoumh.qqauto.domain.TaskExecutionStatus;
import cn.zoumh.qqauto.domain.UserAccount;
import cn.zoumh.qqauto.domain.WithdrawRequest;
import cn.zoumh.qqauto.repository.AnnouncementRepository;
import cn.zoumh.qqauto.repository.ProductRepository;
import cn.zoumh.qqauto.repository.PurchaseOrderRepository;
import cn.zoumh.qqauto.repository.QqAccountRepository;
import cn.zoumh.qqauto.repository.QqTaskPreferenceRepository;
import cn.zoumh.qqauto.repository.RechargeRequestRepository;
import cn.zoumh.qqauto.repository.SiteMessageRepository;
import cn.zoumh.qqauto.repository.TaskCatalogRepository;
import cn.zoumh.qqauto.repository.UserAccountRepository;
import cn.zoumh.qqauto.repository.WithdrawRequestRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PortalService
{
    private final UserAccountRepository userAccountRepository;
    private final AnnouncementRepository announcementRepository;
    private final ProductRepository productRepository;
    private final SiteMessageRepository siteMessageRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final RechargeRequestRepository rechargeRequestRepository;
    private final WithdrawRequestRepository withdrawRequestRepository;
    private final QqAccountRepository qqAccountRepository;
    private final TaskCatalogRepository taskCatalogRepository;
    private final QqTaskPreferenceRepository qqTaskPreferenceRepository;
    private final PasswordEncoder passwordEncoder;

    public UserAccount currentUser(String username)
    {
        return userAccountRepository.findByUsername(username).orElseThrow();
    }

    public List<Announcement> announcements()
    {
        return announcementRepository.findAllByPublishedTrueOrderByPinnedDescCreatedAtDesc();
    }

    public List<Product> products()
    {
        return productRepository.findAllByActiveTrueOrderByFeaturedDescCreatedAtDesc();
    }

    public List<Product> featuredProducts()
    {
        return products().stream().limit(3).toList();
    }

    public List<SiteMessage> messages(UserAccount user)
    {
        return siteMessageRepository.findTop10ByUserOrderByCreatedAtDesc(user);
    }

    public List<PurchaseOrder> orders(UserAccount user)
    {
        return purchaseOrderRepository.findTop20ByUserOrderByCreatedAtDesc(user);
    }

    public List<RechargeRequest> recharges(UserAccount user)
    {
        return rechargeRequestRepository.findTop20ByUserOrderByCreatedAtDesc(user);
    }

    public List<WithdrawRequest> withdrawals(UserAccount user)
    {
        return withdrawRequestRepository.findTop20ByUserOrderByCreatedAtDesc(user);
    }

    public long unreadMessages(UserAccount user)
    {
        return siteMessageRepository.countByUserAndReadFlagFalse(user);
    }

    public List<QqAccount> qqAccounts(UserAccount user)
    {
        return qqAccountRepository.findByUserOrderByCreatedAtDesc(user);
    }

    public QqAccount primaryQqAccount(UserAccount user)
    {
        return qqAccounts(user).stream().findFirst().orElse(null);
    }

    public List<QqTaskPreference> taskPreferences(UserAccount user, Long accountId)
    {
        QqAccount account = qqAccountOf(user, accountId);
        return qqTaskPreferenceRepository.findByQqAccountOrderByTaskCatalogSortOrderAsc(account);
    }

    public long runningQqCount(UserAccount user)
    {
        return qqAccounts(user).stream().filter(item -> item.getStatus() == QqAccountStatus.RUNNING).count();
    }

    public long todayDoneTaskCount(UserAccount user)
    {
        return qqAccounts(user).stream()
            .flatMap(item -> qqTaskPreferenceRepository.findByQqAccountOrderByTaskCatalogSortOrderAsc(item).stream())
            .filter(item -> item.getLastStatus() == TaskExecutionStatus.DONE)
            .count();
    }

    public long totalTaskCount(UserAccount user)
    {
        return qqAccounts(user).stream()
            .mapToLong(item -> qqTaskPreferenceRepository.findByQqAccountOrderByTaskCatalogSortOrderAsc(item).size())
            .sum();
    }

    @Transactional
    public void register(String username, String password, String qq, String email)
    {
        if (userAccountRepository.findByUsername(username).isPresent())
        {
            throw new IllegalArgumentException("用户名已存在");
        }
        UserAccount user = new UserAccount();
        user.setUsername(username);
        user.setDisplayName(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setQqNumber(qq);
        user.setEmail(email);
        user.setRole(RoleType.USER);
        userAccountRepository.save(user);
        siteMessageRepository.save(newMessage(user, "欢迎使用", "账号已创建，你现在可以添加 QQ 账号并开启自动任务。"));
    }

    @Transactional
    public void createOrder(UserAccount user, Long productId, String targetQq, String paymentMethod)
    {
        Product product = productRepository.findById(productId).orElseThrow();
        PurchaseOrder order = new PurchaseOrder();
        order.setUser(user);
        order.setProduct(product);
        order.setAmount(product.getPrice());
        order.setPaymentMethod(paymentMethod);
        order.setTargetQq(targetQq);
        if ("BALANCE".equalsIgnoreCase(paymentMethod) && user.getBalance().compareTo(product.getPrice()) >= 0)
        {
            order.setStatus(OrderStatus.PAID);
            order.setNotes("余额支付成功，等待后台确认开通");
            user.setBalance(user.getBalance().subtract(product.getPrice()));
        }
        else
        {
            order.setStatus(OrderStatus.PENDING);
            order.setNotes("订单已创建，等待人工确认");
        }
        purchaseOrderRepository.save(order);
        siteMessageRepository.save(newMessage(user, "新订单已提交", "订单 #" + order.getId() + " 已创建，状态：" + order.getStatus().name()));
    }

    @Transactional
    public void createRecharge(UserAccount user, BigDecimal amount, String channel, String note)
    {
        RechargeRequest request = new RechargeRequest();
        request.setUser(user);
        request.setAmount(amount);
        request.setChannel(channel);
        request.setNote(note);
        rechargeRequestRepository.save(request);
        siteMessageRepository.save(newMessage(user, "充值申请已提交", "充值金额：" + amount + " 元。"));
    }

    @Transactional
    public void createWithdraw(UserAccount user, BigDecimal amount, String channel, String account)
    {
        if (user.getBalance().compareTo(amount) < 0)
        {
            throw new IllegalArgumentException("余额不足");
        }
        WithdrawRequest request = new WithdrawRequest();
        request.setUser(user);
        request.setAmount(amount);
        request.setChannel(channel);
        request.setAccount(account);
        withdrawRequestRepository.save(request);
        siteMessageRepository.save(newMessage(user, "提现申请已提交", "提现金额：" + amount + " 元。"));
    }

    @Transactional
    public void updateProfile(UserAccount user, String displayName, String email, String qq)
    {
        user.setDisplayName(displayName);
        user.setEmail(email);
        user.setQqNumber(qq);
    }

    @Transactional
    public void addQqAccount(UserAccount user, String qqNumber, String qqPassword, Long productId)
    {
        Product product = productRepository.findById(productId).orElseThrow();
        QqAccount account = new QqAccount();
        account.setUser(user);
        account.setQqNumber(qqNumber);
        account.setQqPassword(passwordEncoder.encode(qqPassword));
        account.setNickname("QQ " + qqNumber.substring(Math.max(0, qqNumber.length() - 4)));
        account.setPackageName(product.getName());
        account.setStatus(QqAccountStatus.RUNNING);
        account.setExpireAt(LocalDateTime.now().plusDays(product.getDurationDays()));
        account.setLastRunAt(LocalDateTime.now().minusMinutes(3));
        qqAccountRepository.save(account);

        taskCatalogRepository.findAllByOrderBySortOrderAsc().forEach(task -> {
            QqTaskPreference preference = new QqTaskPreference();
            preference.setQqAccount(account);
            preference.setTaskCatalog(task);
            preference.setEnabled(true);
            preference.setLastStatus(task.getSortOrder() % 5 == 0 ? TaskExecutionStatus.PENDING : TaskExecutionStatus.DONE);
            qqTaskPreferenceRepository.save(preference);
        });

        siteMessageRepository.save(newMessage(user, "QQ 账号已接入", qqNumber + " 已加入代挂列表，系统将按默认任务清单执行。"));
    }

    @Transactional
    public void toggleTask(UserAccount user, Long accountId, Long preferenceId)
    {
        QqAccount account = qqAccountOf(user, accountId);
        QqTaskPreference preference = qqTaskPreferenceRepository.findById(preferenceId).orElseThrow();
        if (!preference.getQqAccount().getId().equals(account.getId()))
        {
            throw new IllegalArgumentException("任务不属于当前 QQ 账号");
        }
        preference.setEnabled(!preference.isEnabled());
        if (!preference.isEnabled())
        {
            preference.setLastStatus(TaskExecutionStatus.SKIPPED);
        }
        else if (preference.getLastStatus() == TaskExecutionStatus.SKIPPED)
        {
            preference.setLastStatus(TaskExecutionStatus.PENDING);
        }
    }

    @Transactional
    public void markAllTasksPending(UserAccount user, Long accountId)
    {
        QqAccount account = qqAccountOf(user, accountId);
        List<QqTaskPreference> preferences = qqTaskPreferenceRepository.findByQqAccountOrderByTaskCatalogSortOrderAsc(account);
        preferences.stream()
            .filter(QqTaskPreference::isEnabled)
            .forEach(item -> item.setLastStatus(TaskExecutionStatus.PENDING));
        account.setLastRunAt(LocalDateTime.now());
        account.setStatus(QqAccountStatus.RUNNING);
    }

    @Transactional
    public void refreshTaskStatuses(UserAccount user, Long accountId)
    {
        QqAccount account = qqAccountOf(user, accountId);
        List<QqTaskPreference> preferences = qqTaskPreferenceRepository.findByQqAccountOrderByTaskCatalogSortOrderAsc(account);
        preferences.stream()
            .filter(QqTaskPreference::isEnabled)
            .filter(item -> item.getLastStatus() == TaskExecutionStatus.PENDING)
            .findFirst()
            .ifPresent(item -> item.setLastStatus(TaskExecutionStatus.DONE));
        account.setLastRunAt(LocalDateTime.now());
    }

    @Transactional
    public void approveRecharge(Long requestId)
    {
        RechargeRequest request = rechargeRequestRepository.findById(requestId).orElseThrow();
        if (request.getStatus() != RequestStatus.PENDING)
        {
            return;
        }
        request.setStatus(RequestStatus.APPROVED);
        UserAccount user = request.getUser();
        user.setBalance(user.getBalance().add(request.getAmount()));
        siteMessageRepository.save(newMessage(user, "充值已到账", "充值申请 #" + request.getId() + " 已审核通过。"));
    }

    @Transactional
    public void approveWithdraw(Long requestId)
    {
        WithdrawRequest request = withdrawRequestRepository.findById(requestId).orElseThrow();
        if (request.getStatus() != RequestStatus.PENDING)
        {
            return;
        }
        UserAccount user = request.getUser();
        if (user.getBalance().compareTo(request.getAmount()) < 0)
        {
            throw new IllegalArgumentException("用户余额不足，无法审核");
        }
        user.setBalance(user.getBalance().subtract(request.getAmount()));
        request.setStatus(RequestStatus.APPROVED);
        siteMessageRepository.save(newMessage(user, "提现已处理", "提现申请 #" + request.getId() + " 已审核通过。"));
    }

    public List<UserAccount> allUsers()
    {
        return userAccountRepository.findAll();
    }

    public List<Product> allProducts()
    {
        return productRepository.findAll();
    }

    public List<PurchaseOrder> allOrders()
    {
        return purchaseOrderRepository.findAll();
    }

    public List<RechargeRequest> allRechargeRequests()
    {
        return rechargeRequestRepository.findAll();
    }

    public List<WithdrawRequest> allWithdrawRequests()
    {
        return withdrawRequestRepository.findAll();
    }

    public List<Announcement> allAnnouncements()
    {
        return announcementRepository.findAll();
    }

    public List<QqAccount> allQqAccounts()
    {
        return qqAccountRepository.findAll().stream()
            .sorted(Comparator.comparing(QqAccount::getLastRunAt, Comparator.nullsLast(Comparator.reverseOrder())))
            .toList();
    }

    public long totalTaskExecutionCount()
    {
        return qqTaskPreferenceRepository.findAll().size();
    }

    public long completedTaskExecutionCount()
    {
        return qqTaskPreferenceRepository.findAll().stream()
            .filter(item -> item.getLastStatus() == TaskExecutionStatus.DONE)
            .count();
    }

    @Transactional
    public void approveOrder(Long orderId)
    {
        PurchaseOrder order = purchaseOrderRepository.findById(orderId).orElseThrow();
        if (order.getStatus() == OrderStatus.COMPLETED)
        {
            return;
        }
        if (order.getStatus() == OrderStatus.PENDING)
        {
            order.setStatus(OrderStatus.PAID);
        }
        order.setStatus(OrderStatus.COMPLETED);
        order.setNotes("后台已确认开通");
        siteMessageRepository.save(newMessage(order.getUser(), "订单已开通", "订单 #" + order.getId() + " 已确认完成。"));
    }

    @Transactional
    public void sendSiteMessage(Long userId, String title, String content)
    {
        UserAccount user = userAccountRepository.findById(userId).orElseThrow();
        siteMessageRepository.save(newMessage(user, title, content));
    }

    @Transactional
    public void saveAnnouncement(Announcement announcement)
    {
        announcementRepository.save(announcement);
    }

    @Transactional
    public void saveProduct(Product product)
    {
        productRepository.save(product);
    }

    private QqAccount qqAccountOf(UserAccount user, Long accountId)
    {
        return qqAccounts(user).stream()
            .filter(item -> item.getId().equals(accountId))
            .findFirst()
            .orElseThrow();
    }

    private SiteMessage newMessage(UserAccount user, String title, String content)
    {
        SiteMessage message = new SiteMessage();
        message.setUser(user);
        message.setTitle(title);
        message.setContent(content);
        return message;
    }
}
