import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@ContextConfiguration("classpath:spring-redis-publisher.xml")
public class PublisherTest {

	@Autowired
	private StringRedisTemplate redisTemplate;

	@Test
	public void test1() {
		redisTemplate.convertAndSend("helloWorld", "good day");
	}
}
