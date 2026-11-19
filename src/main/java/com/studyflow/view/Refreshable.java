package com.studyflow.view;

/**
 * Interface implemented by views that need their data refreshed
 * when global events occur (such as adding a task from the header).
 */
public interface Refreshable {
    void refresh();
}
