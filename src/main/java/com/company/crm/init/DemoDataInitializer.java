package com.company.crm.init;

import com.company.crm.entity.*;
import io.jmix.core.Metadata;
import io.jmix.core.UnconstrainedDataManager;
import io.jmix.security.role.assignment.RoleAssignmentRoleType;
import io.jmix.securitydata.entity.RoleAssignmentEntity;
import net.datafaker.Faker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Loads demo data on first application startup. If clients table is not empty, does nothing.
 */
@Component
public class DemoDataInitializer {

    private static final Logger log = LoggerFactory.getLogger(DemoDataInitializer.class);

    @Autowired
    private UnconstrainedDataManager dataManager;
    @Autowired
    private Metadata metadata;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @EventListener(ApplicationReadyEvent.class)
    public void init(ApplicationReadyEvent event) {
        Long cnt = dataManager.loadValue("select count(c) from Client c", Long.class).one();
        if (cnt > 0) {
            log.info("Demo data already present ({} clients). Skipping initialization.", cnt);
            return;
        }
        log.info("Initializing demo data...");

        List<User> users = generateUsers();
        assignRoles(users);
        List<Client> clients = generateClients(60, users);
        generateContacts(clients);
        List<Order> orders = generateOrders(clients);
        List<Invoice> invoices = generateInvoices(orders);
        generatePayments(invoices);

        log.info("Demo data initialization finished: clients={} contacts={} orders={} invoices={} payments={}",
                clients.size(),
                dataManager.loadValue("select count(c) from Contact c", Long.class).one(),
                dataManager.loadValue("select count(o) from Order_ o", Long.class).one(),
                dataManager.loadValue("select count(i) from Invoice i", Long.class).one(),
                dataManager.loadValue("select count(p) from Payment p", Long.class).one()
        );
    }

    private List<User> generateUsers() {
        User user;
        List<User> result = new ArrayList<>();

        user = dataManager.create(User.class);
        user.setUsername("alice");
        user.setPassword(createPassword());
        user.setFirstName("Alice");
        user.setLastName("Brown");
        result.add(dataManager.save(user));

        user = dataManager.create(User.class);
        user.setUsername("james");
        user.setPassword(createPassword());
        user.setFirstName("James");
        user.setLastName("Wilson");
        result.add(dataManager.save(user));

        user = dataManager.create(User.class);
        user.setUsername("mary");
        user.setPassword(createPassword());
        user.setFirstName("Mary");
        user.setLastName("Jones");
        result.add(dataManager.save(user));

        user = dataManager.create(User.class);
        user.setUsername("linda");
        user.setPassword(createPassword());
        user.setFirstName("Linda");
        user.setLastName("Evans");
        result.add(dataManager.save(user));

        user = dataManager.create(User.class);
        user.setUsername("susan");
        user.setPassword(createPassword());
        user.setFirstName("Susan");
        user.setLastName("Baker");
        result.add(dataManager.save(user));

        user = dataManager.create(User.class);
        user.setUsername("bob");
        user.setPassword(createPassword());
        user.setFirstName("Robert");
        user.setLastName("Taylor");
        result.add(dataManager.save(user));

        return result;
    }

    private void assignRoles(List<User> users) {
        for (User user : users) {
            boolean isManager = Arrays.asList("alice", "james").contains(user.getUsername());

            RoleAssignmentEntity roleAssignment = dataManager.create(RoleAssignmentEntity.class);
            roleAssignment.setUsername(user.getUsername());
            roleAssignment.setRoleCode(isManager? "manager" : "employee");
            roleAssignment.setRoleType(RoleAssignmentRoleType.RESOURCE);
            dataManager.save(roleAssignment);
        }
    }

    private List<Client> generateClients(int count, List<User> users) {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        var faker = new Faker();
        List<Client> result = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            Client client = metadata.create(Client.class);
            client.setName(faker.company().name());
            client.setFullName(faker.company().name() + " " + faker.company().suffix());
            client.setAddress(faker.address().fullAddress());
            client.setType(ClientType.values()[random.nextInt(ClientType.values().length)]);
            client.setVatNumber(randomVatLike(random));
            client.setRegNumber("REG-" + (1000 + random.nextInt(9000)) + (i % 10));
            client.setWebsite("https://" + faker.internet().domainName());
            client.setAccountManager(users.get(random.nextInt(users.size())));

            result.add(dataManager.save(client));
        }
        log.info("Generated {} clients", result.size());
        return result;
    }

    private void generateContacts(List<Client> clients) {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        var faker = new Faker();
        List<Contact> toSave = new ArrayList<>();
        for (Client client : clients) {
            int n = random.nextInt(1, 4); // 1..3
            for (int i = 0; i < n; i++) {
                Contact contact = metadata.create(Contact.class);
                contact.setClient(client);
                String first = faker.name().firstName();
                String last = faker.name().lastName();
                contact.setPerson(first + " " + last);
                contact.setPosition(randomPosition(random));
                LocalDate start = randomDateWithinYears(2, random);
                contact.setStartDate(start);
                if (random.nextBoolean()) {
                    contact.setFinishDate(start.plusMonths(random.nextInt(1, 18)));
                }
                String domain = domainFromWebsite(client.getWebsite());
                String email = (slug(first) + "." + slug(last) + "@" + domain).toLowerCase(Locale.ROOT);
                contact.setEmail(email);
                contact.setPhone(faker.phoneNumber().cellPhone());
                toSave.add(contact);
            }
        }
        dataManager.save(toSave.toArray());
        log.info("Generated {} contacts", toSave.size());
    }

    private List<Order> generateOrders(List<Client> clients) {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        List<Order> result = new ArrayList<>();
        for (Client client : clients) {
            int n = random.nextInt(0, 9); // 0..8
            for (int i = 0; i < n; i++) {
                Order order = metadata.create(Order.class);
                order.setClient(client);
                LocalDate date = randomDateWithinYears(2, random);
                order.setDate(date);
                order.setQuote("Q-" + date.getYear() + "-" + (1000 + random.nextInt(9000)));
                if (random.nextBoolean()) order.setComment(sampleComment(random));
                BigDecimal total = BigDecimal.valueOf(100 + random.nextInt(9_000)).setScale(2);
                order.setTotal(total);
                if (random.nextInt(4) == 0) {
                    // discount either value or percent
                    if (random.nextBoolean()) order.setDiscountValue(BigDecimal.valueOf(random.nextInt(10, 100)));
                    else order.setDiscountPercent(BigDecimal.valueOf(random.nextInt(1, 30)));
                }
                order.setStatus(OrderStatus.values()[random.nextInt(OrderStatus.values().length)]);
                result.add(dataManager.save(order));
            }
        }
        log.info("Generated {} orders", result.size());
        return result;
    }

    private List<Invoice> generateInvoices(List<Order> orders) {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        List<Invoice> result = new ArrayList<>();
        for (Order order : orders) {
            if (random.nextInt(100) < 70) { // 70% orders have an invoice
                Invoice invoice = metadata.create(Invoice.class);
                invoice.setClient(order.getClient());
                invoice.setOrder(order);
                LocalDate date = order.getDate() != null ? order.getDate().plusDays(random.nextInt(1, 15)) : randomDateWithinYears(2, random);
                invoice.setDate(date);
                invoice.setDueDate(date.plusDays(random.nextInt(7, 45)));
                BigDecimal subtotal = order.getTotal() != null ? order.getTotal() : BigDecimal.valueOf(500 + random.nextInt(5000));
                invoice.setSubtotal(subtotal);
                BigDecimal vat = subtotal.multiply(BigDecimal.valueOf(0.2)).setScale(2, BigDecimal.ROUND_HALF_UP);
                invoice.setVat(vat);
                invoice.setTotal(subtotal.add(vat));
                invoice.setStatus(InvoiceStatus.values()[random.nextInt(InvoiceStatus.values().length)]);
                result.add(dataManager.save(invoice));
            }
        }
        log.info("Generated {} invoices", result.size());
        return result;
    }

    private void generatePayments(List<Invoice> invoices) {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        List<Payment> result = new ArrayList<>();
        for (Invoice invoice : invoices) {
            int n = random.nextInt(0, 5); // 0..4
            BigDecimal remaining = invoice.getTotal() != null ? invoice.getTotal() : BigDecimal.ZERO;
            for (int i = 0; i < n && remaining.compareTo(BigDecimal.ZERO) > 0; i++) {
                Payment payment = metadata.create(Payment.class);
                payment.setInvoice(invoice);
                LocalDate date = (invoice.getDate() != null ? invoice.getDate() : randomDateWithinYears(2, random)).plusDays(random.nextInt(1, 60));
                payment.setDate(date);
                BigDecimal part = remaining.multiply(BigDecimal.valueOf(0.25 + random.nextDouble(0.5))).setScale(2, BigDecimal.ROUND_HALF_UP);
                if (part.compareTo(remaining) > 0) part = remaining;
                payment.setAmount(part);
                remaining = remaining.subtract(part);
                result.add(dataManager.save(payment));
            }
        }
        log.info("Generated {} payments", result.size());
    }

    private String createPassword() {
        return passwordEncoder.encode("1");
    }

    private String randomVatLike(ThreadLocalRandom r) {
        String[] cc = {"US","GB","DE","FR","CA","AU","IE","NL","SE","NO","ES","IT","PL","JP","SG"};
        String country = cc[r.nextInt(cc.length)];
        int part1 = 10 + r.nextInt(89);
        int part2 = 1000000 + r.nextInt(9000000);
        return country + part1 + "-" + part2;
    }


    private String randomPosition(ThreadLocalRandom r) {
        String[] pos = {"CTO", "CIO", "Head of Procurement", "Operations Manager", "HR Lead", "Finance Manager", "IT Specialist", "Project Manager"};
        return pos[r.nextInt(pos.length)];
    }

    private String sampleComment(ThreadLocalRandom r) {
        String[] comments = {
                "Urgent delivery requested.",
                "Include extended warranty.",
                "Customer asked for bulk discount.",
                "Repeat order based on last year's contract.",
                "Requires onsite installation.",
                "Custom branding needed.",
                "Ship in two batches.",
                "Coordinate with procurement before invoicing."
        };
        return comments[r.nextInt(comments.length)];
    }

    private LocalDate randomDateWithinYears(int years, ThreadLocalRandom r) {
        LocalDate now = LocalDate.now();
        LocalDate start = now.minusYears(years);
        long days = now.toEpochDay() - start.toEpochDay();
        return start.plusDays(r.nextLong(days + 1));
        
    }

    private String slug(String s) {
        if (s == null) return "user";
        return s.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", "-").replaceAll("(^-|-$)", "");
    }

    private String domainFromWebsite(String site) {
        if (site == null || site.isBlank()) return "example.com";
        String d = site.replaceFirst("https?://", "");
        int idx = d.indexOf('/');
        return idx > 0 ? d.substring(0, idx) : d;
    }
}
