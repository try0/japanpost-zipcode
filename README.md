♯ utf_ken_all.zipユーティリティー

```java
ZipcodeCSV csv = ZipcodeCSV.getOrDownload();
Iterable<ZipcodeCSVRow> rows = csv.getRows();
```

[住所の郵便番号（1レコード1行、UTF-8形式）（CSV形式）](https://www.post.japanpost.jp/zipcode/download.html)  
 ↓ Download  
java.io.tmpdir  
 ↓ CSV読み込み  
Iterable\<ZipcodeCSVRow\>  
