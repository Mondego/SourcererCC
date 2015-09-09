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
	
	/*public static void copyFile (File sourceFile, File destFile) throws IOException {
	    if (! destFile.exists ()) {
	        destFile.createNewFile ();
	    }
	    FileChannel source = null;
	    FileChannel destination = null;
	    try {
	        source = new FileInputStream (sourceFile).getChannel ();
	        destination = new FileOutputStream (destFile).getChannel ();
	        destination.transferFrom (source, 0, source.size ());
	    } finally {
	        if (source != null) {
	            source.close ();
	        }
	        if (destination != null) {
	            destination.close ();
	        }
	    }
	}


	public static void copyFile (File source, File dest) throws IOException {
	    if (! dest.exists ()) {
	        dest.createNewFile ();
	    }
	    FileChannel from = null;
	    FileChannel to = null;
	    try {
	        from = new FileInputStream (source).getChannel ();
	        to = new FileOutputStream (dest).getChannel ();
	        to.transferFrom (from, 0, from.size ());
	    } finally {
	        if (from != null) {
	            from.close ();
	        }
	        if (to != null) {
	            to.close ();
	        }
	    }
	}


	private static void copy (File source, File target) throws IOException {
	    FileChannel sourceChannel = new FileInputStream (source).getChannel ();
	    FileChannel targetChannel = new FileOutputStream (target).getChannel ();
	    sourceChannel.transferTo (0, sourceChannel.size (), targetChannel);
	    sourceChannel.close ();
	    targetChannel.close ();
	}

	private static void copyFile (File src, File dst) throws IOException {
	    FileChannel in = new FileInputStream (src).getChannel ();
	    FileChannel out = new FileOutputStream (dst).getChannel ();
	    in.transferTo (0, in.size (), out);
	    in.close ();
	    out.close ();
	}



	private static void copy (File source, File target) throws IOException {
	    FileChannel sourceChannel = new FileInputStream (source).getChannel ();
	    FileChannel targetChannel = new FileOutputStream (target).getChannel ();
	    sourceChannel.transferTo (0, sourceChannel.size (), targetChannel);
	    sourceChannel.close ();
	    targetChannel.close ();
	}

	private static void copyFile (String src, String target) throws IOException {
	    FileChannel ic = new FileInputStream (src).getChannel ();
	    FileChannel oc = new FileOutputStream (target).getChannel ();
	    ic.transferTo (0, ic.size (), oc);
	    ic.close ();
	    oc.close ();
	}

	public static File [] chooseFileOpenMultiple (JFrame frame) {
	    File retval [];
	    JFileChooser fc = new JFileChooser ();
	    fc.setDialogTitle ("Select input file.");
	    fc.setFileSelectionMode (JFileChooser.FILES_ONLY);
	    fc.setMultiSelectionEnabled (true);
	    int status = fc.showSaveDialog (frame);
	    if (status == JFileChooser.APPROVE_OPTION) {
	        retval = fc.getSelectedFiles ();
	    } else if (status == JFileChooser.CANCEL_OPTION) {
	        retval = null;
	    } else {
	        retval = null;
	    }

	    fc.setEnabled (false);
	    fc.setVisible (false);
	    return retval;
	}


	public static File [] chooseFileDirectory (JFrame frame) {
	    File retval [];
	    JFileChooser fc = new JFileChooser ();
	    fc.setDialogTitle ("Select input file.");
	    fc.setFileSelectionMode (JFileChooser.DIRECTORIES_ONLY);
	    fc.setMultiSelectionEnabled (false);
	    int status = fc.showSaveDialog (frame);
	    if (status == JFileChooser.APPROVE_OPTION) {
	        retval = fc.getSelectedFiles ();
	    } else if (status == JFileChooser.CANCEL_OPTION) {
	        retval = null;
	    } else {
	        retval = null;
	    }

	    fc.setEnabled (false);
	    fc.setVisible (false);
	    return retval;
	}
	
	private void SaveASGraph () {
	    JFileChooser chooser = new JFileChooser (DirG);
	    chooser.setFileSelectionMode (JFileChooser.FILES_ONLY);
	    ExtensionFileFilter filter = new ExtensionFileFilter ("grf", "GRAPH representation files (*.grf)");
	    chooser.setFileFilter (filter);
	    if (chooser.showSaveDialog (this) != JFileChooser.APPROVE_OPTION) return;

	    DirG = chooser.getSelectedFile ().getParent ();
	    PathG = chooser.getSelectedFile ().getPath ();
	    TabG = chooser.getSelectedFile ().getName ();
	    if (! PathG.endsWith (".grf")) {
	        PathG = PathG + ".grf";
	        TabG = TabG + ".grf";
	    }
	    SaveGraph ();
	}

	private <ExtensionFileFilter> void SaveASSchedule () {
	    JFileChooser chooser = new JFileChooser (DirS);
	    chooser.setFileSelectionMode (JFileChooser.FILES_ONLY);
	    ExtensionFileFilter filter = new ExtensionFileFilter ("sch", "Schedule graph representation files (*.sch)");
	    chooser.setFileFilter (filter);
	    if (chooser.showSaveDialog (this) != JFileChooser.APPROVE_OPTION) return;

	    DirS = chooser.getSelectedFile ().getParent ();
	    PathS = chooser.getSelectedFile ().getPath ();
	    TabS = chooser.getSelectedFile ().getName ();
	    if (! PathS.endsWith (".sch")) {
	        PathS = PathS + ".sch";
	        TabS = TabS + ".sch";
	    }
	    SaveSchedule ();
	}
	
	public static double [] [] transpose (double [] [] M) {
	    int lines = M.length;
	    int columns = M [0].length;
	    double [] [] Mtrans = new double [columns] [lines];
	    for (int i = 0;
	    i < lines; i ++) {
	        for (int j = 0;
	        j < columns; j ++) {
	            Mtrans [j] [i] = M [i] [j];
	        }
	    }
	    return Mtrans;
	}


	public static double [] [] transpose (double [] [] in) {
	    assert in != null;
	    int h = in.length;
	    int w = in [0].length;
	    double [] [] out = new double [w] [h];
	    for (int y = 0;
	    y < h; y ++) {
	        for (int x = 0;
	        x < w; x ++) {
	            out [x] [y] = in [y] [x];
	        }
	    }
	    return out;
	}


	public static double [] [] transposed (double [] [] matrix) {
	    int row = matrix [0].length;
	    int line = matrix.length;
	    double [] [] ans = new double [row] [line];
	    for (int i = 0;
	    i < line; i ++) {
	        for (int j = 0;
	        j < row; j ++) {
	            ans [j] [i] = matrix [i] [j];
	        }
	    }
	    return ans;
	}

	public static double [] [] transpose (double [] [] M) {
	    int lines = M.length;
	    int columns = M [0].length;
	    double [] [] Mtrans = new double [columns] [lines];
	    for (int i = 0;
	    i < lines; i ++) {
	        for (int j = 0;
	        j < columns; j ++) {
	            Mtrans [j] [i] = M [i] [j];
	        }
	    }
	    return Mtrans;
	}

	public static final double [] [] transpose (double [] [] a) {
	    int am = a.length;
	    int an = a [0].length;
	    double [] [] result = new double [an] [am];
	    for (int i = 0;
	    i < am; i ++) {
	        for (int j = 0;
	        j < an; j ++) {
	            result [j] [i] = a [i] [j];
	        }
	    }
	    return result;
	}

	public static double [] [] transpose (double [] [] M) {
	    int lines = M.length;
	    int columns = M [0].length;
	    double [] [] Mtrans = new double [columns] [lines];
	    for (int i = 0;
	    i < lines; i ++) {
	        for (int j = 0;
	        j < columns; j ++) {
	            Mtrans [j] [i] = M [i] [j];
	        }
	    }
	    return Mtrans;
	}
	public static double [] [] transpose (double [] [] values) {
	    double [] [] swapValues = new double [values [0].length] [values.length];
	    for (int x = 0;
	    x < values.length; x ++) {
	        for (int y = 0;
	        y < values [x].length; y ++) {
	            swapValues [y] [x] = values [x] [y];
	        }
	    }
	    return swapValues;
	}

	public static byte [] readFileContentAsBytes (File file) throws IOException {
	    ByteArrayOutputStream bytes = new ByteArrayOutputStream ();
	    FileInputStream is = new FileInputStream (file);
	    try {
	        Utils.transferContent (is, bytes, null);
	    } finally {
	        is.close ();
	    }
	    return bytes.toByteArray ();
	}

	public static byte [] readFile (String inputFileName) throws IOException {
	    ByteArrayOutputStream out = new ByteArrayOutputStream ();
	    InputStream in = new FileInputStream (inputFileName);
	    try {
	        copy (in, out);
	    } finally {
	        in.close ();
	    }
	    return out.toByteArray ();
	}


	public static byte [] zip (String contentlabel, byte [] inbuf) throws IOException {
	    ByteArrayOutputStream bout = new ByteArrayOutputStream ();
	    ZipOutputStream zipout = new ZipOutputStream (bout);
	    ZipEntry ze = new ZipEntry (contentlabel);
	    zipout.putNextEntry (ze);
	    zipout.setLevel (7);
	    zipout.write (inbuf);
	    zipout.closeEntry ();
	    zipout.close ();
	    return bout.toByteArray ();
	}

	public static byte [] zipBytes (String filename, byte [] input) throws IOException {
	    ByteArrayOutputStream baos = new ByteArrayOutputStream ();
	    ZipOutputStream zos = new ZipOutputStream (baos);
	    ZipEntry entry = new ZipEntry (filename);
	    entry.setSize (input.length);
	    zos.putNextEntry (entry);
	    zos.write (input);
	    zos.closeEntry ();
	    zos.close ();
	    return baos.toByteArray ();
	}

public static int rank (int key, int [] a) {
    int lo = 0;
    int hi = a.length - 1;
    while (lo <= hi) {
        int mid = lo + (hi - lo) / 2;
        if (key < a [mid]) hi = mid - 1;
        else if (key > a [mid]) lo = mid + 1;
        else return mid;

    }
    return - 1;
}

public static int binarySearch2 (int arr [], int key) {
    int imin = 0;
    int imax = arr.length - 1;
    while (imin <= imax) {
        int imid = imin + (imax - imin) / 2;
        if (key < arr [imid]) imax = imid - 1;
        else if (key > arr [imid]) imin = imid + 1;
        else return imid;

    }
    return - 1;
}
*/
}
