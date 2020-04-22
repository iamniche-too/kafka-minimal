

import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.util.List;

import org.apache.kafka.clients.consumer.Consumer;
import org.cache2k.benchmark.jmh.ForcedGcMemoryProfiler;

/**
 * Based on:
 * https://github.com/apache/kafka/blob/trunk/examples/src/main/java/kafka/examples/Consumer.java
 * 
 * @author nic
 *
 * @param Integer
 * @param String
 */
public class MemoryUseReportingConsumer<K,V> extends AConsumer {
	
	private long initialMemoryUsageInBytes = 0;
	
	private long peakMemoryUsageInBytes = 0;
	
	public MemoryUseReportingConsumer(Consumer consumer, List<String> topics) {
		super(consumer, topics);
		
		// record initial memory use
		initialMemoryUsageInBytes = getSettledUsedMemory();
	}
	
	protected void reportPeakMemoryUse() {
		long settledMemoryInBytes = getSettledUsedMemory();

		if (settledMemoryInBytes > peakMemoryUsageInBytes) {
			peakMemoryUsageInBytes = settledMemoryInBytes;
		}
		
		System.out.format("[MemoryUseReportingConsumer] peakMemoryUsageInBytes=%d%n", peakMemoryUsageInBytes);
		System.out.format("[MemoryUseReportingConsumer] memoryIncreaseInBytes=%d%n", (peakMemoryUsageInBytes - initialMemoryUsageInBytes));
	}

	private long getCurrentlyUsedMemory() {
		return ManagementFactory.getMemoryMXBean().getHeapMemoryUsage().getUsed()
				+ ManagementFactory.getMemoryMXBean().getNonHeapMemoryUsage().getUsed();
	}

	private long getGcCount() {
		long sum = 0;
		for (GarbageCollectorMXBean b : ManagementFactory.getGarbageCollectorMXBeans()) {
			long count = b.getCollectionCount();
			if (count != -1) {
				sum += count;
			}
		}
		return sum;
	}

	private long getReallyUsedMemory() {
		long before = getGcCount();
		System.gc();
		while (getGcCount() == before)
			;
		return getCurrentlyUsedMemory();
	}

	private long getSettledUsedMemory() {
		long m;
		long m2 = getReallyUsedMemory();
		do {
			try {
				Thread.sleep(567);
			} catch (InterruptedException e) {
			}

			m = m2;
			m2 = ForcedGcMemoryProfiler.getUsedMemory();
		} while (m2 < getReallyUsedMemory());
		return m;
	}
}
