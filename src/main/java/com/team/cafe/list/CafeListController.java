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
                       @RequestParam(required = false) Boolean parking,  // true면 가능만
                       @RequestParam(required = false) Boolean openNow,  // true면 영업중만
                       Model model) {

        var paging = cafeListService.getCafes(kw, page, size, sort, dir, parking, openNow);

        model.addAttribute("paging", paging);
        model.addAttribute("kw", kw);
        model.addAttribute("size", size);
        model.addAttribute("sort", sort);
        model.addAttribute("dir", dir);
        model.addAttribute("parking", parking);
        model.addAttribute("openNow", openNow);
        return "cafe/cafe_list";
    }
}
