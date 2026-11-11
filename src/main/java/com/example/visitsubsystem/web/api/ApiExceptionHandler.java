package com.example.visitsubsystem.web.api;

import com.example.visitsubsystem.service.InconsistentVisitTimestampsException;
import com.example.visitsubsystem.service.InvalidVisitTransitionException;
import com.example.visitsubsystem.service.VisitNotFoundException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(basePackages = "com.example.visitsubsystem.web.api")
public class ApiExceptionHandler {

    @ExceptionHandler(VisitNotFoundException.class)
    public ProblemDetail notFound(VisitNotFoundException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        problem.setTitle("Visit or referenced record not found");
        return problem;
    }

    @ExceptionHandler(InvalidVisitTransitionException.class)
    public ProblemDetail conflict(InvalidVisitTransitionException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
        problem.setTitle("Invalid visit transition");
        return problem;
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ProblemDetail dataConflict(DataIntegrityViolationException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT,
                "The request conflicts with existing data (a visit can only have one call report)");
        problem.setTitle("Data conflict");
        return problem;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail validation(MethodArgumentNotValidException ex) {
        String detail = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .reduce((a, b) -> a + "; " + b)
                .orElse("Invalid request body");
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, detail);
        problem.setTitle("Validation failed");
        return problem;
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail badRequest(IllegalArgumentException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
        problem.setTitle("Invalid request");
        return problem;
    }

    @ExceptionHandler(InconsistentVisitTimestampsException.class)
    public ProblemDetail internal(InconsistentVisitTimestampsException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR,
                "Visit timestamps are inconsistent; the record needs manual attention");
        problem.setTitle("Inconsistent visit data");
        return problem;
    }
}
