package com.team.cafe.Find;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class FindController {
    public String findId() {
        return "find_form";
    }
}
