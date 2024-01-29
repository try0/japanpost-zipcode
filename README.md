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
Iterable<ZipcodeCSVRow> rows = csv.getRows();
```

 
utf_ken_all.zip  
 ↓ Download  
java.io.tmpdir  
 ↓ CSV読み込み  
Iterable\<ZipcodeCSVRow\>  



```java
ZipcodeCSVDownloader downloader = ZipcodeCSVDownloader.get();
File zipFile = downloader.download();
ZipcodeCSVParser parser = new ZipcodeCSVParser(zipFile);
Iterable<ZipcodeCSVRow> rows = parser.parse();
```
