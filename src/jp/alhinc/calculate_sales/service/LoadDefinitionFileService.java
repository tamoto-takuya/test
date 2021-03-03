package jp.alhinc.calculate_sales.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import jp.alhinc.calculate_sales.bean.Branch;
import jp.alhinc.calculate_sales.bean.Branches;
import jp.alhinc.calculate_sales.common.Const;
import jp.alhinc.calculate_sales.common.MessageConst;

public class LoadDefinitionFileService {

	/**
	 * 支店定義ファイルの存在有無を返却する
	 *
	 * @param path
	 *            検索対象ディレクトリパス
	 * @return 支店定義ファイルの存在有無
	 */
	public static boolean existsBranchDefinitionFile(String path) {

		File file = new File(path, Const.BRANCH_DEFINITION_FILE_NAME);
		Boolean fileExists = file.exists();

//		System.out.println(fileExists);

		return fileExists;
	}

	/**
	 * 支店定義ファイルの読み込み
	 *
	 * @param path
	 *            検索対象ディレクトリパス
	 * @return 支店情報一覧
	 * @throws IOException
	 *             支店定義ファイル読み込みに失敗した場合
	 */
	public static Branches load(String path) throws IOException {

		Branches branches = new Branches();
		File file = new File(path, Const.BRANCH_DEFINITION_FILE_NAME);
		BufferedReader br = null;

		try {
			FileReader fr = new FileReader(file);
			br = new BufferedReader(fr);

			String line;

			while((line = br.readLine()) != null) {

				if (isCorrectLine(line)) {
					String[] branchFileList = line.split(Const.ELEMENT_SEPARATOR_CHAR, -1);
					String code = branchFileList[0];
					String name = branchFileList[1];

					Branch branch = new Branch(code, name);
					branches.addBranch(branch);
				} else {
					branches.setErrorMessage(MessageConst.ERROR_DEFINITION_FILE_MALFORMATTED);
					break;
				}
			}

		} catch(IOException e) {
			branches.setErrorMessage(MessageConst.ERROR_OTHERS);

		} finally {
				br.close();
		}

		return branches;
	}

	/**
	 * 支店定義のフォーマットチェックを行う
	 *
	 * @param line
	 *            支店定義行
	 * @return フォーマットの妥当性
	 */
	private static boolean isCorrectLine(String line) {

		if (line == null) {
			return false;
		}

		String[] branch = line.split(Const.ELEMENT_SEPARATOR_CHAR, -1);
		if (branch.length != 2) {
			return false;
		}

		String code = branch[0];
		String name = branch[1];

		return isCorrectCodeFormat(code) == true && isCorrectNameFormat(name) == true;

	}

	/**
	 * 支店コードの妥当性チェックを行う
	 *
	 * @param code
	 *            支店コード
	 * @return 支店コードの妥当性
	 */
	private static boolean isCorrectCodeFormat(String code) {
		if (code.length() != 3) {
			return false;
		}

		for (int i = 0; i < code.length(); i++) {
			if(!Character.isDigit(code.charAt(i))) {
				return false;
			}
		}

		return true;
	}

	/**
	 * 支店名の妥当性チェックを行う
	 *
	 * @param name
	 *            支店名
	 * @return 支店名の妥当性
	 */
	private static boolean isCorrectNameFormat(String name) {
		if (name == null || name.isEmpty()) {
			return false;
		}

		return true;
	}
}
