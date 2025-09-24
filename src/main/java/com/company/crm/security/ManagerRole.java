package com.company.crm.security;

import com.company.crm.entity.Invoice;
import com.company.crm.entity.Order;
import com.company.crm.entity.Payment;
import io.jmix.security.model.EntityAttributePolicyAction;
import io.jmix.security.model.EntityPolicyAction;
import io.jmix.security.role.annotation.EntityAttributePolicy;
import io.jmix.security.role.annotation.EntityPolicy;
import io.jmix.security.role.annotation.ResourceRole;
import io.jmix.securityflowui.role.annotation.MenuPolicy;
import io.jmix.securityflowui.role.annotation.ViewPolicy;

@ResourceRole(name = "Manager", code = ManagerRole.CODE)
public interface ManagerRole extends EmployeeRole {
    String CODE = "manager";

    @MenuPolicy(menuIds = {"Order_.list", "Invoice.list", "Payment.list"})
    @ViewPolicy(viewIds = {"Order_.list", "Invoice.list", "Payment.list", "Invoice.detail", "Order_.detail", "Payment.detail"})
    void screens();

    @EntityAttributePolicy(entityClass = Invoice.class, attributes = "*", action = EntityAttributePolicyAction.MODIFY)
    @EntityPolicy(entityClass = Invoice.class, actions = EntityPolicyAction.ALL)
    void invoice();

    @EntityAttributePolicy(entityClass = Order.class, attributes = "*", action = EntityAttributePolicyAction.MODIFY)
    @EntityPolicy(entityClass = Order.class, actions = EntityPolicyAction.ALL)
    void order();

    @EntityAttributePolicy(entityClass = Payment.class, attributes = "*", action = EntityAttributePolicyAction.MODIFY)
    @EntityPolicy(entityClass = Payment.class, actions = EntityPolicyAction.ALL)
    void payment();
}