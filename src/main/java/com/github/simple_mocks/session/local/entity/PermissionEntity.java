package com.github.simple_mocks.session.local.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * @author sibmaks
 * @since 0.0.1
 */
@Entity
@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "permission")
public class PermissionEntity {
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    @Column(name = "code", nullable = false)
    private String code;
}
