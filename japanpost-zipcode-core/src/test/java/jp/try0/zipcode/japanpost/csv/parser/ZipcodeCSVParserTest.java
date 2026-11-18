package jp.try0.zipcode.japanpost.csv.parser;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import jp.try0.zipcode.japanpost.csv.ZipcodeCSVProperties;
import jp.try0.zipcode.japanpost.csv.data.ZipcodeCSVColumnType;
import jp.try0.zipcode.japanpost.csv.data.ZipcodeCSVRow;
import jp.try0.zipcode.japanpost.testutil.SystemProperties;
import jp.try0.zipcode.japanpost.testutil.TestHttpServer;

/**
 * テスト用CSV（src/test/resources/utf_ken_all_sample.csv）を使ったオフラインのパーサーテスト
 */
public class ZipcodeCSVParserTest {

	private static final String ENCODING_PROPERTY = "jp.try0.zipcode.csv.encoding";

	private static final String HOKKAIDO_ROW = "01101,\"060  \",\"0600000\",\"ﾎｯｶｲﾄﾞｳ\",\"ｻｯﾎﾟﾛｼﾁｭｳｵｳｸ\",\"ｲｶﾆｹｲｻｲｶﾞﾅｲﾊﾞｱｲ\","
			+ "\"北海道\",\"札幌市中央区\",\"以下に掲載がない場合\",0,0,0,0,0,0\n";

	@TempDir
	Path tempDir;

	private static File sampleCsv() throws Exception {
		return Path.of(ZipcodeCSVParserTest.class.getResource("/utf_ken_all_sample.csv").toURI()).toFile();
	}

	private static <T> List<T> toList(Iterable<T> iterable) {
		List<T> list = new ArrayList<>();
		iterable.forEach(list::add);
		return list;
	}

	private static List<ZipcodeCSVRow> readSample() throws Exception {
		return new ZipcodeCSVParser(sampleCsv()).getRowsAsList();
	}

	/**
	 * 生成した{@link CSVParser}を記録するパーサー
	 */
	static class TrackingParser extends ZipcodeCSVParser {
		final List<CSVParser> opened = new ArrayList<>();

		TrackingParser(File file) {
			super(file);
		}

		@Override
		protected CSVParser newCSVParser(File csvFile) throws IOException {
			CSVParser parser = super.newCSVParser(csvFile);
			opened.add(parser);
			return parser;
		}

		void assertAllClosed() {
			assertFalse(opened.isEmpty(), "ファイルが開かれていない");
			assertTrue(opened.stream().allMatch(CSVParser::isClosed), "ファイルが閉じられていない");
		}
	}

	// ---- 行データのマッピング ----

	@Test
	public void getRowsAsListReadsAllRows() throws Exception {
		assertEquals(3, readSample().size());
	}

	@Test
	public void rowsMapStringColumns() throws Exception {
		ZipcodeCSVRow row = readSample().get(0);

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
	public void rowsMapFlagColumns() throws Exception {
		ZipcodeCSVRow row = readSample().get(2);

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

	@Test
	public void rowsShareRepeatedStrings() throws Exception {
		Path csv = tempDir.resolve("repeated.csv");
		Files.writeString(csv,
				"13101,\"100  \",\"1000001\",\"ﾄｳｷｮｳﾄ\",\"ﾁﾖﾀﾞｸ\",\"ﾁﾖﾀﾞ\",\"東京都\",\"千代田区\",\"千代田\",0,0,0,0,0,0\n"
						+ "13101,\"100  \",\"1000002\",\"ﾄｳｷｮｳﾄ\",\"ﾁﾖﾀﾞｸ\",\"ｺｳｷｮｶﾞｲｴﾝ\",\"東京都\",\"千代田区\",\"皇居外苑\",0,0,0,0,0,0\n",
				StandardCharsets.UTF_8);

		List<ZipcodeCSVRow> rows = new ZipcodeCSVParser(csv.toFile()).getRowsAsList();
		ZipcodeCSVRow first = rows.get(0);
		ZipcodeCSVRow second = rows.get(1);

		assertAll(
				() -> assertSame(first.getLocalGovernmentCode(), second.getLocalGovernmentCode()),
				() -> assertSame(first.getLegacyZipcode(), second.getLegacyZipcode()),
				() -> assertSame(first.getPrefectureNameKana(), second.getPrefectureNameKana()),
				() -> assertSame(first.getCityNameKana(), second.getCityNameKana()),
				() -> assertSame(first.getPrefectureName(), second.getPrefectureName()),
				() -> assertSame(first.getCityName(), second.getCityName()));
	}

	@Test
	public void rowsUseConfiguredEncoding() throws Exception {
		Path sjisCsv = tempDir.resolve("sjis.csv");
		Files.write(sjisCsv, HOKKAIDO_ROW.getBytes(Charset.forName("Windows-31J")));

		String original = System.getProperty(ENCODING_PROPERTY);
		System.setProperty(ENCODING_PROPERTY, "Windows-31J");
		try {
			ZipcodeCSVRow row = new ZipcodeCSVParser(sjisCsv.toFile()).getRowsAsList().get(0);

			assertEquals("北海道", row.getPrefectureName());
			assertEquals("ﾎｯｶｲﾄﾞｳ", row.getPrefectureNameKana());
		} finally {
			if (original == null) {
				System.clearProperty(ENCODING_PROPERTY);
			} else {
				System.setProperty(ENCODING_PROPERTY, original);
			}
		}
	}

	@Test
	public void rowsUseEncodingOfGivenProperties() throws Exception {
		Path sjisCsv = tempDir.resolve("sjis.csv");
		Charset sjis = Charset.forName("Windows-31J");
		Files.write(sjisCsv, HOKKAIDO_ROW.getBytes(sjis));
		ZipcodeCSVProperties properties = ZipcodeCSVProperties.builder().csvEncoding(sjis).build();

		try (SystemProperties props = new SystemProperties().set(ENCODING_PROPERTY, "UTF-8")) {
			ZipcodeCSVRow row = new ZipcodeCSVParser(sjisCsv.toFile(), properties).getRowsAsList().get(0);

			assertEquals("北海道", row.getPrefectureName());
		}
	}

	@Test
	public void invalidValueReportsColumnAndClosesFile() throws Exception {
		Path csv = tempDir.resolve("invalid.csv");
		Files.writeString(csv, HOKKAIDO_ROW + HOKKAIDO_ROW.replace(",0,0,0,0,0,0", ",x,0,0,0,0,0") + HOKKAIDO_ROW,
				StandardCharsets.UTF_8);
		TrackingParser parser = new TrackingParser(csv.toFile());

		IllegalArgumentException e = assertThrows(IllegalArgumentException.class, parser::getRowsAsList);

		assertTrue(e.getMessage().contains(ZipcodeCSVColumnType.COL_9_HAS_MULTI_ZIPCODE_AREA.name()), e.getMessage());
		assertInstanceOf(NumberFormatException.class, e.getCause());
		parser.assertAllClosed();
	}

	// ---- リソース管理 ----

	@Test
	public void getRowsAsListClosesFile() throws Exception {
		TrackingParser parser = new TrackingParser(sampleCsv());

		parser.getRowsAsList();

		parser.assertAllClosed();
	}

	@Test
	public void rowsCanBeReadRepeatedly() throws Exception {
		ZipcodeCSVParser parser = new ZipcodeCSVParser(sampleCsv());

		assertEquals(3, parser.getRowsAsList().size());
		assertEquals(3, parser.getRowsAsList().size(), "2回目の読み込みで行が取得できない");
	}

	@Test
	public void withRowsReturnsResultOfFunction() throws Exception {
		List<String> zipcodes = new ZipcodeCSVParser(sampleCsv())
				.withRows(rows -> rows.map(ZipcodeCSVRow::getZipcode).collect(Collectors.toList()));

		assertEquals(List.of("0600000", "1000001", "5400002"), zipcodes);
	}

	@Test
	public void withRowsClosesFileWhenFunctionStopsEarly() throws Exception {
		TrackingParser parser = new TrackingParser(sampleCsv());

		String first = parser.withRows(rows -> rows.findFirst().orElseThrow().getZipcode());

		assertEquals("0600000", first);
		parser.assertAllClosed();
	}

	@Test
	public void withRowsClosesFileWhenFunctionThrows() throws Exception {
		TrackingParser parser = new TrackingParser(sampleCsv());

		assertThrows(IllegalStateException.class, () -> parser.withRows(rows -> {
			throw new IllegalStateException("呼び出し側の例外");
		}));

		parser.assertAllClosed();
	}

	@Test
	public void withRowsThrowsWhenFileIsMissing() {
		ZipcodeCSVParser parser = new ZipcodeCSVParser(tempDir.resolve("missing.csv").toFile());

		assertThrows(IOException.class, () -> parser.withRows(Stream::count));
	}

	@Test
	public void forEachRowVisitsAllRowsInOrder() throws Exception {
		List<String> zipcodes = new ArrayList<>();

		new ZipcodeCSVParser(sampleCsv()).forEachRow(row -> zipcodes.add(row.getZipcode()));

		assertEquals(List.of("0600000", "1000001", "5400002"), zipcodes);
	}

	@Test
	public void forEachRowClosesFileWhenActionThrows() throws Exception {
		TrackingParser parser = new TrackingParser(sampleCsv());

		assertThrows(IllegalStateException.class, () -> parser.forEachRow(row -> {
			throw new IllegalStateException("呼び出し側の例外");
		}));

		parser.assertAllClosed();
	}

	@Test
	public void openRowStreamReadsAllRows() throws Exception {
		try (Stream<ZipcodeCSVRow> rows = new ZipcodeCSVParser(sampleCsv()).openRowStream()) {
			assertEquals(List.of("0600000", "1000001", "5400002"),
					rows.map(ZipcodeCSVRow::getZipcode).collect(Collectors.toList()));
		}
	}

	@Test
	public void openRowStreamClosesFileWhenClosedBeforeEnd() throws Exception {
		TrackingParser parser = new TrackingParser(sampleCsv());

		try (Stream<ZipcodeCSVRow> rows = parser.openRowStream()) {
			assertEquals("0600000", rows.findFirst().orElseThrow().getZipcode());
		}

		parser.assertAllClosed();
	}

	@Test
	public void openRowStreamThrowsWhenFileIsMissing() {
		ZipcodeCSVParser parser = new ZipcodeCSVParser(tempDir.resolve("missing.csv").toFile());

		assertThrows(IOException.class, parser::openRowStream);
	}

	// ---- Zipファイル ----

	private Path sampleZip(String entryName) throws Exception {
		Path zip = tempDir.resolve("utf_ken_all.zip");
		Files.write(zip, TestHttpServer.zip(entryName, TestHttpServer.sampleCsv()));
		return zip;
	}

	@Test
	public void readsRowsDirectlyFromZip() throws Exception {
		List<String> zipcodes = new ZipcodeCSVParser(sampleZip("utf_ken_all.csv").toFile())
				.withRows(rows -> rows.map(ZipcodeCSVRow::getZipcode).collect(Collectors.toList()));

		assertEquals(List.of("0600000", "1000001", "5400002"), zipcodes);
	}

	@Test
	public void readingZipWritesNothingToCacheDir() throws Exception {
		Path cacheDir = tempDir.resolve("cache");
		Path zip = sampleZip("utf_ken_all.csv");

		try (SystemProperties props = new SystemProperties().set(SystemProperties.CACHE_DIR, cacheDir.toString())) {
			new ZipcodeCSVParser(zip.toFile()).getRowsAsList();
		}

		assertFalse(Files.exists(cacheDir), "キャッシュディレクトリーに書き込まれた");
	}

	/**
	 * Windowsでは開いたままのZipファイルを削除できないため、Zipファイルを閉じたことを検証できます。
	 */
	@Test
	public void zipFileIsClosedWhenStoppingEarly() throws Exception {
		Path zip = sampleZip("utf_ken_all.csv");

		new ZipcodeCSVParser(zip.toFile()).withRows(rows -> rows.findFirst());

		Files.delete(zip);
	}

	@Test
	public void zipWithoutCSVFailsWhenRead() throws Exception {
		ZipcodeCSVParser parser = new ZipcodeCSVParser(sampleZip("other.txt").toFile());

		assertThrows(FileNotFoundException.class, parser::getRowsAsList);
	}

	// ---- 低レベルAPI ----

	@Test
	public void parseCSVRecordReturnsParserClosableWithTryWithResources() throws Exception {
		CSVParser used;
		try (CSVParser records = new ZipcodeCSVParser(sampleCsv()).parseCSVRecord()) {
			List<CSVRecord> list = toList(records);
			assertEquals(3, list.size());
			assertEquals("0600000", list.get(0).get(ZipcodeCSVColumnType.COL_2_ZIPCODE.ordinal()));
			used = records;
		}

		assertTrue(used.isClosed());
	}
}
