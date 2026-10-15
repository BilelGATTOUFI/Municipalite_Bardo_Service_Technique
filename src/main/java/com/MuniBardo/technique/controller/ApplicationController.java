package com.MuniBardo.technique.controller;

import com.MuniBardo.technique.entity.Application;
import com.MuniBardo.technique.entity.ApplicationStatus;
import com.MuniBardo.technique.entity.Citizen;
import com.MuniBardo.technique.entity.User;
import com.MuniBardo.technique.repository.ApplicationRepository;
import com.MuniBardo.technique.repository.ApplicationTypeRepository;
import com.MuniBardo.technique.repository.CitizenRepository;
import com.MuniBardo.technique.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class ApplicationController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private ApplicationTypeRepository applicationTypeRepository;

    @Autowired
    private CitizenRepository citizenRepository;

    // عرض قائمة المطالب
    @GetMapping({"/applications", "/applications/"})
    public String listApplications(Model model) {
        model.addAttribute("applications", applicationRepository.findAll());
        return "applications/list";
    }

    // عرض صفحة تسجيل مطلب جديد
    @GetMapping("/applications/new")
    public String showCreateForm(Model model) {
        model.addAttribute("application", new Application());
        model.addAttribute("types", applicationTypeRepository.findAll());
        return "applications/create";
    }

    // حفظ المطلب ومعالجة بيانات المواطن
    @PostMapping("/applications/save")
    public String saveApplication(@ModelAttribute Application application,
                                  @RequestParam String cinOrPassport,
                                  @RequestParam String fullName,
                                  @RequestParam(required = false) String phone) {

        // 1. البحث عن المواطن أو إنشاؤه تلقائياً
        Citizen citizen = citizenRepository.findByCinOrPassport(cinOrPassport)
                .orElseGet(() -> {
                    Citizen newCitizen = new Citizen();
                    newCitizen.setCinOrPassport(cinOrPassport);
                    newCitizen.setFullName(fullName);
                    newCitizen.setPhoneNumber(phone);
                    if (application.getLocationAddress() != null) {
                        newCitizen.setAddress(application.getLocationAddress());
                    }
                    return citizenRepository.save(newCitizen);
                });

        // 2. ربط المواطن بالمطلب
        application.setCitizen(citizen);

        // 3. جلب المستخدم الحالي وربطه بالمطلب
        String currentUsername = org.springframework.security.core.context.SecurityContextHolder
                .getContext().getAuthentication().getName();

        User currentUser = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new RuntimeException("المستخدم غير موجود"));
        application.setCreatedBy(currentUser);

        // 4. تعيين رقم ملف فريد افتراضي إذا كان فارغاً (لتجنب خطأ not-null)
        if (application.getFileNumber() == null || application.getFileNumber().isEmpty()) {
            application.setFileNumber("APP-" + System.currentTimeMillis());
        }

        // 5. تعيين الحالة الافتراضية إذا كانت فارغة
        if (application.getCurrentStatus() == null) {
            application.setCurrentStatus(ApplicationStatus.PENDING);
        }

        // 6. حفظ المطلب نهائياً في قاعدة البيانات
        applicationRepository.save(application);

        return "redirect:/applications";
    }
}