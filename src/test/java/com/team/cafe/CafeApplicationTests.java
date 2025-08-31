package com.team.cafe;

import com.team.cafe.login.LoginRepository;
import com.team.cafe.test.TestData;
import com.team.cafe.test.TestRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;

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
    private LoginRepository loginRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void testJpa01() {
        SiteUser users = new SiteUser();
        users.setUsername("userid");
        users.setEmail("email@email.com");
        users.setPassword(passwordEncoder.encode("1234"));
        users.setNickname("어넝");
        this.loginRepository.save(users);

        SiteUser users2 = new SiteUser();
        users2.setUsername("userid2");
        users2.setEmail("email2@email.com");
        users2.setPassword(passwordEncoder.encode("1234"));
        users2.setNickname("어넝2");
        this.loginRepository.save(users2);

    }

}
