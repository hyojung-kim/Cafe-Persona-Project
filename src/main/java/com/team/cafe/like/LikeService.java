package com.team.cafe.like;


import com.team.cafe.list.Cafe;
import com.team.cafe.list.CafeRepository;
import com.team.cafe.user.SiteUser;
import com.team.cafe.user.SiteUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class LikeService {
    private final CafeRepository cafeRepository;
    private final SiteUserRepository siteUserRepository;


}
