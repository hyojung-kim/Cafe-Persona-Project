package com.team.cafe.bookmark;

import com.team.cafe.user.sjhy.SiteUser;
import com.team.cafe.user.sjhy.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.security.Principal;
import java.util.List;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
public class BookmarkController {

    private final BookmarkService bookmarkService;
    private final UserRepository userRepository;

    @GetMapping("/mypage/my/bookmark")
    public String showMyBookmark(Model model, Principal principal) {

        String username = principal.getName();

        SiteUser user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Invalid user: " + username));
        Long userId = user.getId();


        // 로그인한 사용자 ID로 북마크 목록 조회
        List<Bookmark> bookmark = bookmarkService.getBookmarksByUser(userId);
        System.out.println("북마크 조회 결과: " + bookmark);

        model.addAttribute("bookmark", bookmark);
        return "mypage/my_bookmark";
    }
}
