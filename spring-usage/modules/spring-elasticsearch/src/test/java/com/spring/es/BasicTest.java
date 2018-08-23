package com.spring.es;

import com.spring.data.domain.CustomerRelation;
import com.spring.elasticsearch.repository.BasicRepository;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;

@RunWith(SpringRunner.class)
@ContextConfiguration("classpath:spring-es.xml")
public class BasicTest {
    @Autowired
    private BasicRepository basicRepository;

    private CustomerRelation customerRelation;

    @Before
    public void beforeTest() {
        CustomerRelation customerRelation = new CustomerRelation();
//        customerRelation.setId(RandomUtils.nextLong(100, 2000));
        customerRelation.setAccountType(1);
        customerRelation.setAddress("天安门外大街" + RandomUtils.nextInt(1, 30) + "号");
        customerRelation.setDiscountPercent(RandomUtils.nextDouble(0.5, 1));
        customerRelation.setDiscountBeginTime(DateUtils.addDays(new Date(), -RandomUtils.nextInt(100, 300)));
        customerRelation.setDiscountEndTime(DateUtils.addDays(customerRelation.getDiscountBeginTime(), 365));
        customerRelation.setCompanyName("测试123");
        customerRelation.setCustomerName("didi66");
        customerRelation.setOriginTransporterId(RandomUtils.nextInt(100, 3000));
        customerRelation.setOriginTransporterName(customerRelation.getOriginTransporterId() + "揽收员");
        customerRelation.setUserPin(String.valueOf(RandomUtils.nextInt(10000, 50000)));
        customerRelation.setTelephone("18688912" + RandomUtils.nextInt(1000, 8000));
        customerRelation.setIdCardNo("12010119911215" + RandomUtils.nextInt(8000, 9000));

        this.customerRelation = customerRelation;
    }

    @Test
    public void test1(){
        Object save = basicRepository.save(customerRelation);
        System.out.println(save);
    }
}
