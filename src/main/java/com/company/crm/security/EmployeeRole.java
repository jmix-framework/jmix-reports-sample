package com.company.crm.security;

import com.company.crm.entity.Client;
import com.company.crm.entity.Contact;
import io.jmix.reportsflowui.role.ReportsRunRole;
import io.jmix.security.model.EntityAttributePolicyAction;
import io.jmix.security.model.EntityPolicyAction;
import io.jmix.security.role.annotation.EntityAttributePolicy;
import io.jmix.security.role.annotation.EntityPolicy;
import io.jmix.security.role.annotation.ResourceRole;
import io.jmix.securityflowui.role.UiFilterRole;
import io.jmix.securityflowui.role.annotation.MenuPolicy;
import io.jmix.securityflowui.role.annotation.ViewPolicy;

@ResourceRole(name = "Employee", code = EmployeeRole.CODE)
public interface EmployeeRole extends UiMinimalRole, ReportsRunRole, UiFilterRole {
    String CODE = "employee";

    @MenuPolicy(menuIds = "Client.list")
    @ViewPolicy(viewIds = {"Client.list", "Client.detail", "Contact.detail"})
    void screens();

    @EntityAttributePolicy(entityClass = Client.class, attributes = "*", action = EntityAttributePolicyAction.MODIFY)
    @EntityPolicy(entityClass = Client.class, actions = EntityPolicyAction.ALL)
    void client();

    @EntityAttributePolicy(entityClass = Contact.class, attributes = "*", action = EntityAttributePolicyAction.MODIFY)
    @EntityPolicy(entityClass = Contact.class, actions = EntityPolicyAction.ALL)
    void contact();
}