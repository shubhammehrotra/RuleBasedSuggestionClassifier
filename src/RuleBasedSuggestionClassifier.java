import java.io.*;

import com.csvreader.CsvReader;
import com.csvreader.CsvWriter;

import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;
import edu.stanford.nlp.trees.GrammaticalStructureFactory;
import edu.stanford.nlp.trees.TreebankLanguagePack;
 
public class RuleBasedSuggestionClassifier {
 
	 public static void main(String[] args) throws IOException,
	 ClassNotFoundException {
		 // pre processing required on data
		 // what do we need better precision or recall ?
		 // difficulty with the parser. imperative sentences.
		 // precision or recall 
		 // need
		 // suggestions using keywords very inaccurate. have to study sentence structure for each of the words
		 // time to calculate precision and recall 
		 String grammar = args.length > 0 ? args[0] : "edu/stanford/nlp/models/lexparser/englishPCFG.ser.gz";
		 String[] options = { "-maxLength", "80", "-retainTmpSubcategories" };
		 LexicalizedParser lp = LexicalizedParser.loadModel(grammar, options);
		 TreebankLanguagePack tlp = lp.getOp().langpack();
		 GrammaticalStructureFactory gsf = tlp.grammaticalStructureFactory();
		 String tagged, sample, tag;
		 //String input = "Suggestions Test";
		 //String input = "Non Suggestions";
		 String fileName1 = "allNew_";
		 String outputFile = fileName1 + "_predicted.csv";
		 CsvWriter csvOutput = new CsvWriter(new FileWriter(outputFile, true), ',');
		 String[] keywords = {"suggest","recommend","hopefully","go for","request","it would be nice","adding","should come with","should be able","could come with", "i need" , "we need","needs", "would like to","would love to","allow","add","please"}; // Suggestions based on Keywords.For simple regular expression matching below. Can be extended. Index is taken dynamically at the bottom.
		// go_VB. VB is explicitly mentioned here to only catch if go occurs as a verb.
		 String[] wishKeywords = {".*would like.*(if).*", ".*i wish.*",".*i hope.*",".*i want.*",".*hopefully.*",".*if only.*",".*would be better if.*", ".*(should).*",".*would that.*",".*(can't believe).*(didn't).*",".*(don't believe).*(didn't).*",".*(do want).*",".*i can has.*"};
		 //goldberg et all wish detection
		 MaxentTagger tagger = new MaxentTagger("C:\\Users\\shubham_15294\\Downloads\\stanford-postagger-full-2015-04-20\\models\\english-left3words-distsim.tagger"); // Reading the Stanford Tagger
		 //FileInputStream fstream = new FileInputStream("C:\\Users\\shubham_15294\\Desktop\\Insight\\Suggestion Evaluation\\" + fileName1 + ".txt"); // Set of pre tagged Suggestions.
		 //DataInputStream in = new DataInputStream(fstream);
		 //BufferedReader br = new BufferedReader(new InputStreamReader(in));
		 FileWriter q = new FileWriter("C:\\Users\\shubham_15294\\Desktop\\Insight\\Suggestion Evaluation\\Evaluated.txt",true); // Suggestions Extracted by the rules to be written to this file.
		 BufferedWriter out = new BufferedWriter(q);
		 int truePositive = 0; // total number of sentences correctly predicted.
		 int totalCount = 0; // total number of lines in the input file.
		 int falseNegative;
		 int nc = 0;
		 
		 CsvReader products = new CsvReader(fileName1 + "yes.csv");
		 products.readHeaders();
		 while(products.readRecord()) {
			 System.out.println(totalCount);
			 int predictedS = 0;
			 totalCount++;
			 //String[] parts = tag.split("\\.");
			 //totalCount += parts.length;
			 String productID1 = products.get("id");
			 tag = products.get("tweet");
			 String label1 = products.get("label");
			 int flag = 0;
			 for (int i = 0; i < 1; i++) {
				 tagged = tagger.tagString(tag); //POS Tagging sentence.
				 //System.out.println(tagged); //Printing POS Tagged sentence
				 if (tagged.isEmpty()) {
					 totalCount--;
					 continue;
				 } else if((tagged.indexOf("_MD ") != -1 && tagged.indexOf("_VB",tagged.indexOf("_MD ")) != -1) && tagged.indexOf("_MD ") < tagged.indexOf("_VB",tagged.indexOf("_MD ")) && tagged.indexOf("_JJ",tagged.indexOf("_VB",tagged.indexOf("_MD "))) != -1) {
					 //System.out.println("Yes1"); // Matching if a Modal (MD) is followed by a verb (VB) followed by an adjective (JJ) within the next 5 words.
					 //if (tagged.indexOf("_JJ",tagged.indexOf("_VB",tagged.indexOf("_MD "))) != -1) {
						 truePositive++;
						 flag = 1;
						 predictedS = 1;
						 //break;
						 //System.out.println(tag  + " Rule 1 ");
						 //out.write(tag);
					 //}
					 
				 } else if ((tagged.indexOf("_") - tagged.indexOf("VB") == -1) || (tagged.indexOf("_") - tagged.indexOf("VBP") == -1) || (tagged.indexOf("_") - tagged.indexOf("VBZ") == -1)) {
					 //System.out.println(tag + " Starts with VB etc");
					 truePositive++;
					 flag = 1;
					 predictedS = 1;
					 //break;
					 //System.out.println(tag); // sentences starting with verbs
				 } else if ((tag.indexOf("need to") != -1 || tag.indexOf("needs to") != -1) && tag.indexOf("?") == -1) { // The need to rule. Will be extended with.
						 truePositive++;
						 flag = 1;
						 predictedS = 1;
						 //break;
				 } else if (lp.parse(tag).pennString().replaceAll("[\r\n]+", "").replaceAll(" ", "").indexOf("SBAR(S(VB") != -1) {
					 //Using the stanford parser for clause detectiong 
					 String trimmed = lp.parse(tag).pennString().replaceAll("[\r\n]+", "").replaceAll(" ", "");
					 truePositive++;
					 predictedS = 1;
					 flag = 1;
					 //break;
					 // Clauses are not being identified correctly and as per our requirement.
					 //System.out.println(tag);
				 } else {
					 int check = 0;
					 for (int j = 0; j < keywords.length; j++) {
						 /*if (tagged.toLowerCase().matches("(.*)" + keywords[j] + "(.*)")) {
							 //System.out.println("Yes"); // Matching if the tagged sentence contains any of the above stored keywords. 
							 flag = 1;
							 System.out.println(tag + " Rule 2 " + keywords[j]);
							 //out.write(tag);
							 truePositive++;
							 break;
						 } 	*/ if (tag.toLowerCase().matches("(.*)" + keywords[j] + "(.*)")) {
							 //System.out.println("Yes"); // Matching if the tagged sentence contains any of the above stored keywords. 
							 flag = 1;
							 //System.out.println(tag + " Rule 2 " + keywords[j]);
							 //out.write(tag);
							 check = 1;
							 truePositive++;
							 predictedS = 1;
							 break;
						 } 
					 } if (check == 0) {
						 for (int j = 0; j < wishKeywords.length; j++) {
							 if (tag.toLowerCase().matches(wishKeywords[j])) {
								 	truePositive++;
								 	predictedS = 1;
								 	flag = 1;
					                //System.out.println(tag + "  " + wishKeywords[j]);
					                break;
					            }
						 }
					 } 
				 } if (flag == 0) {
					 //System.out.println("No"); // if none of the above conditions match, the sentence is not classified as a suggestion.
					 //System.out.println(tag+ nc++);
					 predictedS = 0;
				 }
			 } out.newLine();
			 	csvOutput.write(productID1);
				csvOutput.write(tag);
				csvOutput.write(label1);
				csvOutput.write(""+predictedS);
				csvOutput.endRecord();
				
				//csvOutput.close();
		 } out.close();
		 
		 
		 falseNegative = totalCount - truePositive;
		 System.out.println("TruePositive: " + truePositive + " , FalseNegative: " + falseNegative + " , TotalSentenceCount: " + totalCount);
		 //double accuracy = (double)truePositive / (double)totalCount;
		 //System.out.println(accuracy * 100 + " %");
		 
		 /*Non Suggestions Part*/
		 String fileName2 = "Non Suggestions Tweets";
		 //FileInputStream fstream1 = new FileInputStream("C:\\Users\\shubham_15294\\Desktop\\Insight\\Suggestion Evaluation\\" + fileName2 + ".txt"); // Set of pre tagged Suggestions.
		 //DataInputStream in1 = new DataInputStream(fstream1);
		 //BufferedReader br1 = new BufferedReader(new InputStreamReader(in1));
		 FileWriter q1 = new FileWriter("C:\\Users\\shubham_15294\\Desktop\\Insight\\Suggestion Evaluation\\Evaluated.txt",true); // Suggestions Extracted by the rules to be written to this file.
		 BufferedWriter out1 = new BufferedWriter(q1);
		 int falsePositive = 0; // total number of sentences correctly predicted.
		 int totalCountNS = 0; // total number of lines in the input file.
		 //int emptyCount = 0;
		 int trueNegative;
		 //int nc = 0;
		 CsvReader products1 = new CsvReader(fileName1 + "no.csv");
		 products1.readHeaders();
		 while(products1.readRecord()) {
			 System.out.println(totalCountNS);
			 int predictedNS = 0;
			 String productID2 = products1.get("id");
			 tag = products1.get("tweet");
			 String label2 = products1.get("label");
			 totalCountNS++;
			 //String[] parts = tag.split("\\.");
			 //totalCount += parts.length;
			 int flag = 0;
			 for (int i = 0; i < 1; i++) {
				 tagged = tagger.tagString(tag); //POS Tagging sentence.
				 //System.out.println(tagged); //Printing POS Tagged sentence
				 if (tagged.isEmpty()) {
					 //emptyCount++;
					 totalCountNS--;
					 continue;
				 } else if((tagged.indexOf("_MD ") != -1 && tagged.indexOf("_VB",tagged.indexOf("_MD ")) != -1) && tagged.indexOf("_MD ") < tagged.indexOf("_VB",tagged.indexOf("_MD ")) && tagged.indexOf("_JJ",tagged.indexOf("_VB",tagged.indexOf("_MD "))) != -1) {
					 //System.out.println("Yes1"); // Matching if a Modal (MD) is followed by a verb (VB) followed by an adjective (JJ) within the next 5 words.
					 //if (tagged.indexOf("_JJ",tagged.indexOf("_VB",tagged.indexOf("_MD "))) != -1) {
					 	 falsePositive++;
						 flag = 1;
						 predictedNS = 1;
						 //break;
						 //System.out.println(tag  + " Rule 1 ");
						 //out.write(tag);
					 //}
					 
				 } else if ((tagged.indexOf("_") - tagged.indexOf("VB") == -1) || (tagged.indexOf("_") - tagged.indexOf("VBP") == -1) || (tagged.indexOf("_") - tagged.indexOf("VBZ") == -1)) {
					 //System.out.println(tag + " Starts with VB etc");
					 falsePositive++;
					 predictedNS = 1;
					 flag = 1;
					 //break;
					 //System.out.println(tag); // sentences starting with verbs
				 } else if ((tag.indexOf("need to") != -1 || tag.indexOf("needs to") != -1) && tag.indexOf("?") == -1) { // The need to rule. Will be extended with.
					 falsePositive++;
					 flag = 1;
					 predictedNS = 1;
						 //break;
				 } else if (lp.parse(tag).pennString().replaceAll("[\r\n]+", "").replaceAll(" ", "").indexOf("SBAR(S(VB") != -1) {
					 //Using the stanford parser for clause detectiong 
					 String trimmed = lp.parse(tag).pennString().replaceAll("[\r\n]+", "").replaceAll(" ", "");
					 falsePositive++;
					 flag = 1;
					 predictedNS = 1;
					 //break;
					 // Clauses are not being identified correctly and as per our requirement.
					 //System.out.println(tag);
				 } else {
					 int check = 0;
					 for (int j = 0; j < keywords.length; j++) {
						 /*if (tagged.toLowerCase().matches("(.*)" + keywords[j] + "(.*)")) {
							 //System.out.println("Yes"); // Matching if the tagged sentence contains any of the above stored keywords. 
							 flag = 1;
							 System.out.println(tag + " Rule 2 " + keywords[j]);
							 //out.write(tag);
							 truePositive++;
							 break;
						 } 	*/ if (tag.toLowerCase().matches("(.*)" + keywords[j] + "(.*)")) {
							 //System.out.println("Yes"); // Matching if the tagged sentence contains any of the above stored keywords. 
							 flag = 1;
							 //System.out.println(tag + " Rule 2 " + keywords[j]);
							 //out.write(tag);
							 check = 1;
							 falsePositive++;
							 predictedNS = 1;
							 break;
						 } 
					 } if (check == 0) {
						 for (int j = 0; j < wishKeywords.length; j++) {
							 if (tag.toLowerCase().matches(wishKeywords[j])) {
								 	falsePositive++;
								 	predictedNS = 1;
								 	flag = 1;
					                //System.out.println(tag + "  " + wishKeywords[j]);
					                break;
					            }
						 }
					 } 
				 } if (flag == 0) {
					 //System.out.println("No"); // if none of the above conditions match, the sentence is not classified as a suggestion.
					 //System.out.println(tagged+ nc++);
					 predictedNS = 0;
				 }
			 } csvOutput.write(productID2);
				csvOutput.write(tag);
				csvOutput.write(label2);
				csvOutput.write(""+predictedNS);
				csvOutput.endRecord();
				//csvOutput.close();
		 } //out.close();
		 trueNegative = totalCountNS - falsePositive; 
		 csvOutput.close();
		 System.out.println("FalsePositive: " + falsePositive + " , TrueNegative: " + trueNegative + " , TotalSentenceCount: " + totalCountNS);
		 //double accuracy = (double)truePositive / (double)totalCountNS;
		 //System.out.println(accuracy * 100 + " %");
		 double precision = (double)truePositive / (double) (truePositive + falsePositive);
		 double recall = (double)truePositive / (double) (truePositive + falseNegative);
		 double f1Score = (double)(2 * precision * recall) / (double)(precision + recall);
		 System.out.println("Precision: " + precision);
		 System.out.println("Recall: " + recall);
		 System.out.println("F1Score (Class Suggestion): " + f1Score);
	} 
}