package jp.try0.zipcode.japanpost.csv.data;

import org.apache.commons.csv.CSVRecord;

// TODO 注釈記載
/**
 * 郵便番号データファイルの形式
 * 
 * @see https://www.post.japanpost.jp/zipcode/dl/utf-readme.html
 */
public enum ZipcodeCSVColumnType {
	/**
	 * 全国地方公共団体コード（JIS X0401、X0402） 半角数字<br>
	 *
	 * @see https://www.soumu.go.jp/denshijiti/code.html
	 */
	COL_0_LOCAL_GOVERNMENT_CODE("全国地方公共団体コード", "（JIS X0401、X0402） 半角数字") {
		@Override
		void assign(ZipcodeCSVRow row, CSVRecord csvRecord) {
			row.setLocalGovernmentCode(getValue(csvRecord));
		}

		@Override
		void assign(ZipcodeCSVRow row, String value) {
			row.setLocalGovernmentCode(value);
		}
	},
	/**
	 * （旧）郵便番号（5桁） 半角数字
	 */
	COL_1_LEGACY_ZIPCODE("（旧）郵便番号", "（5桁） 半角数字") {
		@Override
		void assign(ZipcodeCSVRow row, CSVRecord csvRecord) {
			row.setLegacyZipcode(getValue(csvRecord));
		}

		@Override
		void assign(ZipcodeCSVRow row, String value) {
			row.setLegacyZipcode(value);
		}
	},
	/**
	 * 郵便番号（7桁） 半角数字
	 */
	COL_2_ZIPCODE("郵便番号", "（7桁） 半角数字") {
		@Override
		void assign(ZipcodeCSVRow row, CSVRecord csvRecord) {
			row.setZipcode(getValue(csvRecord));
		}

		@Override
		void assign(ZipcodeCSVRow row, String value) {
			row.setZipcode(value);
		}
	},
	/**
	 * 都道府県名 半角カタカナ（コード順に掲載）
	 */
	COL_3_PREFECTURE_NAME_KANA("都道府県名 半角カタカナ", "（コード順に掲載）") {
		@Override
		void assign(ZipcodeCSVRow row, CSVRecord csvRecord) {
			row.setPrefectureNameKana(getValue(csvRecord));
		}

		@Override
		void assign(ZipcodeCSVRow row, String value) {
			row.setPrefectureNameKana(value);
		}
	},
	/**
	 * 市区町村名 半角カタカナ（コード順に掲載）
	 */
	COL_4_CITY_NAME_KANA("市区町村名 半角カタカナ", "（コード順に掲載）") {
		@Override
		void assign(ZipcodeCSVRow row, CSVRecord csvRecord) {
			row.setCityNameKana(getValue(csvRecord));
		}

		@Override
		void assign(ZipcodeCSVRow row, String value) {
			row.setCityNameKana(value);
		}
	},
	/**
	 * 町域名 半角カタカナ（五十音順に掲載）
	 */
	COL_5_AREA_NAME_KANA("町域名 半角カタカナ", "（五十音順に掲載）") {
		@Override
		void assign(ZipcodeCSVRow row, CSVRecord csvRecord) {
			row.setAreaNameKana(getValue(csvRecord));
		}

		@Override
		void assign(ZipcodeCSVRow row, String value) {
			row.setAreaNameKana(value);
		}
	},
	/**
	 * 都道府県名 漢字（コード順に掲載）
	 */
	COL_6_PREFECTURE_NAME("都道府県名 漢字", "（コード順に掲載）") {
		@Override
		void assign(ZipcodeCSVRow row, CSVRecord csvRecord) {
			row.setPrefectureName(getValue(csvRecord));
		}

		@Override
		void assign(ZipcodeCSVRow row, String value) {
			row.setPrefectureName(value);
		}
	},
	/**
	 * 市区町村名 漢字（コード順に掲載）
	 */
	COL_7_CITY_NAME("市区町村名 漢字", "（コード順に掲載）") {
		@Override
		void assign(ZipcodeCSVRow row, CSVRecord csvRecord) {
			row.setCityName(getValue(csvRecord));
		}

		@Override
		void assign(ZipcodeCSVRow row, String value) {
			row.setCityName(value);
		}
	},
	/**
	 * 町域名 漢字（五十音順に掲載）
	 */
	COL_8_AREA_NAME("町域名 漢字", "（五十音順に掲載）") {
		@Override
		void assign(ZipcodeCSVRow row, CSVRecord csvRecord) {
			row.setAreaName(getValue(csvRecord));
		}

		@Override
		void assign(ZipcodeCSVRow row, String value) {
			row.setAreaName(value);
		}
	},
	/**
	 * 一町域が二以上の郵便番号で表される場合の表示 （注3） （「1」は該当、「0」は該当せず）
	 */
	COL_9_HAS_MULTI_ZIPCODE_AREA("一町域が二以上の郵便番号で表される場合の表示 ", "（注3） （「1」は該当、「0」は該当せず）") {
		@Override
		void assign(ZipcodeCSVRow row, CSVRecord csvRecord) {
			row.setHasMultiZipcodeArea(Integer.valueOf(getValue(csvRecord)));
		}

		@Override
		void assign(ZipcodeCSVRow row, String value) {
			row.setHasMultiZipcodeArea(Integer.valueOf(value));
		}
	},
	/**
	 * 小字毎に番地が起番されている町域の表示 （注4） （「1」は該当、「0」は該当せず）
	 */
	COL_10_IS_PER_KOAZA("小字毎に番地が起番されている町域の表示", " （注4） （「1」は該当、「0」は該当せず）") {
		@Override
		void assign(ZipcodeCSVRow row, CSVRecord csvRecord) {
			row.setIsPerKoaza(Integer.valueOf(getValue(csvRecord)));
		}

		@Override
		void assign(ZipcodeCSVRow row, String value) {
			row.setIsPerKoaza(Integer.valueOf(value));
		}
	},
	/**
	 * 丁目を有する町域の場合の表示 （「1」は該当、「0」は該当せず）
	 */
	COL_11_HAS_CHOUME("丁目を有する町域の場合の表示", " （「1」は該当、「0」は該当せず）") {
		@Override
		void assign(ZipcodeCSVRow row, CSVRecord csvRecord) {
			row.setHasChoume(Integer.valueOf(getValue(csvRecord)));
		}

		@Override
		void assign(ZipcodeCSVRow row, String value) {
			row.setHasChoume(Integer.valueOf(value));
		}
	},
	/**
	 * 一つの郵便番号で二以上の町域を表す場合の表示 （注5） （「1」は該当、「0」は該当せず）
	 */
	COL_12_HAS_MULTI_AREA_ZIPCODE("一つの郵便番号で二以上の町域を表す場合の表示", " （注5） （「1」は該当、「0」は該当せず）") {
		@Override
		void assign(ZipcodeCSVRow row, CSVRecord csvRecord) {
			row.setHasMultiAreaZipcode(Integer.valueOf(getValue(csvRecord)));
		}

		@Override
		void assign(ZipcodeCSVRow row, String value) {
			row.setHasMultiAreaZipcode(Integer.valueOf(value));
		}
	},
	/**
	 * 更新の表示（注6）（「0」は変更なし、「1」は変更あり、「2」廃止（廃止データのみ使用））
	 */
	COL_13_UPDATE_STATE("更新の表示", "（注6）（「0」は変更なし、「1」は変更あり、「2」廃止（廃止データのみ使用））") {
		@Override
		void assign(ZipcodeCSVRow row, CSVRecord csvRecord) {
			row.setUpdateState(Integer.valueOf(getValue(csvRecord)));
		}

		@Override
		void assign(ZipcodeCSVRow row, String value) {
			row.setUpdateState(Integer.valueOf(value));
		}
	},
	/**
	 * 変更理由
	 * （「0」は変更なし、「1」市政・区政・町政・分区・政令指定都市施行、「2」住居表示の実施、「3」区画整理、「4」郵便区調整等、「5」訂正、「6」廃止（廃止データのみ使用））
	 */
	COL_14_UPDATE_REASON("変更理由 ",
			"（「0」は変更なし、「1」市政・区政・町政・分区・政令指定都市施行、「2」住居表示の実施、「3」区画整理、「4」郵便区調整等、「5」訂正、「6」廃止（廃止データのみ使用））") {
		@Override
		void assign(ZipcodeCSVRow row, CSVRecord csvRecord) {
			row.setUpdateReason(Integer.valueOf(getValue(csvRecord)));
		}

		@Override
		void assign(ZipcodeCSVRow row, String value) {
			row.setUpdateReason(Integer.valueOf(value));
		}
	},

	;

	/**
	 * 以下に掲載がない場合
	 */
	public static final String NOT_LISTED_STRING = "以下に掲載がない場合";

	String description;
	String supplement;

	private ZipcodeCSVColumnType(String description, String supplement) {
		this.description = description;
		this.supplement = supplement;
	}

	/**
	 * rowに値を設定します。
	 *
	 * @param row
	 * @param csvRecord
	 */
	abstract void assign(ZipcodeCSVRow row, CSVRecord csvRecord);

	/**
	 * rowに値を設定します。
	 * 
	 * @param row
	 * @param value
	 */
	abstract void assign(ZipcodeCSVRow row, String value);

	public String getSimpleDescription() {
		return description;
	}

	public String getFullDescription() {
		return description + supplement;
	}

	public String getValue(CSVRecord csvRecord) {
		return csvRecord.get(ordinal());
	}

	/**
	 * {@link ZipcodeCSVRow}へ{@link CSVRecord}から対応するカラムの値を設定します。
	 *
	 * @param row
	 * @param csvRecord
	 */
	public void assignColumnValue(ZipcodeCSVRow row, CSVRecord csvRecord) {
		try {
			assign(row, csvRecord);
		} catch (Exception e) {
			throw new IllegalArgumentException(name() + "　データが正しくない可能性があります.");
		}
	}

	/**
	 * {@link ZipcodeCSVRow}へ{@link CSVRecord}から対応するカラムの値を設定します。
	 * 
	 * @param row
	 * @param value
	 */
	public void assignColumnValue(ZipcodeCSVRow row, String value) {
		try {
			assign(row, value);
		} catch (Exception e) {
			throw new IllegalArgumentException(name() + "　データが正しくない可能性があります.");
		}
	}
}
