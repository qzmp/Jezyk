
import weka.core.Instances;
import weka.core.converters.ArffSaver;
import weka.core.converters.ConverterUtils.DataSource;
import weka.classifiers.bayes.*;
import weka.classifiers.functions.MultilayerPerceptron;
import weka.classifiers.functions.SMO;
import weka.classifiers.meta.AdaBoostM1;
import weka.classifiers.Evaluation;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;

import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Discretize;
import weka.classifiers.trees.J48;
import weka.classifiers.trees.RandomForest;

enum ClassifierType
{
	Bayes, C45, SVM, AdaBoostWithBayes
}

public class Classifier 
{
	private Instances data;
	public String fileNameToSave = "results.csv";
	
	public void loadDataFromFile(String fileName)
	{
		try
		{
			DataSource source = new DataSource(fileName);
			this.data = source.getDataSet();
			this.data.setClassIndex(this.data.numAttributes() - 1);
		}
		
		catch(Exception e)
		{e.printStackTrace();}
	}
	
	public void discretizeData()
	{
		try
		{
			Discretize disc = new Discretize();
			disc.setOptions(weka.core.Utils.splitOptions("-B 5"));
			disc.setInputFormat(this.data);
			this.data = Filter.useFilter(this.data, disc);
		}
		
		catch(Exception e)
		{e.printStackTrace();}
	}
	
	public void saveDataToFileArff()
	{
		try
		{
			 ArffSaver saver = new ArffSaver();
			 saver.setInstances(this.data);
			 saver.setFile(new File("test.arff"));
			 saver.writeBatch();
		}
		
		catch(Exception e)
		{e.printStackTrace();}
	}
	
	public void saveResultsToFile(String path, String result)
	{
		try {
			BufferedWriter dataFileWriter = new BufferedWriter(new FileWriter(path, true));

			dataFileWriter.write("\n" + result + "\n");
			
			dataFileWriter.close();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void classifyData(ClassifierType classifierType, int folds)
	{
		try
		{
			Evaluation eval = new Evaluation(data);
			
			switch(classifierType)
			{
				case Bayes:
					
					NaiveBayes bayes = new NaiveBayes();
					eval.crossValidateModel(bayes, data, folds, new Random(1));
					
					break;
				
				case C45:
					
					J48 tree = new J48();
					eval.crossValidateModel(tree, data, folds, new Random(1));
					
					break;
					
				case SVM:
					
					SMO smo = new SMO();
					eval.crossValidateModel(smo, data, folds, new Random(1));
					
					break;
					
				case AdaBoostWithBayes:
					
					AdaBoostM1 ada = new AdaBoostM1();
					ada.setClassifier(new NaiveBayes());
					ada.setNumIterations(50);
					eval.crossValidateModel(ada, data, folds, new Random(1));
					
					break;
			}
			
			//System.out.println(eval.toSummaryString("\nResults\n======\n", false));
			saveResultsToFile(fileNameToSave, "\n" + classifierType.toString() + "\n" + eval.toSummaryString("\nResults\n======\n", false));
		}
		
		catch(Exception e)
		{e.printStackTrace();}
	}
}
