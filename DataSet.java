import java.util.List;
import java.util.LinkedList;
import java.util.Scanner;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;

public class DataSet {
	
	List<String> dataSet;
	int classAttributteIndex;
	
	//Carrega DataSet de arquivos
	public DataSet(int classAtributteIndex, String[] dataSetFiles)
	{
		dataSet = new LinkedList<String>();
		this.classAttributteIndex = classAtributteIndex;
		
		for(String fileName : dataSetFiles)
		{
			
			readDataSetFile(fileName);
		}
		
	}
	
	public DataSet(int classAtributteIndex, String dataSetFile)
	{
		dataSet = new LinkedList<String>();
		this.classAttributteIndex = classAtributteIndex;
		readDataSetFile(dataSetFile);
	}
	
	private void readDataSetFile(String fileName)
	{
		System.out.println("reading "+fileName);
		try
		{
			//System.out.println(fileName);
			File f = new File(fileName);
			Scanner sc = new Scanner(f);
			
			while(sc.hasNext())
			{
				String line = sc.nextLine();
				dataSet.add(line);
			}
		}
		catch(FileNotFoundException e)
		{
			System.out.println("N�O ACHOU ARQUIVO: "+fileName);
		}
	}
	
	//Imprime distribuicao
	public void checkClassDistribution(int classAtributteIndex)
	{
		HashMap<Integer, List<String>> map = new HashMap<Integer, List<String>>();
		
		for(String dataLine : dataSet)
		{
			String[] data = dataLine.split(",");
			Integer classIn = Integer.parseInt(data[classAtributteIndex]);
			List<String> list = map.get(classIn);
			if(list == null)
			{
				list = new LinkedList<String>();
				map.put(classIn, list);
			}
			
			list.add(dataLine);
		}
		
		for(Integer i : map.keySet())
		{
			System.out.println(i + " : "+ map.get(i).size());
		}
	}
}
