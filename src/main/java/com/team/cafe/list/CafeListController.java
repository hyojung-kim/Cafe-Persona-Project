package com.team.cafe.list;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@RequestMapping("/cafe")
@RequiredArgsConstructor
@Controller
public class CafeListController {

    private final CafeListService cafeListService;

    @GetMapping("/list")
    public String list(Model model) {
        List<Cafe> cafes = cafeListService.getAllCafes();  // 일단 전부
        model.addAttribute("cafes", cafes);
        return "cafe/cafe_list";             // ↓ 이 파일로 렌더링
    }
}
