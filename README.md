# utf_ken_all.zipユーティリティー

[![Maven Central](https://img.shields.io/maven-central/v/jp.try0.zipcode/japanpost-zipcode-core)](https://central.sonatype.com/artifact/jp.try0.zipcode/japanpost-zipcode-core)

[https://www.post.japanpost.jp/zipcode/download.html](https://www.post.japanpost.jp/zipcode/download.html)   
[https://www.post.japanpost.jp/zipcode/dl/utf-zip.html](https://www.post.japanpost.jp/zipcode/dl/utf-zip.html)

```xml
<dependency>
  <groupId>jp.try0.zipcode</groupId>
  <artifactId>japanpost-zipcode-core</artifactId>
  <version>1.0.0</version>
</dependency>
```

```java
ZipcodeCSV csv = ZipcodeCSV.getOrDownload();

// 絞り込み・集計・検索
List<ZipcodeCSVRow> tokyo = csv.withRows(rows -> rows
        .filter(row -> "東京都".equals(row.getPrefectureName()))
        .collect(Collectors.toList()));

// 全行に処理（DB登録など）
csv.forEachRow(row -> {
    // ...
});
```

CSVは1行ずつ読み込むため、全行をメモリーに展開しません。ファイルは処理の終了時（途中終了・例外発生時を含む）に自動で閉じます。

| 用途 | メソッド | メモリー使用量 | ファイルを閉じる |
|---|---|---|---|
| 絞り込み・集計・検索 | `withRows(rows -> ...)` | 結果の分のみ | 自動 |
| 全行に処理 | `forEachRow(row -> ...)` | ほぼ一定 | 自動 |
| 全行をメモリーに保持 | `getRowsAsList()` | 約30MB | 自動 |
| Streamを呼び出し側で管理 | `openRowStream()` | ほぼ一定 | try-with-resourcesで閉じる |

`withRows`の外ではファイルが閉じているため、Streamそのものを戻り値にしないでください。

 
utf_ken_all.zip  
 ↓ Download  
キャッシュディレクトリー（既定はjava.io.tmpdir）  
 ↓ CSV読み込み  
ZipcodeCSVRow  



## 設定

Javaコード、またはシステムプロパティーで変更できます。両方で設定した項目は、Javaコードの値を優先します。

```java
ZipcodeCSVProperties props = ZipcodeCSVProperties.builder()
        .cacheDir(Path.of("/var/cache/myapp/zipcode"))
        .fallbackToCache(true)
        .build();

ZipcodeCSV csv = ZipcodeCSV.getOrDownload(props);
```

```
-Djp.try0.zipcode.csv.cache_dir=/var/cache/myapp/zipcode -Djp.try0.zipcode.csv.fallback_to_cache=true
```

| ビルダー | システムプロパティー | 内容 | 既定値 |
|---|---|---|---|
| `cacheDir(Path)` | `jp.try0.zipcode.csv.cache_dir` | ダウンロードしたファイルの保存先 | `java.io.tmpdir`配下の`jp.try0.zipcode` |
| `cacheDays(int)` | `jp.try0.zipcode.csv.cache_days` | キャッシュを使用する日数 | `10` |
| `fallbackToCache(boolean)` | `jp.try0.zipcode.csv.fallback_to_cache` | ダウンロードに失敗した場合、期限切れのキャッシュを使用する（`true`）か、例外をスローする（`false`）か | `false` |
| `csvEncoding(Charset)` | `jp.try0.zipcode.csv.encoding` | CSVの文字コード | `UTF-8` |
| `downloadUrl(String)` | `jp.try0.zipcode.url.utf_ken_all` | ダウンロードURL | 日本郵便のutf_ken_all.zip |
| `connectionTimeout(Duration)` | `jp.try0.zipcode.http.connection_timeout`（ミリ秒） | 接続タイムアウト | 10秒 |
| `readTimeout(Duration)` | `jp.try0.zipcode.http.read_timeout`（ミリ秒） | 読み込みタイムアウト | 60秒 |

ビルダーで設定しなかった項目は、システムプロパティー、既定値の順に使用します。設定を引数に取らないメソッド（`getOrDownload()`など）は、システムプロパティーと既定値を使用します。  
設定を受け取るメソッド：`ZipcodeCSV.getOrDownload(props)`、`ZipcodeCSV.deleteFiles(props)`、`new ZipcodeCSVParser(file, props)`、`ZipcodeCSVDownloader#download(props)`、`ZipcodeCSVDownloader#existsDownloadUrl(props)`

Linuxでは`java.io.tmpdir`（`/tmp`）を全ユーザーで共有するため、サーバーで使用する場合は`cache_dir`にアプリケーション専用のディレクトリーを指定してください。  
キャッシュの保存先では、ライブラリーが作成するファイル（`utf_ken_all.zip`、`utf_ken_all.csv`、`utf_ken_all*.tmp`）のみ書き込み・削除します。  
`fallback_to_cache`で期限切れのキャッシュを使用した場合は、`System.Logger`（既定ではjava.util.logging）に警告を出力します。

## Zipファイルを直接読み込む

ダウンロードしたZipファイルを直接読み込む場合も、同じメソッドを使用できます。Zipファイルは解凍せずに読み込むため、キャッシュディレクトリーなどにファイルを書き出しません。

```java
ZipcodeCSVDownloader downloader = ZipcodeCSVDownloader.get();
File zipFile = downloader.download();
ZipcodeCSVParser parser = new ZipcodeCSVParser(zipFile);
parser.forEachRow(row -> {
    // ...
});
```

## 1.0.0での変更（0.0.2から）

ファイルを閉じ忘れる原因になるため、`Iterable`を返すメソッドを削除しました。

| 削除 | 移行先 |
|---|---|
| `ZipcodeCSV#getRows()` | `withRows` / `forEachRow` |
| `ZipcodeCSVParser#parse()` | `withRows` / `forEachRow` |
| `ZipcodeFiles.TMP_DIR_PATH` | `ZipcodeFiles.getCacheDir()` |
| `ZipcodeFiles.ZIP_PATH` | `ZipcodeFiles.getZipPath()` |
| `ZipcodeFiles.CSV_PATH` | `ZipcodeFiles.getCSVPath()` |
| `ZipcodeFiles.WORK_PATH` | なし（作業ディレクトリーを使用しなくなりました） |
| `ZipcodeFiles.unzipIfNeeded(File)` | `ZipcodeFiles.openCSV(File)`（解凍せずに読み込む） |
| `ZipcodeFiles.BUFFER` | なし（未使用のため削除） |

その他の変更

- キャッシュの保存先と、ダウンロード失敗時の動作を設定できるようにしました（[設定](#設定)）。
- 設定をJavaコードで指定できるようにしました（`ZipcodeCSVProperties.builder()`）。`ZipcodeCSVProperties`は不変オブジェクトになり、継承できません（`final`）。
- `ZipcodeCSV.deleteFiles()`（`ZipcodeFiles.clean()`）は、保存先ディレクトリーを丸ごと削除せず、ライブラリーが作成したファイルのみ削除します。
- ダウンロード・解凍は一時ファイルに書き込んでから置き換えるため、失敗しても前回のファイルは壊れません。受信が途中で切れた場合は例外をスローします。
- Zipファイルにutf_ken_all.csvが含まれていない場合、`getOrDownload()`は`ZipcodeCSVException`をスローします。
- `ZipcodeCSVParser`はZipファイルを解凍せずに読み込みます。これまではキャッシュのCSVを上書きしていました。壊れたZipファイル（途中までしかないものなど）は`ZipException`をスローします。
- `ZipcodeCSVRow`に`equals`・`hashCode`・`toString`を実装しました。`equals`は各列の値を比較し、行番号（`getRowNumber()`）は比較しません。
- `ZipcodeCSVRow`に、「1」は該当・「0」は該当せずの表示をbooleanで返すメソッドを追加しました。既存の`get～`（int）はそのまま使用できます。

  | 追加（boolean） | 既存（int） |
  |---|---|
  | `hasMultiZipcodeArea()` | `getHasMultiZipcodeArea()` |
  | `isPerKoaza()` | `getIsPerKoaza()` |
  | `hasChoume()` | `getHasChoume()` |
  | `hasMultiAreaZipcode()` | `getHasMultiAreaZipcode()` |
- システムプロパティーに不正な値（数値以外、負の値）を指定した場合、プロパティー名を含む`IllegalArgumentException`をスローします。
- Javaモジュールシステム用に、モジュール名`jp.try0.zipcode.japanpost`（`Automatic-Module-Name`）を設定しました。
- `Prefecture.HOKKAIDO.getName()`は「北海」ではなく「北海道」を返します（`getFullName()`は変更なし）。

依存するcommons-csvを1.14.1に更新したため、commons-ioとcommons-codecが推移的依存関係として追加されます。
