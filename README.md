# utf_ken_all.zipユーティリティー

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

システムプロパティーで変更できます。

| システムプロパティー | 内容 | 既定値 |
|---|---|---|
| `jp.try0.zipcode.csv.cache_dir` | ダウンロードしたファイルの保存先 | `java.io.tmpdir`配下の`jp.try0.zipcode` |
| `jp.try0.zipcode.csv.cache_days` | キャッシュを使用する日数 | `10` |
| `jp.try0.zipcode.csv.fallback_to_cache` | ダウンロードに失敗した場合、期限切れのキャッシュを使用する（`true`）か、例外をスローする（`false`）か | `false` |
| `jp.try0.zipcode.csv.encoding` | CSVの文字コード | `UTF-8` |
| `jp.try0.zipcode.url.utf_ken_all` | ダウンロードURL | 日本郵便のutf_ken_all.zip |
| `jp.try0.zipcode.http.connection_timeout` | 接続タイムアウト（ミリ秒） | `10000` |
| `jp.try0.zipcode.http.read_timeout` | 読み込みタイムアウト（ミリ秒） | `60000` |

```
-Djp.try0.zipcode.csv.cache_dir=/var/cache/myapp/zipcode -Djp.try0.zipcode.csv.fallback_to_cache=true
```

Linuxでは`java.io.tmpdir`（`/tmp`）を全ユーザーで共有するため、サーバーで使用する場合は`cache_dir`にアプリケーション専用のディレクトリーを指定してください。  
キャッシュの保存先では、ライブラリーが作成するファイル（`utf_ken_all.zip`、`utf_ken_all.csv`、`utf_ken_all*.tmp`）のみ書き込み・削除します。  
`fallback_to_cache`で期限切れのキャッシュを使用した場合は、`System.Logger`（既定ではjava.util.logging）に警告を出力します。

## Zipファイルを直接読み込む

ダウンロードしたZipファイルを直接読み込む場合も、同じメソッドを使用できます。

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

その他の変更

- キャッシュの保存先と、ダウンロード失敗時の動作を設定できるようにしました（[設定](#設定)）。
- `ZipcodeCSV.deleteFiles()`（`ZipcodeFiles.clean()`）は、保存先ディレクトリーを丸ごと削除せず、ライブラリーが作成したファイルのみ削除します。
- ダウンロード・解凍は一時ファイルに書き込んでから置き換えるため、失敗しても前回のファイルは壊れません。受信が途中で切れた場合は例外をスローします。
- Zipファイルにutf_ken_all.csvが含まれていない場合、`getOrDownload()`は`ZipcodeCSVException`をスローします。

依存するcommons-csvを1.14.1に更新したため、commons-ioとcommons-codecが推移的依存関係として追加されます。
