package rocks.inspectit.server.util.lookup;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;

/**
 * @author Marius Oehler
 *
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@Warmup(iterations = 3)
@Measurement(iterations = 5)
@Fork(1)
@State(Scope.Thread)
@SuppressWarnings("PMD")
public class CountryLookupUtilPerfTest {

	private CountryLookupUtil countryLookupUtil = new CountryLookupUtil();

	private String ipAddress;

	@Setup
	public void setupOnce() {
		countryLookupUtil.postConstruct();
	}

	@Setup(Level.Iteration)
	public void setupIteration() {
		ThreadLocalRandom random = ThreadLocalRandom.current();
		ipAddress = random.nextLong(1, 255) + "." + random.nextLong(1, 255) + "." + random.nextLong(1, 255) + "." + random.nextLong(1, 255);
		System.out.print(ipAddress + "\t");
	}

	@Benchmark
	public Network lookup() {
		return countryLookupUtil.lookup(ipAddress);
	}

}
