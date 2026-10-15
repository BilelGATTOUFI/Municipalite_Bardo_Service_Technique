package com.MuniBardo.technique.controller;

import com.MuniBardo.technique.entity.Application;
import com.MuniBardo.technique.entity.Citizen;
import com.MuniBardo.technique.entity.User;
import com.MuniBardo.technique.repository.ApplicationRepository;
import com.MuniBardo.technique.repository.ApplicationTypeRepository;
import com.MuniBardo.technique.service.ApplicationService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Controller
@RequestMapping("/applications")
public class ApplicationController {

    private final ApplicationRepository applicationRepository;
    private final ApplicationTypeRepository applicationTypeRepository;
    private final ApplicationService applicationService;

    public ApplicationController(ApplicationRepository applicationRepository,
                                 ApplicationTypeRepository applicationTypeRepository,
                                 ApplicationService applicationService) {
        this.applicationRepository = applicationRepository;
        this.applicationTypeRepository = applicationTypeRepository;
        this.applicationService = applicationService;
    }

    // عرض لوحة التحكم
    @GetMapping
    public String listApplications(Model model) {
        model.addAttribute("applications", applicationRepository.findAll());
        return "applications/list";
    }

    // عرض صفحة تسجيل مطلب جديد
    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("application", new Application());
        model.addAttribute("citizen", new Citizen());
        model.addAttribute("types", applicationTypeRepository.findAll());
        return "applications/create";
    }

    // حفظ المطلب والمواطن مع المرفقات
    @PostMapping("/save")
    public String saveApplication(@ModelAttribute Application application,
                                  @ModelAttribute Citizen citizen,
                                  @RequestParam(value = "files", required = false) List<MultipartFile> files) {
        try {
            // مستخدم وهمي مؤقتاً
            User mockUser = User.builder().id(1L).username("admin").build();
            applicationService.createApplication(application, citizen, files, mockUser);
            return "redirect:/applications";
        } catch (Exception e) {
            e.printStackTrace(); // هذا سيطبع السبب الحقيقي في الـ Console باللون الأحمر
            throw new RuntimeException("خطأ أثناء حفظ المطلب: " + e.getMessage(), e);
        }
    }
}