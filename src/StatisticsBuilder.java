import java.io.File;

import javafx.util.Pair;
import org.w3c.dom.Document;
import org.w3c.dom.*;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Stream;

/**
 * Created by qzmp on 02.01.2017.
 */
public class StatisticsBuilder {

    private boolean containsDesiredTag(String ctag, String[] desiredTags){
        for(String tag : desiredTags){
            if(ctag.contains(tag))
                return true;
        }
        return false;
    }

    private HashMap<String, Double> divideAll(HashMap<String, Double> map, double division){
        for(String key : map.keySet()){
            map.replace(key, map.get(key) / division);
        }
        return map;
    }

    public List<Pair<String, HashMap<String, Double>>> countStatisticsFolder(String path, String[] desiredTags){

        LinkedList<Pair<String, HashMap<String, Double>>> results = new LinkedList<Pair<String, HashMap<String, Double>>>();
        try(Stream<Path> paths = Files.walk(Paths.get(path))) {
            paths.forEach(filePath -> {
                if (Files.isRegularFile(filePath)) {
                    results.add(new Pair(filePath.getFileName().toString(), countStatistics(filePath.toString(), desiredTags)));
                    System.out.println(filePath);
                    //System.out.println(filePath.getFileName());
                   // System.out.println(results.get(0).getValue().keySet());
                   // System.out.println(results.get(0).getValue().get("dzisiaj"));
                }
            });
        }
        catch(IOException e){
            e.printStackTrace();
        }
        return results;
    }


    public HashMap<String, Double> countStatistics(String filename, String[] desiredTags){

        HashMap<String, Double> baseCounts = new HashMap<>();

        try {
            DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
            File file = new File(filename);
            Document doc = docBuilder.parse(file);

            // normalize text representation
            doc.getDocumentElement ().normalize ();
            //System.out.println ("Root element of the doc is " +
                 //   doc.getDocumentElement().getNodeName());

            NodeList listOfTokens = doc.getElementsByTagName("tok");
            int tokenCount = listOfTokens.getLength();
            for(int i = 0; i < tokenCount; i++){
                Node tokenNode = listOfTokens.item(i);
                if(tokenNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element tokenElement = (Element) tokenNode;
                    NodeList lexNodes = tokenElement.getElementsByTagName("lex");
                    int lexNodeCount = lexNodes.getLength();

                    for(int j = 0; j < lexNodeCount; j++){ // iterate over <lex>
                        Element lexElement = (Element) lexNodes.item(j);
                        if(lexElement.hasAttribute("disamb")){
                            NodeList baseList = lexElement.getElementsByTagName("base");
                            Node base = baseList.item(0);

                            NodeList ctagList = lexElement.getElementsByTagName("ctag");
                            Node ctag = ctagList.item(0);
                            if(containsDesiredTag(ctag.getTextContent().trim(), desiredTags)){
                                String baseString = base.getTextContent().trim();
                                if(baseCounts.containsKey(baseString))
                                    baseCounts.put(baseString, baseCounts.get(baseString) + 1);
                                else
                                    baseCounts.put(baseString, 1.d);
                            }
                        }
                    }
                }
            }
            divideAll(baseCounts, tokenCount);
        }
        catch (SAXParseException err) {
            System.out.println ("** Parsing error" + ", line "
                    + err.getLineNumber () + ", uri " + err.getSystemId ());
            System.out.println(" " + err.getMessage ());
        }
        catch (SAXException e) {
            Exception x = e.getException ();
            ((x == null) ? e : x).printStackTrace ();
        }
        catch (Throwable t) {
            t.printStackTrace();
        }

        return baseCounts;
    }

    public static void main(String [] args){
        //sb.getScanner("out.xml");
        //sb.countStatistics("data/out.xml", new String[]{"pred","conj","qub"});
        //sb.countStatisticsFolder("data/deotyma_zagadka_1879.txt.tag/", new String[]{"pred","prep","ppron","conj","qub"});
      
    	List<String[]> functorsConf = new ArrayList<>();
    	
    	functorsConf.addAll(Arrays.asList(
    			new String[] {"pred","prep","ppron","conj","qub"},
    			new String[] {"pred","prep","ppron","conj"},
    			new String[] {"pred","prep","ppron","qub"},
    			new String[] {"pred","prep","conj","qub"},
    			new String[] {"pred","ppron","conj","qub"},
    			new String[] {"prep","ppron","conj","qub"}));

    	List<String> fileNames = new ArrayList<>();
    	
    	for(String[] conf : functorsConf)
    	{
        	DataConverter.convert(new StatisticsBuilder().countStatisticsFolder("data/", conf));
        	
        	String fileName = "data";
        	for(String functor : conf)
        		fileName += "_" + functor; 
        	
        	fileNames.add(fileName + ".csv");
        		
            DataConverter.saveConvertedDataToFile(fileName);
    	}
    	
    	ClassifierType[] cT = {ClassifierType.Bayes,ClassifierType.C45,ClassifierType.AdaBoostWithBayes, ClassifierType.SVM};
    	//ClassifierType[] cT = {ClassifierType.AdaBoostWithBayes};
    	Classifier cF = new Classifier();
    	
    	for(String fileName : fileNames)
    	{
        	for(ClassifierType ct : cT)
        	{
                cF.loadDataFromFile(fileName);
                
                if(ct.equals(ClassifierType.Bayes) || ct.equals(ClassifierType.AdaBoostWithBayes))
                	cF.discretizeData();
                
                cF.fileNameToSave = "results_" + fileName;
                cF.classifyData(ct, 3);
        	}
    	}   
      //cF.saveDataToFileArff();
        
    }

}
