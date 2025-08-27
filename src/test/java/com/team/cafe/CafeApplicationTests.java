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

import static org.junit.jupiter.api.Assertions.assertNotNull;

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
        SiteUser user = userService.join( "test", "test@test.com", "1234");
        assertNotNull(user);

    }
}
