package com.example.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Index;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Column;
import jakarta.persistence.Version;
import jakarta.persistence.PrePersist;
import lombok.Getter;
import lombok.Setter;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.Instant;

@Entity
@Getter
@Setter
@EqualsAndHashCode(of = {"id"})
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "coupon", indexes = @Index(columnList = "code", unique = true))
public class Coupon {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, updatable = false)
    private String code;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private int maxUses;

    @Column(nullable = false)
    private int currentUses;

    @Column(nullable = false)
    private String country;

    @Version
    private Long version;

    @PrePersist
    private void prePersist() {
        this.createdAt = Instant.now();
        this.currentUses = 0;
    }
}