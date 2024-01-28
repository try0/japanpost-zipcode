package jp.try0.zipcode.japanpost.csv.data;

import java.io.Serializable;
import java.lang.reflect.Field;

import org.apache.commons.csv.CSVRecord;

/**
 * Csv行データ<br>
 * 
 * @see https://www.post.japanpost.jp/zipcode/dl/utf-readme.html
 */
public class ZipcodeCSVRow implements Serializable {

	/**
	 * 
	 * @param csvRecord
	 * @return
	 */
	public static ZipcodeCSVRow fromCSVRecord(CSVRecord csvRecord) {
		ZipcodeCSVRow row = new ZipcodeCSVRow();
		row.rowNumber = csvRecord.getRecordNumber();
		for (ZipcodeCSVColumnType col : ZipcodeCSVColumnType.values()) {
			col.assign(row, csvRecord);
		}
		return row;
	}

	/**
	 * CSV行番号
	 */
	private long rowNumber;
	/**
	 * 全国地方公共団体コード（JIS X0401、X0402） 半角数字<br>
	 *
	 * @see https://www.soumu.go.jp/denshijiti/code.html
	 */
	private String localGovernmentCode;
	/**
	 * （旧）郵便番号（5桁） 半角数字
	 */
	private String legacyZipcode;
	/**
	 * 郵便番号（7桁） 半角数字
	 */
	private String zipcode;
	/**
	 * 都道府県名 半角カタカナ（コード順に掲載）
	 */
	private String prefectureNameKana;
	/**
	 * 市区町村名 半角カタカナ（コード順に掲載）
	 */
	private String cityNameKana;
	/**
	 * 町域名 半角カタカナ（五十音順に掲載）
	 */
	private String areaNameKana;
	/**
	 * 都道府県名 漢字（コード順に掲載）
	 */
	private String prefectureName;
	/**
	 * 市区町村名 漢字（コード順に掲載）
	 */
	private String cityName;
	/**
	 * 町域名 漢字（五十音順に掲載）
	 */
	private String areaName;
	/**
	 * 一町域が二以上の郵便番号で表される場合の表示 （注3） （「1」は該当、「0」は該当せず）
	 */
	private int hasMultiZipcodeArea;
	/**
	 * 小字毎に番地が起番されている町域の表示 （注4） （「1」は該当、「0」は該当せず）
	 */
	private int isPerKoaza;
	/**
	 * 丁目を有する町域の場合の表示 （「1」は該当、「0」は該当せず）
	 */
	private int hasChoume;
	/**
	 * 一つの郵便番号で二以上の町域を表す場合の表示 （注5） （「1」は該当、「0」は該当せず）
	 */
	private int hasMultiAreaZipcode;
	/**
	 * 更新の表示（注6）（「0」は変更なし、「1」は変更あり、「2」廃止（廃止データのみ使用））
	 */
	private int updateState;
	/**
	 * 変更理由
	 * （「0」は変更なし、「1」市政・区政・町政・分区・政令指定都市施行、「2」住居表示の実施、「3」区画整理、「4」郵便区調整等、「5」訂正、「6」廃止（廃止データのみ使用））
	 */
	private int updateReason;

	/**
	 * CSV行番号を取得します。
	 * 
	 * @return CSV行番号
	 */
	public long getRowNumber() {
		return rowNumber;
	}

	/**
	 * CSV行番号をセットします。
	 * 
	 * @param rowNumber CSV行番号
	 */
	public void setRowNumber(long rowNumber) {
		this.rowNumber = rowNumber;
	}

	/**
	 * 全国地方公共団体コード（JIS X0401、X0402） 半角数字を取得します。
	 * 
	 * @see https://www.soumu.go.jp/denshijiti/code.html
	 * @return localGovernmentCode
	 */
	public String getLocalGovernmentCode() {
		return localGovernmentCode;
	}

	/**
	 * 全国地方公共団体コード（JIS X0401、X0402） 半角数字を設定します。
	 * 
	 * @param localGovernmentCode 全国地方公共団体コード
	 */
	public void setLocalGovernmentCode(String localGovernmentCode) {
		this.localGovernmentCode = localGovernmentCode;
	}

	/**
	 * （旧）郵便番号（5桁） 半角数字を取得します。
	 * 
	 * @return （旧）郵便番号（5桁） 半角数字
	 */
	public String getLegacyZipcode() {
		return legacyZipcode;
	}

	/**
	 * （旧）郵便番号（5桁） 半角数字をセットします。
	 * 
	 * @param legacyZipcode （旧）郵便番号（5桁） 半角数字
	 */
	public void setLegacyZipcode(String legacyZipcode) {
		this.legacyZipcode = legacyZipcode;
	}

	/**
	 * 郵便番号（7桁） 半角数字を取得します。
	 * 
	 * @return 郵便番号（7桁） 半角数字
	 */
	public String getZipcode() {
		return zipcode;
	}

	/**
	 * 郵便番号（7桁） 半角数字をセットします。
	 * 
	 * @param zipcode 郵便番号（7桁） 半角数字
	 */
	public void setZipcode(String zipcode) {
		this.zipcode = zipcode;
	}

	/**
	 * 都道府県名 半角カタカナ（コード順に掲載）を取得します。
	 * 
	 * @return 都道府県名 半角カタカナ（コード順に掲載）
	 */
	public String getPrefectureNameKana() {
		return prefectureNameKana;
	}

	/**
	 * 都道府県名 半角カタカナ（コード順に掲載）をセットします。
	 * 
	 * @param prefectureNameKana 都道府県名 半角カタカナ（コード順に掲載）
	 */
	public void setPrefectureNameKana(String prefectureNameKana) {
		this.prefectureNameKana = prefectureNameKana;
	}

	/**
	 * 市区町村名 半角カタカナ（コード順に掲載）を取得します。
	 * 
	 * @return 市区町村名 半角カタカナ（コード順に掲載）
	 */
	public String getCityNameKana() {
		return cityNameKana;
	}

	/**
	 * 市区町村名 半角カタカナ（コード順に掲載）を設定します。
	 * 
	 * @param cityNameKana 市区町村名 半角カタカナ（コード順に掲載）
	 */
	public void setCityNameKana(String cityNameKana) {
		this.cityNameKana = cityNameKana;
	}

	/**
	 * 町域名 半角カタカナ（五十音順に掲載）を取得します。
	 * 
	 * @return 町域名 半角カタカナ（五十音順に掲載）
	 */
	public String getAreaNameKana() {
		return areaNameKana;
	}

	/**
	 * 町域名 半角カタカナ（五十音順に掲載）をセットします。
	 * 
	 * @param areaNameKana 町域名 半角カタカナ（五十音順に掲載）
	 */
	public void setAreaNameKana(String areaNameKana) {
		this.areaNameKana = areaNameKana;
	}

	/**
	 * 都道府県名 漢字（コード順に掲載）を取得します。
	 * 
	 * @return 都道府県名 漢字（コード順に掲載）
	 */
	public String getPrefectureName() {
		return prefectureName;
	}

	/**
	 * 都道府県名 漢字（コード順に掲載）をセットします。
	 * 
	 * @param prefectureName 都道府県名 漢字（コード順に掲載）
	 */
	public void setPrefectureName(String prefectureName) {
		this.prefectureName = prefectureName;
	}

	/**
	 * 市区町村名 漢字（コード順に掲載）を取得します。
	 * 
	 * @return 市区町村名 漢字（コード順に掲載）
	 */
	public String getCityName() {
		return cityName;
	}

	/**
	 * 市区町村名 漢字（コード順に掲載）をセットします。
	 * 
	 * @param cityName 市区町村名 漢字（コード順に掲載）
	 */
	public void setCityName(String cityName) {
		this.cityName = cityName;
	}

	/**
	 * 町域名 漢字（五十音順に掲載）を取得します。
	 * 
	 * @return 町域名 漢字（五十音順に掲載）
	 */
	public String getAreaName() {
		return areaName;
	}

	/**
	 * 町域名 漢字（五十音順に掲載）をセットします。
	 * 
	 * @param areaName 町域名 漢字（五十音順に掲載）
	 */
	public void setAreaName(String areaName) {
		this.areaName = areaName;
	}

	/**
	 * 一町域が二以上の郵便番号で表される場合の表示 （注3） （「1」は該当、「0」は該当せず）を取得します。
	 * 
	 * @return 一町域が二以上の郵便番号で表される場合の表示 （注3） （「1」は該当、「0」は該当せず）
	 */
	public int getHasMultiZipcodeArea() {
		return hasMultiZipcodeArea;
	}

	/**
	 * 一町域が二以上の郵便番号で表される場合の表示 （注3） （「1」は該当、「0」は該当せず）をセットします。
	 * 
	 * @param hasMultiZipcodeArea 一町域が二以上の郵便番号で表される場合の表示 （注3） （「1」は該当、「0」は該当せず）
	 */
	public void setHasMultiZipcodeArea(int hasMultiZipcodeArea) {
		this.hasMultiZipcodeArea = hasMultiZipcodeArea;
	}

	/**
	 * 小字毎に番地が起番されている町域の表示 （注4） （「1」は該当、「0」は該当せず）を取得します。
	 * 
	 * @return 小字毎に番地が起番されている町域の表示 （注4） （「1」は該当、「0」は該当せず）
	 */
	public int getIsPerKoaza() {
		return isPerKoaza;
	}

	/**
	 * 小字毎に番地が起番されている町域の表示 （注4） （「1」は該当、「0」は該当せず）をセットします。
	 * 
	 * @param isPerKoaza 小字毎に番地が起番されている町域の表示 （注4） （「1」は該当、「0」は該当せず）
	 */
	public void setIsPerKoaza(int isPerKoaza) {
		this.isPerKoaza = isPerKoaza;
	}

	/**
	 * 丁目を有する町域の場合の表示 （「1」は該当、「0」は該当せず）を取得します。
	 * 
	 * @return 丁目を有する町域の場合の表示 （「1」は該当、「0」は該当せず）
	 */
	public int getHasChoume() {
		return hasChoume;
	}

	/**
	 * 丁目を有する町域の場合の表示 （「1」は該当、「0」は該当せず）をセットします。
	 * 
	 * @param hasChoume 丁目を有する町域の場合の表示 （「1」は該当、「0」は該当せず）
	 */
	public void setHasChoume(int hasChoume) {
		this.hasChoume = hasChoume;
	}

	/**
	 * 一つの郵便番号で二以上の町域を表す場合の表示 （注5） （「1」は該当、「0」は該当せず）を取得します。
	 * 
	 * @return 一つの郵便番号で二以上の町域を表す場合の表示 （注5） （「1」は該当、「0」は該当せず）
	 */
	public int getHasMultiAreaZipcode() {
		return hasMultiAreaZipcode;
	}

	/**
	 * 一つの郵便番号で二以上の町域を表す場合の表示 （注5） （「1」は該当、「0」は該当せず）をセットします。
	 * 
	 * @param hasMultiAreaZipcode 一つの郵便番号で二以上の町域を表す場合の表示 （注5） （「1」は該当、「0」は該当せず）
	 */
	public void setHasMultiAreaZipcode(int hasMultiAreaZipcode) {
		this.hasMultiAreaZipcode = hasMultiAreaZipcode;
	}

	/**
	 * 更新の表示（注6）（「0」は変更なし、「1」は変更あり、「2」廃止（廃止データのみ使用））を取得します。
	 * 
	 * @return 更新の表示（注6）（「0」は変更なし、「1」は変更あり、「2」廃止（廃止データのみ使用））
	 */
	public int getUpdateState() {
		return updateState;
	}

	/**
	 * 更新の表示（注6）（「0」は変更なし、「1」は変更あり、「2」廃止（廃止データのみ使用））をセットします。
	 * 
	 * @param updateState 更新の表示（注6）（「0」は変更なし、「1」は変更あり、「2」廃止（廃止データのみ使用））
	 */
	public void setUpdateState(int updateState) {
		this.updateState = updateState;
	}

	/**
	 * 変更理由を取得します。<br>
	 * （「0」は変更なし、「1」市政・区政・町政・分区・政令指定都市施行、「2」住居表示の実施、「3」区画整理、「4」郵便区調整等、「5」訂正、「6」廃止（廃止データのみ使用））
	 * 
	 * @return 変更理由
	 */
	public int getUpdateReason() {
		return updateReason;
	}

	/**
	 * 変更理由をセットします。<br>
	 * （「0」は変更なし、「1」市政・区政・町政・分区・政令指定都市施行、「2」住居表示の実施、「3」区画整理、「4」郵便区調整等、「5」訂正、「6」廃止（廃止データのみ使用））
	 * 
	 * @param updateReason 変更理由
	 */
	public void setUpdateReason(int updateReason) {
		this.updateReason = updateReason;
	}

	public void debugPrint() {
		for (Field d : getClass().getDeclaredFields()) {
			try {
				d.setAccessible(true);
				Object obj = d.get(this);
				System.out.print(obj + ", ");
			} catch (IllegalArgumentException unthrow) {
				unthrow.printStackTrace();
			} catch (IllegalAccessException unthrow) {
				unthrow.printStackTrace();
			}
		}
		System.out.println();
	}

	public String debugToString() {
		StringBuilder sb = new StringBuilder();
		for (Field d : getClass().getDeclaredFields()) {
			try {
				d.setAccessible(true);
				Object obj = d.get(this);
				sb.append(obj + ", ");
			} catch (IllegalArgumentException unthrow) {
				unthrow.printStackTrace();
			} catch (IllegalAccessException unthrow) {
				unthrow.printStackTrace();
			}
		}

		return sb.toString();
	}
}
