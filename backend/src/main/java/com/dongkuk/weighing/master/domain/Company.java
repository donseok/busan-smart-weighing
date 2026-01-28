package com.dongkuk.weighing.master.domain;

import com.dongkuk.weighing.global.audit.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "tb_company", indexes = {
        @Index(name = "idx_company_name", columnList = "company_name"),
        @Index(name = "idx_company_type", columnList = "company_type")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Company extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "company_id")
    private Long companyId;

    @Column(name = "company_name", nullable = false, length = 100)
    private String companyName;

    @Column(name = "company_type", nullable = false, length = 20)
    private String companyType;

    @Column(name = "business_number", length = 20)
    private String businessNumber;

    @Column(name = "representative", length = 50)
    private String representative;

    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    @Column(name = "address", length = 200)
    private String address;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    @Builder
    public Company(String companyName, String companyType, String businessNumber,
                   String representative, String phoneNumber, String address) {
        this.companyName = companyName;
        this.companyType = companyType;
        this.businessNumber = businessNumber;
        this.representative = representative;
        this.phoneNumber = phoneNumber;
        this.address = address;
    }

    public void update(String companyName, String companyType, String businessNumber,
                       String representative, String phoneNumber, String address) {
        this.companyName = companyName;
        this.companyType = companyType;
        this.businessNumber = businessNumber;
        this.representative = representative;
        this.phoneNumber = phoneNumber;
        this.address = address;
    }

    public void deactivate() {
        this.isActive = false;
    }

    public void activate() {
        this.isActive = true;
    }
}
