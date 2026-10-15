package com.MuniBardo.technique.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "application_types")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ApplicationType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(unique = true, nullable = false, length = 50)
    private String code; // مثال: BUILD_PERMIT, WATER_METER

    @Column(nullable = false, length = 100)
    private String name; // مثال: رخصة بناء، رخصة عداد ماء

    @Column(columnDefinition = "TEXT")
    private String description;
}