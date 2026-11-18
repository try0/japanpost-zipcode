package jp.try0.zipcode.japanpost.csv.parser;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import jp.try0.zipcode.japanpost.csv.data.ZipcodeCSVColumnType;
import jp.try0.zipcode.japanpost.csv.data.ZipcodeCSVRow;

/**
 * テスト用CSV（src/test/resources/utf_ken_all_sample.csv）を使ったオフラインのパーサーテスト
 */
public class ZipcodeCSVParserTest {

	private static List<ZipcodeCSVRow> parseSample() throws Exception {
		File csv = Path.of(ZipcodeCSVParserTest.class.getResource("/utf_ken_all_sample.csv").toURI()).toFile();
		List<ZipcodeCSVRow> rows = new ArrayList<>();
		new ZipcodeCSVParser(csv).parse().forEach(rows::add);
		return rows;
	}

	@Test
	public void parseReadsAllRows() throws Exception {
		assertEquals(3, parseSample().size());
	}

	@Test
	public void parseMapsStringColumns() throws Exception {
		ZipcodeCSVRow row = parseSample().get(0);

		assertAll(
				() -> assertEquals(1, row.getRowNumber()),
				() -> assertEquals("01101", row.getLocalGovernmentCode()),
				() -> assertEquals("060  ", row.getLegacyZipcode()),
				() -> assertEquals("0600000", row.getZipcode()),
				() -> assertEquals("ﾎｯｶｲﾄﾞｳ", row.getPrefectureNameKana()),
				() -> assertEquals("ｻｯﾎﾟﾛｼﾁｭｳｵｳｸ", row.getCityNameKana()),
				() -> assertEquals("ｲｶﾆｹｲｻｲｶﾞﾅｲﾊﾞｱｲ", row.getAreaNameKana()),
				() -> assertEquals("北海道", row.getPrefectureName()),
				() -> assertEquals("札幌市中央区", row.getCityName()),
				() -> assertEquals(ZipcodeCSVColumnType.NOT_LISTED_STRING, row.getAreaName()));
	}

	@Test
	public void parseMapsFlagColumns() throws Exception {
		ZipcodeCSVRow row = parseSample().get(2);

		assertAll(
				() -> assertEquals(3, row.getRowNumber()),
				() -> assertEquals("5400002", row.getZipcode()),
				() -> assertEquals(1, row.getHasMultiZipcodeArea()),
				() -> assertEquals(0, row.getIsPerKoaza()),
				() -> assertEquals(1, row.getHasChoume()),
				() -> assertEquals(0, row.getHasMultiAreaZipcode()),
				() -> assertEquals(1, row.getUpdateState()),
				() -> assertEquals(2, row.getUpdateReason()));
	}
}
