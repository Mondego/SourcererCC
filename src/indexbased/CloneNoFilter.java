/**
 * 
 */
package indexbased;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

/**
 * @author vaibhavsaini
 *
 */
public class CloneNoFilter {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

	private void runExp() throws IOException {
		String s = "暕 賌輈鄍 蒠蓔蜳 儤嬯, 腷腯葹 鷢黳鼶 觛詏貁 揳揓 "
				+ "踆 蛣袹 慖 珝砯砨 焟硱筎 鬋鯫鯚 炾笀耔 煻 湸湤 "
				+ "謺貙蹖 峬峿峹 巘斖蘱 鑴鱱 塝, 壾嵷幓 偢偣唲 蒠蓔蜳"
				+ " 墏 瑽磏 鋡 輷邆 澂漀潫 裌覅詵 倓剟唗 悊惀桷 毄滱漮"
				+ " 樴橉 煃, 蠛趯 跣 幨懅憴 鄃鈌鈅 爂犤繵 馺 趍跠跬 鳼鳹鴅 鐩闤鞿 嵀惉";
		String a = "暕 賌輈鄍 蒠蓔蜳 儤嬯, 腷腯葹 鷢黳鼶 觛詏貁 揳揓 "
				+ "踆 蛣袹 慖 珝砯砨 焟硱筎 鬋鯫鯚 炾笀耔 煻 湸湤 "
				+ "謺貙蹖 峬峿峹 巘斖蘱 鑴鱱 塝, 壾嵷幓 偢偣唲 蒠蓔蜳"
				+ " 墏 瑽磏 鋡 輷邆 澂漀潫 裌覅詵 倓剟唗 悊惀桷 毄滱漮"
				+ " 樴橉 煃, 蠛趯 跣 幨懅憴 鄃鈌鈅 爂犤繵 馺 趍跠跬 鳼鳹鴅 鐩闤鞿 嵀惉";
		File fileDirs = new File("C:\\file.txt");

		BufferedReader in = new BufferedReader(
		new InputStreamReader(new FileInputStream(fileDirs), "UTF-8"));

		String str;

		while ((str = in.readLine()) != null) {
		    System.out.println(str);
		}
	}
	
	private void runExp2() throws IOException {
		String s = "暕 賌輈鄍 蒠蓔蜳 儤嬯, 腷腯葹 鷢黳鼶 觛詏貁 揳揓 "
				+ "踆 蛣袹 慖 珝砯砨 焟硱筎 鬋鯫鯚 炾笀耔 煻 湸湤 "
				+ "謺貙蹖 峬峿峹 巘斖蘱 鑴鱱 塝, 壾嵷幓 偢偣唲 蒠蓔蜳"
				+ " 墏 瑽磏 鋡 輷邆 澂漀潫 裌覅詵 倓剟唗 悊惀桷 毄滱漮"
				+ " 樴橉 煃, 蠛趯 跣 幨懅憴 鄃鈌鈅 爂犤繵 馺 趍跠跬 鳼鳹鴅 鐩闤鞿 嵀惉";
		String a = "暕 賌輈鄍 蒠蓔蜳 儤嬯, 腷腯葹 鷢黳鼶 觛詏貁 揳揓 "
				+ "踆 蛣袹 慖 珝砯砨 焟硱筎 鬋鯫鯚 炾笀耔 煻 湸湤 "
				+ "謺貙蹖 峬峿峹 巘斖蘱 鑴鱱 塝, 壾嵷幓 偢偣唲 蒠蓔蜳"
				+ " 墏 瑽磏 鋡 輷邆 澂漀潫 裌覅詵 倓剟唗 悊惀桷 毄滱漮"
				+ " 樴橉 煃, 蠛趯 跣 幨懅憴 鄃鈌鈅 爂犤繵 馺 趍跠跬 鳼鳹鴅 鐩闤鞿 嵀惉";
		File fileDirs = new File("C:\\file.txt");

		BufferedReader in = new BufferedReader(
		new InputStreamReader(new FileInputStream(fileDirs), "UTF-8"));

		String str;

		while ((str = in.readLine()) != null) {
		    System.out.println(str);
		}
	}

}
