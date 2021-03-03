package jp.alhinc.calculate_sales.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import jp.alhinc.calculate_sales.bean.Branch;
import jp.alhinc.calculate_sales.bean.Branches;
import jp.alhinc.calculate_sales.common.Const;
import jp.alhinc.calculate_sales.common.MessageConst;

public class SummaryService {

	/**
	 * 売上集計処理
	 *
	 * @param path
	 *            ディレクトリパス
	 * @param branches
	 *            支店情報一覧
	 * @return 売上集計後の支店情報一覧
	 * @throws IOException
	 *             売上ファイル読み込みに失敗した場合
	 */
	public static Branches summarize(String path, Branches branches) throws IOException {

		Branches summarizedBranches = new Branches();

		for (Branch branch : branches.getBranchList()) {
			Branch branchCopy = new Branch(branch.getCode(), branch.getName());
			summarizedBranches.addBranch(branchCopy);
		}

		BufferedReader br = null;
		File dir = new File(path);
		File[] rcdList = dir.listFiles();

		try {
			for(int i = 0; i < rcdList.length; i++) {
				if(rcdList[i].getName().matches(Const.SALES_FILE_EXTENSION)) {
					File file = new File(path, rcdList[i].getName());
					FileReader fr = new FileReader(file);
					br = new BufferedReader(fr);

					List<String> rcdFileList = new ArrayList<String>();

					String saleKey = br.readLine();

					while (saleKey != null) {
						rcdFileList.add(saleKey);
						saleKey = br.readLine();
					}

					if (!isCorrectFormat(rcdFileList)) {
						summarizedBranches.setErrorMessage(String.format(MessageConst.ERROR_SALES_FILE_MALFORMATTED, rcdList[i].getName()));
						break;
					}


					String code = rcdFileList.get(0);
					String amount = rcdFileList.get(1);


					if (!isCorrectBranchCode(summarizedBranches, code)) {
						summarizedBranches.setErrorMessage(String.format(MessageConst.ERROR_BRANCH_ABSENCE, rcdList[i].getName()));
						break;
					}



					Branch longAmount = summarizedBranches.getBranch(code);
					longAmount.addAmount(Long.parseLong(amount));

					if (!within10Digit(longAmount)) {
						summarizedBranches.setErrorMessage(MessageConst.ERROR_EXCEEDED_DIGIT);
						break;
					}
				}
			}
		} catch(IOException e) {
			summarizedBranches.setErrorMessage(MessageConst.ERROR_OTHERS);

		} finally {
			if (br != null) {
				br.close();
			}
		}
		return summarizedBranches;
	}

	/**
	 * 支店の存在チェックを行う
	 *
	 * @param branches
	 *            支店情報一覧
	 * @param code
	 *            支店コード
	 * @return 支店の存在有無
	 */
	private static boolean isCorrectBranchCode(Branches branches, String code) {
		if (branches.hasCode(code)) {
			return true;
		}
		return false;
	}

	/**
	 * 金額の桁数チェックを行う
	 *
	 * @param branch
	 *            支店情報
	 * @return 桁数が範囲内であるか
	 */
	private static boolean within10Digit(Branch branch) {
		Long sum = branch.getAmount();

		int numOfDigits = String.valueOf(Math.abs(sum)).length();
		if (numOfDigits > Const.AMOUNT_MAX_DIGIT) {
			return false;
		}
		return true;
	}

	/**
	 * 金額の妥当性チェックを行う
	 *
	 * @param amount
	 *            金額
	 * @return 金額として妥当であるか
	 */
	private static boolean isCorrectAmount(String amount) {
		String regex = "^-[0-9]{1,10}+$|^[0-9]{1,10}+$";
		if(!amount.matches(regex)) {
			return false;
		}

		return true;
	}

	/**
	 * 売上ファイルのフォーマットチェック
	 *
	 * @param rcdContents
	 *            売上ファイル内容
	 * @return 妥当なフォーマットであるか
	 */
	private static boolean isCorrectFormat(List<String> rcdContents) {
		if (rcdContents.size() == Const.SALES_LINE_SIZE && isCorrectAmount(rcdContents.get(1))) {
			return true;
		}
		return false;
	}

}
