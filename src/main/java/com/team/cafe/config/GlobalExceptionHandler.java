package com.team.cafe.config;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalStateException.class)
    public String handleIllegalState(IllegalStateException e,
                                     @RequestHeader(value="Referer", required=false) String referer,
                                     RedirectAttributes ra) {
        ra.addFlashAttribute("alert", e.getMessage());
        return "redirect:" + (referer != null ? referer : "/");
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public String handleIllegalArg(IllegalArgumentException e, Model model) {
        model.addAttribute("message", e.getMessage());
        return "error/common";
    }

    @ExceptionHandler(Exception.class)
    public String handleGeneral(Exception e, Model model) {
        model.addAttribute("message", "예기치 못한 오류가 발생했습니다.");
        return "error/common";
    }
}
