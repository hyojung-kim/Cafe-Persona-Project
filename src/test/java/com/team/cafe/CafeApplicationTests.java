package com.team.cafe;

import com.team.cafe.test.TestData;
import com.team.cafe.test.TestRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

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

}
