package com.MuniBardo.technique.service;

import com.MuniBardo.technique.entity.*;
import com.MuniBardo.technique.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.Year;
import java.util.List;

@Service
public class ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final CitizenRepository citizenRepository;
    private final StatusHistoryRepository statusHistoryRepository;
    private final FileStorageService fileStorageService;
    private final ApplicationTypeRepository applicationTypeRepository; // <-- أضف هذا السطر

    public ApplicationService(ApplicationRepository applicationRepository,
                              CitizenRepository citizenRepository,
                              StatusHistoryRepository statusHistoryRepository,
                              FileStorageService fileStorageService,
                              ApplicationTypeRepository applicationTypeRepository) { // <-- وأضفه هنا أيضاً
        this.applicationRepository = applicationRepository;
        this.citizenRepository = citizenRepository;
        this.statusHistoryRepository = statusHistoryRepository;
        this.fileStorageService = fileStorageService;
        this.applicationTypeRepository = applicationTypeRepository;
    }

    public synchronized String generateFileNumber(String typeCode) {
        int currentYear = Year.now().getValue();
        long countThisYear = applicationRepository.count() + 1;
        return String.format("%d-%s-%04d", currentYear, typeCode.toUpperCase(), countThisYear);
    }

    @Transactional
    public Application createApplication(Application application, Citizen citizen, List<MultipartFile> files, User currentUser) {
        Citizen savedCitizen = citizenRepository.findByCinOrPassport(citizen.getCinOrPassport())
                .orElseGet(() -> citizenRepository.save(citizen));

        application.setCitizen(savedCitizen);
        application.setCreatedBy(currentUser);
        application.setCurrentStatus(ApplicationStatus.PENDING);

        String typeCode = "REQ";
        if (application.getType() != null && application.getType().getId() != null) {
            ApplicationType dbType = applicationTypeRepository.findById(application.getType().getId()).orElse(null);
            if (dbType != null) {
                application.setType(dbType);
                typeCode = dbType.getCode();
            }
        }

        application.setFileNumber(generateFileNumber(typeCode));
        Application savedApp = applicationRepository.save(application);

        if (files != null && !files.isEmpty()) {
            for (MultipartFile file : files) {
                if (!file.isEmpty()) {
                    String savedPath = fileStorageService.storeFile(file);
                    ApplicationDocument doc = ApplicationDocument.builder()
                            .application(savedApp)
                            .documentName(file.getOriginalFilename())
                            .filePath(savedPath)
                            .fileType(file.getContentType())
                            .fileSizeBytes(file.getSize())
                            .uploadedBy(currentUser)
                            .build();
                    savedApp.getDocuments().add(doc);
                }
            }
        }

        StatusHistory history = StatusHistory.builder()
                .application(savedApp)
                .previousStatus(null)
                .newStatus(ApplicationStatus.PENDING)
                .comment("تم تقديم وتسجيل المطلب بنجاح.")
                .changedBy(currentUser)
                .build();
        statusHistoryRepository.save(history);

        return savedApp;
    }

    @Transactional
    public void updateStatus(Long applicationId, ApplicationStatus newStatus, String comment, User currentUser) {
        Application app = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("المطلب غير موجود"));

        ApplicationStatus oldStatus = app.getCurrentStatus();
        app.setCurrentStatus(newStatus);
        applicationRepository.save(app);

        StatusHistory history = StatusHistory.builder()
                .application(app)
                .previousStatus(oldStatus)
                .newStatus(newStatus)
                .comment(comment)
                .changedBy(currentUser)
                .build();
        statusHistoryRepository.save(history);
    }
}