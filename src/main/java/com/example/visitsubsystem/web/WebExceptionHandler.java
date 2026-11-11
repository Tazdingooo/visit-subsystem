package com.example.visitsubsystem.web;

import com.example.visitsubsystem.service.VisitNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.ModelAndView;

@ControllerAdvice
public class WebExceptionHandler {

    @ExceptionHandler(VisitNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ModelAndView notFound(VisitNotFoundException ex) {
        ModelAndView mav = new ModelAndView("error/404");
        mav.addObject("error", ex.getMessage());
        return mav;
    }
}
