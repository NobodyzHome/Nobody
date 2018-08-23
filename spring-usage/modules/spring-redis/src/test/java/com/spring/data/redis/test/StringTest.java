package com.spring.data.redis.test;

import java.util.StringJoiner;
import java.util.StringTokenizer;

public class StringTest {

	public static void main(String[] args) {

		StringJoiner joiner = new StringJoiner("_", "拼接结果：【", "】！");

		/*
		 * StringJoiner是使用prefix + element1 + delimiter + element2 + delimiter +
		 * element3 + suffix的方式来生成拼接后的字符串。
		 * 
		 * 可以看到，StringJoiner有对最后一个元素进行处理，在最后一个后面不添加delimiter。
		 * 
		 * 其中elment1、element2、element3是通过使用StringJoiner的add方法来添加的。
		 * 并且只有在调用StringJoiner的toString方法后才会进行拼接。
		 */

		String contents = joiner.add("小明").add("小黄").add("狗蛋").toString();
		System.out.println(contents);// 输出：“拼接结果：【小明_小黄_狗蛋】！”

		/*
		 * StringTokenizer的作用和StringJoiner正好相反，StringTokenizer是把一个字符串按照指定内容分割开来，
		 * 然后对分割后的结果进行遍历。
		 * 
		 * 举例来说： new StringTokenizer("This is a good day",
		 * " ")就是把"This is a good day"这个字符串按照空格进行分割，分割后的结果为["This","is","a",
		 * "good","day"]。
		 * 然后通过StringTokenizer的hasMoreElements和nextToken方法来对分割后的结果进行遍历，遍历每一个元素。
		 * 
		 * 注意：和StringJoiner相反，StringJoiner是在toString调用时才进行字符串拼接，
		 * 而StringTokenizer是在一创建StringTokenizer对象时就进行字符串分割。
		 */
		// 拆分结果；[This,is,a,good,day,I,love,it,very,much]
		StringTokenizer tokenizer = new StringTokenizer("This is a good day I love it very much", " ");
		// 遍历拆分结果
		while (tokenizer.hasMoreElements()) {
			String token = tokenizer.nextToken();
			System.out.println(token);
		}

		/*
		 * StringJoiner和StringTokenizer联合应用。先使用StringJoiner把一个个字符串元素拼接起来，
		 * 再使用StringTokenizer对这个拼接出的字符串进行分割和遍历。
		 * 
		 * 可以看到：
		 * 
		 * 1.使用StringJoiner的好处是省的我们自己进行拼接操作。我们在遍历元素时，
		 * 只需要把遍历到的元素交给StringJoiner即可。而如果不使用StringJoiner，我们在遍历时，
		 * 需要手动对最后一个元素进行特殊处理，在最后一个元素后面不添加delimiter。
		 * 
		 * 2.使用StringTokenizer的好处是不需要我们使用String.split方法进行拆分，同时，
		 * StringTokenizer会告诉我们拆分后的结果中还有没有下一个元素。
		 */
		String delimiter = "_";
		StringJoiner strJoiner = new StringJoiner(delimiter);
		// 拼接结果：this_is_a_good_day
		String words = strJoiner.add("this").add("is").add("a").add("good").add("day").toString();
		System.out.println(words);

		// 拆分结果：[this,is,a,good,day]
		StringTokenizer strTokenizer = new StringTokenizer(words, delimiter);
		// 遍历拆分结果
		while (strTokenizer.hasMoreTokens()) {
			String token = strTokenizer.nextToken();
			System.out.println(token);
		}
	}
}
