package jp.try0.zipcode.japanpost.csv;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import jp.try0.zipcode.japanpost.csv.data.ZipcodeCSVRow;

/**
 * ネットワークを使わない{@link ZipcodeCSV}のテスト
 */
public class ZipcodeCSVUnitTest {

	@TempDir
	Path tempDir;

	@Test
	public void deleteRemovesItsOwnFile() throws Exception {
		File csvFile = Files.writeString(tempDir.resolve("own.csv"), "a,b,c").toFile();
		ZipcodeCSV csv = new ZipcodeCSV(csvFile);

		csv.delete();

		assertFalse(csvFile.exists(), "保持しているCSVファイルが削除されていない");
	}

	private File sampleCsv() throws Exception {
		return Path.of(getClass().getResource("/utf_ken_all_sample.csv").toURI()).toFile();
	}

	@Test
	public void openRowStreamReadsAllRows() throws Exception {
		try (Stream<ZipcodeCSVRow> rows = new ZipcodeCSV(sampleCsv()).openRowStream()) {
			assertEquals(3, rows.count());
		}
	}

	@Test
	public void withRowsReadsRowsOfItsFile() throws Exception {
		long tokyo = new ZipcodeCSV(sampleCsv())
				.withRows(rows -> rows.filter(row -> "東京都".equals(row.getPrefectureName())).count());

		assertEquals(1, tokyo);
	}

	@Test
	public void getRowsAsListReadsAllRows() throws Exception {
		assertEquals(3, new ZipcodeCSV(sampleCsv()).getRowsAsList().size());
	}
}
