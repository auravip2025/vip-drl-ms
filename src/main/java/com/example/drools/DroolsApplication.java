package com.example.drools;

import org.kie.api.KieServices;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;

import com.example.drools.model.Order;

/**
 * Main application class demonstrating Drools rules engine.
 */
public class DroolsApplication {

    public static void main(String[] args) {
        System.out.println("=== Drools Rules Engine Demo ===\n");

        // Initialize KIE services
        KieServices kieServices = KieServices.Factory.get();
        KieContainer kieContainer = kieServices.getKieClasspathContainer();
        KieSession kieSession = kieContainer.newKieSession("ksession-rules");

        try {
            // Create sample orders
            Order order1 = new Order("ORD-001", "Electronics", 150.00);
            Order order2 = new Order("ORD-002", "Books", 45.00);
            Order order3 = new Order("ORD-003", "Electronics", 500.00);
            Order order4 = new Order("ORD-004", "Clothing", 200.00);

            // Insert facts into the session
            kieSession.insert(order1);
            kieSession.insert(order2);
            kieSession.insert(order3);
            kieSession.insert(order4);

            // Fire all rules
            int rulesFired = kieSession.fireAllRules();
            System.out.println("\nTotal rules fired: " + rulesFired);

            // Display results
            System.out.println("\n=== Order Results ===");
            System.out.println(order1);
            System.out.println(order2);
            System.out.println(order3);
            System.out.println(order4);

        } finally {
            kieSession.dispose();
        }
    }
}
