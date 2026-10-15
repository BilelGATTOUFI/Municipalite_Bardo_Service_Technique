package com.MuniBardo.technique.config;

import com.MuniBardo.technique.entity.ApplicationType;
import com.MuniBardo.technique.entity.User;
import com.MuniBardo.technique.repository.ApplicationTypeRepository;
import com.MuniBardo.technique.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final ApplicationTypeRepository applicationTypeRepository;
    private final UserRepository userRepository;

    public DataInitializer(ApplicationTypeRepository applicationTypeRepository, UserRepository userRepository) {
        this.applicationTypeRepository = applicationTypeRepository;
        this.userRepository = userRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        // 1. تهيئة أنواع المطالب
        if (applicationTypeRepository.count() == 0) {
            applicationTypeRepository.save(ApplicationType.builder()
                    .code("BUILD")
                    .name("رخصة بناء")
                    .description("مطلب الحصول على رخصة أشغال بناء أو توسعة")
                    .build());

            applicationTypeRepository.save(ApplicationType.builder()
                    .code("WATER")
                    .name("رخصة الربط بشبكة الماء")
                    .description("مطلب إدخال أو تغيير عداد الماء الصالح للشرب")
                    .build());

            applicationTypeRepository.save(ApplicationType.builder()
                    .code("ELEC")
                    .name("رخصة الربط بشبكة الكهرباء")
                    .description("مطلب إدخال عداد كهرباء")
                    .build());

            applicationTypeRepository.save(ApplicationType.builder()
                    .code("OCCUP")
                    .name("إشغال الطريق العام")
                    .description("ترخيص مؤقت لاستغلال الرصيف أو الشارع للأشغال")
                    .build());
        }

        if (userRepository.findByUsername("admin").isEmpty()) {
            userRepository.save(User.builder()
                    .username("admin")
                    .password("admin123")
                    .passwordHash("admin123")
                    .fullName("مدير النظام")
                    .role("ROLE_ADMIN") // استخدام ROLE_ADMIN لتوافق مع قيد قاعدة البيانات
                    .isActive(true)
                    .build());
        }

        }

}