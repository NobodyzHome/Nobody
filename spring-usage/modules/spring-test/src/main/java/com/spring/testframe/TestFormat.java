package com.spring.testframe;

import java.text.ChoiceFormat;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.annotation.IfProfileValue;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@ContextConfiguration
@IfProfileValue(name = "user.name", values = { "zhangsan", "maziqiang", "lisi" })
@TestPropertySource(properties = { "datePattern=yyyy-MM-dd", "dateTimePattern=${datePattern} kk:mm:ss",
		"numberPattern=No:#,###.##!" })
public class TestFormat {

	@Configuration
	static class ContextConfiguration {
	}

	@Value("${datePattern}")
	private String datePattern;

	@Value("${dateTimePattern}")
	private String dateTimePattern;

	@Value("${numberPattern}")
	private String numberPattern;

	@Test
	public void testDateFormat() throws ParseException {
		// DateFormat是按照指定的格式，来将一个字符串转换成Date对象，或将一个Date对象格式化成指定样式的字符串
		DateFormat dateFormat = new SimpleDateFormat(datePattern);
		Date date1 = dateFormat.parse("2017-10-21");

		DateFormat dateTimeFormat = new SimpleDateFormat(dateTimePattern);
		Date date2 = dateTimeFormat.parse("2017-10-21 10:20:30");

		System.out.println(date1.compareTo(date2));
		System.out.println(dateFormat.format(date1));
		System.out.println(dateTimeFormat.format(date2));
	}

	@Test
	public void testNumberFormat() throws ParseException {
		// NumberFormat是按照指定的格式，来将一个字符串转换成Number对象，或将一个Number对象格式化成指定样式的字符串
		NumberFormat formatter = new DecimalFormat(numberPattern);
		Number number = formatter.parse("No:13,321.239!");
		System.out.println(number);
		System.out.println(formatter.format(number));
	}

	@Test
	public void testChoiceFormat() throws ParseException {
		/*
		 * ChoiceFormat用于根据传入值的大小，来选择一个要输出的字符串。
		 * 
		 * ChoiceFormat的样式字符串“2000<穷逼|5000#一般水平|8000#中等|12000<还不错|20000<牛逼啊老铁”
		 * 可以这么理解
		 * 
		 * 我们可以把这个字符串分为五个表达式A,B,C,D,E。A代表“2000<穷逼”，当输入的值大于2000的话，返回“穷逼”字符串;
		 * B代表“5000#一般水平”，当输入的值大于等于5000时，返回“一般水平”字符串。
		 * 注意：也就是“2000<”代表表达式要求值大于2000，而“5000#”代表表达式要求值大于等于5000
		 * 
		 * 当有一个值（假设为X）传入给ChoiceFormat后，会有以下分支：
		 * 
		 * 一、当X不满足表达式A时，返回表达式A的对应的字符串
		 * 
		 * 二、 1.满足B，继续判断表达式C；不满足B，则返回B的前一个表达式A对应的字符串
		 * 2.满足C，继续判断表达式D；不满足C，则返回C的前一个表达式B对应的字符串
		 * 3.满足D，继续判断表达式E；不满足D，则返回D的前一个表达式C对应的字符串
		 * 4.满足E，那么由于表达式E已经是最后一个表达式了，因此返回表达式E对应的字符串；不满足E，则返回E的前一个表达式D对应的字符串
		 * 
		 * 以传入的值为8000来举例：
		 * 
		 * 1.8000满足表达式"2000<穷逼"（因为8000>2000），继续向后判断
		 * 2.8000满足表达式“5000#一般水平”（因为8000>=5000）,继续向后判断
		 * 3.8000满足表达式“8000#中等”（因为8000>=8000）,继续向后判断
		 * 4.8000不满足表达式“12000<还不错”（因为8000<12000），那么则使用上一个表达式"8000#中等"对应的字符串，
		 * 因此返回的结果为"中等"
		 */
		ChoiceFormat choiceFormat1 = new ChoiceFormat("2000<穷逼|5000#一般水平|8000#中等|12000<还不错|20000<牛逼啊老铁");
		String number = choiceFormat1.format(20000);
		System.out.println(number);

		ChoiceFormat choiceFormat2 = new ChoiceFormat("0#刚出生|0<小宝宝|2#小坏蛋|5<小朋友");
		System.out.println(choiceFormat2.format(6));

		ChoiceFormat choiceFormat3 = new ChoiceFormat("0#鸭蛋|0<不及格|60#刚刚及格|70#凑活|85<挺好|90#优秀|100#满分|100<超神");

		System.out.println(choiceFormat3.format(-10)); // 鸭蛋
		System.out.println(choiceFormat3.format(0)); // 鸭蛋
		System.out.println(choiceFormat3.format(55)); // 不及格
		System.out.println(choiceFormat3.format(60)); // 刚刚及格
		System.out.println(choiceFormat3.format(70)); // 凑活
		System.out.println(choiceFormat3.format(82)); // 凑活
		System.out.println(choiceFormat3.format(85)); // 凑活
		System.out.println(choiceFormat3.format(86)); // 挺好
		System.out.println(choiceFormat3.format(90)); // 优秀
		System.out.println(choiceFormat3.format(99)); // 优秀
		System.out.println(choiceFormat3.format(100)); // 满分
		System.out.println(choiceFormat3.format(110)); // 超神
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testMessageFormat() {
		/*
		 * MessageFormat用于对一个包含“变量”样式的字符串进行格式化，将传入的值替换到“变量”上，
		 * 同时还可以对传入的值先使用NumberFormat、DateFormat、ChoiceFormat进行格式化，再将格式化后的内容替换到“
		 * 变量”上。
		 * 
		 * 在MessageFormat的样式字符串中可以包含变量。变量的格式是:{index,formatType,formatStyle}，
		 * 其中formatType和formatStyle是一个变量的可选内容。
		 * 
		 * 1.index是变量的索引，从0开始
		 * 
		 * 2.formatType用于告知使用哪个Format类的实例来格式化为变量传入的值。formatType的值可以是number,date,
		 * time,choice，分别使用NumberFormat、DateFormat、DateFormat、
		 * ChoiceFormat来格式化给变量传入的值。
		 * 
		 * 3.formatStyle用于为选好的Format类的实例提供样式字符串
		 * 
		 * 例如：{0,number,No:#,###.#!}代表告知MessageFormat，对传入给第一个变量的值，
		 * 使用NumberFormat来进行格式化，并且给NumberFormat赋值的样式字符串（即pattern属性）为No:#,###.#!
		 * 
		 * 像下例中，如果变量没有给出formatType，即{2}，那么就代表直接把变量的值替换到变量的位置，如果变量给出了formatType，即
		 * {1,number,第#号顾客}，那么不仅会把变量的值替换到变量的位置，还会把变量的值按照指定的Format格式化，将格式化后的内容替换到
		 * 变量的位置。
		 */
		String message1 = "今天是{0,date,yyyy-MM-dd kk:mm:ss},您是{1,number,第#号顾客}"
				+ ",您的积分是：{2}，您的等级是：{2,choice,0#菜鸟|100<新手|250#青铜|350#白银|500<黄金|800<铂金|1000#钻石|2000#至尊}!";
		MessageFormat messageFormat1 = new MessageFormat(message1);
		System.out.println(messageFormat1.format(ArrayUtils.toArray(new Date(), 1333, 810)));
		System.out.println(messageFormat1.format(ArrayUtils.toArray(DateUtils.addDays(new Date(), -30), 310, 1231)));
	}
}