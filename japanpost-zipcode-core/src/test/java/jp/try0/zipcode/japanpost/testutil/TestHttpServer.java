package jp.try0.zipcode.japanpost.testutil;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

/**
 * 日本郵便のダウンロードURLの代わりに使う、ローカルのHTTPサーバー
 */
public final class TestHttpServer implements AutoCloseable {

	private final HttpServer server;
	private final AtomicInteger requests = new AtomicInteger();
	private volatile int status = 200;
	private volatile byte[] body = new byte[0];
	private volatile boolean truncated;

	private TestHttpServer() throws IOException {
		server = HttpServer.create(new InetSocketAddress(InetAddress.getLoopbackAddress(), 0), 0);
		server.createContext("/", this::handle);
		server.start();
	}

	public static TestHttpServer start() throws IOException {
		return new TestHttpServer();
	}

	public String url() {
		InetSocketAddress address = server.getAddress();
		return "http://" + address.getHostString() + ":" + address.getPort() + "/utf_ken_all.zip";
	}

	/**
	 * 指定のステータスと本文で応答します。
	 */
	public TestHttpServer respond(int status, byte[] body) {
		this.status = status;
		this.body = body;
		this.truncated = false;
		return this;
	}

	/**
	 * 本文を送り切る前に接続を切ります。
	 */
	public TestHttpServer respondTruncated(byte[] body) {
		this.status = 200;
		this.body = body;
		this.truncated = true;
		return this;
	}

	public int requestCount() {
		return requests.get();
	}

	private void handle(HttpExchange exchange) throws IOException {
		requests.incrementAndGet();
		try {
			byte[] b = body;
			long length = truncated ? b.length + 1024L : (b.length == 0 ? -1 : b.length);
			exchange.sendResponseHeaders(status, length);
			if (b.length > 0) {
				OutputStream out = exchange.getResponseBody();
				out.write(b);
				out.flush();
			}
		} finally {
			// 本文を送り切っていない場合は、接続が切断される
			exchange.close();
		}
	}

	@Override
	public void close() {
		server.stop(0);
	}

	/**
	 * 1エントリーのZipファイルのバイト列を作成します。
	 */
	public static byte[] zip(String entryName, byte[] content) throws IOException {
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		try (ZipOutputStream zos = new ZipOutputStream(bytes)) {
			zos.putNextEntry(new ZipEntry(entryName));
			zos.write(content);
			zos.closeEntry();
		}
		return bytes.toByteArray();
	}

	/**
	 * テスト用CSV（src/test/resources/utf_ken_all_sample.csv）のバイト列を取得します。
	 */
	public static byte[] sampleCsv() throws Exception {
		return Files.readAllBytes(Path.of(TestHttpServer.class.getResource("/utf_ken_all_sample.csv").toURI()));
	}
}
