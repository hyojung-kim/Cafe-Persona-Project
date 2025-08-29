package com.team.cafe.list;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@RequestMapping("/cafe")
@RequiredArgsConstructor
@Controller
public class CafeListController {

    private final CafeListService cafeListService;

    @GetMapping("/list")
    public String list(@RequestParam(defaultValue = "0") int page,
                       @RequestParam(defaultValue = "3") int size,
                       Model model) {
        Page<Cafe> paging = cafeListService.getCafePage(page, size);
        model.addAttribute("paging", paging);
        return "cafe/cafe_list";
    }
}
