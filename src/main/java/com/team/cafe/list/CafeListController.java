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
                       @RequestParam(required = false) String kw,
                       @RequestParam(defaultValue = "createdAt") String sort,
                       @RequestParam(defaultValue = "desc") String dir,
                       Model model) {

        Page<Cafe> paging = cafeListService.getCafes(kw, page, size, sort, dir);
        model.addAttribute("paging", paging);
        model.addAttribute("kw", kw);       // 입력값 유지용
        model.addAttribute("size", size);   // 페이징 링크에 전달
        model.addAttribute("sort", sort);
        model.addAttribute("dir", dir);
        return "cafe/cafe_list";
    }
}
