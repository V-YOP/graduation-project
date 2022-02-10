package yukina.final_design.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Arrays;

// 计算每个函数执行时间的切面
@Aspect
@Component//使用spring容器进行管理
@Slf4j
public class ServiceAspect {
    /**
     * 首先定义一个切点
     */
    @org.aspectj.lang.annotation.Pointcut("@annotation(yukina.final_design.aspect.CostTime)")
    public void countTime() {
    }
    @Around("countTime()")
    public Object doAround(ProceedingJoinPoint joinPoint) {
        Object obj = null;
        try {
            long beginTime = System.currentTimeMillis();
            obj = joinPoint.proceed();
            //获取方法名称
            String methodName = joinPoint.getSignature().getName();
            //获取类名称
            String className = joinPoint.getSignature().getDeclaringTypeName();
            log.info("类:[{}]，方法:[{}]耗时时间为:[{}]", className, methodName, (System.currentTimeMillis() - beginTime)/1000 + "秒");
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
        return obj;
    }
}