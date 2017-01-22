import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import javafx.util.Pair;

public abstract class DataConverter 
{
	private static List<String[]> convertedData;
	private static List<String> attributesNames;
	private static List<String> classNames;
	
	public static void convert(List<Pair<String, HashMap<String, Double>>> inputData)
	{
		convertedData = new ArrayList<String[]>();
		attributesNames = new ArrayList<String>();
		classNames = new ArrayList<String>();
		
		List<HashMap<String, Double>> examples = new ArrayList<HashMap<String, Double>>();
		
		for(int i = 0; i < inputData.size(); i++)
			examples.add(new HashMap<String, Double>());
		
		inputData.forEach(e -> 
			e.getValue().keySet().forEach(
					w -> {if(!examples.get(0).containsKey(w)) 
							{examples.forEach(ex -> ex.put(w, null));}}));
		
		int dataIndex = 0;
		for(Pair<String, HashMap<String, Double>> e : inputData)
		{
			examples.get(dataIndex).putAll(e.getValue());
			dataIndex++;
		}
		
		examples.forEach(e -> e.keySet().forEach(k -> {if(e.get(k) == null) {e.put(k, 0.0);}}));
		
		//assigning attribute names
		attributesNames.addAll(examples.get(0).keySet());
		
		//assigning class names
		inputData.forEach(e -> classNames.add(e.getKey()));
		
		int  classIndex = 0;
		for(HashMap<String, Double> example : examples)
		{
			String[] tempExample = new String[example.size() + 1];
			int attrIndex = 0;
			
			for(Double attrValue : example.values())
			{
				tempExample[attrIndex] = String.valueOf(attrValue);
				attrIndex++;
			}
			
			tempExample[tempExample.length - 1] = classNames.get(classIndex).split("_")[0];
			convertedData.add(tempExample);
			classIndex++;
		}
	}	
		
	public static void saveConvertedDataToFile(String fileName)
	{
		BufferedWriter dataFileWriter;
		final String path = fileName + ".csv";
		final int attributesQuantity = attributesNames.size();
		
		try 
		{
			dataFileWriter = new BufferedWriter(new FileWriter(path));
			
			for(String attrName : attributesNames)
			{
				dataFileWriter.write(attrName);
				dataFileWriter.write(",");
			}
			
			dataFileWriter.write("class\n");
			for (Iterator<String[]> iteratorExamples = convertedData.iterator(); iteratorExamples.hasNext();)
	    	{	
				int i = 0;
				for(String attr : iteratorExamples.next())
				{
					dataFileWriter.write(attr);
					if (i<attributesQuantity)
						dataFileWriter.write(",");
					
					i++;
				}
				dataFileWriter.write("\n");
    	}
		
		dataFileWriter.close();
		System.out.println("Writing data to file finished successfuly.");
		}
		catch (IOException e) 
		{
			e.printStackTrace();
		}
	}
}
