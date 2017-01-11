package GIZ.Skripts.Umsetzung;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;
 
public class PropertiesToStoreKeysAndValues {
 
	private static File targetFile;
	private static Properties properties;
	private static String newLine = System.lineSeparator();
	
	static
	{
		targetFile = new File("./password.txt");
		
		properties = new Properties();
		
		try
		{
			properties.load(
				new FileInputStream(
				targetFile.getAbsolutePath()));
		}
		
		catch(IOException ioe)
		{
			System.err.println(
				"Unable to read file.");
		}
	}
	
	public static void main(String[] args) 
		throws IOException{
		String username = new String("salazar");
		String password = new String("locket");
		
		if(!targetFile.exists())
			targetFile.createNewFile();
		
		Boolean doesTheKeyValuePairExist = 
			checkIfKeyValuePairExists(
				username, password);
		
		if(doesTheKeyValuePairExist)
			System.err.println("Sorry, can't do it!");
		
		else
		{
			try
			{
				addNewCredentials(
					username, password);
				
				System.out.println(
					"Valid stuff, yo!");
			}
			
			catch(IOException ioe)
			{
				System.err.println(
					"Houston? You there?");
			}
		}
	}
	
	private static void addNewCredentials(
		String username, String password)
		throws IOException
	{
		FileWriter writer = 
			new FileWriter(
				targetFile.getAbsolutePath()
				, true);
        	BufferedWriter buffered_writer = 
			new BufferedWriter(writer);
        	buffered_writer.write(
			newLine + username + ":" + password);
        	buffered_writer.close();
	}
	
	private static Boolean checkIfKeyValuePairExists(
			String username, String password
	)
	   {
        for (String key
                : properties.stringPropertyNames()) {
            if (key.equals(username)
                    && properties.getProperty(key)
                            .equals(password)) {
                return true;
            }
        }

        return false;
    }
}