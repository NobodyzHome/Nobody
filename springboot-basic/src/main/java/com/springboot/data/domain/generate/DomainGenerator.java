package com.springboot.data.domain.generate;

import com.springboot.data.domain.BaseUser;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.time.DateUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class DomainGenerator {

    private static String[] XING = {"张", "李", "王", "赵", "马", "刘", "蒋", "魏", "肖", "高", "欧阳", "东方", "晴明"};
    private static String[] MING = {"大山", "二狗", "狗蛋", "美亚", "丽娜", "蛋蛋", "山炮", "由美", "天龙", "莫愁"};

    public static BaseUser generateUser(Integer id, boolean saveId) {
        BaseUser user = new BaseUser();
        user.setId(saveId ? String.valueOf(id) : null);
        user.setFirstName(randomFromArray(XING));
        user.setSecondName(randomFromArray(MING));
        user.setCode(String.format("My [FIRST] name is %s，and my [secoND] NAME is %s!", user.getFirstName(), user.getSecondName()));
        user.setPassword(RandomStringUtils.random(30, true, true));
        user.setIsValid(RandomUtils.nextBoolean());
        user.setAge(RandomUtils.nextInt(20, 50));
        user.setCreateTime(new Date());
        user.setUpdateTime(DateUtils.addDays(user.getCreateTime(), user.getAge() * (user.getAge() > 30 ? 1 : -1)));
        user.setBirthDay(DateUtils.addYears(DateUtils.truncate(new Date(), Calendar.DAY_OF_MONTH), -user.getAge()));

        return user;
    }

    public static List<BaseUser> generateUsers(Integer count, boolean saveId) {
        List<Integer> list = new ArrayList<>(count);
        for (int pos = 1; pos <= count; pos++) {
            list.add(RandomUtils.nextInt(1000, 10000));
        }
        List<BaseUser> baseUserList = list.stream().map(id -> generateUser(id, saveId)).collect(Collectors.toList());
        return baseUserList;
    }

    private static String randomFromArray(String[] array) {
        return array[RandomUtils.nextInt(0, array.length)];
    }
}
