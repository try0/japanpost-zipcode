# utf_ken_all.zipユーティリティー

[https://www.post.japanpost.jp/zipcode/download.html](https://www.post.japanpost.jp/zipcode/download.html)   
[https://www.post.japanpost.jp/zipcode/dl/utf-zip.html](https://www.post.japanpost.jp/zipcode/dl/utf-zip.html)

```xml
<dependency>
  <groupId>jp.try0.zipcode</groupId>
  <artifactId>japanpost-zipcode-core</artifactId>
  <version>0.0.1</version>
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
java.io.tmpdir  
 ↓ CSV読み込み  
ZipcodeCSVRow  



ダウンロードしたZipファイルを直接読み込む場合も、同じメソッドを使用できます。

```java
ZipcodeCSVDownloader downloader = ZipcodeCSVDownloader.get();
File zipFile = downloader.download();
ZipcodeCSVParser parser = new ZipcodeCSVParser(zipFile);
parser.forEachRow(row -> {
    // ...
});
```

## 0.0.2からの変更

ファイルを閉じ忘れる原因になるため、`Iterable`を返すメソッドを削除しました。

| 削除 | 移行先 |
|---|---|
| `ZipcodeCSV#getRows()` | `withRows` / `forEachRow` |
| `ZipcodeCSVParser#parse()` | `withRows` / `forEachRow` |
