package com.studyflow;

import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.server.PWA;
import com.vaadin.flow.theme.Theme;
import com.vaadin.flow.theme.lumo.Lumo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main Spring Boot Application Entry Point for StudyFlow.
 * Implements AppShellConfigurator to enable Vaadin's Lumo Dark theme.
 */
@SpringBootApplication
@Theme(value = "studyflow", variant = Lumo.DARK)
@PWA(name = "StudyFlow - Student Study Planner", shortName = "StudyFlow")
public class StudyFlowApplication implements AppShellConfigurator {

    public static void main(String[] args) {
        SpringApplication.run(StudyFlowApplication.class, args);
    }
}
