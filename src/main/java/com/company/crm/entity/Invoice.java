package com.company.crm.entity;

import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.metamodel.annotation.DependsOnProperties;
import io.jmix.core.metamodel.annotation.InstanceName;
import io.jmix.core.metamodel.annotation.JmixEntity;
import io.jmix.core.metamodel.datatype.DatatypeFormatter;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@JmixEntity
@Table(name = "INVOICE", indexes = {
        @Index(name = "IDX_INVOICE_ORDER", columnList = "ORDER_ID"),
        @Index(name = "IDX_INVOICE_CLIENT", columnList = "CLIENT_ID")
})
@Entity
public class Invoice {
    @JmixGeneratedValue
    @Column(name = "ID", nullable = false)
    @Id
    private UUID id;

    @Column(name = "VERSION", nullable = false)
    @Version
    private Integer version;

    @JoinColumn(name = "CLIENT_ID", nullable = false)
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Client client;

    @JoinColumn(name = "ORDER_ID")
    @ManyToOne(fetch = FetchType.LAZY)
    private Order order;

    @Column(name = "DATE_")
    private LocalDate date;

    @Column(name = "DUE_DATE")
    private LocalDate dueDate;

    @Column(name = "SUBTOTAL", precision = 19, scale = 2)
    private BigDecimal subtotal;

    @Column(name = "VAT", precision = 19, scale = 2)
    private BigDecimal vat;

    @Column(name = "TOTAL", precision = 19, scale = 2)
    private BigDecimal total;

    @Column(name = "STATUS")
    private Integer status;

    public InvoiceStatus getStatus() {
        return status == null ? null : InvoiceStatus.fromId(status);
    }

    public void setStatus(InvoiceStatus status) {
        this.status = status == null ? null : status.getId();
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public BigDecimal getVat() {
        return vat;
    }

    public void setVat(BigDecimal vat) {
        this.vat = vat;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
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

    @InstanceName
    @DependsOnProperties({"date", "total"})
    public String getInstanceName(DatatypeFormatter datatypeFormatter) {
        return String.format("%s, %s",
                datatypeFormatter.formatLocalDate(date),
                datatypeFormatter.formatBigDecimal(total));
    }
}