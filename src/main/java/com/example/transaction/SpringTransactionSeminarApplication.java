package com.example.transaction;

import com.example.transaction.service.DeclarativeTxOrderService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class SpringTransactionSeminarApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpringTransactionSeminarApplication.class, args);
	}

    @Bean
    CommandLineRunner runner(DeclarativeTxOrderService orderService) {
        return args -> {
            // 여기에 브레이크포인트도 설정 가능
            System.out.println("서비스 호출 시작");
            orderService.placeOrderWithAnno(1L, 5, false);
            System.out.println("서비스 호출 완료");
        };
    }
}
