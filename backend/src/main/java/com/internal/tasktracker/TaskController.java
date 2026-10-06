package com.internal.tasktracker;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@CrossOrigin(origins = "http://localhost:5173")
public class TaskController {

    private static final int MAX_PAGE_SIZE = 100;

    private final TaskRepository taskRepository;

    public TaskController(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    @GetMapping("/api/tasks")
    public ResponseEntity<?> searchTasks(
            @RequestParam(required = false, defaultValue = "") String q,
            @RequestParam(required = false) String status,
            @RequestParam(required = false, defaultValue = "1") int page,
            @RequestParam(required = false, defaultValue = "10") int pageSize) {

        // Normalize query input
        String query = q == null ? "" : q.trim();
        String searchTerm = "%" + query.toLowerCase() + "%";

        // Parse status filter: invalid value is a client error (400), not a server error (500)
        String normalizedStatus = null;
        if (status != null && !status.isEmpty()) {
            try {
                normalizedStatus = TaskStatus.valueOf(status.trim().toUpperCase()).name();
            } catch (IllegalArgumentException e) {
                Map<String, Object> error = new LinkedHashMap<>();
                error.put("error", "Invalid status: " + status);
                error.put("allowed", Arrays.toString(TaskStatus.values()));
                return ResponseEntity.badRequest().body(error);
            }
        }

        // Keep pagination values in a safe range
        page = Math.max(1, page);
        pageSize = Math.min(Math.max(1, pageSize), MAX_PAGE_SIZE);

        System.out.println("[TaskController] q=\"" + query + "\" status=" + normalizedStatus
                + " page=" + page + " pageSize=" + pageSize);

        List<Task> allResults = taskRepository.searchTasks(searchTerm, normalizedStatus);

        long start = (long) (page - 1) * pageSize;
        List<Task> pageResults;
        if (start >= allResults.size()) {
            pageResults = Collections.emptyList();
        } else {
            int end = (int) Math.min(start + pageSize, allResults.size());
            pageResults = allResults.subList((int) start, end);
        }

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("items", pageResults);
        response.put("total", allResults.size());
        response.put("page", page);
        response.put("pageSize", pageSize);

        return ResponseEntity.ok(response);
    }
}