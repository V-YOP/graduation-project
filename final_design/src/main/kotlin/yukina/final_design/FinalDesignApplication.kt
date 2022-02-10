package yukina.final_design

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.transaction.annotation.EnableTransactionManagement

@EnableScheduling
@SpringBootApplication
@EnableTransactionManagement
class FinalDesignApplication

fun main(args: Array<String>) {
	runApplication<FinalDesignApplication>(*args)
}
