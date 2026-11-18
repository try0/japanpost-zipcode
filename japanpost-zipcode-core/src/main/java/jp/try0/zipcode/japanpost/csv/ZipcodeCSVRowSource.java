package jp.try0.zipcode.japanpost.csv;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import jp.try0.zipcode.japanpost.csv.data.ZipcodeCSVRow;

/**
 * 郵便番号データの行を読み込む操作<br>
 * ファイルは先頭から1行ずつ読み込み、{@link #getRowsAsList()}以外は全行をメモリーに展開しません。
 *
 * <table>
 * <caption>用途別のメソッド</caption>
 * <tr><th scope="col">用途</th><th scope="col">メソッド</th><th scope="col">ファイルを閉じる</th></tr>
 * <tr><td>絞り込み・集計・検索</td><td>{@link #withRows(Function)}</td><td>自動</td></tr>
 * <tr><td>全行に処理（DB登録など）</td><td>{@link #forEachRow(Consumer)}</td><td>自動</td></tr>
 * <tr><td>全行をメモリーに保持</td><td>{@link #getRowsAsList()}</td><td>自動</td></tr>
 * <tr><td>Streamを呼び出し側で管理</td><td>{@link #openRowStream()}</td><td>呼び出し側</td></tr>
 * </table>
 */
public interface ZipcodeCSVRowSource {

	/**
	 * 行データの{@link Stream}を開きます。<br>
	 * ファイルを開いた状態で返すため、呼び出し側でtry-with-resourcesを使用してクローズしてください。<br>
	 * 通常は、ファイルを自動で閉じる{@link #withRows(Function)}・{@link #forEachRow(Consumer)}を使用してください。
	 *
	 * <pre>
	 * try (Stream&lt;ZipcodeCSVRow&gt; rows = source.openRowStream()) {
	 *     // ...
	 * }
	 * </pre>
	 *
	 * @return 行データのStream
	 * @throws IOException ファイルを開けない場合
	 */
	Stream<ZipcodeCSVRow> openRowStream() throws IOException;

	/**
	 * 行データの{@link Stream}を関数に渡し、その結果を返します。<br>
	 * 関数の終了後（例外発生時を含む）にファイルを閉じます。途中で走査をやめても問題ありません。
	 *
	 * <pre>
	 * List&lt;ZipcodeCSVRow&gt; tokyo = source.withRows(rows -&gt; rows
	 *         .filter(row -&gt; "東京都".equals(row.getPrefectureName()))
	 *         .collect(Collectors.toList()));
	 * </pre>
	 *
	 * 関数の外ではファイルが閉じているため、Streamやイテレーターを結果として返さないでください。
	 *
	 * @param <R>      結果の型
	 * @param function 行データのStreamを受け取る関数
	 * @return 関数の結果
	 * @throws IOException ファイルを開けない場合
	 */
	default <R> R withRows(Function<? super Stream<ZipcodeCSVRow>, ? extends R> function) throws IOException {
		Objects.requireNonNull(function, "function");
		try (Stream<ZipcodeCSVRow> rows = openRowStream()) {
			return function.apply(rows);
		}
	}

	/**
	 * 全行に対して処理を行います。<br>
	 * 処理の終了後（例外発生時を含む）にファイルを閉じます。
	 *
	 * @param action 行ごとの処理
	 * @throws IOException ファイルを開けない場合
	 */
	default void forEachRow(Consumer<? super ZipcodeCSVRow> action) throws IOException {
		Objects.requireNonNull(action, "action");
		withRows(rows -> {
			rows.forEach(action);
			return null;
		});
	}

	/**
	 * 全行を読み込んだリストを取得します。<br>
	 * 全国分（約12万行）でおよそ30MBのメモリーを使用します。必要な行が限られる場合は{@link #withRows(Function)}を使用してください。
	 *
	 * @return 行リスト
	 * @throws IOException ファイルを開けない場合
	 */
	default List<ZipcodeCSVRow> getRowsAsList() throws IOException {
		return withRows(rows -> rows.collect(Collectors.toList()));
	}
}
