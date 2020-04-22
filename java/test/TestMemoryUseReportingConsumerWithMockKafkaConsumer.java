

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.kafka.clients.consumer.Consumer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class TestMemoryUseReportingConsumerWithMockKafkaConsumer {

	public MemoryUseReportingConsumer<String, byte[]> memoryUseReportingConsumer;

	public static String getConsumerId() {
		String consumerId = "";
		String idAlphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
		Random random = new Random();
		for (int i = 0; i < 7; i++) {
			int index = random.nextInt(idAlphabet.length());
			consumerId = consumerId + idAlphabet.charAt(index);
		}
		return consumerId;
	}

	@BeforeEach
	public void setUp() throws NoSuchAlgorithmException {		
		List<String> topicNames = new ArrayList<String>();
		topicNames.add("test-topic");

		Consumer<String, byte[]> mockConsumer = new MockConsumer();
		memoryUseReportingConsumer = new MemoryUseReportingConsumer(mockConsumer, topicNames);
	}

	@Test
	public void testMemoryUseReportingConsumer() {
		Thread thread = new Thread(memoryUseReportingConsumer);
		thread.start();
		
		// run for specific period
		try {	
			Thread.sleep(1000 * 60 * 5);
		} catch (InterruptedException ex) {
			Thread.currentThread().interrupt();
		}
		
		// wait for thread to exit
		try {	
			memoryUseReportingConsumer.shutdown();
			thread.join();
		} catch (InterruptedException ex) {
		}
		
		assertTrue(memoryUseReportingConsumer.getTotalKbsTransferred() > 0);
	}
}
