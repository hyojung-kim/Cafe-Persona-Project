package com.team.cafe;

import com.team.cafe.test.TestData;
import com.team.cafe.test.TestRepository;
import com.team.cafe.user.SiteUser;
import com.team.cafe.user.UserRepository;
import com.team.cafe.user.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Optional;

@SpringBootTest
class CafeApplicationTests {

    @Autowired
    private TestRepository testRepository;


    @Test
    void testJpa() {
        TestData test = new TestData();
        test.setDb("test용");

        this.testRepository.save(test);  // 첫번째 질문 저장

    }

    @Autowired
    private UserRepository userRepository;
    private UserService userService;

    @Test
    void join() {
        SiteUser siteUser = userService.join(null, "test", "1234", "test@test.com");
    }
}
