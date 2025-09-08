package com.team.cafe.businessuser.sj;

import com.team.cafe.businessuser.sj.owner.cafe.CafeManageService;
import com.team.cafe.user.sjhy.SiteUser;
import com.team.cafe.user.sjhy.UserService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.time.LocalDate;

@RequiredArgsConstructor
@Controller
public class MypageController {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final CafeManageService cafeManageService;
    private final BusinessRepository businessRepository;

    // 현재 로그인한 사용자 가져오기
    private SiteUser getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal().equals("anonymousUser")) {
            return null; // 로그인 안 됨
        }

        Object principal = auth.getPrincipal();
        String username;

        if (principal instanceof UserDetails userDetails) {
            username = userDetails.getUsername();
        } else {
            username = principal.toString();
        }

        return userService.getUser(username); // DB에서 SiteUser 조회
    }

    // 마이페이지 메인
    @GetMapping("/mypage")
    public String mypage(Model model) {
        SiteUser siteUser = getCurrentUser();
        if (siteUser == null) {
            return "redirect:/user/login";
        }

        // ▼ 변경: 템플릿에서 user.businessUser 판별할 수 있게 user 객체를 모델에 넣기
        model.addAttribute("user", siteUser);
        model.addAttribute("isBusiness", siteUser.getBusinessUser() != null); // ✅ 추가


        // (username만 필요 없다면 아래 줄은 지워도 됩니다)
        // model.addAttribute("username", siteUser.getUsername());

        return "mypage/main";
    }


    // 계정 관리 화면
    @GetMapping("/mypage/account")
    public String accountPage(Model model) {
        SiteUser siteUser = getCurrentUser();
        if (siteUser == null) {
            return "redirect:/user/login";
        }

        model.addAttribute("user", siteUser);
        return "mypage/account";
    }



    // 계정 관리 진입 전 - 비밀번호 확인 페이지
    @GetMapping("/mypage/verify_password")
    public String verifyPasswordPage() {
        SiteUser siteUser = getCurrentUser();
        if (siteUser == null) {
            return "redirect:/user/login";
        }
        return "mypage/verify_password"; // 비밀번호 입력 화면
    }

    // 비밀번호 확인 처리
    @PostMapping("/mypage/verify_password")
    public String verifyPassword(@RequestParam String password, Model model) {
        SiteUser siteUser = getCurrentUser();
        if (siteUser == null) {
            return "redirect:/user/login";
        }

        if (!passwordEncoder.matches(password, siteUser.getPassword())) {
            model.addAttribute("error", "비밀번호가 일치하지 않습니다.");
            return "mypage/verify_password";
        }

        // 비밀번호 확인 성공 → 계정 관리 화면으로 이동
        return "redirect:/mypage/account";
    }


    // 계정 관리 수정 처리
    @PostMapping("/mypage/account/update")
    public String updateAccount(@RequestParam String nickname,
                                @RequestParam String email) {
        SiteUser siteUser = getCurrentUser();
        if (siteUser == null) {
            return "redirect:/user/login";
        }

        siteUser.setNickname(nickname);
        siteUser.setEmail(email);
        userService.save(siteUser); // 저장 메서드가 UserService에 필요합니다

        return "redirect:/mypage";
    }

    // 비밀번호 변경
    @PostMapping("/mypage/account/update-password")
    @ResponseBody
    public String updatePassword(HttpServletRequest request, HttpServletResponse response,
                                 @RequestParam String newPassword) throws ServletException {
        SiteUser siteUser = getCurrentUser();
        if (siteUser == null) {
            return "fail"; // 로그인 안 됨
        }

        // 비밀번호 암호화 후 저장
        siteUser.setPassword(passwordEncoder.encode(newPassword));
        userService.save(siteUser);

        // 세션 종료 및 로그아웃 처리
        request.logout(); // Spring Security가 처리하는 로그아웃

        return "success";
    }


    // 휴대폰 번호 변경
    @PostMapping("/mypage/account/update-phone")
    @ResponseBody
    public String updatePhone(@RequestParam String phone) {
        SiteUser siteUser = getCurrentUser();
        if (siteUser == null) {
            return "fail"; // 로그인 안 됨
        }

        // 휴대폰 번호 저장
        siteUser.setPhone(phone);
        userService.save(siteUser);

        return "success";
    }

    // 사업자만 접근 가능한 가드
    @GetMapping("/mypage/cafe/manage")
    public String cafeManage(Model model) {
        SiteUser user = getCurrentUser();
        if (user == null) return "redirect:/user/login";
        if (user.getBusinessUser() == null) return "redirect:/mypage?denied=biz";


        // 서버 가드: 사업자만 통과
        if (user.getBusinessUser() == null) {
            return "redirect:/mypage?denied=biz";
        }
        Business business = businessRepository.findByUserId(user.getId()).orElse(null);
        model.addAttribute("user", user);
        model.addAttribute("business", business);   // 헤더 버튼 분기에 사용
        return "mypage/cafe_manage"; // 임시 페이지 뷰 이름
    }

    // GET: 등록 폼
    @GetMapping("/mypage/cafe/register")
    public String cafeRegister(Model model) {
        SiteUser user = getCurrentUser();
        if (user == null) return "redirect:/user/login";
        if (user.getBusinessUser() == null) return "redirect:/mypage?denied=biz";

        // 이미 보유하면 관리로 돌리기
        if (businessRepository.existsByUserId(user.getId())) {
            return "redirect:/mypage/cafe/manage";
        }
        model.addAttribute("user", user);
        return "mypage/cafe_register";
    }

    // POST: 저장
    @PostMapping("/mypage/cafe/register")
    public String saveCafeRegister(
            @RequestParam String companyName,
            @RequestParam String businessNumber,
            @RequestParam(required=false) String representativeName,
            @RequestParam(required=false) String representativeEmail,
            @RequestParam(required=false) String representativePhone,
            @RequestParam(required=false) String address,
            @RequestParam(required=false) String description,
            RedirectAttributes ra
    ) {
        SiteUser user = getCurrentUser();
        if (user == null) return "redirect:/user/login";
        if (user.getBusinessUser() == null) return "redirect:/mypage?denied=biz";

        try {
            cafeManageService.createBusiness(user, companyName, businessNumber,
                    representativeName, representativeEmail, representativePhone,
                    address, description);
            ra.addFlashAttribute("toast", "사업장이 등록되었습니다.");
            return "redirect:/mypage/cafe/manage";
        } catch (DuplicateBusinessNumberException e) {
            ra.addFlashAttribute("error", "이미 사용 중인 사업자등록번호입니다.");
            return "redirect:/mypage/cafe/register";
        }
    }



}
