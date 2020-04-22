import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.KafkaException;
import org.apache.kafka.common.errors.WakeupException;

public abstract class AConsumer<K, V> implements Runnable {

	protected static final String MESSAGE_TIMESTAMP = "MESSAGE_TIMESTAMP";

	protected static final String MESSAGE_SIZE_KBS = "MESSAGE_SIZE_KBS";

	protected static final String MESSAGE_TOPIC = "MESSAGE_TOPIC";

	public static final String ENDPOINT_URL_CONFIG = "ENDPOINT_URL";

	private Consumer<String, byte[]> consumer = null;;

	private AtomicBoolean shutdown;

	private CountDownLatch shutdownLatch;

	private long POLL_INTERVAL_IN_MS = 5 * 1000;

	private long THROUGHPUT_DEBUG_INTERVAL_SEC = 10;

	private int NO_MESSAGE_REPORTING_INTERVAL_IN_MILLIS = 10 * 1000;

	private int KBS_IN_MB = 1000;

	private int noMessageCount = 0;

	private long currentTime, noMessageReportTime;

	private long windowStartTime = 0;

	private long kBsInWindow, totalKBsTransferred = 0;
	
	public AConsumer(Consumer consumer, List<String> topics) {
		this.consumer = consumer;
		this.shutdown = new AtomicBoolean(false);
		this.shutdownLatch = new CountDownLatch(1);
	}

	private Map<String, Object> processRecord(ConsumerRecord record) {
		if (record == null)
			return null;

		// extract metadata to map so ConsumerRecord doesn't "pollute" this codebase
		Map<String, Object> meta = new HashMap<String, Object>();
		meta.put(MESSAGE_TOPIC, record.topic());
		meta.put(MESSAGE_SIZE_KBS, record.serializedValueSize() / 1000);
		meta.put(MESSAGE_TIMESTAMP, record.timestamp());
		
		return meta;
	}


	private void reportThroughput(long kBsInWindow, long windowLengthInSecs) {
		float throughputMBPerS = (float) (kBsInWindow / (float) (windowLengthInSecs * KBS_IN_MB));
		System.out.format("[AConsumer] - Throughput in window (%d KB in %d secs): %.2f MB/s%n", kBsInWindow,
				windowLengthInSecs, throughputMBPerS);
		System.out.format("[AConsumer] - Total transferred: %d MBs%n", totalKBsTransferred / 1000);
	}
	
	protected void report(long kBsInWindow, long windowLengthInSecs) {
		reportThroughput(kBsInWindow, windowLengthInSecs);
	}

	private void processNoMessage() {
		noMessageCount++;

		if ((currentTime - noMessageReportTime) > NO_MESSAGE_REPORTING_INTERVAL_IN_MILLIS) {
			System.out.println("[AConsumer] - Number of polls returning no messages: " + noMessageCount);
			// reset the no message count and report time
			noMessageCount = 0;
			noMessageReportTime = currentTime;
		}
	}

	// process the metadata from the message
	protected void processMeta(Map<String, Object> meta) {
		// Maintain figures for throughput reporting
		int messageSize = (int) meta.get(MESSAGE_SIZE_KBS);

		kBsInWindow += messageSize;
		totalKBsTransferred += messageSize;
	}
	
	public void run() {
		System.out.println("[AConsumer] - Running...");

		try {
			noMessageReportTime = System.currentTimeMillis();
			windowStartTime = System.currentTimeMillis();

			while (!shutdown.get()) {
				try {
					ConsumerRecords<String, byte[]> records = consumer.poll(Duration.ofMillis(POLL_INTERVAL_IN_MS));
					currentTime = System.currentTimeMillis();

					if (records != null) {
						if (records.isEmpty()) {
							processNoMessage();
						} else {
							for (ConsumerRecord<String, byte[]> record : records) {
								Map<String, Object> meta = processRecord(record);

								if (meta != null) {
									processMeta(meta);
								} else {
									processNoMessage();
								}
							}
						}
					} else {
						processNoMessage();
					}

					consumer.commitAsync();
					
					// Determine if we should report throughput
					long windowLengthInSecs = (this.currentTime - this.windowStartTime) / 1000;

					if (windowLengthInSecs > THROUGHPUT_DEBUG_INTERVAL_SEC) {
						report(kBsInWindow, windowLengthInSecs);

						// Reset ready for the next throughput indication
						windowStartTime = System.currentTimeMillis();
						kBsInWindow = 0;
					}
				} catch (WakeupException e) {
					// Ignore exception if closing
					if (!shutdown.get()) throw e;
				} catch (KafkaException e) {
					System.out.println("KafkaException from client: " + e.getMessage());
				}
			}
		} finally {
			consumer.close();
			shutdownLatch.countDown();
		}

		System.out.println("[AConsumer] - Exiting...");
	}

	public void shutdown() throws InterruptedException {
		System.out.println("[AConsumer] - Shutting down...");
		shutdown.set(true);
		shutdownLatch.await();
	}

	public long getTotalKbsTransferred() {
		return totalKBsTransferred;
	}
}
