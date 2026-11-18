package jp.try0.zipcode.japanpost.csv.data;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class ZipcodeCSVRowTest {

	@Test
	public void debugToStringListsOnlyRowValues() {
		ZipcodeCSVRow row = new ZipcodeCSVRow();
		row.setZipcode("0600000");

		String debug = row.debugToString();

		assertTrue(debug.contains("0600000"), debug);
		assertFalse(debug.contains(ZipcodeCSVColumnType.class.getName()), "static項目が出力されている: " + debug);
	}
}
