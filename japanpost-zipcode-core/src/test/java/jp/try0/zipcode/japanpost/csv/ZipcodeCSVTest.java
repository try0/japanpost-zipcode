package jp.try0.zipcode.japanpost.csv;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.io.TempDir;

import jp.try0.zipcode.japanpost.testutil.SystemProperties;
import jp.try0.zipcode.japanpost.util.ZipcodeFiles;

/**
 * 日本郵便のサーバーからダウンロードするため、"network"タグを付与しています。<br>
 * キャッシュは一時ディレクトリーを使用します。
 */
@Tag("network")
@TestMethodOrder(MethodOrderer.MethodName.class)
public class ZipcodeCSVTest {

	@TempDir
	Path tempDir;

	private final SystemProperties props = new SystemProperties();

	@BeforeEach
	public void useTempCacheDir() {
		props.set(SystemProperties.CACHE_DIR, tempDir.toString())
				.set(SystemProperties.DOWNLOAD_URL, null);
	}

	@AfterEach
	public void restore() {
		props.close();
	}

//	@Test
	public void cacheTest() throws IOException {
		{
			File file = ZipcodeFiles.getCSVPath().toFile();
			if (file.exists()) {
				if (!file.delete()) {
					throw new RuntimeException();
				}
			}
		}

		ZipcodeCSV csvA = ZipcodeCSV.getOrDownload();
		var timeA = Files.getLastModifiedTime(csvA.getFile().toPath());
		Assertions.assertNotNull(csvA);

		ZipcodeCSV csvB = ZipcodeCSV.getOrDownload();
		var timeB = Files.getLastModifiedTime(csvB.getFile().toPath());
		Assertions.assertNotNull(csvB);

		Assertions.assertEquals(timeA, timeB, "一時ディレクトリーから取得できない");
	}

//	@Test
	public void deleteTest() throws IOException {
		ZipcodeCSV csv = ZipcodeCSV.getOrDownload();
		Assertions.assertTrue(csv.getFile().exists(), "削除テスト準備エラー");

		csv.delete();

		Assertions.assertFalse(csv.getFile().exists(), "削除できない");

	}

	@Test
	public void serializeTest() throws Exception {
		ZipcodeCSV csv = ZipcodeCSV.getOrDownload();
		serialize(csv);
	}

	private byte[] serialize(Object object) throws IOException {
		try (ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
				ObjectOutputStream outputStream = new ObjectOutputStream(byteStream);) {
			outputStream.writeObject(object);
			return byteStream.toByteArray();
		}
	}
}
