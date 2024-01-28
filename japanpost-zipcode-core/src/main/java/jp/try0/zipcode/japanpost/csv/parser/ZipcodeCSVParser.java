package jp.try0.zipcode.japanpost.csv.parser;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Iterator;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import jp.try0.zipcode.japanpost.csv.data.ZipcodeCSVRow;
import jp.try0.zipcode.japanpost.util.ZipcodeFiles;

/**
 * パーサー
 * 
 * @see https://www.post.japanpost.jp/zipcode/dl/readme.html
 */
public class ZipcodeCSVParser {

	File csvFile;

	public ZipcodeCSVParser(File file) {
		csvFile = ZipcodeFiles.unzipIfNeeded(file);
	}

	/**
	 * {@link CSVParser}を生成します。
	 *
	 * @param csvFile
	 * @return
	 * @throws IOException
	 */
	protected CSVParser newCSVParser(File csvFile) throws IOException {
		InputStreamReader reader = new InputStreamReader(new FileInputStream(csvFile), "UTF-8");
		return CSVFormat.DEFAULT.parse(reader);
	}

	/**
	 * {@link org.apache.commons.csv.CSVRecord}を取得します。
	 * 
	 * @return
	 * @throws IOException
	 */
	public Iterable<CSVRecord> parseCSVRecord() throws IOException {
		return newCSVParser(csvFile);
	}

	/**
	 * {@link ZipcodeCSVRow}にマッピングした行データを取得します。
	 * 
	 * @return
	 * @throws IOException
	 */
	public Iterable<ZipcodeCSVRow> parse() throws IOException {
		return new Iterable<ZipcodeCSVRow>() {
			final CSVParser parser = newCSVParser(csvFile);
			final Iterator<CSVRecord> it = parser.iterator();
			final Iterator<ZipcodeCSVRow> mappedIt = new Iterator<ZipcodeCSVRow>() {

				@Override
				public boolean hasNext() {
					return it.hasNext();
				}

				@Override
				public ZipcodeCSVRow next() {
					return ZipcodeCSVRow.fromCSVRecord(it.next());
				}
			};

			@Override
			public Iterator<ZipcodeCSVRow> iterator() {
				return mappedIt;
			}
		};
	}

}
