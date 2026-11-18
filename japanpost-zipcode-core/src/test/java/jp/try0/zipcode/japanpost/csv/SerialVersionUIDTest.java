package jp.try0.zipcode.japanpost.csv;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ObjectStreamClass;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import jp.try0.zipcode.japanpost.csv.data.ZipcodeCSVRow;

/**
 * クラスを変更してもシリアライズ済みのデータを読めるよう、serialVersionUIDを固定していることを検証します。
 */
public class SerialVersionUIDTest {

	@ParameterizedTest
	@ValueSource(classes = { ZipcodeCSV.class, ZipcodeCSVRow.class, ZipcodeCSVException.class,
			ZipcodeCSVProperties.class })
	public void declaresFixedSerialVersionUID(Class<?> type) {
		assertEquals(1L, ObjectStreamClass.lookup(type).getSerialVersionUID());
	}
}
