import static java.lang.System.*;
import java.util.List;
import java.io.*;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;

import com.hankcs.hanlp.HanLP;
import com.hankcs.hanlp.seg.common.Term;
import com.hankcs.hanlp.seg.*;
import com.hankcs.hanlp.seg.NShort.*;

import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.evaluation.NominalPrediction;
import weka.classifiers.rules.DecisionTable;
import weka.classifiers.rules.PART;
import weka.classifiers.trees.DecisionStump;
import weka.classifiers.trees.J48;
import weka.core.FastVector;
import weka.core.Instances;

public class run {
	public static int wordsLength = 54;
	public static String[] words = {"之", "其", "或", "亦", "方", "于", "即", "皆", "因", "仍", "故", "尚", "呢"
							, "了", "的", "着", "一", "不", "乃", "呀", "吗", "咧", "啊", "把", "让", "向"
							, "往", "是", "在", "越", "更", "比", "很", "偏", "别", "好", "可", "便", "就"
							, "但", "儿", "又", "也", "都", "要", "这", "那", "你", "我", "他", "来", "去"
							, "道", "说"};
	public static void CalNumof(String fileAddr, PrintWriter pw, int tag) {
		BufferedReader datafile = null;
		try {
			datafile = new BufferedReader(new FileReader(fileAddr));
		} catch (FileNotFoundException ex) {
			err.println("File not found: " + fileAddr);
		}
		int []wordsNumber;
		wordsNumber = new int[54];
		try {
			List<Term> termList = null;
			String str = null;
			do {
				str = datafile.readLine();
				termList = HanLP.segment(str);
				boolean printFlag = false;
				for (int i = 0; i < termList.size(); ++i) 
					for (int j = 0; j < wordsLength; ++j)
						if (termList.get(i).word.equals(words[j]))
							wordsNumber[j]++;
			} while(str != null);
		} catch (Exception ex) {}
		for (int i = 0; i < wordsLength; ++i)
			pw.write(wordsNumber[i] + ", ");
		pw.write(tag + "\r\n");
	}
	public static Evaluation classify(Classifier model, Instances trainSet, Instances testSet) throws Exception {
			Evaluation evaluation = new Evaluation(trainSet);
			model.buildClassifier(trainSet);
			evaluation.evaluateModel(model, testSet);
			return evaluation;
	}
	public static double calculateAccuracy(FastVector predictions) {
		double correct = 0;
		for (int i = 0; i < predictions.size(); ++i) {
			NominalPrediction np = (NominalPrediction) predictions.elementAt(i);
			if (np.predicted() == np.actual())
				correct++;
		}
		return 100 * correct / predictions.size();
	}

	public static void main(String[] args) {
        File file = new File("trainArff.txt");
        PrintWriter pw = null;  
        // 创建文件对应FileOutputStream  
        try {
	        pw = new PrintWriter(new FileOutputStream(file));
        } catch(IOException ex) {
        	ex.printStackTrace();
        }
        pw.write("@relation i_wanna_eat_chicken\r\n");
        pw.write("\r\n");
        for (int i = 0; i < wordsLength; ++i) {
        	pw.write("@attribute " + words[i] + " numeric\r\n");
        }
        pw.write("@attribute tag {0, 1, 2}\r\n");
        pw.write("\r\n");
        pw.write("@data\r\n");

		String fileAddr = args[0];
		BufferedReader datafile = null;
		try {
			datafile = new BufferedReader(new FileReader(fileAddr));
		} catch(FileNotFoundException ex) {
			err.println("File not found: " + fileAddr);
		}
		String str = null;

		try {
			while(true) {
				str = datafile.readLine();
				if(str == null)
					break;
				int tag = Integer.valueOf(str.substring(0, 1)).intValue();
				//tag = tag == 1 ? 0 : tag;
				CalNumof(str.substring(2), pw, tag);
			}
		} catch (Exception ex){};
		// 关闭文件
		pw.close();

		file = new File("testArff.txt");
        pw = null;  
        // 创建文件对应FileOutputStream  
        try {
	        pw = new PrintWriter(new FileOutputStream(file));
        } catch(IOException ex) {
        	ex.printStackTrace();
        }
        pw.write("@relation i_wanna_eat_chicken\r\n");
        pw.write("\r\n");
        for (int i = 0; i < wordsLength; ++i) {
        	pw.write("@attribute " + words[i] + " numeric\r\n");
        }
        pw.write("@attribute tag {0, 1, 2}\r\n");
        pw.write("\r\n");
        pw.write("@data\r\n");

		fileAddr = args[1];
		datafile = null;
		try {
			datafile = new BufferedReader(new FileReader(fileAddr));
		} catch(FileNotFoundException ex) {
			err.println("File not found: " + fileAddr);
		}
		str = null;

		try {
			while(true) {
				str = datafile.readLine();
				if(str == null)
					break;
				int tag = Integer.valueOf(str.substring(0, 1)).intValue();
				CalNumof(str.substring(2), pw, tag);
			}
		} catch (Exception ex){};

		// 关闭文件
		pw.close();

		datafile = null;
		String filename = "trainArff.txt";
		FileReader fr = null;
		try {
			fr = new FileReader(filename);
			datafile = new BufferedReader(fr);
		} catch(FileNotFoundException ex) {
			err.println("File not found: " + filename);
		}

		Instances trainSet = null;
		try {
			trainSet = new Instances(datafile);
			datafile.close();
			fr.close();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		trainSet.setClassIndex(trainSet.numAttributes() - 1);
		
		filename = "testArff.txt";
		try {
			fr = new FileReader(filename);
			datafile = new BufferedReader(fr);
		} catch(FileNotFoundException ex) {
			err.println("File not found: " + filename);
		}
		
		Instances testSet = null;
		try {
			testSet = new Instances(datafile);
			datafile.close();
			fr.close();
		} catch(IOException ex) {
			ex.printStackTrace();
		}
		testSet.setClassIndex(testSet.numAttributes() - 1);

		Classifier models = new J48();
		FastVector predictions = new FastVector();


		try {
			Evaluation validation = classify(models, trainSet, testSet);
			predictions.appendElements(validation.predictions());

			double accuracy = calculateAccuracy(predictions);
			out.println("Accuracy of" + models.getClass().getSimpleName() + ":"
				+ String.format("%.2f%%", accuracy)
				+ "\n-------------------=======-------");
		} catch (Exception ex) {}


		// 删除中间文件
		file = new File("trainArff.txt");
		try {
			file.delete();	
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		file = new File("testArff.txt");
		try {
			file.delete();	
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		// 输出结果文件
		file = new File("HW3_1600012786.txt");
		pw = null;
		try {
			pw = new PrintWriter(new FileOutputStream(file));
		} catch(IOException ex) {
			ex.printStackTrace();
		}
		for (int i = 0; i < predictions.size(); ++i) {
			NominalPrediction np = (NominalPrediction) predictions.elementAt(i);
			pw.write((int)np.predicted() + "\r\n");
		}
		pw.close();
	}
}

