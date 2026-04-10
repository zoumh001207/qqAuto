package cn.zoumh.qqauto.controller;

import cn.zoumh.qqauto.domain.UserAccount;
import cn.zoumh.qqauto.service.PortalService;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class UserController
{
    private final PortalService portalService;

    @GetMapping("/user/dashboard")
    public String dashboard(Authentication authentication, Model model)
    {
        UserAccount user = portalService.currentUser(authentication.getName());
        model.addAttribute("currentUser", user);
        model.addAttribute("announcements", portalService.announcements());
        model.addAttribute("products", portalService.featuredProducts());
        model.addAttribute("messages", portalService.messages(user));
        model.addAttribute("orders", portalService.orders(user));
        model.addAttribute("qqAccounts", portalService.qqAccounts(user));
        model.addAttribute("unreadCount", portalService.unreadMessages(user));
        model.addAttribute("runningCount", portalService.runningQqCount(user));
        model.addAttribute("doneTaskCount", portalService.todayDoneTaskCount(user));
        model.addAttribute("totalTaskCount", portalService.totalTaskCount(user));
        return "user/dashboard";
    }

    @GetMapping("/user/tasks")
    public String tasks(Authentication authentication,
                        @RequestParam(required = false) Long accountId,
                        Model model)
    {
        UserAccount user = portalService.currentUser(authentication.getName());
        var qqAccounts = portalService.qqAccounts(user);
        var selectedAccount = accountId != null ? qqAccounts.stream().filter(item -> item.getId().equals(accountId)).findFirst().orElse(null) : portalService.primaryQqAccount(user);
        model.addAttribute("currentUser", user);
        model.addAttribute("qqAccounts", qqAccounts);
        model.addAttribute("selectedAccount", selectedAccount);
        model.addAttribute("taskPreferences", selectedAccount == null ? java.util.List.of() : portalService.taskPreferences(user, selectedAccount.getId()));
        return "user/tasks";
    }

    @PostMapping("/user/qq-accounts")
    public String addQqAccount(Authentication authentication,
                               @RequestParam String qqNumber,
                               @RequestParam String qqPassword,
                               @RequestParam Long productId,
                               RedirectAttributes redirectAttributes)
    {
        portalService.addQqAccount(portalService.currentUser(authentication.getName()), qqNumber, qqPassword, productId);
        redirectAttributes.addFlashAttribute("success", "QQ账号已加入代挂列表。");
        return "redirect:/user/dashboard";
    }

    @PostMapping("/user/tasks/{accountId}/toggle/{preferenceId}")
    public String toggleTask(Authentication authentication,
                             @PathVariable Long accountId,
                             @PathVariable Long preferenceId,
                             RedirectAttributes redirectAttributes)
    {
        portalService.toggleTask(portalService.currentUser(authentication.getName()), accountId, preferenceId);
        redirectAttributes.addFlashAttribute("success", "任务开关已更新。");
        return "redirect:/user/tasks?accountId=" + accountId;
    }

    @PostMapping("/user/tasks/{accountId}/run-all")
    public String runAllTasks(Authentication authentication,
                              @PathVariable Long accountId,
                              RedirectAttributes redirectAttributes)
    {
        portalService.markAllTasksPending(portalService.currentUser(authentication.getName()), accountId);
        redirectAttributes.addFlashAttribute("success", "一键补挂已提交。");
        return "redirect:/user/tasks?accountId=" + accountId;
    }

    @PostMapping("/user/tasks/{accountId}/refresh")
    public String refreshTasks(Authentication authentication,
                               @PathVariable Long accountId,
                               RedirectAttributes redirectAttributes)
    {
        portalService.refreshTaskStatuses(portalService.currentUser(authentication.getName()), accountId);
        redirectAttributes.addFlashAttribute("success", "任务状态已刷新。");
        return "redirect:/user/tasks?accountId=" + accountId;
    }

    @GetMapping("/user/profile")
    public String profile(Authentication authentication, Model model)
    {
        model.addAttribute("currentUser", portalService.currentUser(authentication.getName()));
        model.addAttribute("qqAccounts", portalService.qqAccounts(portalService.currentUser(authentication.getName())));
        return "user/profile";
    }

    @PostMapping("/user/profile")
    public String updateProfile(Authentication authentication,
                                @RequestParam String displayName,
                                @RequestParam(required = false) String email,
                                @RequestParam String qq,
                                RedirectAttributes redirectAttributes)
    {
        portalService.updateProfile(portalService.currentUser(authentication.getName()), displayName, email, qq);
        redirectAttributes.addFlashAttribute("success", "资料已更新。");
        return "redirect:/user/profile";
    }

    @GetMapping("/user/messages")
    public String messages(Authentication authentication, Model model)
    {
        UserAccount user = portalService.currentUser(authentication.getName());
        model.addAttribute("currentUser", user);
        model.addAttribute("messages", portalService.messages(user));
        return "user/messages";
    }

    @GetMapping("/user/orders")
    public String orders(Authentication authentication, Model model)
    {
        UserAccount user = portalService.currentUser(authentication.getName());
        model.addAttribute("currentUser", user);
        model.addAttribute("orders", portalService.orders(user));
        model.addAttribute("products", portalService.products());
        return "user/orders";
    }

    @PostMapping("/user/orders")
    public String createOrder(Authentication authentication,
                              @RequestParam Long productId,
                              @RequestParam String targetQq,
                              @RequestParam String paymentMethod,
                              RedirectAttributes redirectAttributes)
    {
        portalService.createOrder(portalService.currentUser(authentication.getName()), productId, targetQq, paymentMethod);
        redirectAttributes.addFlashAttribute("success", "订单已创建。");
        return "redirect:/user/orders";
    }

    @GetMapping("/user/wallet")
    public String wallet(Authentication authentication, Model model)
    {
        UserAccount user = portalService.currentUser(authentication.getName());
        model.addAttribute("currentUser", user);
        model.addAttribute("recharges", portalService.recharges(user));
        model.addAttribute("withdrawals", portalService.withdrawals(user));
        return "user/wallet";
    }

    @PostMapping("/user/recharge")
    public String recharge(Authentication authentication,
                           @RequestParam BigDecimal amount,
                           @RequestParam String channel,
                           @RequestParam(required = false) String note,
                           RedirectAttributes redirectAttributes)
    {
        portalService.createRecharge(portalService.currentUser(authentication.getName()), amount, channel, note);
        redirectAttributes.addFlashAttribute("success", "充值申请已提交。");
        return "redirect:/user/wallet";
    }

    @PostMapping("/user/withdraw")
    public String withdraw(Authentication authentication,
                           @RequestParam BigDecimal amount,
                           @RequestParam String channel,
                           @RequestParam String account,
                           RedirectAttributes redirectAttributes)
    {
        try
        {
            portalService.createWithdraw(portalService.currentUser(authentication.getName()), amount, channel, account);
            redirectAttributes.addFlashAttribute("success", "提现申请已提交。");
        }
        catch (IllegalArgumentException ex)
        {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/user/wallet";
    }
}
