package jp.try0.zipcode.japanpost.csv.parser;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import jp.try0.zipcode.japanpost.csv.ZipcodeCSVProperties;
import jp.try0.zipcode.japanpost.csv.ZipcodeCSVRowSource;
import jp.try0.zipcode.japanpost.csv.data.ZipcodeCSVRow;
import jp.try0.zipcode.japanpost.util.ZipcodeFiles;

/**
 * パーサー<br>
 * 行データの読み込み方法は{@link ZipcodeCSVRowSource}を参照してください。
 *
 * @see https://www.post.japanpost.jp/zipcode/dl/readme.html
 */
public class ZipcodeCSVParser implements ZipcodeCSVRowSource {

	File csvFile;

	public ZipcodeCSVParser(File file) {
		csvFile = ZipcodeFiles.unzipIfNeeded(file);
	}

	/**
	 * {@link CSVParser}を生成します。<br>
	 * 文字コードは{@link ZipcodeCSVProperties#getCSVEncoding()}を使用します。
	 *
	 * @param csvFile
	 * @return
	 * @throws IOException
	 */
	protected CSVParser newCSVParser(File csvFile) throws IOException {
		Charset charset = Charset.forName(ZipcodeCSVProperties.get().getCSVEncoding());
		InputStreamReader reader = new InputStreamReader(Files.newInputStream(csvFile.toPath()), charset);
		return CSVFormat.DEFAULT.parse(reader);
	}

	/**
	 * {@link org.apache.commons.csv.CSVRecord}を取得する{@link CSVParser}を生成します。<br>
	 * ファイルを開いた状態で返すため、呼び出し側で必ずクローズしてください。
	 *
	 * <pre>
	 * try (CSVParser records = parser.parseCSVRecord()) {
	 *     for (CSVRecord record : records) {
	 *         // ...
	 *     }
	 * }
	 * </pre>
	 *
	 * @return CSVParser
	 * @throws IOException
	 */
	public CSVParser parseCSVRecord() throws IOException {
		return newCSVParser(csvFile);
	}

	/**
	 * {@link ZipcodeCSVRow}にマッピングした行データの{@link Stream}を開きます。<br>
	 * 最後まで読み込んだ時点、またはクローズ時にファイルを閉じます。
	 */
	@Override
	public Stream<ZipcodeCSVRow> openRowStream() throws IOException {
		if (!csvFile.isFile()) {
			throw new FileNotFoundException(csvFile.getPath());
		}

		RowIterator it = new RowIterator(newCSVParser(csvFile));
		return StreamSupport
				.stream(Spliterators.spliteratorUnknownSize(it, Spliterator.ORDERED | Spliterator.NONNULL), false)
				.onClose(it::close);
	}

	/**
	 * {@link ZipcodeCSVRow}にマッピングするイテレーター<br>
	 * 最後まで読み込んだ時点、または例外発生時に{@link CSVParser}を閉じます。
	 */
	private static class RowIterator implements Iterator<ZipcodeCSVRow> {

		final CSVParser parser;
		final Iterator<CSVRecord> it;

		/**
		 * 種類の少ない列の文字列インスタンスを行間で共有するためのプール<br>
		 * 全件保持した場合のメモリー使用量を約半分に削減します。イテレーターと共に破棄されます。
		 */
		final Map<String, String> stringPool = new HashMap<>();

		RowIterator(CSVParser parser) {
			this.parser = parser;
			this.it = parser.iterator();
		}

		@Override
		public boolean hasNext() {
			try {
				boolean hasNext = it.hasNext();
				if (!hasNext) {
					close();
				}
				return hasNext;
			} catch (RuntimeException e) {
				close();
				throw e;
			}
		}

		@Override
		public ZipcodeCSVRow next() {
			if (!hasNext()) {
				throw new NoSuchElementException();
			}

			try {
				return shareRepeatedValues(ZipcodeCSVRow.fromCSVRecord(it.next()));
			} catch (RuntimeException e) {
				close();
				throw e;
			}
		}

		/**
		 * 種類の少ない列の値を、プール済みの文字列インスタンスに置き換えます。<br>
		 * 郵便番号・町域名はほぼ一意で効果が薄いため対象外です。
		 */
		private ZipcodeCSVRow shareRepeatedValues(ZipcodeCSVRow row) {
			row.setLocalGovernmentCode(share(row.getLocalGovernmentCode()));
			row.setLegacyZipcode(share(row.getLegacyZipcode()));
			row.setPrefectureNameKana(share(row.getPrefectureNameKana()));
			row.setCityNameKana(share(row.getCityNameKana()));
			row.setPrefectureName(share(row.getPrefectureName()));
			row.setCityName(share(row.getCityName()));
			return row;
		}

		private String share(String value) {
			String pooled = stringPool.putIfAbsent(value, value);
			return pooled != null ? pooled : value;
		}

		void close() {
			try {
				parser.close();
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			}
		}
	}

}
