package jp.try0.zipcode.japanpost.type;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

public class PrefectureTest {

	@Test
	public void codesAreSequentialFrom1To47() {
		Prefecture[] values = Prefecture.values();
		assertEquals(47, values.length);
		for (int i = 0; i < values.length; i++) {
			assertEquals(i + 1, values[i].getCode(), values[i].name());
		}
	}

	@ParameterizedTest
	@CsvSource({
			"HOKKAIDO, 北海道",
			"TOKYO, 東京都",
			"KYOTO, 京都府",
			"OSAKA, 大阪府",
			"KANAGAWA, 神奈川県",
			"OKINAWA, 沖縄県",
	})
	public void fullName(Prefecture prefecture, String expected) {
		assertEquals(expected, prefecture.getFullName());
	}
}
