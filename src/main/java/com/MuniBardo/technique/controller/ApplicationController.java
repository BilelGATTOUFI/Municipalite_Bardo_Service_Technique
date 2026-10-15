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
import org.springframework.web.bind.annotation.PathVariable;

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

    // 1. عرض تفاصيل المطلب وسجل التعديلات
    @GetMapping("/applications/{id}")
    public String viewApplicationDetails(@PathVariable Long id, Model model) {
        Application application = applicationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("المطلب غير موجود"));

        model.addAttribute("application", application);
        return "applications/details";
    }

    // 2. عرض صفحة تعديل المطلب
    @GetMapping("/applications/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        Application application = applicationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("المطلب غير موجود"));
        model.addAttribute("application", application);
        model.addAttribute("types", applicationTypeRepository.findAll());
        model.addAttribute("statuses", ApplicationStatus.values()); // تمرير الحالات المتاحة
        return "applications/edit"; // اسم صفحة التعديل
    }

    // 3. حفظ التعديلات وتاريخها واسم المستخدم المُعدِّل
    @PostMapping("/applications/update/{id}")
    public String updateApplication(@PathVariable Long id,
                                    @ModelAttribute Application updatedApplication,
                                    @RequestParam(required = false) String notes) {

        Application existingApplication = applicationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("المطلب غير موجود"));

        // جلب المستخدم الحالي الذي قام بالتعديل
        String currentUsername = org.springframework.security.core.context.SecurityContextHolder
                .getContext().getAuthentication().getName();
        User currentUser = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new RuntimeException("المستخدم غير موجود"));

        // تحديث البيانات الأساسية
        existingApplication.setCurrentStatus(updatedApplication.getCurrentStatus());
        existingApplication.setLocationAddress(updatedApplication.getLocationAddress());
        existingApplication.setNotes(notes != null ? notes : existingApplication.getNotes());
        existingApplication.setUpdatedAt(java.time.LocalDateTime.now()); // تحديث وقت التعديل

        // تسجيل العملية في جدول السجل (StatusHistory) إذا كان متاحاً لديك
        /*
        StatusHistory history = new StatusHistory();
        history.setApplication(existingApplication);
        history.setStatus(updatedApplication.getCurrentStatus());
        history.setChangedBy(currentUser); // أو تخزين اسم المستخدم كـ String حسب تصميم الـ Entity لديك
        history.setChangedAt(java.time.LocalDateTime.now());
        statusHistoryRepository.save(history);
        */

        applicationRepository.save(existingApplication);

        return "redirect:/applications/" + id; // العودة لصفحة التفاصيل بعد التعديل
    }











}