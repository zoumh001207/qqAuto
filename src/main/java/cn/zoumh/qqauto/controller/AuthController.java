package cn.zoumh.qqauto.controller;

import cn.zoumh.qqauto.service.PortalService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class AuthController
{
    private final PortalService portalService;

    @GetMapping("/")
    public String root()
    {
        return "redirect:/login";
    }

    @GetMapping("/login")
    public String login()
    {
        return "auth/login";
    }

    @GetMapping("/register")
    public String register()
    {
        return "auth/register";
    }

    @PostMapping("/register")
    public String doRegister(@RequestParam String username,
                             @RequestParam String password,
                             @RequestParam String qq,
                             @RequestParam(required = false) String email,
                             RedirectAttributes redirectAttributes)
    {
        try
        {
            portalService.register(username, password, qq, email);
            redirectAttributes.addFlashAttribute("success", "注册成功，请直接登录。");
            return "redirect:/login";
        }
        catch (IllegalArgumentException ex)
        {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
            return "redirect:/register";
        }
    }

    @GetMapping("/forgot-password")
    public String forgotPassword(Model model)
    {
        model.addAttribute("notice", "首版仅提供人工找回流程，请联系后台管理员处理。");
        return "auth/forgot-password";
    }
}
