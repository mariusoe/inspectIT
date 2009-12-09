package info.novatec.inspectit.rcp.editor.preferences.control.samplingrate.test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;
import info.novatec.inspectit.communication.DefaultData;
import info.novatec.inspectit.communication.data.ClassLoadingInformationData;
import info.novatec.inspectit.rcp.editor.preferences.control.samplingrate.SamplingRateMode;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

public class TimeframeDividerTest {

	/**
	 * The list with data objects.
	 */
	private List<? extends DefaultData> dataObjects;

	/**
	 * The sampling rate mode.
	 */
	SamplingRateMode mode = SamplingRateMode.TIMEFRAME_DIVIDER;

	@BeforeTest
	public void initTestClass() {
		dataObjects = createDataObjects(50);
	}

	/**
	 * Creates countOfObjects data objects.
	 * 
	 * @param countOfObjects
	 *            The count of objects to create.
	 * @return The list with data objects.
	 */
	public List<ClassLoadingInformationData> createDataObjects(int countOfObjects) {
		List<ClassLoadingInformationData> tempObjects = new ArrayList<ClassLoadingInformationData>();

		// the time of the first data object is Mon Sep 15 11:00:00 CEST 2008
		long currentTime = 1221469200000L;

		for (int i = 0; i < countOfObjects; i++) {
			ClassLoadingInformationData data = new ClassLoadingInformationData(new Timestamp(currentTime), 1, 5);
			currentTime += 5000L;
			int count = 5;
			data.setCount(count);
			data.setId(-1L);
			data.setMinLoadedClassCount(2000 + i);
			data.setMaxLoadedClassCount(3000 + i);
			int totalLoadedClassCount = (((data.getMinLoadedClassCount() + data.getMaxLoadedClassCount())) / 2) / count;
			data.setTotalLoadedClassCount(totalLoadedClassCount);

			data.setMinUnloadedClassCount(20 + i);
			data.setMaxUnloadedClassCount(30 + i);
			long totalUnloadedClassCount = ((data.getMinUnloadedClassCount() + data.getMaxUnloadedClassCount()) / 2) / count;
			data.setTotalUnloadedClassCount(totalUnloadedClassCount);

			data.setMinTotalLoadedClassCount(10000);
			data.setMaxTotalLoadedClassCount(15000);
			long totalTotalLoadedClassCount = ((data.getMinTotalLoadedClassCount() + data.getMaxTotalLoadedClassCount()) / 2) / count;
			data.setTotalTotalLoadedClassCount(totalTotalLoadedClassCount);

			tempObjects.add(data);
		}

		return tempObjects;
	}

	/**
	 * When the passed data object list isn't empty, then the result list
	 * shouldn't be empty too.
	 */
	@Test
	public void resultValuesExist() {
		List<? extends DefaultData> resultList = null;
		int samplingRate = 20;

		// from time is Mon Sep 15 10:50:12 CEST 2008
		Date fromDate = new Date(1221468612000L);

		// to time is Mon Sep 15 12:00:00 CEST 2008
		Date toDate = new Date(1221472800000L);

		resultList = mode.adjustSamplingRate(dataObjects, fromDate, toDate, samplingRate);

		assertNotNull(resultList);
	}

	/**
	 * When nine values are in one timeframe and one value is in another
	 * timeframe, then the result is 2.
	 */
	@Test
	public void nineValuesInOneTimeframe() {
		List<ClassLoadingInformationData> tempList = new ArrayList<ClassLoadingInformationData>();
		List<? extends DefaultData> resultList = null;
		int samplingRate = 5;

		// from time is Mon Sep 15 11:00:00 CEST 2008
		Date fromDate = new Date(1221469200000L);

		// to time is Mon Sep 15 11:20:00 CEST 2008
		Date toDate = new Date(1221470400000L);

		// create nine data objects
		tempList = createDataObjects(9);

		// create one data object
		ClassLoadingInformationData data = new ClassLoadingInformationData(new Timestamp(fromDate.getTime() + 300000L), 1, 5);
		int count = 5;
		data.setCount(count);
		data.setId(-1L);
		data.setMinLoadedClassCount(2010);
		data.setMaxLoadedClassCount(3010);
		int totalLoadedClassCount = (((data.getMinLoadedClassCount() + data.getMaxLoadedClassCount())) / 2) / count;
		data.setTotalLoadedClassCount(totalLoadedClassCount);

		data.setMinUnloadedClassCount(30);
		data.setMaxUnloadedClassCount(40);
		long totalUnloadedClassCount = ((data.getMinUnloadedClassCount() + data.getMaxUnloadedClassCount()) / 2) / count;
		data.setTotalUnloadedClassCount(totalUnloadedClassCount);

		data.setMinTotalLoadedClassCount(10000);
		data.setMaxTotalLoadedClassCount(15000);
		long totalTotalLoadedClassCount = ((data.getMinTotalLoadedClassCount() + data.getMaxTotalLoadedClassCount()) / 2) / count;
		data.setTotalTotalLoadedClassCount(totalTotalLoadedClassCount);

		tempList.add(data);

		resultList = mode.adjustSamplingRate(tempList, fromDate, toDate, samplingRate);

		assertSame(resultList.size(), 2);

	}

	/**
	 * No data to aggregate when an empty list is passed.
	 */
	@Test
	public void emptyListAsParameter() {
		List<? extends DefaultData> resultList = null;
		int samplingRate = 20;

		// from time is Mon Sep 15 10:50:12 CEST 2008
		Date fromDate = new Date(1221468612000L);

		// to time is Mon Sep 15 12:00:00 CEST 2008
		Date toDate = new Date(1221472800000L);

		resultList = mode.adjustSamplingRate(null, fromDate, toDate, samplingRate);

		assertNull(resultList);
	}

	/**
	 * No data to aggregate when sampling rate is bigger than the count of data
	 * objects.
	 */
	@Test
	public void noDataToAggregateWithLessObjectsThanSamplingRate() {
		List<? extends DefaultData> resultList = null;
		List<ClassLoadingInformationData> tempList = createDataObjects(30);
		int samplingRate = 50;

		// from time is Mon Sep 15 10:50:12 CEST 2008
		Date fromDate = new Date(1221468612000L);

		// to time is Mon Sep 15 12:00:00 CEST 2008
		Date toDate = new Date(1221472800000L);

		resultList = mode.adjustSamplingRate(tempList, fromDate, toDate, samplingRate);

		assertTrue(resultList.size() < samplingRate);
	}

	/**
	 * No data to aggregate when no sampling rate is set.
	 */
	@Test
	public void noDataToAggregateWhenNoSamplingRateIsSet() {
		List<? extends DefaultData> resultList = null;

		// from time is Mon Sep 15 10:50:12 CEST 2008
		Date fromDate = new Date(1221468612000L);

		// to time is Mon Sep 15 12:00:00 CEST 2008
		Date toDate = new Date(1221472800000L);

		resultList = mode.adjustSamplingRate(dataObjects, fromDate, toDate, 0);

		assertEquals(dataObjects, resultList);
	}

	/**
	 * All aggregated values must lie in between from and to.
	 */
	@Test
	public void aggregatedValuesBetweenFromTo() {
		List<? extends DefaultData> resultList = null;
		int samplingRate = 20;

		// from time is Mon Sep 15 10:50:12 CEST 2008
		Date fromDate = new Date(1221468612000L);

		// to time is Mon Sep 15 12:00:00 CEST 2008
		Date toDate = new Date(1221472800000L);

		resultList = mode.adjustSamplingRate(dataObjects, fromDate, toDate, samplingRate);

		for (DefaultData defaultData : resultList) {
			long dataTime = defaultData.getTimeStamp().getTime();
			assertTrue((dataTime >= fromDate.getTime()) && (dataTime <= toDate.getTime()));
		}
	}

	/**
	 * If sampling rate is set, then values should be aggregated.
	 */
	@Test
	public void samplingRateIsSet() {
		List<? extends DefaultData> resultList = null;
		int samplingRate = 20;

		// from time is Mon Sep 15 10:50:12 CEST 2008
		Date fromDate = new Date(1221468612000L);

		// to time is Mon Sep 15 12:00:00 CEST 2008
		Date toDate = new Date(1221472800000L);

		resultList = mode.adjustSamplingRate(dataObjects, fromDate, toDate, samplingRate);

		assertTrue(resultList.size() <= samplingRate);
	}

	/**
	 * If the from and to time are smaller then the time of the first data
	 * object, then no data should be aggregated.
	 */
	@Test
	public void fromAndToBeforeDataExists() {
		List<? extends DefaultData> resultList = null;
		int samplingRate = 20;

		// the fromDate is Mon Sep 15 10:00:00 CEST 2008
		Date fromDate = new Date(1221465600000L);

		// the toDate is Mon Sep 15 10:30:00 CEST 2008
		Date toDate = new Date(1221467400000L);

		resultList = mode.adjustSamplingRate(dataObjects, fromDate, toDate, samplingRate);

		assertSame(resultList.size(), 0);
	}

	/**
	 * If all values are in one timeframe, then the result must be 1.
	 */
	@Test
	public void allValuesInOneTimeframe() {
		List<? extends DefaultData> resultList = null;
		int samplingRate = 20;

		// from time is Sun Sep 14 06:00:00 CEST 2008
		Date fromDate = new Date(1221364800000L);

		// to time is Mon Sep 15 12:00:00 CEST 2008
		Date toDate = new Date(1221472800000L);

		resultList = mode.adjustSamplingRate(dataObjects, fromDate, toDate, samplingRate);

		assertSame(resultList.size(), 1);
	}

}
