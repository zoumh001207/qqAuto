package cn.zoumh.qqauto.controller;

import cn.zoumh.qqauto.domain.Announcement;
import cn.zoumh.qqauto.domain.Product;
import cn.zoumh.qqauto.service.PortalService;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class AdminController
{
    private final PortalService portalService;

    @GetMapping("/admin")
    public String index(Model model)
    {
        model.addAttribute("users", portalService.allUsers());
        model.addAttribute("products", portalService.allProducts());
        model.addAttribute("orders", portalService.allOrders());
        model.addAttribute("recharges", portalService.allRechargeRequests());
        model.addAttribute("withdrawals", portalService.allWithdrawRequests());
        model.addAttribute("announcements", portalService.allAnnouncements());
        return "admin/index";
    }

    @PostMapping("/admin/announcement")
    public String saveAnnouncement(@RequestParam String title,
                                   @RequestParam String content,
                                   @RequestParam(defaultValue = "false") boolean published,
                                   @RequestParam(defaultValue = "false") boolean pinned,
                                   RedirectAttributes redirectAttributes)
    {
        Announcement announcement = new Announcement();
        announcement.setTitle(title);
        announcement.setContent(content);
        announcement.setPublished(published);
        announcement.setPinned(pinned);
        portalService.saveAnnouncement(announcement);
        redirectAttributes.addFlashAttribute("success", "公告已保存。");
        return "redirect:/admin";
    }

    @PostMapping("/admin/product")
    public String saveProduct(@RequestParam String name,
                              @RequestParam String category,
                              @RequestParam String versionTag,
                              @RequestParam String summary,
                              @RequestParam BigDecimal price,
                              @RequestParam Integer durationDays,
                              @RequestParam(defaultValue = "false") boolean active,
                              @RequestParam(defaultValue = "false") boolean featured,
                              RedirectAttributes redirectAttributes)
    {
        Product product = new Product();
        product.setName(name);
        product.setCategory(category);
        product.setVersionTag(versionTag);
        product.setSummary(summary);
        product.setPrice(price);
        product.setDurationDays(durationDays);
        product.setActive(active);
        product.setFeatured(featured);
        portalService.saveProduct(product);
        redirectAttributes.addFlashAttribute("success", "商品已保存。");
        return "redirect:/admin";
    }

    @PostMapping("/admin/order/{id}/approve")
    public String approveOrder(@PathVariable Long id, RedirectAttributes redirectAttributes)
    {
        portalService.approveOrder(id);
        redirectAttributes.addFlashAttribute("success", "订单已确认。");
        return "redirect:/admin";
    }

    @PostMapping("/admin/recharge/{id}/approve")
    public String approveRecharge(@PathVariable Long id, RedirectAttributes redirectAttributes)
    {
        portalService.approveRecharge(id);
        redirectAttributes.addFlashAttribute("success", "充值已审核。");
        return "redirect:/admin";
    }

    @PostMapping("/admin/withdraw/{id}/approve")
    public String approveWithdraw(@PathVariable Long id, RedirectAttributes redirectAttributes)
    {
        portalService.approveWithdraw(id);
        redirectAttributes.addFlashAttribute("success", "提现已审核。");
        return "redirect:/admin";
    }

    @PostMapping("/admin/message")
    public String sendMessage(@RequestParam Long userId,
                              @RequestParam String title,
                              @RequestParam String content,
                              RedirectAttributes redirectAttributes)
    {
        portalService.sendSiteMessage(userId, title, content);
        redirectAttributes.addFlashAttribute("success", "站内消息已发送。");
        return "redirect:/admin";
    }
}
