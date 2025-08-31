package com.team.cafe.controller;

import com.team.cafe.dto.ReviewCreateRequest;
import com.team.cafe.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.Principal;
import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    @PreAuthorize("isAuthenticated()")
    @PostMapping
    public String create(@Valid @ModelAttribute ReviewCreateRequest req,
                         @RequestParam(name = "images", required = false)List<MultipartFile> images,
                         Principal principal) throws IOException {
        reviewService.createReview(principal.getName(), req, images);
        return "redirect:/cafes/" + req.cafeId();
    }

}
