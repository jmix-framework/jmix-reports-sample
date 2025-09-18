package com.company.crm.entity;

import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.metamodel.annotation.Composition;
import io.jmix.core.metamodel.annotation.InstanceName;
import io.jmix.core.metamodel.annotation.JmixEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

@JmixEntity
@Table(name = "CLIENT", indexes = {
        @Index(name = "IDX_CLIENT_ACCOUNT_MANAGER", columnList = "ACCOUNT_MANAGER_ID")
})
@Entity
public class Client {
    @JmixGeneratedValue
    @Column(name = "ID", nullable = false)
    @Id
    private UUID id;

    @Column(name = "VERSION", nullable = false)
    @Version
    private Integer version;

    @InstanceName
    @Column(name = "NAME", nullable = false)
    @NotNull
    private String name;

    @Column(name = "FULL_NAME")
    private String fullName;

    @Column(name = "ADDRESS")
    private String address;

    @Column(name = "TYPE_", length = 1)
    private String type;

    @Column(name = "VAT_NUMBER")
    private String vatNumber;

    @Column(name = "REG_NUMBER")
    private String regNumber;

    @Column(name = "WEBSITE")
    private String website;

    @JoinColumn(name = "ACCOUNT_MANAGER_ID")
    @ManyToOne(fetch = FetchType.LAZY)
    private User accountManager;

    @Composition
    @OrderBy("person")
    @OneToMany(mappedBy = "client")
    private List<Contact> contacts;

    public List<Contact> getContacts() {
        return contacts;
    }

    public void setContacts(List<Contact> contacts) {
        this.contacts = contacts;
    }

    public ClientType getType() {
        return type == null ? null : ClientType.fromId(type);
    }

    public void setType(ClientType type) {
        this.type = type == null ? null : type.getId();
    }

    public User getAccountManager() {
        return accountManager;
    }

    public void setAccountManager(User accountManager) {
        this.accountManager = accountManager;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public String getRegNumber() {
        return regNumber;
    }

    public void setRegNumber(String regNumber) {
        this.regNumber = regNumber;
    }

    public String getVatNumber() {
        return vatNumber;
    }

    public void setVatNumber(String vatNumber) {
        this.vatNumber = vatNumber;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

}