package com.app.platform.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "platforms")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Platform {
    @Id
    @GeneratedValue
    private UUID id;
    private String name;
    private String domain;
    private String logoUrl;
    private boolean verified;
}
