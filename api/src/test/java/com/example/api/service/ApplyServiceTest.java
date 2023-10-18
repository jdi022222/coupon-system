package com.example.api.service;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.example.api.repository.CouponRepository;

@SpringBootTest
class ApplyServiceTest {

	@Autowired
	private ApplyService applyService;

	@Autowired
	private CouponRepository couponRepository;

	@Test
	public void 한번만응모() throws Exception{
		applyService.apply(1L);

		long count = couponRepository.count();

		Assertions.assertThat(count).isEqualTo(1);
	}

	@Test
	public void 여러명응모() throws Exception {
		int threadCount = 1000;
		ExecutorService executorService = Executors.newFixedThreadPool(32); // 병렬 작업을 도와주는 java api
		CountDownLatch countDownLatch = new CountDownLatch(threadCount); // 다른 스레드의 작업이 끝날 때까지 기다리는 클래스

		for (int i = 0; i < threadCount; i++) {
			long userId = i;
			executorService.submit(() -> {
					try {
						applyService.apply(userId);
					} finally {
						countDownLatch.countDown();
					}
				}
			);
		}

		countDownLatch.await();

		long count = couponRepository.count();

		Assertions.assertThat(count).isEqualTo(100); // race condition 발생!
	}

}