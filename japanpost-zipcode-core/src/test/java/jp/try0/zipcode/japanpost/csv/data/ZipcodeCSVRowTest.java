package jp.try0.zipcode.japanpost.csv.data;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class ZipcodeCSVRowTest {

	private static ZipcodeCSVRow tokyo() {
		ZipcodeCSVRow row = new ZipcodeCSVRow();
		row.setRowNumber(1);
		row.setLocalGovernmentCode("13101");
		row.setLegacyZipcode("100  ");
		row.setZipcode("1000001");
		row.setPrefectureNameKana("ﾄｳｷｮｳﾄ");
		row.setCityNameKana("ﾁﾖﾀﾞｸ");
		row.setAreaNameKana("ﾁﾖﾀﾞ");
		row.setPrefectureName("東京都");
		row.setCityName("千代田区");
		row.setAreaName("千代田");
		row.setHasMultiZipcodeArea(0);
		row.setIsPerKoaza(0);
		row.setHasChoume(0);
		row.setHasMultiAreaZipcode(0);
		row.setUpdateState(0);
		row.setUpdateReason(0);
		return row;
	}

	@Test
	public void flagMethodsReturnTrueOnlyWhenValueIsOne() {
		ZipcodeCSVRow row = tokyo();

		assertAll(
				() -> assertFalse(row.hasMultiZipcodeArea()),
				() -> assertFalse(row.isPerKoaza()),
				() -> assertFalse(row.hasChoume()),
				() -> assertFalse(row.hasMultiAreaZipcode()));

		row.setHasMultiZipcodeArea(1);
		row.setIsPerKoaza(1);
		row.setHasChoume(1);
		row.setHasMultiAreaZipcode(1);

		assertAll(
				() -> assertTrue(row.hasMultiZipcodeArea()),
				() -> assertTrue(row.isPerKoaza()),
				() -> assertTrue(row.hasChoume()),
				() -> assertTrue(row.hasMultiAreaZipcode()));
	}

	@Test
	public void debugToStringListsOnlyRowValues() {
		ZipcodeCSVRow row = new ZipcodeCSVRow();
		row.setZipcode("0600000");

		String debug = row.debugToString();

		assertTrue(debug.contains("0600000"), debug);
		assertFalse(debug.contains(ZipcodeCSVColumnType.class.getName()), "static項目が出力されている: " + debug);
	}

	@Test
	public void toStringShowsNamedValues() {
		String text = tokyo().toString();

		assertTrue(text.contains("zipcode=1000001"), text);
		assertTrue(text.contains("prefectureName=東京都"), text);
		assertTrue(text.contains("rowNumber=1"), text);
	}

	@Test
	public void rowsWithSameValuesAreEqual() {
		assertEquals(tokyo(), tokyo());
		assertEquals(tokyo().hashCode(), tokyo().hashCode());
	}

	@Test
	public void rowsWithDifferentValuesAreNotEqual() {
		ZipcodeCSVRow other = tokyo();
		other.setAreaName("丸の内");

		assertNotEquals(tokyo(), other);

		ZipcodeCSVRow otherFlag = tokyo();
		otherFlag.setUpdateReason(5);

		assertNotEquals(tokyo(), otherFlag);
	}

	/**
	 * 行番号はファイル内の位置のため、比較に含めない（月次データの差分比較などで、行の挿入による位置ずれを無視できる）
	 */
	@Test
	public void equalityIgnoresRowNumber() {
		ZipcodeCSVRow moved = tokyo();
		moved.setRowNumber(100);

		assertEquals(tokyo(), moved);
		assertEquals(tokyo().hashCode(), moved.hashCode());
	}
}
